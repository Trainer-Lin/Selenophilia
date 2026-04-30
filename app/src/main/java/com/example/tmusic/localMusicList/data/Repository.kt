package com.example.tmusic.localMusicList.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.example.tmusic.localMusicList.data.room.MusicDao
import com.example.tmusic.localMusicList.data.room.MusicEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/*TODO: 使用MediaStore查询本地音频库 , 存储到一个临时List里 ,本地音频库是一个表格 , 每个字段代表一个音频的属性 , 使用SQL风格查询
        MediaStore是一个常量容器 , 包含音频库所有字段, 代码中的所有操作由ContentResolver执行
*/

class Repository(private val context: Context, private val musicDao: MusicDao) {
    private data class LyricsIndexEntry(
        val uri: Uri,
        val displayName: String,
        val relativePath: String?,
        val normalizedKey: String
    )

    private val contentResolver: ContentResolver = context.contentResolver
    private val musicUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    private val projection =
        arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.RELATIVE_PATH,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
        )

    suspend fun updateMusicList() =
        withContext(Dispatchers.IO) {
            val musicList = mutableListOf<MusicEntity>()
            val lyricsIndex = buildLyricsIndex()
            val selection =
                "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 30000"

            contentResolver.query(
                musicUri,
                projection,
                selection,
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        try {
                            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                            val title =
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                                    ?: "未知标题"
                            val artist =
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                                    ?: "未知艺术家"
                            val duration =
                                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                            val displayName =
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                            val relativePath =
                                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH))
                            val legacyPath = cursor.getOptionalString(MediaStore.Audio.Media.DATA)
                            val filePath = resolveAudioFilePath(displayName, relativePath, legacyPath)
                            val fileSize =
                                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))

                            if (fileSize > 30000) {
                                val uri = ContentUris.withAppendedId(musicUri, id)
                                val albumArt = getAlbumArt(uri)
                                val lyrics =
                                    getLyrics(
                                        audioFilePath = filePath,
                                        displayName = displayName,
                                        relativePath = relativePath,
                                        title = title,
                                        artist = artist,
                                        lyricsIndex = lyricsIndex
                                    )
                                val music =
                                    MusicEntity(
                                        id,
                                        uri.toString(),
                                        title,
                                        artist,
                                        duration,
                                        albumArt,
                                        lyrics
                                    )
                                musicList.add(music)
                            }
                        } catch (e: Exception) {
                            continue
                        }
                    } while (cursor.moveToNext())
                }
            }

            musicDao.deleteAllMusic()
            musicDao.updateMusic(musicList)
        }

    private fun getAlbumArt(uri: Uri): String? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val albumArt = retriever.embeddedPicture
        retriever.release()

        if (albumArt != null) {
            val fileName = uri.hashCode().toString()
            val bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.size)
            return saveAlbumArtToCache(bitmap, fileName)
        }
        return null
    }

    private fun saveAlbumArtToCache(bitmap: Bitmap, fileName: String): String? {
        val desFile = File(context.cacheDir, fileName)
        val fos = FileOutputStream(desFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.close()
        return desFile.absolutePath
    }

    private fun getLyrics(
        audioFilePath: String?,
        displayName: String?,
        relativePath: String?,
        title: String?,
        artist: String?,
        lyricsIndex: Map<String, List<LyricsIndexEntry>>
    ): String? {
        try {
            val candidateNames = buildLyricsCandidateNames(audioFilePath, displayName, title)
            val audioFile = audioFilePath?.let(::File)
            val parentDir = audioFile?.parentFile

            if (parentDir != null && parentDir.exists()) {
                val localLrcFile = findLyricsFileInDirectory(parentDir, candidateNames)
                if (localLrcFile.exists() && localLrcFile.canRead()) {
                    val dataSource = com.example.tmusic.lyric.data.LyricDataSource()
                    return dataSource.readLyricText(localLrcFile.inputStream())
                }
            }

            // Try to extract embedded lyrics from the audio file itself
            val embeddedLyrics = com.example.tmusic.lyric.data.EmbeddedLyricExtractor.extractLyrics(audioFilePath)
            if (!embeddedLyrics.isNullOrBlank()) {
                return embeddedLyrics
            }

            val candidateKeys = buildLyricsCandidateKeys(audioFilePath, displayName, title, artist)
            val matchedEntry =
                findLyricsEntry(
                    candidateKeys = candidateKeys,
                    candidateNames = candidateNames,
                    relativePath = relativePath,
                    lyricsIndex = lyricsIndex
                )
            if (matchedEntry != null) {
                return readLyricsFromUri(matchedEntry.uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun resolveAudioFilePath(
        displayName: String?,
        relativePath: String?,
        legacyPath: String?
    ): String? {
        if (!relativePath.isNullOrEmpty() && !displayName.isNullOrEmpty()) {
            return File(Environment.getExternalStorageDirectory(), relativePath + displayName).absolutePath
        }
        return legacyPath
    }

    private fun buildLyricsCandidateNames(
        audioFilePath: String?,
        displayName: String?,
        title: String?
    ): List<String> {
        val candidates = linkedSetOf<String>()
        val fileNameBase = audioFilePath?.let { File(it).nameWithoutExtension }?.trim().orEmpty()
        val displayNameBase = displayName?.substringBeforeLast('.')?.trim().orEmpty()
        val titleBase = title?.trim().orEmpty()

        if (fileNameBase.isNotEmpty()) candidates.add("$fileNameBase.lrc")
        if (displayNameBase.isNotEmpty()) candidates.add("$displayNameBase.lrc")
        if (titleBase.isNotEmpty()) candidates.add("$titleBase.lrc")

        return candidates.toList()
    }

    private fun buildLyricsCandidateKeys(
        audioFilePath: String?,
        displayName: String?,
        title: String?,
        artist: String?
    ): List<String> {
        val keys = linkedSetOf<String>()
        val fileNameBase = audioFilePath?.let { File(it).nameWithoutExtension }?.trim().orEmpty()
        val displayNameBase = displayName?.substringBeforeLast('.')?.trim().orEmpty()
        val titleBase = title?.trim().orEmpty()
        val artistBase = artist?.trim().orEmpty()

        listOf(
            fileNameBase,
            displayNameBase,
            titleBase,
            stripBracketContent(fileNameBase),
            stripBracketContent(displayNameBase),
            stripBracketContent(titleBase),
            listOf(artistBase, titleBase).filter { it.isNotBlank() }.joinToString(" "),
            listOf(titleBase, artistBase).filter { it.isNotBlank() }.joinToString(" "),
            listOf(artistBase, stripBracketContent(titleBase)).filter { it.isNotBlank() }.joinToString(" "),
            listOf(stripBracketContent(titleBase), artistBase).filter { it.isNotBlank() }.joinToString(" ")
        ).forEach { candidate ->
            val normalized = normalizeLyricsKey(candidate)
            if (normalized.isNotEmpty()) {
                keys.add(normalized)
            }
        }

        return keys.toList()
    }

    private fun findLyricsFileInDirectory(parentDir: File, candidateNames: List<String>): File {
        val files = parentDir.listFiles().orEmpty()
        candidateNames.forEach { candidate ->
            files.firstOrNull { file ->
                file.isFile && file.name.equals(candidate, ignoreCase = true)
            }?.let { return it }
        }
        return File(parentDir, candidateNames.firstOrNull() ?: "")
    }

    private fun buildLyricsIndex(): Map<String, List<LyricsIndexEntry>> {
        val lyricsMap = linkedMapOf<String, MutableList<LyricsIndexEntry>>()
        val filesUri = MediaStore.Files.getContentUri("external")

        contentResolver.query(
            filesUri,
            arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.RELATIVE_PATH
            ),
            "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ? OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?",
            arrayOf("%.lrc", "%.LRC"),
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val displayName =
                    cursor.getOptionalString(MediaStore.Files.FileColumns.DISPLAY_NAME) ?: continue
                val baseName = displayName.substringBeforeLast('.', displayName)
                val normalizedKey = normalizeLyricsKey(baseName)
                if (normalizedKey.isEmpty()) continue

                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                val entry =
                    LyricsIndexEntry(
                        uri = ContentUris.withAppendedId(filesUri, id),
                        displayName = displayName,
                        relativePath = cursor.getOptionalString(MediaStore.Files.FileColumns.RELATIVE_PATH),
                        normalizedKey = normalizedKey
                    )
                lyricsMap.getOrPut(normalizedKey) { mutableListOf() }.add(entry)
            }
        }

        return lyricsMap
    }

    private fun findLyricsEntry(
        candidateKeys: List<String>,
        candidateNames: List<String>,
        relativePath: String?,
        lyricsIndex: Map<String, List<LyricsIndexEntry>>
    ): LyricsIndexEntry? {
        candidateKeys.forEach { key ->
            val entries = lyricsIndex[key].orEmpty()
            if (entries.isEmpty()) return@forEach

            entries.firstOrNull { entry ->
                candidateNames.any { it.equals(entry.displayName, ignoreCase = true) } &&
                    entry.relativePath.equals(relativePath, ignoreCase = true)
            }?.let { return it }

            entries.firstOrNull { entry ->
                candidateNames.any { it.equals(entry.displayName, ignoreCase = true) }
            }?.let { return it }

            entries.firstOrNull()?.let { return it }
        }

        val titleKey =
            candidateKeys.maxByOrNull { it.length }
                ?.takeIf { it.isNotEmpty() }
        val allEntries = lyricsIndex.values.flatten().distinctBy { it.uri }
        return allEntries
            .map { entry ->
                entry to scoreLyricsEntry(entry, candidateKeys, titleKey, relativePath)
            }
            .filter { it.second >= 100 } // 提高准入门槛，不能随便匹配
            .maxByOrNull { it.second }
            ?.first
    }

    private fun scoreLyricsEntry(
        entry: LyricsIndexEntry,
        candidateKeys: List<String>,
        titleKey: String?,
        relativePath: String?
    ): Int {
        var score = 0
        val isSameDir = !relativePath.isNullOrEmpty() && entry.relativePath.equals(relativePath, ignoreCase = true)
        
        if (candidateKeys.any { it == entry.normalizedKey }) {
            score += 120
            if (isSameDir) score += 40
            return score // 完全匹配直接高分
        }
        
        if (candidateKeys.any { candidate ->
                candidate.length >= 4 &&
                    (entry.normalizedKey.contains(candidate) || candidate.contains(entry.normalizedKey))
            }) {
            score += 80
        }
        if (!titleKey.isNullOrEmpty() && titleKey.length >= 3 &&
            (entry.normalizedKey.contains(titleKey) || titleKey.contains(entry.normalizedKey))
        ) {
            score += 60
        }
        
        // 只有在名字有一定匹配度时，同目录才作为加分项，否则同目录的唯一歌词会污染其他歌
        if (score >= 60 && isSameDir) {
            score += 40
        }
        
        return score
    }

    private fun stripBracketContent(value: String): String {
        return value.replace(Regex("\\(.*?\\)|\\[.*?\\]|\\{.*?\\}|（.*?）|【.*?】"), "").trim()
    }

    private fun readLyricsFromUri(uri: Uri): String? {
        contentResolver.openInputStream(uri)?.use { stream ->
            val dataSource = com.example.tmusic.lyric.data.LyricDataSource()
            return dataSource.readLyricText(stream)
        }
        return null
    }

    private fun normalizeLyricsKey(value: String?): String {
        return value
            ?.lowercase()
            ?.replace(Regex("\\.lrc$"), "")
            ?.replace(Regex("[^\\p{L}\\p{N}]"), "")
            ?.trim()
            .orEmpty()
    }

    private fun android.database.Cursor.getOptionalString(columnName: String): String? {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getString(index) else null
    }

    suspend fun getAllMusic(): List<MusicEntity> {
        return musicDao.getAllMusicByTitle()
    }
}

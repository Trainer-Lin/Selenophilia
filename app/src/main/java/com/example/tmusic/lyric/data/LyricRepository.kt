package com.example.tmusic.lyric.data

import com.example.tmusic.localMusicList.data.room.MusicEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class LyricRepository(
    private val dataSource: LyricDataSource
) {
    // In-memory cache for parsed lyrics to avoid re-parsing during rapid switches
    private val memoryCache = ConcurrentHashMap<Long, Lyric>()

    suspend fun getLyric(musicEntity: MusicEntity?): Result<Lyric> = withContext(Dispatchers.IO) {
        if (musicEntity == null) {
            return@withContext Result.failure(Exception("No music playing"))
        }

        val cached = memoryCache[musicEntity.id]
        if (cached != null) {
            return@withContext Result.success(cached)
        }

        val rawLyrics = musicEntity.lyrics
        if (rawLyrics.isNullOrBlank()) {
            return@withContext Result.failure(Exception("No lyric file found for this song"))
        }

        try {
            val parsedLyric = LyricParser.parse(rawLyrics)
            
            if (parsedLyric.lines.isEmpty()) {
                return@withContext Result.failure(Exception("Lyric file is empty or unsupported format"))
            }

            memoryCache[musicEntity.id] = parsedLyric
            Result.success(parsedLyric)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // In case we want to parse from a raw string directly
    suspend fun parseRawLyric(raw: String): Result<Lyric> = withContext(Dispatchers.IO) {
        try {
            val parsedLyric = LyricParser.parse(raw)
            if (parsedLyric.lines.isEmpty()) {
                Result.failure(Exception("Lyric file is empty or unsupported format"))
            } else {
                Result.success(parsedLyric)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

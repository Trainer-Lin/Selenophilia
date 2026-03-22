package com.example.tmusic.localMusicList.data

import com.example.tmusic.localMusicList.data.room.MusicEntity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.example.tmusic.localMusicList.data.room.MusicDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/*TODO: 使用MediaStore查询本地音频库 , 存储到一个临时List里 ,本地音频库是一个表格 , 每个字段代表一个音频的属性 , 使用SQL风格查询
        MediaStore是一个常量容器 , 包含音频库所有字段, 代码中的所有操作由ContentResolver执行
*/

class Repository(private val context: Context , private val musicDao: MusicDao){
    //TODO: 使用ContentResolver的query方法返回一个用于遍历音频库的指针Cursor


    private val contentResolver: ContentResolver = context.contentResolver

    private val musicUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    private val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.DATA, // 文件路径
        MediaStore.Audio.Media.SIZE,  // 文件大小
    )
    suspend fun updateMusicList() = withContext(Dispatchers.IO) { //禁止在主线程中执行IO操作 用这个转移到IO线程上面
        val musicList = mutableListOf<MusicEntity>()
        
        // 添加筛选条件：只查询音乐文件，排除铃声、通知音等
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 30000" // 时长大于30秒
        
        val cursor = contentResolver.query(
            musicUri,  //uri SQL中的FROM 指定从哪张表格查询
            projection,  //projection  SQL中的SELECT , 指定查询哪些字段
            selection,  //selection  SQL中的WHERE , 指定行过滤条件
            null,  //selectionArgs , 替换上面?的实际值
            "${MediaStore.Audio.Media.TITLE} ASC"   //sortOrder 按标题排序
        )?.use { //?.调用方式可以保证Cursor在使用完毕后自动关闭
            if (it.moveToFirst()) {
                do {
                    try {
                        val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                        val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) ?: "未知标题"
                        val artist = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "未知艺术家"
                        val duration = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                        val filePath = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                        val fileSize = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                        
                        // 过滤掉过小的文件（可能不是完整音乐）
                        if (fileSize > 30000) { // 大于
                            val uri = ContentUris.withAppendedId(musicUri, id)
                            val albumArt = getAlbumArt(uri)
                            val music = MusicEntity(id, uri.toString(), title, artist, duration, albumArt)
                            musicList.add(music)
                        }
                    } catch (e: Exception) {
                        // 跳过有问题的文件
                        continue
                    }
                } while (it.moveToNext())
            }
        }
        
        musicDao.deleteAllMusic()
        musicDao.updateMusic(musicList)
    }

    private fun getAlbumArt(uri: Uri): String?{
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val albumArt = retriever.embeddedPicture //返回一个字节数组
        retriever.release()

        if(albumArt != null){
            val fileName = uri.hashCode().toString() //保证文件的唯一性
            val bitmap = BitmapFactory.decodeByteArray(albumArt, 0, albumArt.size)
            return saveAlbumArtToCache(bitmap, fileName)
        }
        return null
    }

    //为了把歌曲封面保存到缓存目录中
    private fun saveAlbumArtToCache(bitmap: Bitmap, fileName: String):String?{
        val desFile = File(context.cacheDir, fileName)
        val fos = FileOutputStream(desFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)//压缩图片
        fos.close()
        return desFile.absolutePath
    }

    suspend fun getAllMusic(): List<MusicEntity> {
        return musicDao.getAllMusicByTitle()
    }

}
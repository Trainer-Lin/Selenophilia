package com.example.tmusic.localMusicList.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MusicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) //如果存在冲突,则替换旧数据
    suspend fun updateMusic(musicList: List<MusicEntity>) //更新音乐源

    @Query("DELETE FROM music")
    suspend fun deleteAllMusic() //删除所有音乐

    @Query("SELECT * FROM music ORDER BY title")
    suspend fun getAllMusicByTitle(): List<MusicEntity> //根据标题排序查询所有音乐

    @Query("DELETE FROM music WHERE id = :id")
    suspend fun deleteMusic(id: Long) //根据id删除音乐
}
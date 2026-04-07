package com.example.tmusic.listAndMusic.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistMusicDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMusic(crossRef: PlaylistCrossRef)

    @Delete
    suspend fun deleteMusic(crossRef: PlaylistCrossRef)

    @Query("SELECT musicId FROM playlist_cross_ref WHERE playlistId = :playlistId")
    fun getMusicIdFromList(playlistId: Long): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_cross_ref WHERE playlistId = :playlistId AND musicId = :musicId)")
    suspend fun isMusicInPlaylist(playlistId: Long, musicId: Long): Boolean
}
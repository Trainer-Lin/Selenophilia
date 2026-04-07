package com.example.tmusic.home.data

import com.example.tmusic.home.data.room.PlaylistDao
import com.example.tmusic.home.data.room.PlaylistEntity
import com.example.tmusic.listAndMusic.room.PlaylistCrossRef
import com.example.tmusic.listAndMusic.room.PlaylistMusicDao
import com.example.tmusic.localMusicList.data.room.MusicDao
import com.example.tmusic.localMusicList.data.room.MusicEntity
import kotlinx.coroutines.flow.Flow

class PlaylistRepository(private val playlistDao: PlaylistDao) {
    //增加歌单部分
    fun getAllPlaylists(): Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    suspend fun getPlaylistById(id: Long): PlaylistEntity? = playlistDao.getPlaylistById(id)

    suspend fun insertPlaylist(playlist: PlaylistEntity): Long = playlistDao.insertPlaylist(playlist)

    suspend fun updatePlaylist(playlist: PlaylistEntity) = playlistDao.updatePlaylist(playlist)

    suspend fun deletePlaylist(playlist: PlaylistEntity) = playlistDao.deletePlaylist(playlist)

    suspend fun deletePlaylistById(id: Long) = playlistDao.deletePlaylistById(id)

}
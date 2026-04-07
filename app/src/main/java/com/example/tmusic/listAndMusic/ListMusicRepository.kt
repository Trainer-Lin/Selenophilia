package com.example.tmusic.listAndMusic

import com.example.tmusic.home.data.room.PlaylistDao
import com.example.tmusic.listAndMusic.room.PlaylistCrossRef
import com.example.tmusic.listAndMusic.room.PlaylistMusicDao
import com.example.tmusic.localMusicList.data.room.MusicDao
import com.example.tmusic.localMusicList.data.room.MusicEntity
import kotlinx.coroutines.flow.Flow

class ListMusicRepository(
        private val playlistMusicDao: PlaylistMusicDao,
        private val  musicDao: MusicDao
) {
    suspend fun addMusicToPlaylist(crossRef: PlaylistCrossRef) = playlistMusicDao.insertMusic(crossRef)

    suspend fun deleteMusicFromPlaylist(crossRef: PlaylistCrossRef) = playlistMusicDao.deleteMusic(crossRef)

    fun getMusicIdFromList(playlistId: Long): Flow<List<Long>> = playlistMusicDao.getMusicIdFromList(playlistId)

    suspend fun isMusicInPlaylist(playlistId: Long, musicId: Long): Boolean = playlistMusicDao.isMusicInPlaylist(playlistId, musicId)

    suspend fun getMusicByIds(ids: List<Long>): List<MusicEntity> = musicDao.getMusicByIds(ids)
}
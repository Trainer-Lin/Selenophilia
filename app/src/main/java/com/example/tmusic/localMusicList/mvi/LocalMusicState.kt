package com.example.tmusic.localMusicList.mvi

import com.example.tmusic.localMusicList.data.room.MusicEntity

data class LocalMusicState(
        val isLoading: Boolean = false,
        val musicList: List<MusicEntity> = emptyList()
    )
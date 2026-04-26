package com.example.tmusic.localMusicList.mvi

import com.example.tmusic.localMusicList.data.room.MusicEntity

data class LocalMusicState(
    val isLoading: Boolean = false,
    val musicList: List<MusicEntity> = emptyList(),
    val sortType: SortType = SortType.DEFAULT
)

enum class SortType {
    DEFAULT,
    BY_NAME,
    BY_DATE_NEW_TO_OLD,
    BY_DATE_OLD_TO_NEW,
    BY_ARTIST
}
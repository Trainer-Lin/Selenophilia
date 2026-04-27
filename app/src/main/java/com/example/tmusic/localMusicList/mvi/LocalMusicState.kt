package com.example.tmusic.localMusicList.mvi

import com.example.tmusic.localMusicList.data.room.MusicEntity

data class LocalMusicState(
    val isLoading: Boolean = false,
    val musicList: List<MusicEntity> = emptyList(), // Filtered and sorted list to display
    val originalMusicList: List<MusicEntity> = emptyList(), // Unfiltered original list
    val sortType: SortType = SortType.BY_NAME,
    val searchQuery: String = ""
)

enum class SortType {
    DEFAULT,
    BY_NAME,
    BY_DATE_NEW_TO_OLD,
    BY_DATE_OLD_TO_NEW,
    BY_ARTIST
}
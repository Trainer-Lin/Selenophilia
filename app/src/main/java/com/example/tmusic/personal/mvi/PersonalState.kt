package com.example.tmusic.personal.mvi

import com.example.tmusic.localMusicList.data.room.MusicEntity
import com.example.tmusic.personal.data.MostPlayedSong

data class PersonalState(
    val daysUsed: Int = 1,
    val focusCount: Int = 0,
    val focusDurationHours: Int = 0,
    val totalPlayHours: Int = 0,
    val mostPlayedSong: MostPlayedSong? = null,
    val playHistory: List<MusicEntity> = emptyList()
)

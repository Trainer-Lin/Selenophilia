package com.example.tmusic.home.mvvm.model

import com.example.tmusic.home.data.room.PlaylistEntity

data class PlaylistState(
    val playlists: List<PlaylistEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
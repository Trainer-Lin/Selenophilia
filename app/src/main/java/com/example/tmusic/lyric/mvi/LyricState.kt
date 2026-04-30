package com.example.tmusic.lyric.mvi

import com.example.tmusic.lyric.data.Lyric

data class LyricState(
    val isLoading: Boolean = false,
    val lyric: Lyric = Lyric.EMPTY,
    val error: String? = null
)

package com.example.tmusic.lyric.mvi

sealed class LyricIntent {
    data class LoadLyric(val rawLyric: String?) : LyricIntent()
}

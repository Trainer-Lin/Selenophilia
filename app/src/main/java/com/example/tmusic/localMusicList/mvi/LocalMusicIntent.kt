package com.example.tmusic.localMusicList.mvi

sealed class LocalMusicIntent {
    object LoadLocalMusic : LocalMusicIntent()
}
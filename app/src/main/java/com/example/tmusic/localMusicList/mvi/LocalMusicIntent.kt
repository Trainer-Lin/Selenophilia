package com.example.tmusic.localMusicList.mvi

sealed class LocalMusicIntent {
    object LoadLocalMusic : LocalMusicIntent()
    
    sealed class SortMusic : LocalMusicIntent() {
        object ByName : SortMusic()
        object ByDateNewToOld : SortMusic()
        object ByDateOldToNew : SortMusic()
        object ByArtist : SortMusic()
    }
}
package com.example.tmusic.listAndMusic.model

data class ListMusicState(
    val isAdding: Boolean = false,
    val isDeleting: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)
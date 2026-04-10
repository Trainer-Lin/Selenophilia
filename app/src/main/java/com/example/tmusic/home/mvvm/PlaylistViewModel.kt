package com.example.tmusic.home.mvvm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmusic.home.data.PlaylistRepository
import com.example.tmusic.home.data.room.PlaylistDatabase
import com.example.tmusic.home.data.room.PlaylistEntity
import com.example.tmusic.home.mvvm.model.PlaylistState
import com.example.tmusic.listAndMusic.room.PlaylistCrossRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaylistViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlaylistRepository
    private val colorList = listOf("#A772BE", "#9C9BEB", "#E1B5D4")

    private val _uiState = MutableStateFlow(PlaylistState())
    val uiState: StateFlow<PlaylistState> = _uiState.asStateFlow()

    init {
        val database = PlaylistDatabase.getInstance(application)
        repository = PlaylistRepository(
            database.playlistDao())
        loadPlaylists()
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            repository.getAllPlaylists().collect { playlists ->
                _uiState.update { it.copy(playlists = playlists, isLoading = false) }
            }
        }
    }

    fun addPlaylist(name: String, description: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            val currentCount = _uiState.value.playlists.size
            val colorIndex = currentCount % colorList.size
            val playlist = PlaylistEntity(
                name = name.trim(),
                description = description,
                colorIndex = colorIndex
            )
            repository.insertPlaylist(playlist)
        }
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }

    fun updatePlaylist(name: String, playlistId: Long) {
        if(name.isBlank())return
        viewModelScope.launch {
           val playlist = _uiState.value.playlists.first{ it.id == playlistId }
            val newPlaylist = playlist.copy(name = name.trim())
            repository.updatePlaylist(newPlaylist)
        }
    }

}

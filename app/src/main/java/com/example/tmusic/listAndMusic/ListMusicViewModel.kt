package com.example.tmusic.listAndMusic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmusic.listAndMusic.model.ListMusicState
import com.example.tmusic.listAndMusic.room.PlaylistCrossRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListMusicViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var repository: ListMusicRepository

    private val _uiState = MutableStateFlow(ListMusicState())
    val uiState: StateFlow<ListMusicState> = _uiState.asStateFlow()

    fun initRepository(repo: ListMusicRepository) {
        repository = repo
    }

    fun addMusicToPlaylist(playlistId: Long, musicId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAdding = true, successMessage = null, error = null) }
            try {
                if (repository.isMusicInPlaylist(playlistId, musicId)) {
                    _uiState.update { it.copy(isAdding = false, error = "音乐已在歌单中") }
                    return@launch
                }
                repository.addMusicToPlaylist(PlaylistCrossRef(playlistId, musicId))
                _uiState.update { it.copy(isAdding = false, successMessage = "已添加到歌单") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAdding = false, error = "添加失败: ${e.message}") }
            }
        }
    }

    fun deleteMusicFromPlaylist(playlistId: Long, musicId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, successMessage = null, error = null) }
            try {
                repository.deleteMusicFromPlaylist(PlaylistCrossRef(playlistId, musicId))
                _uiState.update { it.copy(isDeleting = false, successMessage = "已从歌单移除") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isDeleting = false, error = "移除失败: ${e.message}") }
            }
        }
    }

    fun loadUserPlaylist(playlistId: Long) {
        viewModelScope.launch {
            val playlist = getMusicFromList(playlistId)
            playlist.collect { ids -> repository.getMusicByIds(ids) }
        }
    }

    fun getMusicFromList(playlistId: Long): Flow<List<Long>> =
            repository.getMusicIdFromList(playlistId)

    suspend fun isMusicInPlaylist(playlistId: Long, musicId: Long): Boolean =
            repository.isMusicInPlaylist(playlistId, musicId)

    fun consumeSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }
}

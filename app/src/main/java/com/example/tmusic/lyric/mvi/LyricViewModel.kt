package com.example.tmusic.lyric.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tmusic.base.BaseMviViewModel
import com.example.tmusic.lyric.data.Lyric
import com.example.tmusic.lyric.data.LyricRepository
import kotlinx.coroutines.launch

class LyricViewModel(
    private val repository: LyricRepository
) : BaseMviViewModel<LyricState, LyricIntent>() {

    override fun initState(): LyricState {
        return LyricState()
    }

    override fun handleIntent(intent: LyricIntent) {
        when (intent) {
            is LyricIntent.LoadLyric -> loadLyric(intent.rawLyric)
        }
    }

    private fun loadLyric(rawLyric: String?) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            
            if (rawLyric.isNullOrBlank()) {
                updateState { it.copy(isLoading = false, lyric = Lyric.EMPTY, error = "No lyric data available") }
                return@launch
            }

            val result = repository.parseRawLyric(rawLyric)
            if (result.isSuccess) {
                updateState { it.copy(isLoading = false, lyric = result.getOrNull()!!) }
            } else {
                updateState { 
                    it.copy(
                        isLoading = false, 
                        lyric = Lyric.EMPTY, 
                        error = result.exceptionOrNull()?.message ?: "Unknown error"
                    ) 
                }
            }
        }
    }
}

class LyricViewModelFactory(
    private val repository: LyricRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LyricViewModel::class.java)) {
            return LyricViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

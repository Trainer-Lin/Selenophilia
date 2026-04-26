package com.example.tmusic.localMusicList.mvi

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmusic.base.BaseMviViewModel
import com.example.tmusic.localMusicList.data.Repository
import com.example.tmusic.localMusicList.data.room.MusicEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LocalMusicViewModel(private val repository: Repository): BaseMviViewModel<LocalMusicState, LocalMusicIntent>() {
//    private val _viewState = MutableStateFlow(LocalMusicState())
//    val viewState:StateFlow<LocalMusicState> = _viewState.asStateFlow()

    override fun handleIntent(intent: LocalMusicIntent){
        when(intent){
            is LocalMusicIntent.LoadLocalMusic -> loadMusic()
            is LocalMusicIntent.SortMusic.ByName -> sortMusic(SortType.BY_NAME)
            is LocalMusicIntent.SortMusic.ByDateNewToOld -> sortMusic(SortType.BY_DATE_NEW_TO_OLD)
            is LocalMusicIntent.SortMusic.ByDateOldToNew -> sortMusic(SortType.BY_DATE_OLD_TO_NEW)
            is LocalMusicIntent.SortMusic.ByArtist -> sortMusic(SortType.BY_ARTIST)
        }
    }

    init{
        loadSaveMusic()
    }

    override fun initState(): LocalMusicState {
        return LocalMusicState()
    }

    private fun loadSaveMusic(){
        viewModelScope.launch{
            val musicList = repository.getAllMusic()
            updateState{ state ->
                state.copy(
                    musicList = musicList,
                    isLoading = false
                )
            }
        }
    }

    fun loadMusic(){
        viewModelScope.launch {
            updateState{state ->
                state.copy(
                    isLoading = true
                )
            }
            try{
                Log.d("readMusic", "开始扫描音乐文件, isLoading is ${initState().isLoading}")
                repository.updateMusicList()
                val musicList = repository.getAllMusic()
                updateState{state ->
                    state.copy(
                        musicList = musicList,
                        isLoading = false
                    )
                }
                Log.d("readMusic", "扫描完成，共找到 ${musicList.size} 首音乐")
            }catch(e: Exception){
                e.printStackTrace()
                Log.e("readMusic", "扫描音乐出错: ${e.message}")
                loadSaveMusic() //读取保存的
            }
        }
    }

    private fun sortMusic(sortType: SortType) {
        val currentList = viewState.value.musicList
        val sortedList = when (sortType) {
            SortType.BY_NAME -> currentList.sortedBy { it.title }
            SortType.BY_DATE_NEW_TO_OLD -> currentList.sortedByDescending { it.id }
            SortType.BY_DATE_OLD_TO_NEW -> currentList.sortedBy { it.id }
            SortType.BY_ARTIST -> currentList.sortedBy { it.artist }
            else -> currentList
        }
        updateState { state ->
            state.copy(
                musicList = sortedList,
                sortType = sortType
            )
        }
    }
}
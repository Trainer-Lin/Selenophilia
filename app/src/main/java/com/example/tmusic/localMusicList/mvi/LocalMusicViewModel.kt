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
            is LocalMusicIntent.SearchMusic -> searchMusic(intent.query)
        }
    }

    init{
        loadSaveMusic()
    }

    override fun initState(): LocalMusicState {
        val savedSortName = com.example.tmusic.TApplication.mmkv.decodeString("music_sort_type", SortType.BY_NAME.name)
        val savedSort = try {
            SortType.valueOf(savedSortName ?: SortType.BY_NAME.name)
        } catch (e: Exception) {
            SortType.BY_NAME
        }
        return LocalMusicState(sortType = savedSort)
    }

    private fun loadSaveMusic(){
        viewModelScope.launch{
            val musicList = repository.getAllMusic()
            updateState{ state ->
                state.copy(
                    musicList = applySearchAndSort(musicList, state.searchQuery, state.sortType),
                    originalMusicList = musicList,
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
                        musicList = applySearchAndSort(musicList, state.searchQuery, state.sortType),
                        originalMusicList = musicList,
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
        com.example.tmusic.TApplication.mmkv.encode("music_sort_type", sortType.name)
        val currentState = viewState.value
        updateState { state ->
            state.copy(
                musicList = applySearchAndSort(currentState.originalMusicList, currentState.searchQuery, sortType),
                sortType = sortType
            )
        }
    }

    private fun searchMusic(query: String) {
        val currentState = viewState.value
        updateState { state ->
            state.copy(
                musicList = applySearchAndSort(currentState.originalMusicList, query, currentState.sortType),
                searchQuery = query
            )
        }
    }

    private fun applySearchAndSort(
        originalList: List<MusicEntity>,
        query: String,
        sortType: SortType
    ): List<MusicEntity> {
        // 1. Filter by query
        val filteredList = if (query.isBlank()) {
            originalList
        } else {
            val lowerQuery = query.lowercase()
            originalList.filter {
                it.title.lowercase().contains(lowerQuery) || it.artist.lowercase().contains(lowerQuery)
            }
        }

        // 2. Sort the filtered list
        return when (sortType) {
            SortType.BY_NAME -> filteredList.sortedBy { it.title }
            SortType.BY_DATE_NEW_TO_OLD -> filteredList.sortedByDescending { it.id }
            SortType.BY_DATE_OLD_TO_NEW -> filteredList.sortedBy { it.id }
            SortType.BY_ARTIST -> filteredList.sortedBy { it.artist }
            else -> filteredList.sortedBy { it.title }
        }
    }
}
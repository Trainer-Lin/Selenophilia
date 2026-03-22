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

}
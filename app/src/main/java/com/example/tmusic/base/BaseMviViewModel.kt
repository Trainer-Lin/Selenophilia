package com.example.tmusic.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseMviViewModel<S, I> : ViewModel() {
    // 私有可变State
    private val _viewState = MutableStateFlow(initState())
    // 对外暴露只读State
    val viewState: StateFlow<S> = _viewState.asStateFlow()

    protected open fun updateState(block:(S) -> S){
        _viewState.update(block)
    }

    // 抽象方法：初始化默认State
    protected abstract fun initState(): S
    // 抽象方法：处理Intent（子类实现具体业务）
    abstract fun handleIntent(intent: I)
}
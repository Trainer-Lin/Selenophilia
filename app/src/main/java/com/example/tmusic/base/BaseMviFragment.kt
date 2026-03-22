package com.example.tmusic.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.example.tmusic.localMusicList.mvi.LocalMusicState
import kotlinx.coroutines.launch

abstract class BaseMviFragment<S,I,VM: ViewModel,VB: ViewBinding>: Fragment() {
    protected lateinit var binding: VB
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = createViewBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    abstract fun handleUiState(state: S) //更新UI
    abstract fun sendIntent(intent:I) //发送Intent
    abstract fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?):VB
}
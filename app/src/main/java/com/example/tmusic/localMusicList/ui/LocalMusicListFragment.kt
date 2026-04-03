package com.example.tmusic.localMusicList.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tmusic.MainActivity
import com.example.tmusic.R
import com.example.tmusic.base.BaseMviFragment
import com.example.tmusic.databinding.FragmentLocalMusicListBinding
import com.example.tmusic.home.data.room.PlaylistEntity
import com.example.tmusic.home.mvvm.PlaylistViewModel
import com.example.tmusic.localMusicList.data.Repository
import com.example.tmusic.localMusicList.data.room.MusicDatabase
import com.example.tmusic.localMusicList.data.room.MusicEntity
import com.example.tmusic.localMusicList.mvi.LocalMusicIntent
import com.example.tmusic.localMusicList.mvi.LocalMusicState
import com.example.tmusic.localMusicList.mvi.LocalMusicViewModel
import com.example.tmusic.widget.PlaylistSelectDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LocalMusicListFragment :
        BaseMviFragment<
                LocalMusicState,
                LocalMusicIntent,
                LocalMusicViewModel,
                FragmentLocalMusicListBinding>() {
    private var loadingDialog: AlertDialog? = null
    private lateinit var adapter: LocalMusicListAdapter
    private var currentMusicList: List<MusicEntity> = emptyList()
    private var currentMusicIndex: Int = 0
    private val application by lazy { requireActivity().application }
    private val db by lazy { MusicDatabase.getInstance(application) }
    private val musicDao by lazy { db.musicDao() }
    private val repository by lazy { Repository(application, musicDao) }
    private lateinit var viewModel: LocalMusicViewModel
    private lateinit var playlistViewModel: PlaylistViewModel
    companion object {
        const val TAG = "MusicListFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = LocalMusicViewModel(repository)
        playlistViewModel = ViewModelProvider(this)[PlaylistViewModel::class.java]
        initView()

        requireActivity()
                .onBackPressedDispatcher
                .addCallback(
                        viewLifecycleOwner,
                        object : OnBackPressedCallback(true) {
                            override fun handleOnBackPressed() {
                                (activity as MainActivity).switchFragment(MainActivity.TAG_HOME)
                            }
                        }
                )

        // Setup Toolbar
        binding.btnBack.setOnClickListener {
            (activity as MainActivity).switchFragment(MainActivity.TAG_HOME)
        }

        binding.btnAdd.setOnClickListener {
            val intent = LocalMusicIntent.LoadLocalMusic
            updateMusic(intent)
        }

        binding.btnSearch.setOnClickListener {
            Toast.makeText(context, "搜索功能开发中...", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch { viewModel.viewState.collect { state -> handleUiState(state) } }
    }

    override fun createViewBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ): FragmentLocalMusicListBinding {
        Log.d("readMusic", "createViewBinding")
        return FragmentLocalMusicListBinding.inflate(inflater, container, false)
    }

    override fun handleUiState(state: LocalMusicState) {
        if (state.isLoading) showDialog() else hideDialog()
        currentMusicList = state.musicList
        if (currentMusicList.isEmpty()) {
            currentMusicIndex = 0
        } else {
            currentMusicIndex = currentMusicIndex.coerceIn(0, currentMusicList.lastIndex)
        }
        adapter.updateMusicList(state.musicList)
    }

    override fun sendIntent(intent: LocalMusicIntent) {
        viewModel.handleIntent(intent)
    }
    fun updateMusic(intent: LocalMusicIntent) {
        // showDialog()
        sendIntent(intent)
    }

    private fun showDialog() {
        if (loadingDialog != null) return
        val dialog = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading, null)
        loadingDialog =
                AlertDialog.Builder(requireContext()).setView(dialog).setCancelable(false).show()
    }

    private fun hideDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun initView() {
        val recyclerView = binding.musicList
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter =
                LocalMusicListAdapter(
                        ArrayList(),
                        { list, index ->
                            currentMusicList = list
                            currentMusicIndex = index
                            val host = activity as MainActivity
                            host.playOrPause(list, index)
                        },
                        { music -> showPlaylistSelectDialog(music) }
                )
        recyclerView.adapter = adapter
    }

    private fun showPlaylistSelectDialog(music: MusicEntity) {
        lifecycleScope.launch {
            val state = playlistViewModel.uiState.first()
            if (state.playlists.isNotEmpty()) {
                PlaylistSelectDialog(requireContext(), state.playlists) { playlist ->
                            addMusicToPlaylist(music, playlist)
                        }
                        .show()
            } else {
                Toast.makeText(context, "暂无歌单，请先创建歌单", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addMusicToPlaylist(music: MusicEntity, playlist: PlaylistEntity) {
        Toast.makeText(context, "已添加到歌单: ${playlist.name}", Toast.LENGTH_SHORT).show()
    }

    fun getCurrentMusicList(): List<MusicEntity> {
        return currentMusicList
    }

    fun getCurrentMusicIndex(): Int {
        if (currentMusicList.isEmpty()) return 0
        return currentMusicIndex.coerceIn(0, currentMusicList.lastIndex)
    }
}

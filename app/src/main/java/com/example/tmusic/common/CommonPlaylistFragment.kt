package com.example.tmusic.common

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.tmusic.MainActivity
import com.example.tmusic.R
import com.example.tmusic.base.BaseFragment
import com.example.tmusic.databinding.FragmentCommonPlaylistBinding
import com.example.tmusic.home.data.room.PlaylistDatabase
import com.example.tmusic.home.mvvm.PlaylistViewModel
import com.example.tmusic.listAndMusic.ListMusicRepository
import com.example.tmusic.listAndMusic.ListMusicViewModel
import com.example.tmusic.localMusicList.data.room.MusicDatabase
import com.example.tmusic.localMusicList.data.room.MusicEntity
import com.example.tmusic.widget.PlaylistSelectDialog
import kotlin.getValue
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CommonPlaylistFragment :
        BaseFragment<FragmentCommonPlaylistBinding>(FragmentCommonPlaylistBinding::inflate) {

    private lateinit var playlistViewModel: PlaylistViewModel
    private val listMusicViewModel by viewModels<ListMusicViewModel>()
    private lateinit var adapter: CommonPlaylistAdapter

    private var playlistId: Long = -1
    private var currentMusicList: List<MusicEntity> = emptyList()
    private var currentMusicIndex: Int = 0

    companion object {
        fun newInstance(playlistId: Long): CommonPlaylistFragment {
            return CommonPlaylistFragment().apply {
                arguments = Bundle().apply { putLong("playlistId", playlistId) }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playlistId = arguments?.getLong("playlistId") ?: -1
        initRepository()
        initView()
        observeMusicList()
        observeListMusicState()
        refreshSystemBars()
    }

    private fun initRepository() {
        val application = requireActivity().application
        val musicDb = MusicDatabase.Companion.getInstance(application)
        val musicDao = musicDb.musicDao()
        val playlistDb = PlaylistDatabase.Companion.getInstance(application)
        val playlistMusicDao = playlistDb.playlistMusicDao()
        val listMusicRepository = ListMusicRepository(playlistMusicDao, musicDao)

        //    listMusicViewModel = ListMusicViewModel(application)
        listMusicViewModel.initRepository(listMusicRepository)
    }

    override fun initView() {
        playlistViewModel = ViewModelProvider(this)[PlaylistViewModel::class.java]
        adapter =
                CommonPlaylistAdapter(
                        ArrayList(),
                        { list, index ->
                            currentMusicList = list
                            currentMusicIndex = index
                            (activity as? MainActivity)?.playOrPause(list, index)
                            updateNowPlaying()
                        },
                        { music -> showPlaylistSelectDialogForMusic(music) },
                        { music -> deleteMusicFromPlaylist(music) }
                )

        binding.playlistList.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistList.adapter = adapter

        binding.btnBack.setOnClickListener { navigateBackToHome() }

        binding.btnSearch.setOnClickListener {
            Toast.makeText(context, "搜索功能开发中...", Toast.LENGTH_SHORT).show()
        }

        binding.playPauseBtn.setOnClickListener {
            (activity as? MainActivity)?.playOrPause(
                    (activity as? MainActivity)?.currentMusicList ?: emptyList(),
                    (activity as? MainActivity)?.currentIndex ?: 0
            )
            updateNowPlaying()
        }

        binding.nowPlayingCard.setOnClickListener {
            val host = activity as? MainActivity ?: return@setOnClickListener
            host.lastTag = MainActivity.TAG_COMMON_PLAYLIST
            host.lastPlaylistId = playlistId
            host.goToMusicPlay()
        }

        updateNowPlaying()
    }
    private fun navigateBackToHome() {
        (activity as? MainActivity)?.navigateToHome()
    }

    private fun refreshSystemBars() {
        (activity as? MainActivity)?.ensureStatusBarVisible()
    }

    private fun observeMusicList() {
        viewLifecycleOwner.lifecycleScope.launch {
            listMusicViewModel.getMusicFromList(playlistId).collectLatest { musicIds ->
                if (musicIds.isEmpty()) {
                    currentMusicList = emptyList()
                    adapter.updateMusicList(emptyList())
                } else {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val musicList = getMusicByIds(musicIds)
                        currentMusicList = musicList
                        adapter.updateMusicList(musicList)
                    }
                }
            }
        }
    }

    private suspend fun getMusicByIds(ids: List<Long>): List<MusicEntity> {
        val musicDb = MusicDatabase.Companion.getInstance(requireActivity().application)
        val musicDao = musicDb.musicDao()
        return musicDao.getMusicByIds(ids)
    }

    private fun observeListMusicState() {
        viewLifecycleOwner.lifecycleScope.launch {
            listMusicViewModel.uiState.collect { state ->
                state.successMessage?.let { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    listMusicViewModel.consumeSuccessMessage()
                }
                state.error?.let { err ->
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    listMusicViewModel.consumeError()
                }
            }
        }
    }

    private fun showPlaylistSelectDialogForMusic(music: MusicEntity) {
        lifecycleScope.launch {
            val state = playlistViewModel.uiState.value
            Log.d("CommonPlaylistFragment", "${state.playlists.size}")
            if (state.playlists.isNotEmpty()) {
                PlaylistSelectDialog(requireContext(), state.playlists) { playlist ->
                            listMusicViewModel.addMusicToPlaylist(playlist.id, music.id)
                        }
                        .show()
            } else {
                Toast.makeText(context, "暂无歌单，请先创建歌单", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteMusicFromPlaylist(music: MusicEntity) {
        listMusicViewModel.deleteMusicFromPlaylist(playlistId, music.id)
    }

    fun getCurrentMusicList(): List<MusicEntity> = currentMusicList

    fun getCurrentMusicIndex(): Int {
        if (currentMusicList.isEmpty()) return 0
        return currentMusicIndex.coerceIn(0, currentMusicList.lastIndex)
    }

    private fun updateNowPlaying() {
        val host = activity as? MainActivity ?: return
        host.updateSongInfo()

        val cover = host.albumCover
        if (cover != null) {
            Glide.with(this).load(cover).into(binding.albumCover)
        } else {
            binding.albumCover.setImageResource(R.drawable.bg_moon_new)
        }

        binding.songTitle.text = host.songTitle ?: "暂无歌曲播放哦"
        binding.artistName.text = host.artistName ?: "未知艺术家"

        if (host.isPlaying()) {
            binding.playPauseBtn.setImageResource(R.drawable.icon_pause_new)
        } else {
            binding.playPauseBtn.setImageResource(R.drawable.icon_play_new)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            refreshSystemBars()
            updateNowPlaying()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshSystemBars()
        updateNowPlaying()
    }

    @Deprecated("Deprecated in Java")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            refreshSystemBars()
        }
    }
}

package com.example.tmusic.common

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
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
import com.example.tmusic.localMusicList.mvi.SortType
import com.example.tmusic.widget.PlaylistSelectDialog
import kotlin.getValue
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CommonPlaylistFragment :
        BaseFragment<FragmentCommonPlaylistBinding>(FragmentCommonPlaylistBinding::inflate) {

    private lateinit var playlistViewModel: PlaylistViewModel
    private val listMusicViewModel by viewModels<ListMusicViewModel>()
    private lateinit var adapter: CommonPlaylistAdapter

    private var playlistId: Long = -1
    private var currentMusicList: List<MusicEntity> = emptyList()
    private var originalMusicList: List<MusicEntity> = emptyList()
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
                            val host = activity as? MainActivity
                            if (host != null) {
                                // 强制同步更新 host 里的播放列表和索引
                                host.currentMusicList = list
                                host.currentIndex = index
                                host.playOrPause(list, index)
                            }
                            updateNowPlaying()
                        },
                        { music -> showPlaylistSelectDialogForMusic(music) },
                        { music -> deleteMusicFromPlaylist(music) }
                )

        binding.playlistList.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistList.adapter = adapter

        binding.btnBack.setOnClickListener { navigateBackToHome() }

        binding.btnSearch.setOnClickListener {
            binding.titleContainer.visibility = View.GONE
            binding.searchContainer.visibility = View.VISIBLE
            binding.etSearch.requestFocus()
        }

        binding.btnCloseSearch.setOnClickListener {
            binding.etSearch.text.clear()
            binding.searchContainer.visibility = View.GONE
            binding.titleContainer.visibility = View.VISIBLE
        }

        binding.etSearch.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {}
                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {}
                    override fun afterTextChanged(s: Editable?) {
                        listMusicViewModel.searchPlaylistMusic(s.toString())
                    }
                }
        )

        binding.btnSort.setOnClickListener { view ->
            val popupMenu = PopupMenu(requireContext(), view)
            popupMenu.menu.add(0, 1, 0, "按歌曲名称排序")
            popupMenu.menu.add(0, 2, 0, "按从新到旧排序")
            popupMenu.menu.add(0, 3, 0, "按从旧到新排序")
            popupMenu.menu.add(0, 4, 0, "按歌手名称排序")
            popupMenu.setOnMenuItemClickListener { item ->
                val sortType =
                        when (item.itemId) {
                            1 -> SortType.BY_NAME
                            2 -> SortType.BY_DATE_NEW_TO_OLD
                            3 -> SortType.BY_DATE_OLD_TO_NEW
                            4 -> SortType.BY_ARTIST
                            else -> SortType.BY_NAME
                        }
                listMusicViewModel.sortPlaylistMusic(sortType)
                true
            }
            popupMenu.show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            playlistViewModel.uiState.collectLatest { state ->
                val playlist = state.playlists.find { it.id == playlistId }
                binding.tvPlaylistName.text = playlist?.name ?: "歌单详情"
            }
        }

        binding.playPauseBtn.setOnClickListener {
            (activity as? MainActivity)?.playOrPause(
                    (activity as? MainActivity)?.currentMusicList ?: emptyList(),
                    (activity as? MainActivity)?.currentIndex ?: 0
            )
            updateNowPlaying()
        }

        binding.btnPrevious.setOnClickListener {
            val host = activity as? MainActivity ?: return@setOnClickListener
            host.playPrevious()
            updateNowPlaying()
        }

        binding.btnNext.setOnClickListener {
            val host = activity as? MainActivity ?: return@setOnClickListener
            host.playNext()
            updateNowPlaying()
        }

        binding.nowPlayingCard.setOnClickListener {
            val host = activity as? MainActivity ?: return@setOnClickListener
            host.goToMusicPlay()
        }

        updateNowPlaying()
    }
    private fun navigateBackToHome() {
        (activity as? MainActivity)?.navigateBack()
    }

    private fun refreshSystemBars() {
        (activity as? MainActivity)?.ensureStatusBarVisible()
    }

    private fun observeMusicList() {
        viewLifecycleOwner.lifecycleScope.launch {
            listMusicViewModel.getMusicFromList(playlistId).collectLatest { musicIds ->
                if (musicIds.isEmpty()) {
                    currentMusicList = emptyList()
                    originalMusicList = emptyList()
                    adapter.updateMusicList(emptyList())
                } else {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val musicList = getMusicByIds(musicIds)
                        originalMusicList = musicList
                        currentMusicList =
                                applySearchAndSort(
                                        musicList,
                                        listMusicViewModel.uiState.value.searchQuery,
                                        listMusicViewModel.uiState.value.sortType
                                )
                        adapter.updateMusicList(currentMusicList)
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
                if (originalMusicList.isNotEmpty()) {
                    val processedList =
                            applySearchAndSort(originalMusicList, state.searchQuery, state.sortType)
                    if (processedList != currentMusicList ||
                                    state.sortType != SortType.BY_NAME ||
                                    state.searchQuery.isNotEmpty()
                    ) {
                        currentMusicList = processedList
                        adapter.updateMusicList(processedList)
                    }
                }
            }
        }
    }

    private fun applySearchAndSort(
            originalList: List<MusicEntity>,
            query: String,
            sortType: SortType
    ): List<MusicEntity> {
        // 1. Filter by query
        val filteredList =
                if (query.isBlank()) {
                    originalList
                } else {
                    val lowerQuery = query.lowercase()
                    originalList.filter {
                        it.title.lowercase().contains(lowerQuery) ||
                                it.artist.lowercase().contains(lowerQuery)
                    }
                }

        // 2. Sort the filtered list
        return when (sortType) {
            SortType.BY_NAME -> filteredList.sortedWith(com.example.tmusic.utils.MusicSortUtils.musicNameComparator)
            SortType.BY_DATE_NEW_TO_OLD -> filteredList.sortedByDescending { it.id }
            SortType.BY_DATE_OLD_TO_NEW -> filteredList.sortedBy { it.id }
            SortType.BY_ARTIST -> filteredList.sortedBy { it.artist }
            else -> filteredList.sortedWith(com.example.tmusic.utils.MusicSortUtils.musicNameComparator)
        }
    }

    private fun showPlaylistSelectDialogForMusic(music: MusicEntity) {
        lifecycleScope.launch {
            val state = playlistViewModel.uiState.value
            Log.d("CommonPlaylistFragment", "${state.playlists.size}")
            if (state.playlists.isNotEmpty()) {
                PlaylistSelectDialog(requireContext(), state.playlists) { playlist ->
                            addMusicToPlaylist(playlist.id, music)
                        }
                        .show()
            } else {
                Toast.makeText(context, "暂无歌单，请先创建歌单", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteMusicFromPlaylist(music: MusicEntity) {
        listMusicViewModel.deleteMusicFromPlaylist(playlistId, music.id) {
            updatePlaylistCoverAfterMusicChange()
        }
    }

    private fun addMusicToPlaylist(playlistIdTo: Long, music: MusicEntity) {
        listMusicViewModel.addMusicToPlaylist(playlistIdTo, music.id) {
            lifecycleScope.launch { updatePlaylistCoverAfterMusicChangeForPlaylist(playlistIdTo) }
        }
    }

    private fun updatePlaylistCoverAfterMusicChange() {
        lifecycleScope.launch {
            try {
                val application = requireActivity().application
                val musicDao = MusicDatabase.getInstance(application).musicDao()
                val playlistMusicDao = PlaylistDatabase.getInstance(application).playlistMusicDao()

                val latestId = playlistMusicDao.getLatestMusicIdFromList(playlistId)
                if (latestId != null) {
                    val musics =
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                musicDao.getMusicByIds(listOf(latestId))
                            }
                    if (musics.isNotEmpty() && musics.first().albumArt != null) {
                        playlistViewModel.updatePlaylistCover(playlistId, musics.first().albumArt)
                    } else {
                        playlistViewModel.updatePlaylistCover(playlistId, null)
                    }
                } else {
                    playlistViewModel.updatePlaylistCover(playlistId, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updatePlaylistCoverAfterMusicChangeForPlaylist(targetPlaylistId: Long) {
        try {
            val application = requireActivity().application
            val musicDao = MusicDatabase.getInstance(application).musicDao()
            val playlistMusicDao = PlaylistDatabase.getInstance(application).playlistMusicDao()

            lifecycleScope.launch {
                try {
                    val latestId = playlistMusicDao.getLatestMusicIdFromList(targetPlaylistId)
                    if (latestId != null) {
                        val musics =
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    musicDao.getMusicByIds(listOf(latestId))
                                }
                        if (musics.isNotEmpty() && musics.first().albumArt != null) {
                            playlistViewModel.updatePlaylistCover(
                                    targetPlaylistId,
                                    musics.first().albumArt
                            )
                        } else {
                            playlistViewModel.updatePlaylistCover(targetPlaylistId, null)
                        }
                    } else {
                        playlistViewModel.updatePlaylistCover(targetPlaylistId, null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentMusicList(): List<MusicEntity> = currentMusicList

    fun getCurrentMusicIndex(): Int {
        if (currentMusicList.isEmpty()) return 0
        return currentMusicIndex.coerceIn(0, currentMusicList.lastIndex)
    }

    fun updateNowPlaying() {
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

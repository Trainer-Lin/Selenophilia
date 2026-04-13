package com.example.tmusic.home.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.tmusic.MainActivity
import com.example.tmusic.R
import com.example.tmusic.base.BaseFragment
import com.example.tmusic.databinding.FragmentHomeBinding
import com.example.tmusic.databinding.ItemPlaylistBinding
import com.example.tmusic.home.data.room.PlaylistEntity
import com.example.tmusic.home.mvvm.PlaylistViewModel
import com.example.tmusic.widget.AddPlaylistDialog
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private lateinit var viewModel: PlaylistViewModel
    private val colorList = listOf("#A772BE", "#9C9BEB", "#E1B5D4")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[PlaylistViewModel::class.java]
        initView()
        observePlaylists()
    }

    override fun initView() {
        updateGreeting()

        binding.localMusic.setOnClickListener {
            (activity as MainActivity).switchFragment(MainActivity.Companion.TAG_LOCAL_MUSIC)
        }

        binding.playPauseBtn.setOnClickListener {
            val host = activity as MainActivity
            host.playOrPause(host.currentMusicList, host.currentIndex)
            updateUi()
        }

        binding.nextBtn.setOnClickListener {
            (activity as MainActivity).playNext()
            updateUi()
        }

        binding.prevBtn.setOnClickListener {
            (activity as MainActivity).playPrevious()
            updateUi()
        }

        binding.btnAddPlaylist.setOnClickListener { showAddPlaylistDialog() }

        binding.musicCard.setOnClickListener {
            (activity as MainActivity).goToMusicPlay()
        }
    }

    private fun updateGreeting() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val (emoji, time) =
                when (hour) {
                    in 0..5 -> "🌙" to " Night"
                    in 6..11 -> "☀️" to " Morning"
                    in 12..17 -> "🌤️" to " Afternoon"
                    else -> "🌙" to "Evening"
                }
        binding.tvGreetingEmoji.text = emoji
        binding.tvGreetingTime.text = time
    }

    private fun observePlaylists() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state -> renderPlaylists(state.playlists) }
        }
    }

    private fun renderPlaylists(playlists: List<PlaylistEntity>) {
        binding.playlistItemsContainer.removeAllViews()
        playlists.forEach { playlist ->
            val itemBinding =
                    ItemPlaylistBinding.inflate(
                            LayoutInflater.from(context),
                            binding.playlistItemsContainer,
                            false
                    )
            itemBinding.tvPlaylistTitle.text = playlist.name
            itemBinding.root.setCardBackgroundColor(
                colorList[playlist.colorIndex % colorList.size].toColorInt()
            )
            val playlistId = playlist.id

            itemBinding.btnEditPlaylist.setOnClickListener {
               showUpdatePlaylistDialog(playlistId)
            }

            itemBinding.btnDeletePlaylist.setOnClickListener {
                viewModel.deletePlaylist(playlist)
            }

            itemBinding.root.setOnClickListener {
                (activity as MainActivity).goToMusicList(playlistId)
            }
            binding.playlistItemsContainer.addView(itemBinding.root)
        }
    }

    private fun showAddPlaylistDialog() {
        AddPlaylistDialog(requireContext(), "创建歌单") { name ->
            viewModel.addPlaylist(name) }.show()
    }

    private fun showUpdatePlaylistDialog(id: Long) {
        AddPlaylistDialog(requireContext(), "编辑歌单名称") { name->
            viewModel.updatePlaylist(name,id) }.show()
    }

    private fun updateUi() {
        val host = activity as MainActivity
        host.updateSongInfo()

        val cover = host.albumCover
        if (cover != null) {
            Glide.with(this).load(cover).into(binding.albumCover)
        } else {
            binding.albumCover.setImageResource(R.drawable.bg_moon_new)
        }

        binding.songTitle.text = host.songTitle ?: "暂无歌曲播放哦"
        binding.artistName.text = host.artistName ?: "未知艺术家"

        Log.d("HomeFragment", "${host.songTitle}")

        if (host.isPlaying()) {
            binding.playPauseBtn.setImageResource(R.drawable.icon_pause_new)
        } else {
            binding.playPauseBtn.setImageResource(R.drawable.icon_play_new)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            updateUi()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUi()
    }
}

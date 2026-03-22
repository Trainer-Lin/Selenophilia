package com.example.tmusic.home

import android.os.Bundle
import android.view.View
import com.example.tmusic.MainActivity
import com.example.tmusic.R
import com.example.tmusic.base.BaseFragment
import com.example.tmusic.databinding.FragmentHomeBinding
import com.example.tmusic.localMusicList.ui.LocalMusicListFragment
import com.bumptech.glide.Glide

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun initView() {
        binding.localMusic.setOnClickListener {
            (activity as MainActivity).switchFragment(LocalMusicListFragment())
        }

        binding.playPauseBtn.setOnClickListener{
            val host = activity as MainActivity
            host.playOrPause(host.currentMusicList, host.currentIndex)
            updateUi()
        }

        binding.nextBtn.setOnClickListener{
            (activity as MainActivity).playNext()
            updateUi()
        }

        binding.prevBtn.setOnClickListener{
            (activity as MainActivity).playPrevious()
            updateUi()
        }
        
    }

    private fun updateUi(){
        val host = activity as MainActivity
        host.updateSongInfo()

        val cover = host.albumCover
        if (cover != null) {
            Glide.with(this)
                .load(cover)
                .into(binding.albumCover)
        } else {
            binding.albumCover.setImageResource(R.drawable.ic_launcher_foreground)
        }

        binding.songTitle.text = host.songTitle ?: "暂无歌曲播放哦"
        binding.artistName.text = host.artistName ?: "未知艺术家"

        if (host.isPlaying()) {
            binding.playPauseBtn.setImageResource(R.drawable.icon_pause_new)
        } else {
            binding.playPauseBtn.setImageResource(R.drawable.icon_play_new)
        }
    }

    override fun onResume() {
        super.onResume()
        updateUi()
    }

}

package com.example.tmusic

import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.example.tmusic.base.BaseFragment
import com.example.tmusic.databinding.FragmentMusicPlayBinding

class MusicPlayFragment :
        BaseFragment<FragmentMusicPlayBinding>(FragmentMusicPlayBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun initView() {
        binding.btnBack.setOnClickListener { activity?.onBackPressed() }

        binding.btnPlayPause.setOnClickListener {
            val host = activity as? MainActivity ?: return@setOnClickListener
            host.playOrPause(host.currentMusicList, host.currentIndex)
            updateUi()
        }

        binding.btnNext.setOnClickListener {
            val host = activity as? MainActivity ?: return@setOnClickListener
            host.playNext()
            updateUi()
        }

        binding.btnPrevious.setOnClickListener {
            val host = activity as? MainActivity ?: return@setOnClickListener
            host.playPrevious()
            updateUi()
        }
    }

    private fun updateUi() {
        val host = activity as? MainActivity ?: return

        val cover = host.albumCover
        if (cover != null) {
            Glide.with(this).load(cover).into(binding.albumCover)
        } else {
            binding.albumCover.setImageResource(R.drawable.bg_moon_new)
        }

        binding.songTitle.text = host.songTitle ?: "暂无歌曲播放哦"
        binding.artistName.text = host.artistName ?: "未知艺术家"

        if (host.isPlaying()) {
            binding.btnPlayPause.setImageResource(R.drawable.icon_pause_new)
        } else {
            binding.btnPlayPause.setImageResource(R.drawable.icon_play_new)
        }
    }

    override fun onResume() {
        super.onResume()
        updateUi()
    }
}

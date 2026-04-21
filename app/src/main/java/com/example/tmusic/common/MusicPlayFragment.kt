package com.example.tmusic.common

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.annotation.OptIn
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.example.tmusic.MainActivity
import com.example.tmusic.R
import com.example.tmusic.base.BaseFragment
import com.example.tmusic.databinding.FragmentMusicPlayBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MusicPlayFragment :
        BaseFragment<FragmentMusicPlayBinding>(FragmentMusicPlayBinding::inflate) {

    private var isUserSeeking: Boolean = false
    private var progressJob: Job? = null
    private var progressAnimator: ValueAnimator? = null
    private var progressInitialized = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun initView() {
        Log.d("MusicPlay", "initViewStart")
        startProgressListener()
        Log.d("MusicPlay", "startProgressListener")

        binding.btnBack.setOnClickListener {
            (activity as? MainActivity)?.navigateBack()
        }

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

        binding.progressBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    @UnstableApi
                    override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int, // 进度百分比
                            fromUser: Boolean
                    ) {
                        if (fromUser) {
                            val service = (activity as MainActivity).getMusicService()
                            if (service != null) {
                                val duration = service.getCurrentDuration()
                                val newPosition = progress.toLong() * duration / 1000
                                binding.currentTime.text = formatTime(newPosition) // 拖动改变显示时间
                                if (!isUserSeeking) service.seekTo(newPosition) // 单击事件跳转
                            }
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        isUserSeeking = true
                    }

                    @OptIn(UnstableApi::class)
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        isUserSeeking = false
                        val service = (activity as MainActivity).getMusicService()
                        if (service != null) {
                            val progress = seekBar?.progress ?: 0
                            val duration = service.getCurrentDuration()
                            val newPosition = progress * duration / 1000
                            service.seekTo(newPosition)
                        }
                    }
                }
        )
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateUi() {
        val host = activity as? MainActivity ?: return

        val cover = host.albumCover
        if (cover != null) {
            Glide.with(this).load(cover).into(binding.albumCover)
        } else {
            binding.albumCover.setImageResource(R.drawable.bg_heart)
        }

        binding.songTitle.text = host.songTitle ?: "暂无歌曲播放哦"
        binding.artistName.text = host.artistName ?: "未知艺术家"

        if (host.isPlaying()) {
            binding.btnPlayPause.setImageResource(R.drawable.icon_pause_new)
        } else {
            binding.btnPlayPause.setImageResource(R.drawable.icon_play_new)
        }
    }

    /** 更新进度条和时间显示（带平滑动画） */

    @OptIn(UnstableApi::class)
    private fun updateProgress(){
        val host = activity as? MainActivity ?: return
        val service = host.getMusicService() ?: return
        val duration = service.getCurrentDuration()
        if (duration <= 0L) return
        val position = service.getCurrentPosition()
        val actualProgress =(position.toFloat() / duration * 1000).toInt()
        if (!progressInitialized) {
            binding.progressBar.progress = actualProgress
            progressInitialized = true
        } else {
            val currentProgress = binding.progressBar.progress
            updateProgressAnimate(currentProgress,  actualProgress)
        }
        binding.currentTime.text = formatTime(position)
        binding.totalTime.text = formatTime(duration)
    }

    private fun updateProgressAnimate(currentProgress: Int, targetProgress: Int){
        progressAnimator?.cancel()
        progressAnimator = null
        if(kotlin.math.abs(currentProgress - targetProgress ) <=5){
            binding.progressBar. progress = targetProgress
        }else{
            val animator = ValueAnimator.ofInt(currentProgress, targetProgress) //创建动画 ，从当前进度到目标进度
            val progressBar = binding.progressBar
            animator.duration = 1000
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener {animation ->
                progressBar.progress = animation.animatedValue as Int //动画每更新一次， 更新一次Progress
            }
            animator.start()
            progressAnimator = animator
        }
    }
    @OptIn(UnstableApi::class)
    private fun startProgressListener() {
        if (progressJob?.isActive == true) return
        progressJob = viewLifecycleOwner.lifecycleScope.launch { //让生命周期到onDestroyView就结束
            while (isActive) {
                if (!isUserSeeking) {
                    updateProgress()
                }
                delay(1000)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startProgressListener()
        updateUi()
    }

    override fun onDestroyView() {
        progressJob?.cancel()
        progressJob = null
        progressAnimator?.cancel()
        progressAnimator = null
        progressInitialized = false
        super.onDestroyView()
    }
}

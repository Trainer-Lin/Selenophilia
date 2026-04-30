package com.example.tmusic.musicPlay

import android.animation.ValueAnimator
import android.os.Bundle
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

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

    @OptIn(UnstableApi::class)
    override fun initView() {
        startProgressListener()
        
        val host = activity as? MainActivity
        val service = host?.getMusicService()
        if (service != null) {
            updateOrderIcon(service.playMode)
        }

        binding.btnBack.setOnClickListener {
            (activity as? MainActivity)?.navigateBack()
        }

        binding.btnPlayPause.setOnClickListener {
            val hostActivity = activity as? MainActivity ?: return@setOnClickListener
            hostActivity.playOrPause(hostActivity.currentMusicList, hostActivity.currentIndex)
            updateUi()
        }

        binding.btnNext.setOnClickListener {
            val hostActivity = activity as? MainActivity ?: return@setOnClickListener
            hostActivity.playNext()
            updateUi()
        }

        binding.btnPrevious.setOnClickListener {
            val hostActivity = activity as? MainActivity ?: return@setOnClickListener
            hostActivity.playPrevious()
            updateUi()
        }

        binding.btnPlayOrder.setOnClickListener {
            val hostActivity = activity as? MainActivity ?: return@setOnClickListener
            val s = hostActivity.getMusicService() ?: return@setOnClickListener
            s.playMode = when(s.playMode){
                0 -> 1
                1 -> 2
                else -> 0
            }
            updateOrderIcon(s.playMode)
        }

        binding.btnSubtitles.setOnClickListener {
            toggleLyrics()
        }

        binding.albumCover.setOnClickListener {
            toggleLyrics()
        }

        binding.lyricsScrollView.setOnClickListener {
            toggleLyrics()
        }
        
        binding.lyricsTextView.setOnClickListener {
            toggleLyrics()
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
                            }
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        isUserSeeking = true
                        progressAnimator?.cancel() // 停止当前正在播放的动画，防止覆盖用户的单击进度
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

    private fun updateOrderIcon(playMode: Int){
        when(playMode){
            0 -> binding.btnPlayOrder.setImageResource(R.drawable.icon_repeat_list)
            1 -> binding.btnPlayOrder.setImageResource(R.drawable.icon_shuffle)
            2 -> binding.btnPlayOrder.setImageResource(R.drawable.icon_repeat_one)
        }
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
        
        val lyrics = host.lyrics
        if (!lyrics.isNullOrEmpty()) {
            binding.lyricsTextView.text = lyrics
        } else {
            binding.lyricsTextView.text = "暂无歌词"
        }

        if (host.isPlaying()) {
            binding.btnPlayPause.setImageResource(R.drawable.icon_pause_new)
        } else {
            binding.btnPlayPause.setImageResource(R.drawable.icon_play_new)
        }
    }

    private fun toggleLyrics() {
        if (binding.lyricsScrollView.visibility == View.VISIBLE) {
            binding.lyricsScrollView.visibility = View.GONE
            binding.coverInfoGroup.visibility = View.VISIBLE
        } else {
            binding.coverInfoGroup.visibility = View.INVISIBLE
            binding.lyricsScrollView.visibility = View.VISIBLE
        }
    }

    /** 更新进度条和时间显示（带平滑动画） */

    @OptIn(UnstableApi::class)
    private fun updateProgress(){
        val host = activity as? MainActivity ?: return
        val service = host.getMusicService() ?: return
        
        // 自动切歌时更新UI
        if (host.currentIndex != service.getCurrentMusicIndex()) {
            host.updateSongInfo()
            updateUi()
        }

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
        if(abs(currentProgress - targetProgress ) <=5){
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
        (activity as? MainActivity)?.updateSongInfo()
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
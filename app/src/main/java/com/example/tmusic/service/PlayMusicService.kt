package com.example.tmusic.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.tmusic.localMusicList.data.room.MusicEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@UnstableApi
@SuppressLint("RestrictedApi")
class PlayMusicService : Service() {
    private val musicBinder = MusicBinder()
    // 创建协程作用域
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // 核心播放/系统交互对象
    private var exoPlayer: ExoPlayer? = null // 播放核心，谷歌官方媒体库，负责音频解码、播放
    private var mediaSession: MediaSession? = null // 新版媒体会话，对接系统媒体控制(锁屏显示音乐控制等)
    private var musicList: List<MusicEntity> = emptyList() // 播放列表
    private var currentMusicIndex: Int = 0
    var isPlaying: Boolean
        get() = exoPlayer?.isPlaying ?: false
        set(value) {
            exoPlayer?.playWhenReady = value
        }

    companion object {
        const val CHANNEL_ID = "play_music"
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY_OR_PAUSE = "com.example.tmusic.service.ACTION_PLAY_OR_PAUSE"
        const val ACTION_PREVIOUS = "com.example.tmusic.service.ACTION_PREVIOUS"
        const val ACTION_NEXT = "com.example.tmusic.service.ACTION_NEXT"
        const val EXTRA_MUSIC_LIST = "extra_music_list"
        const val EXTRA_MUSIC_INDEX = "extra_music_index"
    }
    inner class MusicBinder : Binder() {
        fun getService() = this@PlayMusicService // Activity拿到binder后，获取Service实例，调用其中方法
    }
    override fun onBind(intent: Intent): IBinder {
        return musicBinder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        initExoplayer()
        initMediaSession()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放播放器
        exoPlayer?.release()
        exoPlayer = null
        // 释放 MediaSession
        mediaSession?.release()
        mediaSession = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_OR_PAUSE -> {
                if (!isPlaying) {
                    val newMusicList =
                            intent.getParcelableArrayListExtra<MusicEntity>(EXTRA_MUSIC_LIST)
                    val newIndex = intent.getIntExtra(EXTRA_MUSIC_INDEX, 0)
                    if (newMusicList != null) playOrPauseMusic(newMusicList.toList(), newIndex)
                } else pauseMusic()
            }
            ACTION_PREVIOUS -> playPrevious()
            ACTION_NEXT -> playNext()
        }
        return START_STICKY
    }

    private fun initExoplayer() {
        val audioAttributes =
                AudioAttributes.Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .setUsage(C.USAGE_MEDIA)
                        .build()

        exoPlayer = ExoPlayer.Builder(this).setAudioAttributes(audioAttributes, true).build()

        exoPlayer?.addListener(
                object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_ENDED -> {
                                playNext()
                            }
                        }
                    }
                }
        )
    }
    private fun initMediaSession() {
        mediaSession = MediaSession.Builder(this, exoPlayer!!).build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                    NotificationChannel(
                                    CHANNEL_ID,
                                    "Play Music",
                                    NotificationManager.IMPORTANCE_MIN
                            )
                            .apply { setShowBadge(false) }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(): Notification {
        // return android.app.Notification.Builder(this, "hidden_music_playback")
        return NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play) // 系统强制要求必须有小图标
                .setShowWhen(false) // 不显示时间
                .build()
    }

    fun pauseMusic() {
        exoPlayer?.pause()
    }
    fun playMusic() {
        exoPlayer?.play()
    }

    private fun syncCurrentMusicIndex() {
        val playerIndex = exoPlayer?.currentMediaItemIndex ?: C.INDEX_UNSET
        if (playerIndex != C.INDEX_UNSET) {
            currentMusicIndex = playerIndex
        }
    }

    fun playPrevious() {
        syncCurrentMusicIndex()
        if (musicList.isEmpty()) return
        val prevIndex =
                if (currentMusicIndex > 0) {
                    currentMusicIndex - 1
                } else {
                    musicList.size - 1
                }
        exoPlayer?.seekTo(prevIndex, 0)
        currentMusicIndex = prevIndex
    }
    fun playNext() {
        syncCurrentMusicIndex()
        if (musicList.isEmpty()) return
        val nextIndex =
                if (currentMusicIndex < musicList.size - 1) {
                    currentMusicIndex + 1
                } else {
                    0
                }
        exoPlayer?.seekTo(nextIndex, 0)
        currentMusicIndex = nextIndex
    }
    fun playOrPauseMusic(musicList: List<MusicEntity>, index: Int) {
        if (musicList.isEmpty()) return
        syncCurrentMusicIndex()
        val sameList = this.musicList.map { it.uri } == musicList.map { it.uri }
        val safeIndex = index.coerceIn(0, musicList.lastIndex)
        val sameTrack = sameList && currentMusicIndex == safeIndex

        if (sameTrack && (exoPlayer?.mediaItemCount ?: 0) > 0) {
            if (exoPlayer?.isPlaying == true) {
                pauseMusic()
            } else {
                playMusic()
            }
            return
        } // 处理点击同一首歌的逻辑

        this.musicList = musicList
        this.currentMusicIndex = safeIndex
        val mediaItems =
                musicList.map { music ->
                    MediaItem.Builder()
                            .setUri(music.uri)
                            .setMediaMetadata(
                                    MediaMetadata.Builder()
                                            .setTitle(music.title)
                                            .setArtist(music.artist)
                                            .build()
                            )
                            .build()
                }
        exoPlayer?.clearMediaItems()
        exoPlayer?.addMediaItems(mediaItems)
        exoPlayer?.prepare()
        exoPlayer?.seekTo(safeIndex, 0)
        playMusic()
    }
    fun getCurrentMusicList(): List<MusicEntity> {
        return musicList
    }

    fun getCurrentMusicIndex(): Int {
        if (musicList.isEmpty()) return 0
        return currentMusicIndex.coerceIn(0, musicList.lastIndex)
    }

    fun getCurrentMusic(): MusicEntity? {
        if (musicList.isEmpty()) return null
        return musicList[getCurrentMusicIndex()]
    }
    fun resumeMusic() {
        playMusic()
    }
    fun seekTo(pos: Long) {
        exoPlayer?.seekTo(pos)
    }
}

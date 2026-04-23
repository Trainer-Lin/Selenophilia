package com.example.tmusic

import com.example.tmusic.web.WebMusicFragment
import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.tmusic.base.FullScreenActivity
import com.example.tmusic.common.CommonPlaylistFragment
import com.example.tmusic.common.MusicPlayFragment
import com.example.tmusic.databinding.ActivityMainBinding
import com.example.tmusic.home.ui.HomeFragment
import com.example.tmusic.localMusicList.data.room.MusicEntity
import com.example.tmusic.localMusicList.ui.LocalMusicListFragment
import com.example.tmusic.service.PlayMusicService
import com.example.tmusic.study.ui.StudyFragment

@OptIn(UnstableApi::class)
class MainActivity : FullScreenActivity<ActivityMainBinding>() {

    companion object {
        const val TAG = "MainActivity"
        const val UPDATE_MUSIC_REQUEST = 1001
        const val READ_MUSIC_PERMISSION = 1002
        const val POST_NOTIFICATION_PERMISSION = 1003
    }

    private var musicService: PlayMusicService? = null
    private lateinit var navController: NavController
    override fun createViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    var currentMusicList: List<MusicEntity> = emptyList()
    var currentIndex: Int = 0

    var albumCover: String? = null
    var songTitle: String? = null
    var artistName: String? = null

    private val serviceConnection =
            object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as PlayMusicService.MusicBinder
                    musicService = binder.getService()
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    musicService = null
                }
            }

    private fun initService() {
        val intent = Intent(this, PlayMusicService::class.java)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initService()
        requestAudioPermission()
        Looper.myQueue().addIdleHandler {
            requestAudioPermission()
            false
        }

        initNavigation()
    }

    fun goToMusicList(id: Long) {
        val bundle = Bundle().apply { putLong("playlistId", id) }
        navController.navigate(R.id.commonPlaylist, bundle)
    }

    fun goToMusicPlay() {
        navController.navigate(R.id.musicPlayFragment)
    }

    fun navigateBack(){
        navController.navigateUp()
    }

    fun navigateToHome() {
        navController.popBackStack(R.id.homeFragment, false)
    }

    fun ensureStatusBarVisible() {
        WindowCompat.getInsetsController(window, window.decorView)
            ?.show(WindowInsetsCompat.Type.statusBars())
    }

    private fun initNavigation(){
        val navHost = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHost.navController
        setupDestination()
        navController.addOnDestinationChangedListener {
            _, destination, _->
                updateBottomNavVisibility(destination.id)
                ensureStatusBarVisible()
        }
    }

    private fun navigateTo(id: Int){
        if(navController.currentDestination?.id == id)  return
        navController.navigate(id)
    }

    private fun setupDestination(){
        binding.navHome.setOnClickListener {
            navigateTo(R.id.homeFragment)
        }
        binding.navStudy.setOnClickListener {
            navigateTo(R.id.studyFragment)
        }
        binding.navDiary.setOnClickListener {
            navigateTo(R.id.webMusicFragment)
        }
    }
    private fun updateBottomNavVisibility(id: Int) {
        when(id){
            R.id.localMusicFragment ->binding.bottomNavCard.visibility = android.view.View.GONE
            R.id.commonPlaylist-> binding.bottomNavCard.visibility = android.view.View.GONE
            R.id.webMusicFragment-> binding.bottomNavCard.visibility = android.view.View.GONE
            R.id.musicPlayFragment -> binding.bottomNavCard.visibility = android.view.View.GONE
            else -> binding.bottomNavCard.visibility = android.view.View.VISIBLE
        }
    }

    // 获取当前Fragment
    private fun getCurrentFragment(): Fragment? =
            supportFragmentManager.findFragmentById(R.id.fragment_container)

    fun playOrPause(musicList: List<MusicEntity>, index: Int) {
        if (musicList.isEmpty()) {
            clearSongInfo()
            return
        }
        val safeIndex = index.coerceIn(0, musicList.lastIndex)
        musicService?.playOrPauseMusic(musicList, safeIndex)
        if (musicService != null) {
            syncSongInfoFromService()
        } else {
            saveSongInfo(musicList, safeIndex)
        }
    }

    fun playNext() {
        musicService?.playNext()
        syncSongInfoFromService()
    }

    fun playPrevious() {
        musicService?.playPrevious()
        syncSongInfoFromService()
    }

    fun updateSongInfo() {
        syncSongInfoFromService()
    }

    fun isPlaying(): Boolean {
        return musicService?.isPlaying == true
    }
    
    /**
     * 获取音乐播放服务实例，供Fragment调用
     */
    fun getMusicService(): PlayMusicService? {
        return musicService
    }

    private fun syncSongInfoFromService() {
        val service = musicService ?: return
        val list = service.getCurrentMusicList()
        if (list.isEmpty()) {
            clearSongInfo()
            return
        }
        saveSongInfo(list, service.getCurrentMusicIndex())
    }
    private fun saveSongInfo(musicList: List<MusicEntity>, index: Int) {
        if (musicList.isEmpty()) {
            clearSongInfo()
            return
        }
        currentMusicList = musicList
        currentIndex = index.coerceIn(0, currentMusicList.lastIndex)
        val music = currentMusicList[currentIndex]
        albumCover = music.albumArt
        songTitle = music.title
        artistName = music.artist
    }

    private fun clearSongInfo() {
        currentMusicList = emptyList()
        currentIndex = 0
        albumCover = null
        songTitle = null
        artistName = null
    }

    fun showMessage(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).apply {
            val cardView =
                    CardView(applicationContext).apply {
                        radius = 25f
                        cardElevation = 8f
                        setCardBackgroundColor(getColor(R.color.white))
                        useCompatPadding = true
                    }

            val textView =
                    TextView(applicationContext).apply {
                        text = message
                        textSize = 17f
                        setTextColor(getColor(R.color.black))
                        gravity = Gravity.CENTER
                        setPadding(80, 40, 80, 40)
                    }
            cardView.addView(textView)
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 140)
            view = cardView
            show()
        }
    }

    private fun requestAudioPermission() {
        // 根据安卓版本判断需要申请的权限
        val permission = Manifest.permission.READ_MEDIA_AUDIO

        // 检查权限是否已授予
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        ) {
            // 未授予，申请权限
            ActivityCompat.requestPermissions(this, arrayOf(permission), READ_MUSIC_PERMISSION)
        }
    }

    private fun requestNotificationPermission() {
        // 根据安卓版本判断需要申请的权限
        val permission = Manifest.permission.POST_NOTIFICATIONS

        // 检查权限是否已授予
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        ) {
            // 未授予，申请权限
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    POST_NOTIFICATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_MUSIC_PERMISSION -> requestNotificationPermission()
        }
    }
}

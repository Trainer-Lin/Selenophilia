package com.example.tmusic

import WebMusicFragment
import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
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
        const val TAG_HOME = "1"
        const val TAG_STUDY = "2"
        const val TAG_WEB = "3"
        const val TAG_LOCAL_MUSIC = "4"
        const val UPDATE_MUSIC_REQUEST = 1001
        const val READ_MUSIC_PERMISSION = 1002
        const val POST_NOTIFICATION_PERMISSION = 1003
    }

    private var musicService: PlayMusicService? = null

    override fun createViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    var currentMusicList: List<MusicEntity> = emptyList()
    var currentIndex: Int = 0

    var albumCover: String? = null
    var songTitle: String? = null
    var artistName: String? = null

    private var currentFragmentTag: String? = TAG_HOME
    private val fragments = mutableMapOf<String, Fragment>()

    private fun getOrCreateFragment(tag: String): Fragment {
        return fragments[tag]
                ?: when (tag) {
                    TAG_HOME -> HomeFragment()
                    TAG_STUDY -> StudyFragment()
                    TAG_LOCAL_MUSIC -> LocalMusicListFragment()
                    TAG_WEB -> WebMusicFragment()
                    else -> HomeFragment()
                }.also { fragments[tag] = it }
    }

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

        initBottomNavigation()
        switchFragment(TAG_HOME)
    }

    private fun initBottomNavigation() {
        binding.navHome.setOnClickListener {
            if (currentFragmentTag != TAG_HOME) {
                switchFragment(TAG_HOME)
            }
        }

        binding.navStudy.setOnClickListener {
            if (currentFragmentTag != TAG_STUDY) {
                switchFragment(TAG_STUDY)
            }
        }

        binding.navDiary.setOnClickListener {
            if (currentFragmentTag != TAG_WEB) {
                switchFragment(TAG_WEB)
            }
        }
    }

    fun switchFragment(tag: String) {
        val transaction = supportFragmentManager.beginTransaction()
        val newFragment = getOrCreateFragment(tag)

        if (currentFragmentTag != tag) {
            fragments[currentFragmentTag]?.let { transaction.hide(it) }
        }

        if (newFragment.isAdded) {
            transaction.show(newFragment)
        } else {
            transaction.add(R.id.fragment_container, newFragment, tag)
        }
        currentFragmentTag = tag
        transaction.commit()
        updateBottomNavVisibility(newFragment)
    }

    fun goToMusicList(id: Long) {
        val fragment = CommonPlaylistFragment.newInstance(id)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        updateBottomNavVisibility(fragment)
    }

    fun goToMusicPlay() {
        val fragment = MusicPlayFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        updateBottomNavVisibility(fragment)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Update visibility when popping back stack
        val currentFragment = getCurrentFragment()
        if (currentFragment != null) {
            updateBottomNavVisibility(currentFragment)
        }
    }

    private fun updateBottomNavVisibility(fragment: Fragment) {
//        if (fragment is LocalMusicListFragment) {
//            binding.bottomNavCard.visibility = android.view.View.GONE
//        } else if (fragment is CommonPlaylistFragment) {
//            binding.bottomNavCard.visibility = android.view.View.GONE
//        } else if (fragment is WebMusicFragment) {
//            binding.bottomNavCard.visibility = android.view.View.GONE
//        } else {
//            binding.bottomNavCard.visibility = android.view.View.VISIBLE
//        }
//
        when(fragment){
            is LocalMusicListFragment -> binding.bottomNavCard.visibility = android.view.View.GONE
            is CommonPlaylistFragment -> binding.bottomNavCard.visibility = android.view.View.GONE
            is WebMusicFragment -> binding.bottomNavCard.visibility = android.view.View.GONE
            is MusicPlayFragment -> binding.bottomNavCard.visibility = android.view.View.GONE
            else -> binding.bottomNavCard.visibility = android.view.View.VISIBLE
        }

    }

    fun togglePlayFromHome() {
        if (musicService == null) return
        if (musicService!!.isPlaying) {
            musicService!!.pauseMusic()
        } else {
            // If we just want to resume or play current list
            // PlayMusicService logic: playOrPauseMusic checks if same track.
            // If we just call resumeMusic() or playMusic(), it might work if prepared.
            musicService!!.playMusic()
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

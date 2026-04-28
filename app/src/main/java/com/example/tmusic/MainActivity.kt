package com.example.tmusic

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
import androidx.viewpager2.widget.ViewPager2
import com.example.tmusic.MainPagerAdapter
import com.example.tmusic.base.FullScreenActivity
import com.example.tmusic.common.CommonPlaylistFragment
import com.example.tmusic.common.MusicPlayFragment
import com.example.tmusic.databinding.ActivityMainBinding
import com.example.tmusic.home.ui.HomeFragment
import com.example.tmusic.localMusicList.data.room.MusicEntity
import com.example.tmusic.localMusicList.ui.LocalMusicListFragment
import com.example.tmusic.service.PlayMusicService
import com.example.tmusic.study.ui.StudyFragment
import com.example.tmusic.web.WebMusicFragment

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
    private lateinit var pagerAdapter: MainPagerAdapter

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

    override fun onResume() {
        super.onResume()
        setCustomDensity(this, application, 412)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initService()
        requestAudioPermission()
        Looper.myQueue().addIdleHandler {
            requestAudioPermission()
            false
        }

        initViewPager()
        initNavigation()
        setupBottomNav()
    }

    private fun initViewPager() {
        pagerAdapter = MainPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        
        val sharedPrefs = getSharedPreferences("Settings", android.content.Context.MODE_PRIVATE)
        val isFocusMode = sharedPrefs.getBoolean("isFocusMode", false)
        val initialItem = if (isFocusMode) 0 else 1
        
        binding.viewPager.setCurrentItem(initialItem, false)
        updateNavIcon(initialItem)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Log.d("ViewPager","{${binding.viewPager.currentItem}}")
                updateNavIcon(position)
            }
        })
    }

    private fun setupBottomNav() {
        binding.navHome.setOnClickListener {
            if (binding.viewPager.currentItem != 1) {
                binding.viewPager.currentItem = 1
            }
        }
        binding.navStudy.setOnClickListener {
            if (binding.viewPager.currentItem != 0) {
                binding.viewPager.currentItem = 0
            }
        }
        binding.navSettings.setOnClickListener {
            if (binding.viewPager.currentItem != 2) {
                binding.viewPager.currentItem = 2
            }
        }
    }

    private fun updateNavIcon(position: Int) {
        val selectedColor = ContextCompat.getColor(this, R.color.purple_500)
        val normalColor = ContextCompat.getColor(this, R.color.black)
        binding.navStudy.setColorFilter(if (position == 0) selectedColor else normalColor)
        binding.navHome.setColorFilter(if (position == 1) selectedColor else normalColor)
        binding.navSettings.setColorFilter(if (position == 2) selectedColor else normalColor)
    }

    fun goToMusicList(id: Long) {
        showNavContainer()
        val bundle = Bundle().apply { putLong("playlistId", id) }
        navController.navigate(R.id.action_homeFragment_to_commonPlaylist, bundle)
    }

    fun goToMusicPlay() {
        showNavContainer()
        val currentDestination = navController.currentDestination?.id
        val actionId = when (currentDestination) {
            R.id.homeFragment -> R.id.action_homeFragment_to_musicPlayFragment
            R.id.localMusicFragment -> R.id.action_localMusicFragment_to_musicPlayFragment
            R.id.commonPlaylist -> R.id.action_commonPlaylist_to_musicPlayFragment
            R.id.studyFragment -> R.id.action_studyFragment_to_musicPlayFragment
            else -> R.id.musicPlayFragment
        }
        navController.navigate(actionId)
    }

    fun navigateBack() {
        navController.navigateUp()
        if (navController.currentDestination?.id == R.id.homeFragment) {
            showViewPager()
        }
    }

    fun navigateToHome() {
        showViewPager()
        binding.viewPager.currentItem = 1
    }

    fun ensureStatusBarVisible() {
        WindowCompat.getInsetsController(window, window.decorView)
            ?.show(WindowInsetsCompat.Type.statusBars())
    }

    private fun initNavigation() {
        val navHost = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHost.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateBottomNavVisibility(destination.id)
            ensureStatusBarVisible()
        }
    }

    private fun navigateTo(id: Int) {
        navController.navigate(id)
    }

    private fun updateBottomNavVisibility(id: Int) {
        when (id) {
            R.id.localMusicFragment -> binding.bottomNavCard.visibility = android.view.View.GONE
            R.id.commonPlaylist -> binding.bottomNavCard.visibility = android.view.View.GONE
            R.id.musicPlayFragment -> binding.bottomNavCard.visibility = android.view.View.GONE
            R.id.personalFragment -> binding.bottomNavCard.visibility = android.view.View.GONE
            R.id.webMusicFragment -> binding.bottomNavCard.visibility = android.view.View.GONE
            else -> binding.bottomNavCard.visibility = android.view.View.VISIBLE
        }
    }

    fun goToWebMusic() {
        showNavContainer()
        navController.navigate(R.id.webMusicFragment)
    }

    fun goToLocalMusic() {
        showNavContainer()
        navController.navigate(R.id.action_homeFragment_to_localMusicFragment)
    }

    fun goToPersonalMusic() {
        showNavContainer()
        navController.navigate(R.id.action_homeFragment_to_personalFragment)
    }

    private fun showNavContainer() {
        binding.viewPager.visibility = android.view.View.GONE
        binding.fragmentContainer.visibility = android.view.View.VISIBLE
    }

    private fun showViewPager() {
        binding.fragmentContainer.visibility = android.view.View.GONE
        binding.viewPager.visibility = android.view.View.VISIBLE
    }

    private fun getCurrentFragment(): Fragment? =
            supportFragmentManager.findFragmentById(R.id.fragment_container)

    fun playOrPause(musicList: List<MusicEntity>, index: Int) {
        if (musicList.isEmpty()) {
            clearSongInfo()
            return
        }
        val safeIndex = index.coerceIn(0, musicList.lastIndex)
        saveSongInfo(musicList, safeIndex)
        musicService?.playOrPauseMusic(musicList, safeIndex)
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
        val permission = Manifest.permission.READ_MEDIA_AUDIO
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), READ_MUSIC_PERMISSION)
        }
    }

    private fun requestNotificationPermission() {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
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

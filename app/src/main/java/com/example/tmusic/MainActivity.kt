package com.example.tmusic

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
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
import com.example.tmusic.musicPlay.MusicPlayFragment
import com.example.tmusic.databinding.ActivityMainBinding
import com.example.tmusic.home.ui.HomeFragment
import com.example.tmusic.localMusicList.data.room.MusicEntity
import com.example.tmusic.localMusicList.ui.LocalMusicListFragment
import com.example.tmusic.service.PlayMusicService
import com.example.tmusic.study.ui.StudyFragment
import com.example.tmusic.web.WebMusicFragment

@OptIn(UnstableApi::class)
class MainActivity : FullScreenActivity<ActivityMainBinding>() {
    private data class LyricsIndexEntry(
        val uri: Uri,
        val displayName: String,
        val relativePath: String?,
        val normalizedKey: String
    )

    companion object {
        const val TAG = "MainActivity"
        const val UPDATE_MUSIC_REQUEST = 1001
        const val READ_MUSIC_PERMISSION = 1002
        const val POST_NOTIFICATION_PERMISSION = 1003
        private const val NAV_EXIT_ANIM_DURATION = 300L
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
    var lyrics: String? = null
    private var pendingAllFilesAccessRequest = false

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
        if (pendingAllFilesAccessRequest && hasAllFilesAccessPermission()) {
            pendingAllFilesAccessRequest = false
            requestStartupPermissions()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initService()
        requestStartupPermissions()
        Looper.myQueue().addIdleHandler {
            requestStartupPermissions()
            false
        }

        initViewPager()
        initNavigation()
        setupBottomNav()
        setupBackPressed()
    }

    private fun setupBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.fragmentContainer.visibility == android.view.View.VISIBLE) {
                    navigateBack()
                } else if (binding.viewPager.currentItem != 1) {
                    binding.viewPager.setCurrentItem(1, true)
                } else {
                    finish()
                }
            }
        })
    }

    private fun initViewPager() {
        pagerAdapter = MainPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        
        val sharedPrefs = getSharedPreferences("Settings", android.content.Context.MODE_PRIVATE)
        val isFocusMode = sharedPrefs.getBoolean("isFocusMode", false)
        
        // Check if there is a saved page index
        val savedIndex = sharedPrefs.getInt("last_viewpager_index", -1)
        val initialItem = if (savedIndex != -1) {
            savedIndex
        } else if (isFocusMode) {
            0 
        } else {
            1
        }
        
        binding.viewPager.setCurrentItem(initialItem, false)
        updateNavIcon(initialItem)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Log.d("ViewPager","{${binding.viewPager.currentItem}}")
                updateNavIcon(position)
                sharedPrefs.edit().putInt("last_viewpager_index", position).apply()
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
        val typedValue = android.util.TypedValue()
        theme.resolveAttribute(R.attr.themeColorTextTitle, typedValue, true)
        val selectedColor = typedValue.data
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
        val isNavigated = navController.navigateUp()

        if (!isNavigated) {
            navigateToHome()
            return
        }

        if (navController.currentDestination?.id == R.id.homeFragment) {
            binding.fragmentContainer.postDelayed(
                {
                    if (navController.currentDestination?.id == R.id.homeFragment) {
                        showViewPager()
                    }
                },
                NAV_EXIT_ANIM_DURATION
            )
        }
    }

    fun navigateToHome() {
        showViewPager()
        binding.viewPager.setCurrentItem(1, false)
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
            notifyFragments()
            return
        }
        val safeIndex = index.coerceIn(0, musicList.lastIndex)
        saveSongInfo(musicList, safeIndex)
        musicService?.playOrPauseMusic(musicList, safeIndex)
        notifyFragments()
    }

    fun playNext() {
        musicService?.playNext()
        syncSongInfoFromService()
        notifyFragments()
    }

    fun playPrevious() {
        musicService?.playPrevious()
        syncSongInfoFromService()
        notifyFragments()
    }

    fun updateSongInfo() {
        syncSongInfoFromService()
    }

    private fun notifyFragments() {
        // Notify ViewPager fragments
        for (i in 0 until pagerAdapter.itemCount) {
            val fragment = supportFragmentManager.findFragmentByTag("f$i")
            when (fragment) {
                is HomeFragment -> fragment.updateUi()
                is StudyFragment -> fragment.updateUi()
            }
        }
        
        // Notify current navigation fragment if any
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? NavHostFragment
        val currentNavFragment = navHostFragment?.childFragmentManager?.primaryNavigationFragment
        when (currentNavFragment) {
            is LocalMusicListFragment -> currentNavFragment.updateNowPlaying()
            is CommonPlaylistFragment -> currentNavFragment.updateNowPlaying()
            is MusicPlayFragment -> currentNavFragment.refreshUi()
        }
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
        
        // If lyrics are not in DB, try to fetch them on the fly
        if (music.lyrics.isNullOrEmpty()) {
            lyrics = fetchLyricsOnTheFly(music.uri)
        } else {
            lyrics = music.lyrics
        }
    }

    private fun fetchLyricsOnTheFly(uriString: String?): String? {
        if (uriString.isNullOrEmpty()) return null
        try {
            val uri = Uri.parse(uriString)
            val projection = arrayOf(
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.RELATIVE_PATH,
                MediaStore.Audio.Media.DATA
            )
            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val title =
                        cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                        )
                    val artist =
                        cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                        )
                    val displayName =
                        cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                        )
                    val relativePath =
                        cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)
                        )
                    val legacyPath = cursor.getOptionalString(MediaStore.Audio.Media.DATA)
                    val filePath = resolveAudioFilePath(displayName, relativePath, legacyPath)
                    val candidateNames = buildLyricsCandidateNames(filePath, displayName, title)
                    if (!filePath.isNullOrEmpty()) {
                        val audioFile = java.io.File(filePath)
                        val parentDir = audioFile.parentFile

                        if (parentDir != null && parentDir.exists()) {
                            val lrcFile = findLyricsFileInDirectory(parentDir, candidateNames)
                            if (lrcFile.exists() && lrcFile.canRead()) {
                                return lrcFile.readText()
                            }
                        }

                        val embeddedLyrics = com.example.tmusic.lyric.data.EmbeddedLyricExtractor.extractLyrics(filePath)
                        if (!embeddedLyrics.isNullOrBlank()) {
                            return embeddedLyrics
                        }
                    }
                    val candidateKeys = buildLyricsCandidateKeys(filePath, displayName, title, artist)
                    val lyricsIndex = buildLyricsIndex()
                    val matchedEntry =
                        findLyricsEntry(
                            candidateKeys = candidateKeys,
                            candidateNames = candidateNames,
                            relativePath = relativePath,
                            lyricsIndex = lyricsIndex
                        )
                    if (matchedEntry != null) {
                        return readLyricsFromUri(matchedEntry.uri)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun resolveAudioFilePath(
        displayName: String?,
        relativePath: String?,
        legacyPath: String?
    ): String? {
        if (!relativePath.isNullOrEmpty() && !displayName.isNullOrEmpty()) {
            return java.io.File(Environment.getExternalStorageDirectory(), relativePath + displayName)
                .absolutePath
        }
        return legacyPath
    }

    private fun buildLyricsCandidateNames(
        audioFilePath: String?,
        displayName: String?,
        title: String?
    ): List<String> {
        val candidates = linkedSetOf<String>()
        val fileNameBase = audioFilePath?.let { java.io.File(it).nameWithoutExtension }?.trim().orEmpty()
        val displayNameBase = displayName?.substringBeforeLast('.')?.trim().orEmpty()
        val titleBase = title?.trim().orEmpty()

        if (fileNameBase.isNotEmpty()) candidates.add("$fileNameBase.lrc")
        if (displayNameBase.isNotEmpty()) candidates.add("$displayNameBase.lrc")
        if (titleBase.isNotEmpty()) candidates.add("$titleBase.lrc")

        return candidates.toList()
    }

    private fun buildLyricsCandidateKeys(
        audioFilePath: String?,
        displayName: String?,
        title: String?,
        artist: String?
    ): List<String> {
        val keys = linkedSetOf<String>()
        val fileNameBase = audioFilePath?.let { java.io.File(it).nameWithoutExtension }?.trim().orEmpty()
        val displayNameBase = displayName?.substringBeforeLast('.')?.trim().orEmpty()
        val titleBase = title?.trim().orEmpty()
        val artistBase = artist?.trim().orEmpty()

        listOf(
            fileNameBase,
            displayNameBase,
            titleBase,
            stripBracketContent(fileNameBase),
            stripBracketContent(displayNameBase),
            stripBracketContent(titleBase),
            listOf(artistBase, titleBase).filter { it.isNotBlank() }.joinToString(" "),
            listOf(titleBase, artistBase).filter { it.isNotBlank() }.joinToString(" "),
            listOf(artistBase, stripBracketContent(titleBase)).filter { it.isNotBlank() }.joinToString(" "),
            listOf(stripBracketContent(titleBase), artistBase).filter { it.isNotBlank() }.joinToString(" ")
        ).forEach { candidate ->
            val normalized = normalizeLyricsKey(candidate)
            if (normalized.isNotEmpty()) {
                keys.add(normalized)
            }
        }

        return keys.toList()
    }

    private fun findLyricsFileInDirectory(
        parentDir: java.io.File,
        candidateNames: List<String>
    ): java.io.File {
        val files = parentDir.listFiles().orEmpty()
        candidateNames.forEach { candidate ->
            files.firstOrNull { file ->
                file.isFile && file.name.equals(candidate, ignoreCase = true)
            }?.let { return it }
        }
        return java.io.File(parentDir, candidateNames.firstOrNull() ?: "")
    }

    private fun buildLyricsIndex(): Map<String, List<LyricsIndexEntry>> {
        val lyricsMap = linkedMapOf<String, MutableList<LyricsIndexEntry>>()
        val filesUri = MediaStore.Files.getContentUri("external")

        contentResolver.query(
            filesUri,
            arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.RELATIVE_PATH
            ),
            "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ? OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?",
            arrayOf("%.lrc", "%.LRC"),
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val displayName =
                    cursor.getOptionalString(MediaStore.Files.FileColumns.DISPLAY_NAME) ?: continue
                val baseName = displayName.substringBeforeLast('.', displayName)
                val normalizedKey = normalizeLyricsKey(baseName)
                if (normalizedKey.isEmpty()) continue

                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                val entry =
                    LyricsIndexEntry(
                        uri = android.content.ContentUris.withAppendedId(filesUri, id),
                        displayName = displayName,
                        relativePath = cursor.getOptionalString(MediaStore.Files.FileColumns.RELATIVE_PATH),
                        normalizedKey = normalizedKey
                    )
                lyricsMap.getOrPut(normalizedKey) { mutableListOf() }.add(entry)
            }
        }
        return lyricsMap
    }

    private fun findLyricsEntry(
        candidateKeys: List<String>,
        candidateNames: List<String>,
        relativePath: String?,
        lyricsIndex: Map<String, List<LyricsIndexEntry>>
    ): LyricsIndexEntry? {
        candidateKeys.forEach { key ->
            val entries = lyricsIndex[key].orEmpty()
            if (entries.isEmpty()) return@forEach

            entries.firstOrNull { entry ->
                candidateNames.any { it.equals(entry.displayName, ignoreCase = true) } &&
                    entry.relativePath.equals(relativePath, ignoreCase = true)
            }?.let { return it }

            entries.firstOrNull { entry ->
                candidateNames.any { it.equals(entry.displayName, ignoreCase = true) }
            }?.let { return it }

            entries.firstOrNull()?.let { return it }
        }

        val titleKey =
            candidateKeys.maxByOrNull { it.length }
                ?.takeIf { it.isNotEmpty() }
        val allEntries = lyricsIndex.values.flatten().distinctBy { it.uri }
        return allEntries
            .map { entry ->
                entry to scoreLyricsEntry(entry, candidateKeys, titleKey, relativePath)
            }
            .filter { it.second >= 100 } // 提高准入门槛，不能随便匹配
            .maxByOrNull { it.second }
            ?.first
    }

    private fun scoreLyricsEntry(
        entry: LyricsIndexEntry,
        candidateKeys: List<String>,
        titleKey: String?,
        relativePath: String?
    ): Int {
        var score = 0
        val isSameDir = !relativePath.isNullOrEmpty() && entry.relativePath.equals(relativePath, ignoreCase = true)
        
        if (candidateKeys.any { it == entry.normalizedKey }) {
            score += 120
            if (isSameDir) score += 40
            return score // 完全匹配直接高分
        }
        
        if (candidateKeys.any { candidate ->
                candidate.length >= 4 &&
                    (entry.normalizedKey.contains(candidate) || candidate.contains(entry.normalizedKey))
            }) {
            score += 80
        }
        if (!titleKey.isNullOrEmpty() && titleKey.length >= 3 &&
            (entry.normalizedKey.contains(titleKey) || titleKey.contains(entry.normalizedKey))
        ) {
            score += 60
        }
        
        // 只有在名字有一定匹配度时，同目录才作为加分项，否则同目录的唯一歌词会污染其他歌
        if (score >= 60 && isSameDir) {
            score += 40
        }
        
        return score
    }

    private fun stripBracketContent(value: String): String {
        return value.replace(Regex("\\(.*?\\)|\\[.*?\\]|\\{.*?\\}|（.*?）|【.*?】"), "").trim()
    }

    private fun readLyricsFromUri(uri: Uri): String? {
        contentResolver.openInputStream(uri)?.use { stream ->
            return stream.bufferedReader().readText()
        }
        return null
    }

    private fun normalizeLyricsKey(value: String?): String {
        return value
            ?.lowercase()
            ?.replace(Regex("\\.lrc$"), "")
            ?.replace(Regex("[^\\p{L}\\p{N}]"), "")
            ?.trim()
            .orEmpty()
    }

    private fun android.database.Cursor.getOptionalString(columnName: String): String? {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getString(index) else null
    }

    private fun clearSongInfo() {
        currentMusicList = emptyList()
        currentIndex = 0
        albumCover = null
        songTitle = null
        artistName = null
        lyrics = null
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

    private fun requestStartupPermissions() {
        if (!hasAudioPermission()) {
            requestAudioPermission()
            return
        }
        if (!hasAllFilesAccessPermission()) {
            requestAllFilesAccessPermission()
            return
        }
        requestNotificationPermission()
    }

    private fun hasAudioPermission(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestAudioPermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val permissionsToRequest =
            permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), READ_MUSIC_PERMISSION)
        }
    }

    private fun hasAllFilesAccessPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager()
    }

    private fun requestAllFilesAccessPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        pendingAllFilesAccessRequest = true
        val intent =
            Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:$packageName")
            )
        try {
            startActivity(intent)
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
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
            READ_MUSIC_PERMISSION -> requestStartupPermissions()
        }
    }
}

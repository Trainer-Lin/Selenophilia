package com.example.tmusic.web

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.example.tmusic.MainActivity
import com.example.tmusic.R
import kotlin.math.abs

class WebMusicFragment : Fragment() {
    private var webView: WebView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_web_music, container, false)
        webView = view.findViewById<WebView>(R.id.webView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settings = webView?.settings
        settings?.apply {
            javaScriptEnabled = true // JavaScript支持
            domStorageEnabled = true // 本地存储
            useWideViewPort = true // 自适应屏幕
            loadWithOverviewMode = true
            cacheMode = WebSettings.LOAD_DEFAULT // 缓存模式
            allowFileAccess = true
        }

        webView?.setDownloadListener { url, _, _, _, _ ->
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        }

        webView?.loadUrl("https://www.gequke.com/")

        showTipDialog()

        setupDraggableFab()
    }

    private fun setupDraggableFab() {
        val fab = view?.findViewById<ImageButton>(R.id.fabBack) ?: return
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        fab.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = v.x.toInt()
                    initialY = v.y.toInt()
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    v.x = initialX + deltaX
                    v.y = initialY + deltaY
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    if (abs(deltaX) < 10 && abs(deltaY) < 10) {
                        (activity as MainActivity).navigateToHome()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun showTipDialog() {
        val dialog =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_tip_mp3_warning, null)
        val tipDialog =
                AlertDialog.Builder(requireContext(), R.style.TransparentAlertDialog)
                        .setView(dialog)
                        .create()
        dialog.findViewById<Button>(R.id.btnConfirm).setOnClickListener { tipDialog.dismiss() }
        tipDialog.show()

        val metrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireContext().display?.getMetrics(metrics)
        }
        val width = (300 * metrics.density).toInt()
        val height = (600 * metrics.density).toInt()
        tipDialog.window?.setLayout(width, height)
    }
}
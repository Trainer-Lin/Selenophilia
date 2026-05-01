package com.example.tmusic.setting.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.tmusic.MainActivity
import com.example.tmusic.R
import com.example.tmusic.databinding.FragmentSettingBinding
import com.example.tmusic.localMusicList.data.Repository
import com.example.tmusic.localMusicList.data.room.MusicDatabase
import kotlinx.coroutines.launch

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private val application by lazy { requireActivity().application }
    private val db by lazy { MusicDatabase.getInstance(application) }
    private val musicDao by lazy { db.musicDao() }
    private val repository by lazy { Repository(application, musicDao) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initInitialInterface()
        
        binding.btnScanMusic.setOnClickListener {
            scanMusic()
        }

        binding.llManageSources.setOnClickListener {
            // Jump to WebView (WebMusicFragment)
            (activity as? MainActivity)?.goToWebMusic()
        }

        binding.llAboutUs.setOnClickListener {
            showAboutUsDialog()
        }

        binding.cvThemePurple.setOnClickListener {
            changeTheme("purple")
        }
        (binding.cvThemePurple.parent as View).setOnClickListener {
            changeTheme("purple")
        }
        
        binding.cvThemeYellow.setOnClickListener {
            changeTheme("yellow")
        }
        (binding.cvThemeYellow.parent as View).setOnClickListener {
            changeTheme("yellow")
        }
        
        binding.cvThemePink.setOnClickListener {
            changeTheme("pink")
        }
        (binding.cvThemePink.parent as View).setOnClickListener {
            changeTheme("pink")
        }
    }

    private fun changeTheme(themeName: String) {
        val sharedPrefs = requireActivity().getSharedPreferences("ThemeSettings", Context.MODE_PRIVATE)
        val currentTheme = sharedPrefs.getString("current_theme", "purple")
        if (currentTheme != themeName) {
            sharedPrefs.edit().putString("current_theme", themeName).apply()
            requireActivity().recreate()
        }
    }

    private fun initInitialInterface() {
        val sharedPrefs = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val isFocusMode = sharedPrefs.getBoolean("isFocusMode", false)
        
        if (isFocusMode) {
            binding.rgInitialInterface.check(R.id.rbFocusMode)
        } else {
            binding.rgInitialInterface.check(R.id.rbMainInterface)
        }

        binding.rgInitialInterface.setOnCheckedChangeListener { _, checkedId ->
            val editor = sharedPrefs.edit()
            if (checkedId == R.id.rbFocusMode) {
                editor.putBoolean("isFocusMode", true)
                editor.putInt("last_viewpager_index", 0)
            } else {
                editor.putBoolean("isFocusMode", false)
                editor.putInt("last_viewpager_index", 1)
            }
            editor.apply()
        }
    }

    private fun scanMusic() {
        viewLifecycleOwner.lifecycleScope.launch {
            (activity as? MainActivity)?.showMessage("开始扫描音乐...")
            repository.updateMusicList()
            (activity as? MainActivity)?.showMessage("扫描完成")
        }
    }

    private fun showAboutUsDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_about_us)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        val btnConfirm = dialog.findViewById<TextView>(R.id.btnConfirm)
        btnConfirm.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

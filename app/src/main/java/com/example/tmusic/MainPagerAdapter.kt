package com.example.tmusic

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tmusic.home.ui.HomeFragment
import com.example.tmusic.setting.ui.SettingFragment
import com.example.tmusic.study.ui.StudyFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3
    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> StudyFragment()
        1 -> HomeFragment()
        2 -> SettingFragment()
        else -> throw IllegalStateException()
    }
}
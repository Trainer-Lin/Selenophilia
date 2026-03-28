package com.example.tmusic

import android.app.Application
import com.tencent.mmkv.MMKV

class TAppliaction: Application() {
    companion object {
        lateinit var instance: TAppliaction
            private set
    }

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
    }

}
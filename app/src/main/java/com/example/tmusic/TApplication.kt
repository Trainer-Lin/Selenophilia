package com.example.tmusic

import android.app.Application
import android.util.Log
import com.tencent.mmkv.MMKV

class TApplication : Application() {
    companion object {
        lateinit var instance: TApplication
            private set

        private const val TAG = "TApplication"
        lateinit var mmkv: MMKV
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d("TApplication" , "step1")
      MMKV.initialize(this)
        Log.d("TApplication" , "step2")
      mmkv = MMKV.defaultMMKV()
        Log.d("TApplication" , "step3")
        val flag = if(mmkv == null) {
            Log.d(TAG, "mmkv is null")
            false
        } else {
            Log.d(TAG, "mmkv is not null")
            true
        }

    }
}
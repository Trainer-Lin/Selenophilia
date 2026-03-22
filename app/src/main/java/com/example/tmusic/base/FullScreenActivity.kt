package com.example.tmusic.base

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding

abstract class FullScreenActivity<VB: ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: VB
    private set

    /**
     * 子类必须实现此方法来创建ViewBinding实例
     */
    protected abstract fun createViewBinding(): VB
    //屏幕适配: 保持UI在不同尺寸设备上显示一致
    //designWidthDp: 设计稿的宽度, 单位为dp , 为412dp(下面调用)

    private fun setCustomDensity(activity: Activity, application: Application, designWidthDp: Int) {
        val appDisplayMetrics =
            application.resources.displayMetrics //displayMetrics: 显示指标, 包含屏幕的宽度、高度、密度、屏幕DPI等信息

        val targetDensity =
            1.0f * appDisplayMetrics.widthPixels / designWidthDp  //修改刻度 px = dp * density
        val targetDensityDpi = (targetDensity * 160).toInt()  //修改屏幕DPI 乘160是系统要求
        var sNonCompactScaleDensity =
            appDisplayMetrics.scaledDensity //保存系统默认字体缩放密度的一个变量 , 如果用户改变了字体大小, 则需要更新这个值 ,不然写死了改不了
        application.registerComponentCallbacks(object :
            ComponentCallbacks { //设置成回调： 系统检测到变化时， 自动调用这个方法
            override fun onConfigurationChanged(newConfig: Configuration) {
                if (newConfig.fontScale > 0) { //如果缩放比例有效, 则更新字体缩放密度
                    sNonCompactScaleDensity =
                        application.resources.displayMetrics.scaledDensity //这里的已经是新值 , 只是看符不符合给当前字体设置的条件
                }                              //这里是应用级别的字体
            }

            override fun onLowMemory() {
            }

        })
        val targetScaleDensity =
            targetDensity * (sNonCompactScaleDensity / appDisplayMetrics.density) //修改字体缩放密度 , 比如用户改为2.4 , 后面的是修改前值 , 比如默认2.0 , 那就是放大了1.2倍 字体也放大1.2倍

        appDisplayMetrics.density = targetDensity
        appDisplayMetrics.densityDpi = targetDensityDpi
        appDisplayMetrics.scaledDensity = targetScaleDensity //修改原始值

        val activityDisplayMetrics = activity.resources.displayMetrics
        activityDisplayMetrics.density = targetDensity
        activityDisplayMetrics.densityDpi = targetDensityDpi
        activityDisplayMetrics.scaledDensity = targetScaleDensity  //Activity级别的字体
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setCustomDensity(this, application, 412)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }//这个listener是监听系统配置 , 比如窗口创建完成 , 屏幕旋转 , 系统UI变化 , 就是保证时刻正确显示界面
        binding = createViewBinding()
        setContentView(binding.root)
    }

}

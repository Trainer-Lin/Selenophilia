plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("com.google.devtools.ksp") version "2.0.21-1.0.25"
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.tmusic"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tmusic"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }

}



dependencies {
    //Media3音频播放器
    implementation (libs.androidx.media3.exoplayer)

    implementation (libs.androidx.media3.ui)
    implementation (libs.androidx.media3.common)
    implementation (libs.androidx.media3.session)
    implementation (libs.androidx.media3.datasource)
    implementation (libs.androidx.media3.datasource.cronet)
    implementation(libs.androidx.media3.extractor)

    //Room持久化存储
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx) // Kotlin扩展
    ksp(libs.androidx.room.compiler) // 注解处理器

    //mmkv
    implementation("com.tencent:mmkv:1.3.2")

    //这些暂时用不上
    implementation (libs.androidx.media3.exoplayer.hls) //hls流媒体 , 直播用
    implementation (libs.androidx.media3.exoplayer.rtsp) //RTSP流媒体 , 监控用
    implementation (libs.androidx.media3.datasource.rtmp)//RTMP流媒体 , 直播推流用

    //RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.0.0")

    //ViewModel和Lifecycle相关依赖
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2") // ViewModel Kotlin扩展
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")   // Lifecycle运行时Kotlin扩展
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2") // LiveData Kotlin扩展
    implementation("androidx.lifecycle:lifecycle-common-java8:2.6.2") // Java8生命周期支持
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.6.2") // ViewModel状态保存

    //Glide图片加载库
    implementation("com.github.bumptech.glide:glide:4.16.0")

    //SmartRefreshLayout
    implementation("io.github.scwang90:refresh-layout-kernel:2.1.0")
    implementation("io.github.scwang90:refresh-header-classics:2.1.0")
    implementation("io.github.scwang90:refresh-footer-classics:2.1.0")

    implementation("androidx.fragment:fragment-ktx:1.6.0")
    // Navigation (XML NavGraph)
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

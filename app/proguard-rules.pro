# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Please add these rules to your existing proguard-rules.pro
-keep @androidx.room.Entity class * { *; }
-keep class com.example.tmusic.localMusicList.data.room.MusicEntity { *; }
-keep class com.example.tmusic.home.data.room.PlaylistEntity { *; }
-keep class com.example.tmusic.study.data.PlanEntity { *; }
-keep class com.example.tmusic.listAndMusic.room.PlaylistCrossRef { *; }

-keep class com.example.tmusic.personal.data.MostPlayedSong { *; }
-keep class com.example.tmusic.personal.mvi.PersonalState { *; }
-keep class com.example.tmusic.listAndMusic.model.ListMusicState { *; }
-keep class com.example.tmusic.localMusicList.mvi.LocalMusicState { *; }
-keep class com.example.tmusic.study.mvi.StudyState { *; }
-keep class com.example.tmusic.home.mvvm.model.PlaylistState { *; }

-keepnames class * implements android.os.Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Glide
-keep public class * extends com.bumptech.glide.module.AppGlideModule { *; }
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule { *; }
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl { *; }

# SmartRefreshLayout
-keep class com.scwang.smart.refresh.layout.** { *; }
-dontwarn com.scwang.smart.refresh.layout.**

# WebView
-keepattributes AnnotationDefault,SourceFile,LineNumberTable,JavaScriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# MMKV
-keep class com.tencent.mmkv.** { *; }
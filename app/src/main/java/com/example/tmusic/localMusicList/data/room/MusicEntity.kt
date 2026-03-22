package com.example.tmusic.localMusicList.data.room

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = "music",
    indices = [
        Index(value = ["title"]),
        Index(value = ["artist"]),
        Index(value = ["artist", "title"]),
        Index(value = ["title", "artist"])
    ]
)
@Parcelize
data class MusicEntity (
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var uri: String,
    var title: String,
    var artist: String,
    var duration: Long,
    var albumArt: String? = null // 封面图片的路径或URL
): Parcelable
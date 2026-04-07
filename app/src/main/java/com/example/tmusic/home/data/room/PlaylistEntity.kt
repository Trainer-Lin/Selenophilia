package com.example.tmusic.home.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist")
data class PlaylistEntity(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val name: String,
        val description: String = "",
        val coverPath: String? = null,
        val createTime: Long = System.currentTimeMillis(),
        val colorIndex: Int = 0
)

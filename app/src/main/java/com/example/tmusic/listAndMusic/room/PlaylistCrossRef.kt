package com.example.tmusic.listAndMusic.room

import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.tmusic.home.data.room.PlaylistEntity

@Entity(
    tableName = "playlist_cross_ref",
    primaryKeys = ["playlistId", "musicId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlaylistCrossRef(
    val playlistId: Long,
    val musicId: Long,
    val addedTime: Long = System.currentTimeMillis()
)
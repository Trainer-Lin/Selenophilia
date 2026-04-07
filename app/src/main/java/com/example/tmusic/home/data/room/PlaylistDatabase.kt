package com.example.tmusic.home.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tmusic.listAndMusic.room.PlaylistCrossRef
import com.example.tmusic.listAndMusic.room.PlaylistMusicDao

@Database(
        entities = [PlaylistEntity::class, PlaylistCrossRef::class],
        version = 1,
        exportSchema = false
)
abstract class PlaylistDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistMusicDao(): PlaylistMusicDao

    companion object {
        @Volatile private var INSTANCE: PlaylistDatabase? = null

        fun getInstance(context: Context): PlaylistDatabase {
            return INSTANCE
                    ?: synchronized(this) {
                        val instance =
                                Room.databaseBuilder(
                                                context.applicationContext,
                                                PlaylistDatabase::class.java,
                                                "PlaylistDatabase"
                                        )
                                        .build()
                        INSTANCE = instance
                        instance
                    }
        }
    }
}

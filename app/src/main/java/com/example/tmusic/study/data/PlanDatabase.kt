package com.example.tmusic.study.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tmusic.study.data.PlanDao
import com.example.tmusic.study.data.PlanEntity

@Database(entities = [PlanEntity::class], version = 1)
abstract class PlanDatabase : RoomDatabase() {
    abstract fun planDao(): PlanDao
    companion object {

        @Volatile
        private var INSTANCE: PlanDatabase? = null

        fun getInstance(context: Context): PlanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlanDatabase::class.java,
                    "StudyDatabase"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
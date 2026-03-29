package com.example.tmusic.study.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "studyPlan")
data class PlanEntity(
    @PrimaryKey(autoGenerate = true )val id: Int = 0,
    val content:String,
    val isFinished:Boolean,
    val date: String
)
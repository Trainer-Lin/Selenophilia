package com.example.tmusic.study.mvi

import com.example.tmusic.study.data.PlanEntity

data class StudyState(
    val remainSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val isWorking: Boolean = false,
    val plans: List<PlanEntity> = emptyList()
)

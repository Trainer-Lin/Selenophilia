package com.example.tmusic.study.mvi

import com.example.tmusic.study.data.PlanEntity

data class StudyState(
    val remainSeconds: Int,
    val totalSeconds: Int,
    val isWorking: Boolean = false,
    val plans: List<PlanEntity> = emptyList(),
    val isCountUp: Boolean = false
)

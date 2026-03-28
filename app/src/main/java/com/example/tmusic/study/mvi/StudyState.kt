package com.example.tmusic.study.mvi

data class StudyState(
    val remainSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val isWorking: Boolean = false,
    val plans: List<Plan> = emptyList(),
    val plansCount:Int = plans.size
)

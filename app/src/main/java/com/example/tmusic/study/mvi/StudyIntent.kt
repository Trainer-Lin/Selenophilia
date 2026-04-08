package com.example.tmusic.study.mvi

import com.example.tmusic.study.data.PlanEntity

sealed class StudyIntent {
    object StartPause: StudyIntent()
    object Restart: StudyIntent()
    data class AddPlan(val plan: PlanEntity): StudyIntent()
    data class DeletePlan(val planId: Int): StudyIntent()
    data class TogglePlanComplete(val planId: Int): StudyIntent()
    data class SetDuration(val minutes: Int, val isCountUp: Boolean): StudyIntent()
}

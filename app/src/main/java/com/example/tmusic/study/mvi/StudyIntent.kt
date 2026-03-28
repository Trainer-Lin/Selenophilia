package com.example.tmusic.study.mvi

sealed class StudyIntent {
    object StartPause: StudyIntent()
    object Restart: StudyIntent()
    data class AddPlan(val content: String): StudyIntent()
    data class DeletePlan(val planId: Int): StudyIntent()
    data class TogglePlanComplete(val planId: Int): StudyIntent()
}
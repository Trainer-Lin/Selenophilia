package com.example.tmusic.personal.mvi

sealed class PersonalIntent {
    object LoadData : PersonalIntent()
}

package com.example.tmusic.personal.mvi

import com.example.tmusic.base.BaseMviViewModel
import com.example.tmusic.personal.data.PersonalRepository

class PersonalViewModel : BaseMviViewModel<PersonalState, PersonalIntent>() {

    override fun initState(): PersonalState {
        return PersonalState()
    }

    override fun handleIntent(intent: PersonalIntent) {
        when (intent) {
            is PersonalIntent.LoadData -> loadData()
        }
    }

    private fun loadData() {
        val days = PersonalRepository.getAppStartDays()
        val fCount = PersonalRepository.getFocusCount()
        val fHours = PersonalRepository.getFocusDurationHours()
        val pHours = PersonalRepository.getTotalPlayDurationHours()
        val mostPlayed = PersonalRepository.getMostPlayedSong()
        val history = PersonalRepository.getPlayHistory()

        updateState {
            it.copy(
                daysUsed = days,
                focusCount = fCount,
                focusDurationHours = fHours,
                totalPlayHours = pHours,
                mostPlayedSong = mostPlayed,
                playHistory = history
            )
        }
    }
}

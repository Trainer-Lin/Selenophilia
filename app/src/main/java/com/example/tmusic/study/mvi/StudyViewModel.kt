package com.example.tmusic.study.mvi

import androidx.lifecycle.viewModelScope
import com.example.tmusic.base.BaseMviViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StudyViewModel: BaseMviViewModel<StudyState, StudyIntent>() {

    private var timerJob: Job? = null //计时器任务

    override fun initState(): StudyState {
        return StudyState()
    }

    override fun handleIntent(intent: StudyIntent) {
        when(intent){
            is StudyIntent.StartPause -> {
               if(!viewState.value.isWorking)startTimer()
               else pauseTimer()
            }
            is StudyIntent.Restart -> {
                resetTimer()
            }
            is StudyIntent.AddPlan -> {
                addPlan(intent.content)
            }
            is StudyIntent.DeletePlan -> {
                deletePlan(intent.planId)
            }
            is StudyIntent.TogglePlanComplete -> {
                togglePlanComplete(intent.planId)
            }
        }
    }

    private fun addPlan(content: String) {
        val newId = viewState.value.plans.maxOfOrNull { it.id }?.plus(1) ?: 0
        val newPlan = Plan(content = content, isFinished = false, id = newId)
        updateState { oldState ->
            oldState.copy(
                plans = oldState.plans + newPlan,
                plansCount = oldState.plansCount + 1
            )
        }
    }

    private fun deletePlan(planId: Int) {
        updateState { oldState ->
            oldState.copy(
                plans = oldState.plans.filter { it.id != planId }
            )
        }
    }

    private fun togglePlanComplete(planId: Int) {
        updateState { oldState ->
            oldState.copy(
                plans = oldState.plans.map { 
                    if (it.id == planId) it.copy(isFinished = !it.isFinished)
                    else it
                }
            )
        }
    }

    //启动计时器
    private fun startTimer(){
        if (timerJob?.isActive == true || viewState.value.remainSeconds <= 0) return
        updateState { oldState ->
            oldState.copy(
                isWorking = true
            )
        }
        timerJob = viewModelScope.launch {
            while(viewState.value.remainSeconds > 0){
                delay(1000)
                if (viewState.value.remainSeconds <= 0) break
                updateState{ oldState ->
                    oldState.copy(
                        remainSeconds = (oldState.remainSeconds - 1).coerceAtLeast(0)
                    )
                }
            }
            timerJob = null
            updateState { oldState ->
                oldState.copy(
                    remainSeconds = 0,
                    isWorking = false
                )
            }
        }
    }
    //暂停计时器
    private fun pauseTimer(){
        timerJob?.cancel()
        timerJob = null
        viewModelScope.launch{
            updateState{oldState ->
                oldState.copy(
                    isWorking = false
                )
            }
        }
    }
    //重置计时器
    private fun resetTimer(){
        timerJob?.cancel()
        timerJob = null
        viewModelScope.launch {
            updateState{oldState ->
                oldState.copy(
                    remainSeconds = oldState.totalSeconds,
                    isWorking = false
                )
            }
        }
    }


}

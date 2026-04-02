package com.example.tmusic.study.mvi

import android.app.Application
import android.icu.util.Calendar
import androidx.lifecycle.viewModelScope
import com.example.tmusic.TAppliaction
import com.example.tmusic.base.BaseMviViewModel
import com.example.tmusic.localMusicList.data.room.MusicDatabase
import com.example.tmusic.study.data.PlanEntity
import com.example.tmusic.study.data.room.PlanDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StudyViewModel(application: Application): BaseMviViewModel<StudyState, StudyIntent>() {
    private var timerJob: Job? = null //计时器任务
    private val db = PlanDatabase.getInstance(application)
    private val dao = db.planDao()

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
                addPlan(intent.plan)
            }
            is StudyIntent.DeletePlan -> {
                deletePlan(intent.planId)
            }
            is StudyIntent.TogglePlanComplete -> {
                togglePlanComplete(intent.planId)
            }
        }
    }

    fun loadPlans(){
        viewModelScope.launch{
            val date = getDate()
            val plans = dao.queryPlanByDate(date)
            updateState{ oldState ->
                oldState.copy(plans = plans)
            }
        }
    }

    fun loadPlansByDate(date: String) {
        viewModelScope.launch {
            val plans = dao.queryPlanByDate(date)
            updateState { oldState ->
                oldState.copy(plans = plans)
            }
        }
    }
    private fun addPlan(plan: PlanEntity){
        viewModelScope.launch {
            dao.insertPlan(plan)
            val date = plan.date
            val plans = dao.queryPlanByDate(date)
            updateState{ oldState ->
                oldState.copy(plans = plans)
            }
        }
    }

    private fun deletePlan(planId: Int) {
        viewModelScope.launch{
            val plan = dao.queryPlanById(planId)
            val date = plan.date  // 先获取 date
            dao.deletePlan(planId)  // 再删除
            val plans = dao.queryPlanByDate(date)  // 查询同一天的计划
            updateState { oldState ->
                oldState.copy(plans = plans)
            }
        }
    }

    private fun togglePlanComplete(planId: Int) {
      viewModelScope.launch {
          val plan = dao.queryPlanById(planId)
          dao.updatePlan(planId, !plan.isFinished)
          val plans = dao.queryPlanByDate(plan.date)
          updateState{oldState ->
              oldState.copy(plans = plans)
          }
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

    private fun getDate(): String{
        val date = Calendar.getInstance()
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH) + 1
        val day = date.get(Calendar.DAY_OF_MONTH)
        val dateString = String.format("%04d-%02d-%02d", year,month,day)
        return dateString
    }


}

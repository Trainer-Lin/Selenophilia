package com.example.tmusic.study.mvi

import android.icu.util.Calendar
import androidx.lifecycle.viewModelScope
import com.example.tmusic.TApplication
import com.example.tmusic.base.BaseMviViewModel
import com.example.tmusic.study.data.PlanDatabase
import com.example.tmusic.study.data.PlanEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StudyViewModel : BaseMviViewModel<StudyState, StudyIntent>() {
    private var timerJob: Job? = null
    private val db = PlanDatabase.getInstance(TApplication.instance)
    private val dao = db.planDao()

    override fun initState(): StudyState {
        val mk = TApplication.mmkv
        val savedTotalSeconds = mk.decodeInt("totalSeconds", 25 * 60)
        val savedIsCountUp = mk.decodeBool("isCountUp", false)

        if(!savedIsCountUp) {
            return StudyState(
                totalSeconds = savedTotalSeconds,
                remainSeconds = savedTotalSeconds,
                isCountUp = savedIsCountUp
            )
        }

        else {
            return StudyState(
                totalSeconds = 0,
                remainSeconds = 0,
                isCountUp = savedIsCountUp
            )
        }
    }
    override fun handleIntent(intent: StudyIntent) {
        when (intent) {
            is StudyIntent.StartPause -> {
                if (!viewState.value.isWorking) startTimer() else pauseTimer()
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
            is StudyIntent.SetDuration -> {
                setDuration(intent.minutes, intent.isCountUp)
            }
        }
    }

    fun loadPlans() {
        viewModelScope.launch {
            val date = getDate()
            val plans = dao.queryPlanByDate(date)
            updateState { oldState -> oldState.copy(plans = plans) }
        }
    }

    fun loadPlansByDate(date: String) {
        viewModelScope.launch {
            val plans = dao.queryPlanByDate(date)
            updateState { oldState -> oldState.copy(plans = plans) }
        }
    }
    private fun addPlan(plan: PlanEntity) {
        viewModelScope.launch {
            dao.insertPlan(plan)
            val date = plan.date
            val plans = dao.queryPlanByDate(date)
            updateState { oldState -> oldState.copy(plans = plans) }
        }
    }

    private fun deletePlan(planId: Int) {
        viewModelScope.launch {
            val plan = dao.queryPlanById(planId)
            val date = plan.date // 先获取 date
            dao.deletePlan(planId) // 再删除
            val plans = dao.queryPlanByDate(date) // 查询同一天的计划
            updateState { oldState -> oldState.copy(plans = plans) }
        }
    }

    private fun togglePlanComplete(planId: Int) {
        viewModelScope.launch {
            val plan = dao.queryPlanById(planId)
            dao.updatePlan(planId, !plan.isFinished)
            val plans = dao.queryPlanByDate(plan.date)
            updateState { oldState -> oldState.copy(plans = plans) }
        }
    }

    // 启动计时器
    private fun startTimer() {
        if (timerJob?.isActive == true) return
        updateState { oldState -> oldState.copy(isWorking = true) }
        timerJob =
                viewModelScope.launch {
                    if (viewState.value.isCountUp) {
                        while (viewState.value.isWorking) {
                            delay(1000)
                            updateState { oldState ->
                                oldState.copy(remainSeconds = oldState.remainSeconds + 1)
                            }
                        } // 正计时逻辑
                    } else {
                        while (viewState.value.remainSeconds > 0) {
                            delay(1000)
                            if (viewState.value.remainSeconds <= 0) break
                            updateState { oldState ->
                                oldState.copy(
                                        remainSeconds =
                                                (oldState.remainSeconds - 1).coerceAtLeast(0)
                                )
                            }
                        }
                        timerJob = null
                        updateState { oldState ->
                            oldState.copy(remainSeconds = 0, isWorking = false)
                        }
                    }
                }
    }

    // 设置时长
    private fun setDuration(minutes: Int, isCountUp: Boolean) {
        val mk = TApplication.mmkv
        timerJob?.cancel()
        timerJob = null
        val seconds = minutes * 60
        mk.encode("totalSeconds", seconds)
        mk.encode("isCountUp", isCountUp)
        viewModelScope.launch {
            updateState { oldState ->
                if (isCountUp) {
                    oldState.copy(
                            remainSeconds = 0,
                            totalSeconds = seconds,
                            isWorking = false,
                            isCountUp = true
                    )
                } else {
                    oldState.copy(
                            remainSeconds = seconds,
                            totalSeconds = seconds,
                            isWorking = false,
                            isCountUp = false
                    )
                }
            }
        }
    }
    // 暂停计时器
    private fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
        viewModelScope.launch { updateState { oldState -> oldState.copy(isWorking = false) } }
    }
    // 重置计时器
    private fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        if (viewState.value.isCountUp) {
            viewModelScope.launch {
                updateState { oldState -> oldState.copy(remainSeconds = 0, isWorking = false) }
            }
        } else {
            viewModelScope.launch {
                updateState { oldState ->
                    oldState.copy(remainSeconds = oldState.totalSeconds, isWorking = false)
                }
            }
        }
    }

    private fun getDate(): String {
        val date = Calendar.getInstance()
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH) + 1
        val day = date.get(Calendar.DAY_OF_MONTH)
        val dateString = String.format("%04d-%02d-%02d", year, month, day)
        return dateString
    }
}

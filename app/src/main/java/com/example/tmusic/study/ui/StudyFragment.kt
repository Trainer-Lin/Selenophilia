package com.example.tmusic.study.ui

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.tmusic.MainActivity
import com.example.tmusic.R
import com.example.tmusic.base.BaseFragment
import com.example.tmusic.databinding.FragmentStudyBinding
import com.example.tmusic.databinding.ItemStudyTaskBinding
import com.example.tmusic.study.data.PlanEntity
import com.example.tmusic.study.mvi.StudyIntent
import com.example.tmusic.study.mvi.StudyViewModel
import com.example.tmusic.widget.AddPlanDialog
import com.example.tmusic.widget.SelectDurationDialog
import kotlinx.coroutines.launch

class StudyFragment : BaseFragment<FragmentStudyBinding>(FragmentStudyBinding::inflate) {

    private val viewModel: StudyViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("StudyFragment" ,"Hello")
        initView()
    }

    override fun initView() {
        viewModel.loadPlans()
        updateUi()
        observeState()

        binding.btnAddTask.setOnClickListener { showAddPlanDialog() }
        binding.btnStartPause.setOnClickListener { sendIntent(StudyIntent.StartPause) }
        binding.btnRestart.setOnClickListener { sendIntent(StudyIntent.Restart) }
        binding.btnPlayMusic.setOnClickListener {
            val host = activity as MainActivity
            host.playOrPause(host.currentMusicList, host.currentIndex)
            updateUi()
        }
        binding.btnSettings.setOnClickListener { showSelectDurationDialog() }
    }

    private fun showAddPlanDialog() {
        AddPlanDialog(requireContext()) { content ->
                    val plan = PlanEntity(content = content, isFinished = false, date = getDate())
                    sendIntent(StudyIntent.AddPlan(plan))
                }
                .show()
    }

    private fun showSelectDurationDialog() {
        SelectDurationDialog(requireContext()) { minutes, isCountUp ->
                    sendIntent(StudyIntent.SetDuration(minutes, isCountUp))
                }
                .show()
    }

    private fun getDate(): String {
        val date = Calendar.getInstance()
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH) + 1
        val day = date.get(Calendar.DAY_OF_MONTH)
        val dateString = String.format("%04d-%02d-%02d", year, month, day)
        return dateString
    }

    private fun updatePlansCount() {
        binding.plansCount.text = "${viewModel.viewState.value.plans.size} TASKS"
    }
    private fun updateUi() {
        val host = activity as MainActivity
        host.updateSongInfo()

        val cover = host.albumCover
        if (cover != null) {
            Glide.with(this).load(cover).into(binding.ivAlbumCover)
        } else {
            binding.ivAlbumCover.setImageResource(R.drawable.ic_launcher_foreground)
        }

        binding.tvSongTitle.text = host.songTitle ?: "暂无歌曲播放哦"
        binding.tvArtistName.text = host.artistName ?: "未知艺术家"

        if (host.isPlaying()) {
            binding.btnPlayMusic.setImageResource(R.drawable.ic_study_pause)
        } else {
            binding.btnPlayMusic.setImageResource(R.drawable.icon_play_new)
        }
        updateDateSelector()
    }

    private fun updateDateSelector() {
        val calendar = Calendar.getInstance()
        val todayDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val mondayOffset =
                if (todayDayOfWeek == Calendar.SUNDAY) 6 else todayDayOfWeek - Calendar.MONDAY

        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val dateViews =
                listOf(
                        binding.dateMon,
                        binding.dateTue,
                        binding.dateWed,
                        binding.dateThu,
                        binding.dateFri,
                        binding.dateSat,
                        binding.dateSun
                )

        dateViews.forEachIndexed { index, dateView ->
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val dateString = String.format("%04d-%02d-%02d", year, month, day)

            val dayText = dateView.getChildAt(1) as? TextView
            val dayOfWeekText = dateView.getChildAt(0) as? TextView
            dayText?.text = day.toString()

            if (index == mondayOffset) {
                dateView.setBackgroundResource(R.drawable.bg_date_selector_selected)
                dayOfWeekText?.setTextColor(0xFFFFFFFF.toInt())
                dayText?.setTextColor(0xFFFFFFFF.toInt())
            } else {
                dateView.setBackgroundResource(R.drawable.bg_study_task_item)
                dayOfWeekText?.setTextColor(0xFF888888.toInt())
                dayText?.setTextColor(0xFF888888.toInt())
            }

            dateView.setOnClickListener {
                viewModel.loadPlansByDate(dateString)
                updateDateSelectorStyle(index, dateViews)
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun updateDateSelectorStyle(selectedIndex: Int, dateViews: List<LinearLayout>) {
        dateViews.forEachIndexed { index, dateView ->
            val dayText = dateView.getChildAt(1) as? TextView
            val dayOfWeekText = dateView.getChildAt(0) as? TextView
            if (index == selectedIndex) {
                dateView.setBackgroundResource(R.drawable.bg_date_selector_selected)
                dayOfWeekText?.setTextColor(0xFFFFFFFF.toInt())
                dayText?.setTextColor(0xFFFFFFFF.toInt())
            } else {
                dateView.setBackgroundResource(R.drawable.bg_study_task_item)
                dayOfWeekText?.setTextColor(0xFF888888.toInt())
                dayText?.setTextColor(0xFF888888.toInt())
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            updateUi()
        }
    }

    private fun sendIntent(intent: StudyIntent) {
        viewModel.handleIntent(intent)
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.viewState.collect { state ->
                renderTimer(state.remainSeconds, state.isWorking)
                createPlans(state.plans)
                binding.plansCount.text = "${state.plans.size} TASKS"
            }
        }
    }

    private fun renderTimer(remainSeconds: Int, isWorking: Boolean) {
        binding.tvTime.text = formatTime(remainSeconds)
        if (isWorking) {
            binding.tvStartPause.text = "Pause"
        } else {
            binding.tvStartPause.text = "Start\nFocusing"
        }
    }

    private fun formatTime(seconds: Int): String =
            String.format("%02d:%02d", seconds / 60, seconds % 60)

    private fun createPlans(plans: List<PlanEntity>) {
        binding.tasksContainer.removeAllViews()
        if (plans.isEmpty()) {
            binding.tvNoPlans.visibility = View.VISIBLE
        } else {
            binding.tvNoPlans.visibility = View.GONE
            plans.forEach { plan ->
                val itemBinding =
                        ItemStudyTaskBinding.inflate(
                                LayoutInflater.from(requireContext()),
                                binding.tasksContainer,
                                false
                        )

                itemBinding.tvTaskName.text = plan.content
                if (plan.isFinished) {
                    itemBinding.ivTaskStatus.setImageResource(R.drawable.ic_study_check)
                    itemBinding.tvTaskName.setTextColor(0xFF999999.toInt())
                } else {
                    itemBinding.ivTaskStatus.setImageResource(R.drawable.ic_study_circle_outline)
                }

                itemBinding.ivTaskStatus.setOnClickListener {
                    sendIntent(StudyIntent.TogglePlanComplete(plan.id))
                }

                itemBinding.ivTaskDelete.setOnClickListener {
                    sendIntent(StudyIntent.DeletePlan(plan.id))
                }
                binding.tasksContainer.addView(itemBinding.root)
            }
        }
    }
}

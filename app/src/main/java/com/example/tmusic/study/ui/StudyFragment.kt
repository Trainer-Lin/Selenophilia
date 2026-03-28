package com.example.tmusic.study.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.tmusic.MainActivity
import com.example.tmusic.R
import com.example.tmusic.base.BaseFragment
import com.example.tmusic.databinding.FragmentStudyBinding
import com.example.tmusic.databinding.ItemStudyTaskBinding
import com.example.tmusic.study.mvi.Plan
import com.example.tmusic.study.mvi.StudyIntent
import com.example.tmusic.study.mvi.StudyViewModel
import kotlinx.coroutines.launch

class StudyFragment : BaseFragment<FragmentStudyBinding>(FragmentStudyBinding::inflate) {
    private val viewModel = StudyViewModel()
    private var addPlanDialog: Dialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun initView() {
        updateUi()
        observeState()
        updatePlansCount()

        binding.btnAddTask.setOnClickListener {
            showAddPlanDialog()
        }
        binding.btnStartPause.setOnClickListener {
            sendIntent(StudyIntent.StartPause)
        }
        binding.btnRestart.setOnClickListener {
            sendIntent(StudyIntent.Restart)
        }
        binding.btnPlayMusic.setOnClickListener {
            val host = activity as MainActivity
            host.playOrPause(host.currentMusicList, host.currentIndex)
            updateUi()
        }
    }

    private fun showAddPlanDialog() {
        if (addPlanDialog?.isShowing == true) return
        
        addPlanDialog = Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_add_plan)
            
            window?.apply {
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawableResource(android.R.color.transparent)
                setGravity(android.view.Gravity.BOTTOM)
            }
            
            val etPlanContent = findViewById<EditText>(R.id.etPlanContent)
            val btnConfirm = findViewById<ImageButton>(R.id.btnConfirm)
            val btnClose = findViewById<ImageButton>(R.id.btnClose)
            
            btnConfirm.setOnClickListener {
                val content = etPlanContent.text.toString().trim()
                if (content.isBlank()) {
                    //Toast.makeText(requireContext(), "请输入计划内容", Toast.LENGTH_SHORT).show()
                    (activity as MainActivity).showMessage("请输入计划内容")
                    return@setOnClickListener
                }
                sendIntent(StudyIntent.AddPlan(content))
                updatePlansCount()
                dismiss()
            }
            
            btnClose.setOnClickListener {
                dismiss()
            }
            
            show()
        }
    }

    private fun updatePlansCount(){
        binding.plansCount.text = "${viewModel.viewState.value.plans.size} TASKS"
    }
    private fun updateUi(){
        val host = activity as MainActivity
        host.updateSongInfo()

        val cover = host.albumCover
        if (cover != null) {
            Glide.with(this)
                .load(cover)
                .into(binding.ivAlbumCover)
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

    private fun formatTime(seconds: Int): String = String.format("%02d:%02d", seconds / 60, seconds % 60)

    private fun createPlans(plans: List<Plan>) {
        binding.tasksContainer.removeAllViews()
        plans.forEach { plan ->
            val itemBinding = ItemStudyTaskBinding.inflate(
                LayoutInflater.from(requireContext()),
                binding.tasksContainer,
                false
            )
            
            itemBinding.tvTaskName.text = plan.content
            if (plan.isFinished) {
                itemBinding.ivTaskStatus.setImageResource(R.drawable.ic_study_check)
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

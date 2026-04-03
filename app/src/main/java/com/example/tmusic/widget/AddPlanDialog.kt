package com.example.tmusic.widget

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import com.example.tmusic.databinding.DialogAddPlanBinding

class AddPlanDialog(
    private val context: Context,
    private val onPlanConfirmed: (String) -> Unit
) {
    private val dialog = Dialog(context)
    private lateinit var binding: DialogAddPlanBinding

    fun show() {
        binding = DialogAddPlanBinding.inflate(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as android.view.LayoutInflater)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawableResource(android.R.color.transparent)
            setGravity(Gravity.BOTTOM)
        }

        binding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            val content = binding.etPlanContent.text.toString().trim()
            if (content.isNotBlank()) {
                onPlanConfirmed(content)
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
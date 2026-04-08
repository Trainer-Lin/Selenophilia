package com.example.tmusic.widget

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import com.example.tmusic.databinding.DialogSelectDurationBinding

class SelectDurationDialog(
        private val context: Context,
        private val onDurationConfirmed: (minutes: Int, isCountUp: Boolean) -> Unit
) {
    private val dialog = Dialog(context)
    private lateinit var binding: DialogSelectDurationBinding

    fun show() {
        binding =
                DialogSelectDurationBinding.inflate(
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as
                                android.view.LayoutInflater
                )
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawableResource(android.R.color.transparent)
            setGravity(Gravity.CENTER)
        }

        binding.btnConfirm.setOnClickListener {
            val minutesText = binding.etMinutes.text.toString().trim()
            val minutes =
                    try {
                        minutesText.toInt().coerceAtLeast(1)
                    } catch (e: NumberFormatException) {
                        25
                    }
            val isCountUp = binding.cbCountUp.isChecked
            onDurationConfirmed(minutes, isCountUp)
            dialog.dismiss()
        }

        dialog.show()
    }
}

package com.example.tmusic.widget

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.example.tmusic.MainActivity
import com.example.tmusic.R
import com.example.tmusic.TApplication
import com.example.tmusic.databinding.DialogSelectDurationBinding

class SelectDurationDialog(
        private val context: Context,
        private val onDurationConfirmed: (minutes: Int, isCountUp: Boolean) -> Unit
) {
    private val dialog = Dialog(context)
    private lateinit var binding: DialogSelectDurationBinding
    private val activity = context as MainActivity

    fun show() {
        binding =
                DialogSelectDurationBinding.inflate(
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as
                                android.view.LayoutInflater
                )
        val mmkv  = TApplication.mmkv
        val totalSeconds = mmkv.decodeInt("totalSeconds", 25 * 60)
        Log.d("SelectDuration", "$totalSeconds")
        binding.etMinutes.setText((totalSeconds / 60).toString())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawableResource(android.R.color.transparent)
            setGravity(Gravity.CENTER)
        }

        binding.btnConfirm.setOnClickListener {
            if(binding.etMinutes.text.isEmpty()){
                activity.showMessage("请输入有效时间 ！")
                return@setOnClickListener
            }
            val minutesText = binding.etMinutes.text.toString().trim()
            val minutes = minutesText.toInt().coerceAtLeast(1)
            val isCountUp = binding.cbCountUp.isChecked
            onDurationConfirmed(minutes, isCountUp)
            dialog.dismiss()
        }

        dialog.show()
    }


}

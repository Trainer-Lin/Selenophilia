package com.example.tmusic.widget

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import com.example.tmusic.databinding.DialogDeletePlaylistBinding

class DeletePlaylistDialog(
        private val context: Context,
        private val onConfirm: () -> Unit,
) {
    private val dialog = Dialog(context)
    private lateinit var binding: DialogDeletePlaylistBinding

    fun show() {
        binding =
                DialogDeletePlaylistBinding.inflate(
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as
                                android.view.LayoutInflater
                )
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.window?.apply {
            setLayout(
                    (context.resources.displayMetrics.widthPixels * 0.85).toInt(),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawableResource(android.R.color.transparent)
            setGravity(Gravity.CENTER)
        }

        binding.btnCancel.setOnClickListener { dialog.dismiss() }

        binding.btnConfirm.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        dialog.show()
    }
}

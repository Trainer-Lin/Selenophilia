package com.example.tmusic.widget

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import com.example.tmusic.databinding.DialogAddPlaylistBinding

class AddPlaylistDialog(
        private val context: Context,
        private var title: String = "创建歌单",
        private val onPlaylistConfirmed: (String) -> Unit,
) {
    private val dialog = Dialog(context)
    private lateinit var binding: DialogAddPlaylistBinding

    fun show() {
        binding =
                DialogAddPlaylistBinding.inflate(
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as
                                android.view.LayoutInflater
                )
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding.tvDialogTitle.text = title
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
            val name = binding.etPlaylistName.text.toString().trim()
            if (name.isNotBlank()) {
                onPlaylistConfirmed(name)
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}

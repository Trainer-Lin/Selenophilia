package com.example.tmusic.widget

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmusic.databinding.DialogSelectPlaylistBinding
import com.example.tmusic.home.data.room.PlaylistEntity

class PlaylistSelectDialog(
    private val context: Context,
    private val playlists: List<PlaylistEntity>,
    private val onPlaylistSelected: (PlaylistEntity) -> Unit
) {
    private val dialog = Dialog(context)
    private lateinit var binding: DialogSelectPlaylistBinding

    fun show() {
        binding = DialogSelectPlaylistBinding.inflate(LayoutInflater.from(context))
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

        val colorList = listOf("#A772BE", "#9C9EBB", "#E1B5D4")

        binding.rvPlaylists.layoutManager = LinearLayoutManager(context)
        binding.rvPlaylists.adapter = PlaylistItemAdapter(playlists, colorList) { playlist ->
            onPlaylistSelected(playlist)
            dialog.dismiss()
        }

        dialog.show()
    }
}

class PlaylistItemAdapter(
    private val playlists: List<PlaylistEntity>,
    private val colorList: List<String>,
    private val onItemClick: (PlaylistEntity) -> Unit
) : RecyclerView.Adapter<PlaylistItemAdapter.ViewHolder>() {

    inner class ViewHolder(val recyclerItemBinding: com.example.tmusic.databinding.ItemPlaylistBinding) :
        RecyclerView.ViewHolder(recyclerItemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = com.example.tmusic.databinding.ItemPlaylistBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.recyclerItemBinding
        val playlist = playlists[position]

        binding.tvPlaylistTitle.text = playlist.name
        binding.root.setCardBackgroundColor(
            android.graphics.Color.parseColor(colorList[playlist.colorIndex % colorList.size])
        )
        binding.root.setOnClickListener {
            onItemClick(playlist)
        }
    }

    override fun getItemCount(): Int = playlists.size
}
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

        val colorList = listOf(
            getThemeColor(context, com.example.tmusic.R.attr.themeColorPlaylistLoop1),
            getThemeColor(context, com.example.tmusic.R.attr.themeColorPlaylistLoop2),
            getThemeColor(context, com.example.tmusic.R.attr.themeColorPlaylistLoop3)
        )

        binding.rvPlaylists.layoutManager = LinearLayoutManager(context)
        binding.rvPlaylists.adapter = PlaylistItemAdapter(playlists, colorList) { playlist ->
            onPlaylistSelected(playlist)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getThemeColor(context: Context, attrResId: Int): Int {
        val typedValue = android.util.TypedValue()
        context.theme.resolveAttribute(attrResId, typedValue, true)
        return typedValue.data
    }
}

class PlaylistItemAdapter(
    private val playlists: List<PlaylistEntity>,
    private val colorList: List<Int>,
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
                    colorList[playlist.colorIndex % colorList.size]
                )
        
        if (!playlist.coverPath.isNullOrEmpty()) {
            com.bumptech.glide.Glide.with(holder.itemView.context)
                .load(playlist.coverPath)
                .into(binding.ivPlaylistCover)
        } else {
            binding.ivPlaylistCover.setImageResource(com.example.tmusic.R.drawable.bg_cat)
        }

        binding.root.setOnClickListener {
            onItemClick(playlist)
        }
    }

    override fun getItemCount(): Int = playlists.size
}
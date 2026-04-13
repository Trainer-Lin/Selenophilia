package com.example.tmusic.common

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmusic.databinding.MusicListItemBinding
import com.example.tmusic.localMusicList.data.room.MusicEntity

class CommonPlaylistAdapter(
    private val musicList: ArrayList<MusicEntity>,
    private val onMusicClick: (musicList: List<MusicEntity>, index: Int) -> Unit,
    private val onAddToPlaylist: (MusicEntity) -> Unit,
    private val onDeleteFromPlaylist: (MusicEntity) -> Unit
) : RecyclerView.Adapter<CommonPlaylistAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: MusicListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MusicListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val music = musicList[position]

        binding.musicName.text = music.title
        binding.artistName.text = music.artist
        Glide.with(holder.itemView.context)
            .load(music.albumArt)
            .into(binding.musicImg)

        holder.itemView.setOnClickListener {
            val index = holder.bindingAdapterPosition
            if (index != RecyclerView.NO_POSITION) {
                onMusicClick(musicList.toList(), index)
            }
        }

        binding.addToList.setOnClickListener {
            val index = holder.bindingAdapterPosition
            if (index != RecyclerView.NO_POSITION) {
                onAddToPlaylist(musicList[index])
            }
        }
    }

    override fun getItemCount(): Int = musicList.size

    fun updateMusicList(newList: List<MusicEntity>) {
        Log.d("CommonPlaylist", "updateMusicList: $newList")
        this.musicList.clear()
        this.musicList.addAll(newList)
        notifyDataSetChanged()
    }
}
package com.example.a2ndproject.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.muiscplayerproject.PreviewFragment
import com.example.muiscplayerproject.R
import com.example.muiscplayerproject.databinding.MusicItemBinding
import com.tonevellah.musicplayerapp.model.Song

class SongAdapter(
    var songs: List<Song?>,
    var player: ExoPlayer,
    private val listener: OnItemClickListener)
    :RecyclerView.Adapter<SongAdapter.SongViewHolder>() {



    inner class SongViewHolder(private val binding: MusicItemBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(song:Song){
            binding.songName.text=song.name
            binding.album.text=song.albumName
            binding.singer.text=song.singer

            //album art
            val albumartUri:Uri? = song.albumartUri
            if (albumartUri != null) {
                binding.songImage.setImageURI(albumartUri)
            }
            else{
                binding.songImage.setImageResource(R.drawable.music)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = MusicItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun getItemCount(): Int {
      return songs.size
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        songs[position]?.let { holder.bind(it) }
        holder.itemView.setOnClickListener {
            //media item
            val mediaItem: MediaItem = getMediaItem(song)
            if (!player.isPlaying) {
                player.setMediaItems(getMediaItems(), position, 0)
            } else {
                player.pause()
                player.seekTo(position, 0)
            }
            player.prepare()
            player.play()
          //  listener.onItemClick(position)
        }
    }
    private fun getMediaItems(): List<MediaItem> {
        val mediaItems: MutableList<MediaItem> = ArrayList<MediaItem>()
        for (song in songs) {
            val mediaItem: MediaItem = MediaItem.Builder()
                .setUri(song?.uri)
                .setMediaMetadata(getMetadata(song))
                .build()
            mediaItems.add(mediaItem)
        }
        return mediaItems
    }
    private fun getMediaItem(song: Song?): MediaItem {
        return MediaItem.Builder()
            .setUri(song?.uri)
            .setMediaMetadata(getMetadata(song))
            .build()
    }
    private fun getMetadata(song: Song?): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(song?.name)
            .setArtworkUri(song?.albumartUri)
            .build()
    }

}
interface OnItemClickListener {
    fun onItemClick(position: Int)
}
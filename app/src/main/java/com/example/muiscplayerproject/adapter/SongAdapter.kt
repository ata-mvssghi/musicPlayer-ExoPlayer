package com.example.a2ndproject.adapter

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import com.example.a2ndproject.sharedViewModel.SharedViewModel
import com.example.muiscplayerproject.R
import com.example.muiscplayerproject.databinding.MusicItemBinding
import com.example.muiscplayerproject.fragments.PreviewFragment.playingSong.currentSong
import com.example.muiscplayerproject.service.MusicService
import com.tonevellah.musicplayerapp.model.Song
import java.lang.Exception

class SongAdapter(
    var songs: List<Song?>,
    var player: ExoPlayer,
    val context:Context,
)
    :RecyclerView.Adapter<SongAdapter.SongViewHolder>() {


    inner class SongViewHolder(private val binding: MusicItemBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(song:Song){
            binding.songName.text=song.name
            binding.album.text=song.albumName
            binding.singer.text=song.singer

            //album art
            val albumartUri:Uri? = song.albumartUri
            if (albumartUri != null) {
                val contentResolver: ContentResolver = context.contentResolver
                try {
                    val inputStream = contentResolver.openInputStream(albumartUri)
                    if (inputStream != null) {
                        // The URI points to a valid image
                        binding.songImage.setImageURI(albumartUri)
                    } else {
                        // The URI doesn't point to a valid image
                        binding.songImage.setImageResource(R.drawable.headphones)
                    }
                } catch (e: Exception) {
                    // Error occurred while opening the URI
                    binding.songImage.setImageResource(R.drawable.headphones)
                }
            } else {
                // The URI is null
                binding.songImage.setImageResource(R.drawable.headphones)
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

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        songs[position]?.let { holder.bind(it) }
        holder.itemView.setOnClickListener {
            Intent(context,MusicService::class.java).also {
                it.action=MusicService.Actions.Start.toString()
                context.startService(it)
            }
           currentSong= songs.get(position)!!
            //media item
            if (!player.isPlaying) {
                player.setMediaItems(getMediaItems(), position, 0)
            }
            else {
                player.pause()
                player.seekTo(position, 0)
            }
            player.prepare()
            player.play()
            SharedViewModel.initializedPlaying=true
            SharedViewModel.setIsPaused(false)
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
            .setAlbumArtist(song?.singer)
            .setArtworkUri(song?.albumartUri)
            .build()
    }

}
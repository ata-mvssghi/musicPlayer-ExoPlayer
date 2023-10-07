package com.example.muiscplayerproject.adapter

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.muiscplayerproject.sharedViewModel.SharedViewModel
import com.example.muiscplayerproject.R
import com.example.muiscplayerproject.databinding.MusicItemBinding
import com.example.muiscplayerproject.fragments.PreviewFragment.playingSong.currentSong
import com.example.muiscplayerproject.service.MusicService
import com.example.muiscplayerproject.model.Song
import java.lang.Exception

class SongAdapter(
    var player: ExoPlayer,
    val context:Context,
)
    :RecyclerView.Adapter<SongAdapter.SongViewHolder>() {


    inner class SongViewHolder(private val binding: MusicItemBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(song: Song){
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
    private val differCallback = object : DiffUtil.ItemCallback<Song>(){
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.uri == newItem.uri &&
                    oldItem.albumartUri == newItem.albumartUri
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem==newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = MusicItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        Log.i("music","on bind view holder called")
        differ.currentList[position]?.let { holder.bind(it) }
        holder.itemView.setOnClickListener {
            Intent(context,MusicService::class.java).also {
                it.action=MusicService.Actions.Start.toString()
                context.startService(it)
            }
           currentSong= differ.currentList[position]!!
            Log.i("music","size of current list is =${differ.currentList.size}")
            //media item

                player.setMediaItems(getMediaItems(), position, 0)
            if(player.isPlaying) {
                player.pause()
            }
            player.seekTo(position, 0)
            player.prepare()
            player.play()
            SharedViewModel.initializedPlaying=true
            SharedViewModel.setIsPaused(false)
        }
    }
    private fun getMediaItems(): List<MediaItem> {
        val mediaItems: MutableList<MediaItem> = ArrayList<MediaItem>()
        Log.i("music","get media item called with list size of=${differ.currentList.size}")
        for (song in differ.currentList) {
            val mediaItem: MediaItem = MediaItem.Builder()
                .setUri(song?.uri)
                .setMediaMetadata(getMetadata(song))
                .build()
            mediaItems.add(mediaItem)
        }
        return mediaItems
    }
    private fun getMetadata(song: Song?): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(song?.name)
            .setAlbumArtist(song?.singer)
            .setArtworkUri(song?.albumartUri)
            .build()
    }

}
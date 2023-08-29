package com.example.muiscplayerproject

import android.content.ContentResolver
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.a2ndproject.sharedViewModel.SharedViewModel
import com.example.muiscplayerproject.databinding.FragmentPlayerBinding
import java.lang.Exception
import java.util.Objects
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class Player : Fragment() {
    lateinit var player: ExoPlayer
    lateinit var binding: FragmentPlayerBinding
    lateinit var executorService: ScheduledExecutorService
    lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i("music","on create view called in player")
        // Inflate the layout for this fragment
        binding=FragmentPlayerBinding.inflate(inflater)

        // sharedViewModel.player.observe(viewLifecycleOwner) { exoPlayer ->
//            player = exoPlayer
//            // You can use 'player' for playback control here or in other parts of the fragment
//            player?.playWhenReady = true // Example of using the player safely
//            Log.i("music","music player is initialize in the plyaer frge")
//        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //assign
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        Log.i("music","on view created")
//        sharedViewModel.player.observe(requireActivity()) { exoPlayer ->
//            player = exoPlayer
//            // You can use 'player' for playback control here or in other parts of the fragment
//            player?.playWhenReady = true // Example of using the player safely
//            Log.i("music","music player is initialize in the plyaer frge")
//            playerControls(player)
//        }
        gettingPlayer()
        //assign
        //back btn clicked
        binding.back.setOnClickListener {
            findNavController().navigate(R.id.action_player_to_previewFragment)
        }

        //getting the player

    }


    private fun gettingPlayer() {
        sharedViewModel.player.observe(requireActivity()) { livePlayer ->
            if (livePlayer != null) {
                Log.i("music","paho bnki da null dari")
                player = livePlayer
                //player controls

                playerControls(player)

                executorService = Executors.newSingleThreadScheduledExecutor()
                executorService.scheduleAtFixedRate(
                    { updatePlayerPositionProgress() },
                    0, 1, TimeUnit.SECONDS)
            }
            else{
                Log.i("music","paho buki null di")
            }
        }
    }
    fun formatTime(milliseconds: Int): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    fun updatePlayerPositionProgress() {
        requireActivity().runOnUiThread {
            if (player.isPlaying) {
                binding.currentDuration.text = formatTime(player.currentPosition.toInt())
                binding.seekBar.progress = player.currentPosition.toInt()
            }
        }
    }
    fun playerControls(player: ExoPlayer){
        Log.i("music","is not null the player")
        player.addListener(object : Player.Listener{
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                assert(mediaItem != null)
                binding.name.setText(mediaItem!!.mediaMetadata.title)
                binding.currentDuration.setText(formatTime(player.currentPosition.toInt()))
                binding.seekBar.setProgress(player.currentPosition.toInt())
                binding.totalDuration.setText(formatTime(player.duration.toInt()))
                binding.seekBar.setMax(player.duration.toInt())
                binding.playButton.setImageResource(R.drawable.player_pause)
                showCurrentArtwork()
                updatePlayerPositionProgress()
                if (!player.isPlaying) {
                    player.play()
                }
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    binding.name.setText(Objects.requireNonNull(player.currentMediaItem)?.mediaMetadata?.title)
                    binding.playButton.setImageResource(R.drawable.player_pause)
                    binding.currentDuration.setText(formatTime(player.currentPosition.toInt()))
                    binding.seekBar.setProgress(player.currentPosition.toInt())
                    binding.totalDuration.setText(formatTime(player.duration.toInt()))
                    binding.seekBar.setMax(player.duration.toInt())
                    showCurrentArtwork()
                    updatePlayerPositionProgress()
                } else {
                    binding.playButton.setImageResource(R.drawable.player_play)
                }
            }
        })
        //checking if the player is playing
        if (player.isPlaying) {
            binding.name.setText(Objects.requireNonNull(player.currentMediaItem)?.mediaMetadata?.title)
            binding.currentDuration.setText(formatTime(player.currentPosition.toInt()))
            binding.seekBar.setProgress(player.currentPosition.toInt())
            binding.totalDuration.setText(formatTime(player.duration.toInt()))
            binding.seekBar.setMax(player.duration.toInt())
            binding.playButton.setImageResource(R.drawable.player_pause)
            showCurrentArtwork()
            updatePlayerPositionProgress()
            binding.nextButton.setOnClickListener {
                if(player.hasNextMediaItem()){
                    player.seekToNext()
                    showCurrentArtwork()
                    updatePlayerPositionProgress()
                }
            }
            binding.previousButton.setOnClickListener {
                if(player.hasPreviousMediaItem()){
                    player.seekToPrevious()
                    showCurrentArtwork()
                    updatePlayerPositionProgress()
                }
            }
            binding.playButton.setOnClickListener {
                if(player.isPlaying){
                    player.pause()
                    binding.playButton.setImageResource(R.drawable.player_play)
                }
                else {
                    player.play()
                    binding.playButton.setImageResource(R.drawable.player_pause)
                }
            }
            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                private var progressValue = 0

                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    progressValue = progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // Leave this empty if you don't need any action here
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar?.progress = progressValue
                    binding.currentDuration.text = formatTime(progressValue)
                    player.seekTo(progressValue.toLong())
                }
            })

        }

    }

    fun showCurrentArtwork(){
        val artworkUri = player.currentMediaItem?.mediaMetadata?.artworkUri
        val songPicImageView=binding.songPic
        val backPic=binding.backImage

        binding.name.setText(Objects.requireNonNull(player.currentMediaItem)?.mediaMetadata?.title)

        if (artworkUri != null) {
            val contentResolver: ContentResolver = requireContext().contentResolver

            try {
                val inputStream = contentResolver.openInputStream(artworkUri)
                if (inputStream != null) {
                    // The URI points to a valid image
                    Glide.with(requireActivity())
                        .load(artworkUri)
                        .into(songPicImageView)
                    Glide.with(requireContext())
                        .load(artworkUri)
                        .into(backPic)
                } else {
                    // The URI doesn't point to a valid image
                    binding.songPic.setImageResource(R.drawable.headphones)
                    binding.backImage.setImageResource(R.drawable.mountain)
                }
            } catch (e: Exception) {
                // Error occurred while opening the URI
                binding.songPic.setImageResource(R.drawable.headphones)
                binding.backImage.setImageResource(R.drawable.mountain)
            }
        } else {
            // The URI is null
            binding.songPic.setImageResource(R.drawable.headphones)
            binding.backImage.setImageResource(R.drawable.mountain)
        }

    }

}
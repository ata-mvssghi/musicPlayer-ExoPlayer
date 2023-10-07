package com.example.muiscplayerproject.fragments

import android.content.ContentResolver
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.muiscplayerproject.sharedViewModel.SharedViewModel
import com.example.muiscplayerproject.MainActivity
import com.example.muiscplayerproject.MainActivity.Companion.MyTag
import com.example.muiscplayerproject.R
import com.example.muiscplayerproject.databinding.FragmentPlayerBinding
import com.example.muiscplayerproject.fragments.Player.playerSong.playerCurrentSong
import com.example.muiscplayerproject.room.MusicDao
import com.example.muiscplayerproject.room.MusicDatabase
import com.example.muiscplayerproject.room.SongEntity
import com.example.muiscplayerproject.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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
    lateinit var db:MusicDatabase
    lateinit var dao: MusicDao
    var exists=false
    private val _isBooleanLiveData = MutableLiveData<Boolean>()
    val isBooleanLiveData: LiveData<Boolean> = _isBooleanLiveData
    object  playerSong{
        lateinit var playerCurrentSong: Song
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(MyTag,"recreated!!!!!!!!!!!!!!111 the player")
        getActivity()?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        db=(activity as MainActivity).db
        dao=db.dao
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(MyTag,"on create view called in player")
        // Inflate the layout for this fragment
        binding=FragmentPlayerBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //assign
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        Log.i(MyTag,"on view created")
        viewLifecycleOwner.lifecycleScope.launch {
            SharedViewModel.isPaused.collect { isPaused ->
                if (isPaused) {
                    binding.playButton.setImageResource(R.drawable.player_play)
                } else {
                    binding.playButton.setImageResource(R.drawable.player_pause)
                }
            }
        }
        gettingPlayer()
        doesItExist()
        //back btn clicked
        binding.back.setOnClickListener {
            Log.i("music","back to preview fragment")
            findNavController().popBackStack()
        }

        isBooleanLiveData.observe(viewLifecycleOwner , Observer {
            newValue->
            if(newValue){
                binding.saveImage.setImageResource(R.drawable.favortie)
            }
            else{
                binding.saveImage.setImageResource(R.drawable.favorite_border)
            }
        })

    }



    private fun gettingPlayer() {
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.player.collect { livePlayer ->
                if (livePlayer != null) {
                    player = livePlayer
                    Log.i(MyTag,"player got set in player  fragment")
                    if(player.playbackState==Player.STATE_BUFFERING)
                        binding.playButton.setImageResource(R.drawable.player_play)
                    playerControls(player)

                    executorService = Executors.newSingleThreadScheduledExecutor()
                    executorService.scheduleAtFixedRate(
                        { updatePlayerPositionProgress() },
                        0, 1, TimeUnit.SECONDS)
                }
                else{
                    Log.i(MyTag,"the player in PLAYER fragment is null")
                }
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
                binding.currentDuration.text = formatTime(player.currentPosition.toInt())
                binding.seekBar.progress = player.currentPosition.toInt()
        }

    }
    fun playerControls(player: ExoPlayer){
        Log.i(MyTag,"the player is not null ")
        player.addListener(object : Player.Listener{
            //it's ok
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                assert(mediaItem != null)
                val title=mediaItem?.mediaMetadata?.title ?:"<UNTITLED SONG>"
                binding.name.setText(title)
                binding.singerPlayer.setText(mediaItem?.mediaMetadata?.albumArtist)
                binding.currentDuration.setText(formatTime(player.currentPosition.toInt()))
                binding.seekBar.setProgress(player.currentPosition.toInt())
                binding.totalDuration.setText(formatTime(player.duration.toInt()))
                binding.seekBar.setMax(player.duration.toInt())
                binding.playButton.setImageResource(R.drawable.player_pause)
                //checking for if the fragment is attached to any context or not to prevent issues relateed
                // to context calls
                if(isAdded) {
                    showCurrentArtwork()
                    Log.i(MyTag,"on media item transition called")
                    updatePlayerPositionProgress()
                }
                if (!player.isPlaying) {
                    player.play()
                }
                Log.i("music","mmd is my cousin")
                updateCurrentSong()

            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    binding.name.setText(Objects.requireNonNull(player.currentMediaItem)?.mediaMetadata?.title)
                    binding.singerPlayer.setText(Objects.requireNonNull(player.currentMediaItem)?.mediaMetadata?.albumArtist)
                    if(player.isPlaying)
                        binding.playButton.setImageResource(R.drawable.player_pause)
                    else
                        binding.playButton.setImageResource(R.drawable.player_play)
                    binding.currentDuration.setText(formatTime(player.currentPosition.toInt()))
                    binding.seekBar.setProgress(player.currentPosition.toInt())
                    binding.totalDuration.setText(formatTime(player.duration.toInt()))
                    binding.seekBar.setMax(player.duration.toInt())
                    if(isAdded()) {
                        showCurrentArtwork()
                        Log.i(MyTag,"on play back state changed")
                        updatePlayerPositionProgress()
                    }
                } else {
                    binding.playButton.setImageResource(R.drawable.player_play)
                }
            }
        })
        //checking if the player is playing
            binding.name.setText(Objects.requireNonNull(player.currentMediaItem)?.mediaMetadata?.title)
            binding.singerPlayer.setText(Objects.requireNonNull(player.currentMediaItem)?.mediaMetadata?.albumArtist)
            binding.currentDuration.setText(formatTime(player.currentPosition.toInt()))
            binding.seekBar.setProgress(player.currentPosition.toInt())
            binding.totalDuration.setText(formatTime(player.duration.toInt()))
            binding.seekBar.setMax(player.duration.toInt())
            //binding.playButton.setImageResource(R.drawable.player_pause)
            showCurrentArtwork()
            updatePlayerPositionProgress()
            binding.nextButton.setOnClickListener {
                Log.i(MyTag,"listener of next")
                if(player.hasNextMediaItem()){
                    player.seekToNext()
                    showCurrentArtwork()
                    updatePlayerPositionProgress()
                    updateCurrentSong()

                }
            }
            binding.previousButton.setOnClickListener {
                if(player.hasPreviousMediaItem()){
                    player.seekToPrevious()
                    showCurrentArtwork()
                    updatePlayerPositionProgress()
                    updateCurrentSong()
                }
            }
            binding.playButton.setOnClickListener {
                if(player.isPlaying){
                    player.pause()
                    binding.playButton.setImageResource(R.drawable.player_play)
                    Log.i(MyTag,"122")
                    SharedViewModel.setIsPaused(true)
                }
                else {
                    player.play()
                    Log.i(MyTag,"123")
                    binding.playButton.setImageResource(R.drawable.player_pause)
                    SharedViewModel.setIsPaused(false)
                }
            }
            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                private var progressValue = 0

                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    progressValue = progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    Log.i(MyTag,"on stop tracking")
                    seekBar?.progress = progressValue
                    binding.currentDuration.text = formatTime(progressValue)
                    player.seekTo(progressValue.toLong())
                }
            })

            binding.saveImage.setOnClickListener {
                var exists = false
                runBlocking {
                CoroutineScope(Dispatchers.IO).launch {
                    if (dao.doesSongNameExist(playerCurrentSong.name) > 0) {
                       exists = true
                        Log.i(MyTag, "exists!")
                    }
                    if (exists) {
                        dao.deleteSongByName(playerCurrentSong.name)
                        withContext(Dispatchers.Main){
                            Toast.makeText(
                                requireContext(),
                                "Song Deleted From Favorites List!",
                                Toast.LENGTH_SHORT
                            ).show()
                            updateBooleanValue(false)
                        }
                    }
                    else {
                        withContext(Dispatchers.Main){
                            Toast.makeText(
                                requireContext(),
                                "Song Added to the favorites list successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            updateBooleanValue(true)
                        }
                        val newEntity = SongEntity(
                            0,
                            playerCurrentSong.uri,
                            playerCurrentSong.name,
                            playerCurrentSong.albumartUri,
                            playerCurrentSong.singer,
                            playerCurrentSong.albumName
                        )
                        CoroutineScope(Dispatchers.Default).launch {
                            dao.insertSongToFavorites(
                                newEntity
                            )
                        }
                    }

                }
            }

            }



    }

    fun showCurrentArtwork(){
        val artworkUri = player.currentMediaItem?.mediaMetadata?.artworkUri
        val songPicImageView=binding.songPic
        val backPic=binding.backImage
        binding.name.setText(Objects.requireNonNull(player.currentMediaItem)?.mediaMetadata?.title)
        binding.singerPlayer.setText(Objects.requireNonNull(player.currentMediaItem)?.mediaMetadata?.albumArtist)
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
     fun updateCurrentSong(){
         playerCurrentSong.name= player.currentMediaItem?.mediaMetadata?.title as String
         playerCurrentSong.albumartUri=player.currentMediaItem?.mediaMetadata?.artworkUri
         playerCurrentSong.singer= player.currentMediaItem?.mediaMetadata?.albumArtist as String
        val currentMediaItem: MediaItem? = player.currentMediaItem
        val currentUri: Uri? = currentMediaItem?.playbackProperties?.uri
        if (currentUri != null) {
            playerCurrentSong.uri=currentUri
        }
         doesItExist()
    }
    override fun onDestroy() {
        super.onDestroy()
        getActivity()?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
    fun updateBooleanValue(newValue: Boolean) {
        _isBooleanLiveData.value = newValue
    }
    fun doesItExist(){
        CoroutineScope(Dispatchers.IO).launch {
            if(dao.doesSongNameExist(playerCurrentSong.name)>0){
                Log.i("music","exists!!!!!!!!!!!!!!!!!!!!!!!!!! , name=${playerCurrentSong.name}")
                exists=true
            }
            else
                exists=false
            withContext(Dispatchers.Main){
                if(exists){
                    updateBooleanValue(true)
                }
                else{
                    updateBooleanValue(false)
                }
            }
        }
    }
}

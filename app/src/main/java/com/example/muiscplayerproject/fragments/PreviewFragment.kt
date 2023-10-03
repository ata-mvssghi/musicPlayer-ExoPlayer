package com.example.muiscplayerproject.fragments

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a2ndproject.adapter.SongAdapter
import com.example.a2ndproject.sharedViewModel.SharedViewModel
import com.example.a2ndproject.sharedViewModel.SharedViewModel.Companion.initializedPlaying
import com.example.muiscplayerproject.MainActivity
import com.example.muiscplayerproject.MainActivity.Companion.MyTag
import com.example.muiscplayerproject.R
import com.example.muiscplayerproject.databinding.FragmentPreviewBinding
import com.example.muiscplayerproject.fragments.Player.playerSong.playerCurrentSong
import com.example.muiscplayerproject.fragments.PreviewFragment.playingSong.currentSong
import com.tonevellah.musicplayerapp.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PreviewFragment : Fragment() {
    lateinit var binding: FragmentPreviewBinding
    lateinit var player: ExoPlayer
    lateinit var adapter: SongAdapter
    lateinit var recyclerview: RecyclerView
    val permissionRequestCode = 1

    private val sharedViewModel: SharedViewModel by lazy {
        (requireActivity() as MainActivity).sharedViewModel
    }
    object  playingSong{
        lateinit var currentSong:Song
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(MyTag,"on create called on Preview Fragment")

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPreviewBinding.inflate(inflater)
        val navController = NavHostFragment.findNavController(this)
        val bottomNavigationView=binding.bottomNavigationView
       // bottomNavigationView.selectedItemId=R.id.songs
        bottomNavigationView.setOnNavigationItemSelectedListener{item ->
            when(item.itemId){
                R.id.songs->{
                    true
                }
                R.id.favorites->{
                    navController.navigate(R.id.action_previewFragment_to_favoriteFragment)
                    true
                }
                else->false
            }
        }
        player= sharedViewModel.player.value!!
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.player.collect { livePlayer ->
                if (livePlayer != null) {
                    player = livePlayer

                    Log.i(MyTag, "Player updated in  preview fragment")
                }
                else{
                    Log.i(MyTag,"the player in preview fragment is null")
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            SharedViewModel.isPaused.collect { isPaused ->
                if (isPaused) {
                    binding.play.setImageResource(R.drawable.baseline_play_circle_24)
                } else {
                    binding.play.setImageResource(R.drawable.baseline_pause_circle_24)
                }
            }
        }
        recyclerview = binding.recyclerView
        //checking if the permission is already given or not
        if  (!isPermissionGranted()) {
            Log.i(MyTag,"permission not granted yet" )
            requestPermission()
        }
        else{
            fetchSongs()
            //player controls
            playerControls()
            initiate()
        }
        return binding.root

    }
    fun initiate(){
        Log.i(MyTag,"initiate called")
        if (player.playbackState!=Player.STATE_IDLE){
            binding.currentSong.text=player.currentMediaItem?.mediaMetadata?.title
        }
        if(player.isPlaying){
            binding.play.setImageResource(R.drawable.baseline_pause_circle_24)
        }
        else{
            binding.play.setImageResource(R.drawable.baseline_play_circle_24)
        }
    }
    fun playerControls() {
        binding.bottomContainer.setOnClickListener {
            if(initializedPlaying) {
//                val direction=PreviewFragmentDirections.
//                actionPreviewFragmentToPlayer(currentSong)
                findNavController().navigate(R.id.action_previewFragment_to_player)
                playerCurrentSong= currentSong
                Log.i("music", "navigated by player controls function")
            }
            else
                Toast.makeText(requireContext(),"Please Select A Song",Toast.LENGTH_SHORT).show()
        }
        binding.next.setOnClickListener {
            if(initializedPlaying) {
                if (player.hasNextMediaItem()) {
                    player.seekToNext()
                }
            }
            else
                Toast.makeText(requireContext(),"Please Select A Song",Toast.LENGTH_SHORT).show()
        }
        binding.previous.setOnClickListener {
            if(initializedPlaying) {
                if (player.hasPreviousMediaItem()) {
                    player.seekToPrevious()
                }
            }
            else
                Toast.makeText(requireContext(),"Please Select A Song",Toast.LENGTH_SHORT).show()
        }
        binding.play.setOnClickListener {
            if(initializedPlaying) {
                if (player.isPlaying) {
                    player.pause()
                    binding.play.setImageResource(R.drawable.baseline_play_circle_24)
                    SharedViewModel.setIsPaused(true)
                }
                else {
                    if (player.playbackState == Player.STATE_IDLE) {
                        // Player was stopped, prepare and start playback
                        player.setPlayWhenReady(true)
                        player.prepare()
                    } else if (player.playbackState == Player.STATE_ENDED) {
                        // Player finished playing, seek to the beginning and start again
                        player.seekToDefaultPosition()
                        player.setPlayWhenReady(true)
                    } else {
                        // Resume playback
                        player.setPlayWhenReady(true)
                    }
                    SharedViewModel.setIsPaused(false)
                    binding.play.setImageResource(R.drawable.baseline_pause_circle_24)
                }
            }
            else
                Toast.makeText(requireContext(),"Please Select A Song",Toast.LENGTH_SHORT).show()
        }

        //player listener
        playerListener()
    }

    private fun playerListener() {
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.let {
                    binding.currentSong.text = it.mediaMetadata?.title
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    player.currentMediaItem?.mediaMetadata?.title?.let {
                        binding.currentSong.text = it
                        binding.play.setImageResource(R.drawable.baseline_pause_circle_24)
                    }
                }
            }
        })
    }

    fun fetchSongs() {
            //define list to carry the songs
            val songs: ArrayList<Song> = ArrayList<Song>()
                val songLibraryUri: Uri
                songLibraryUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                //projection
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ARTIST
                )

                //sort order
                val sortOrder = MediaStore.Audio.Media.DATE_MODIFIED + " DESC"


                requireContext().contentResolver.query(
                    songLibraryUri,
                    projection,
                    null,
                    null,
                    sortOrder
                )
                    ?.use { cursor ->

                        //cache the cursor indices
                        val idColumn: Int = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                        val nameColumn: Int =
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                        val durationColumn: Int =
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                        val sizeColumn: Int =
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                        val albumIDColumn: Int =
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                        val albumNameColumn: Int =
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                        val singerName: Int =
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)

                        //getting the values
                        while (cursor.moveToNext()) {
                            //get values of columns for a give audio file
                            val id: Long = cursor.getLong(idColumn)
                            var name: String = cursor.getString(nameColumn)
                            val duration: Int = cursor.getInt(durationColumn)
                            val size: Int = cursor.getInt(sizeColumn)
                            val albumID: Long = cursor.getLong(albumIDColumn)
                            val albumName: String = cursor.getString(albumNameColumn)
                            val artistName: String = cursor.getString(singerName)

                            //song uri
                            val uri =
                                ContentUris.withAppendedId(
                                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    id
                                )

                            //album art uri
                            val albumartUri = ContentUris.withAppendedId(
                                Uri.parse("content://media/external/audio/albumart"),
                                albumID
                            )

                            //remove .mp3 extension on song's name
                            name = name.substring(0, name.lastIndexOf("."))

                            //song item
                            val song = Song(
                                id,
                                uri,
                                name,
                                duration,
                                size,
                                albumID,
                                albumName,
                                albumartUri,
                                artistName
                            )
                            //add song to songs list
                            songs.add(song)
                            Log.i(MyTag, "does these call again each time?")
                        }

                    //show songs on rv
                    showSongs(songs)
                }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun showSongs(songs: List<Song>) {
        //layout manager
        //LinearLayoutManager layoutManager = new LigridSpanSizenearLayoutManager(this);
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation=LinearLayoutManager.VERTICAL
        recyclerview.setLayoutManager(layoutManager)

        adapter = SongAdapter(songs, player, requireContext())
        recyclerview.adapter = adapter
    }


    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

            // Request permission again
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                permissionRequestCode
            )
        } else {
            // Request permission for the first time
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                permissionRequestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(),"WELCOME!",Toast.LENGTH_SHORT).show()
                fetchSongs()
                //player controls
                playerControls()
                initiate()
            }
            else {
                // Permission denied, request again
                requestPermission()
            }
        }
    }

}
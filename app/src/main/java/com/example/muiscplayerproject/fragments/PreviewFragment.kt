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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a2ndproject.adapter.OnItemClickListener
import com.example.a2ndproject.adapter.SongAdapter
import com.example.a2ndproject.sharedViewModel.SharedViewModel
import com.example.muiscplayerproject.MainActivity
import com.example.muiscplayerproject.R
import com.example.muiscplayerproject.databinding.FragmentPreviewBinding
import com.tonevellah.musicplayerapp.model.Song

class PreviewFragment : Fragment(), OnItemClickListener {
    lateinit var binding: FragmentPreviewBinding
    lateinit var player: ExoPlayer
    lateinit var adapter: SongAdapter
    lateinit var recyclerview: RecyclerView
    val permissionRequestCode = 1

    private val sharedViewModel: SharedViewModel by lazy {
        (requireActivity() as MainActivity).sharedViewModel
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("music","on create called on Preview Fragment")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this 5fragment
        binding = FragmentPreviewBinding.inflate(inflater)
//        sharedViewModel.player.observe(viewLifecycleOwner) { exoPlayer ->
//            player = exoPlayer
//            // You can use 'player' for playback control here or in other parts of the fragment
//            player.playWhenReady = true // Example of using the player safely
//            Log.i("music","player initialized in preview")
//        }
        // sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        player= sharedViewModel.player.value!!
        sharedViewModel.player.observe(requireActivity()) { livePlayer ->
            if (livePlayer != null) {
                player = livePlayer
                Log.i("music", "player initialized in preview fragment")
            }
        }
        SharedViewModel.isPaused.observe(requireActivity()) { isPAused ->
            if(isPAused)
                binding.play.setImageResource(R.drawable.baseline_play_circle_24)
            else
                binding.play.setImageResource(R.drawable.baseline_pause_circle_24)

        }
        recyclerview = binding.recyclerView
        if (!isPermissionGranted()) {
            Log.i("music","permission not granted yet" )
            requestPermission()
        }
        fetchSongs()
        //player controls
        playerControls()
        initiate()
        return binding.root

    }
    fun initiate(){
        Log.i("music","initiate called")
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
//           findNavController().navigate(R.id.action_previewFragment_to_player)

//                val playerViewFragment = Player()
//                val fragmentTag: String = playerViewFragment.javaClass.name
//                (context as MainActivity).supportFragmentManager
//                    .beginTransaction()
//                    .replace(R.id.myHost, playerViewFragment)
//                    .addToBackStack(fragmentTag)
//                    .commit()
            findNavController().navigate(R.id.action_previewFragment_to_player)


        }
        binding.next.setOnClickListener {
            if (player.hasNextMediaItem()) {
                player.seekToNext()
            }
        }
        binding.previous.setOnClickListener {
            if (player.hasPreviousMediaItem()) {
                player.seekToPrevious()
            }
        }
        binding.play.setOnClickListener {
            if (player.isPlaying) {
                player.stop()
                binding.play.setImageResource(R.drawable.baseline_play_circle_24)
                SharedViewModel.isPaused.postValue(true)
            } else {
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
                SharedViewModel.isPaused.postValue(false)
                binding.play.setImageResource(R.drawable.baseline_pause_circle_24)
            }
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
        val sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC"


        requireContext().contentResolver.query(songLibraryUri, projection, null, null, sortOrder)
            ?.use { cursor ->

                //cache the cursor indices
                val idColumn: Int = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameColumn: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val durationColumn: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeColumn: Int = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val albumIDColumn: Int =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val  albumNameColumn:Int=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val singerName:Int=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)

                //getting the values
                while (cursor.moveToNext()) {
                    //get values of columns for a give audio file
                    val id: Long = cursor.getLong(idColumn)
                    var name: String = cursor.getString(nameColumn)
                    val duration: Int = cursor.getInt(durationColumn)
                    val size: Int = cursor.getInt(sizeColumn)
                    val albumID: Long = cursor.getLong(albumIDColumn)
                    val albumName:String=cursor.getString(albumNameColumn)
                    val artistName:String=cursor.getString(singerName)

                    //song uri
                    val uri =
                        ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                    //album art uri
                    val albumartUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumID
                    )

                    //remove .mp3 extension on song's name
                    name = name.substring(0, name.lastIndexOf("."))

                    //song item
                    val song = Song(id, uri, name, duration, size, albumID,albumName, albumartUri,artistName)
                    //add song to songs list
                    songs.add(song)
                    Log.i("music", "does these call again each time?")
                }
                //show songs on rv
                showSongs(songs)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
//        if (player.isPlaying) {
//            player.stop()
//        }
//        player.release()
    }

    private fun showSongs(songs: List<Song>) {
        //layout manager
        //LinearLayoutManager layoutManager = new LigridSpanSizenearLayoutManager(this);
        val layoutManager = GridLayoutManager(requireContext(), 1)
        recyclerview.setLayoutManager(layoutManager)

        adapter = SongAdapter(songs, player, this,requireContext())
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
            // Explain to the user why the permission is needed (optional)

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
                // Permission granted, you can proceed with accessing external storage
            } else {
                // Permission denied, request again
                requestPermission()
            }
        }
    }

    override fun onItemClick(position: Int) {
//        val navHostFragment = childFragmentManager.findFragmentById(R.id.myHost) as NavHostFragment
//        val navController = navHostFragment.navController
//        navController.navigate(R.id.action_previewFragment_to_player)
        findNavController().navigate(R.id.action_previewFragment_to_player)
        Log.i("music","navigated successffully")
    }

}
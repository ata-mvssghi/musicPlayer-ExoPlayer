package com.example.muiscplayerproject.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.muiscplayerproject.adapter.SongAdapter
import com.example.muiscplayerproject.sharedViewModel.SharedViewModel
import com.example.muiscplayerproject.MainActivity
import com.example.muiscplayerproject.R
import com.example.muiscplayerproject.databinding.FragmentFavoriteBinding
import com.example.muiscplayerproject.room.MusicDao
import com.example.muiscplayerproject.room.MusicDatabase
import com.example.muiscplayerproject.model.Song
import com.example.muiscplayerproject.room.SongEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteFragment : Fragment() {
    lateinit var binding:FragmentFavoriteBinding
    lateinit var player: ExoPlayer
    lateinit var adapter: SongAdapter
    lateinit var recyclerview: RecyclerView
    lateinit var db: MusicDatabase
    lateinit var dao: MusicDao
    val songs: ArrayList<Song> = ArrayList<Song>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db=(activity as MainActivity).db
        dao=db.dao
        Log.i("music","on create called in favorite fragment")
    }
    private val sharedViewModel: SharedViewModel by lazy {
        (requireActivity() as MainActivity).sharedViewModel
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteBinding.inflate(inflater)
        val bottomNavigationView=binding.bottomNavigationView2
        bottomNavigationView.selectedItemId=R.id.favorites
        // bottomNavigationView.selectedItemId=R.id.songs
        bottomNavigationView.setOnNavigationItemSelectedListener{item ->
            when(item.itemId){
                R.id.songs->{
                    findNavController().navigate(R.id.action_favoriteFragment_to_previewFragment)
                    true
                }
                R.id.favorites->{
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
                    Log.i(MainActivity.MyTag, "Player updated in  favorite fragment")
                }
                else{
                    Log.i(MainActivity.MyTag,"the player in favortie fragment is null")
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            SharedViewModel.isPaused.collect { isPaused ->
                if (isPaused) {
                    binding.playF.setImageResource(R.drawable.baseline_play_circle_24)
                } else {
                    binding.playF.setImageResource(R.drawable.baseline_pause_circle_24)
                }
            }
        }
        recyclerview = binding.recyclerViewFavorite

        if (isAdded){
            fetchSongs()
            //player controls
            playerControls()
            initiate()
        }
            return (binding.root)
    }
     private fun search(query:String){
         val newList: ArrayList<Song> = ArrayList<Song>()
         val allSongs: ArrayList<Song> = ArrayList<Song>()
         CoroutineScope(Dispatchers.IO).launch {
             val songEntities = dao.getAllSongs()
             for(songEntity in songEntities){
                 val newSong=Song(songEntity.id.toLong(),songEntity.uri,songEntity.name,1,1,1,
                     songEntity.albumName,songEntity.albumartUri,songEntity.singer)
                 allSongs.add(newSong)
             }

             for (song in allSongs) {
                 if (song.name.contains(query, ignoreCase = true)) {
                     newList.add(song)
                 }
             }
             adapter.differ.submitList(newList.reversed())
         }
    }
    fun initiate(){
        Log.i(MainActivity.MyTag,"initiate called")
        if (player.playbackState!= Player.STATE_IDLE){
            binding.currentSongF.text=player.currentMediaItem?.mediaMetadata?.title
        }
        if(player.isPlaying){
            binding.playF.setImageResource(R.drawable.baseline_pause_circle_24)
        }
        else{
            binding.playF.setImageResource(R.drawable.baseline_play_circle_24)
        }
    }

    fun fetchSongs() {
        //define list to carry the songs
        // Assuming you are in a Fragment or an Activity
        Log.i("music","fetched song called in favorite frag")
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val songEntities = async(Dispatchers.IO) {
                    dao.getAllSongs()
                }.await()

                withContext(Dispatchers.Main) {
                        Log.i("music","size=${songEntities.size}")
                        songs.removeAll(songs)
                        val tempList = songEntities.iterator()
                        while (tempList.hasNext()) {
                            val songEntity = tempList.next()
                            val newSong = Song(
                                songEntity.id.toLong(),
                                songEntity.uri,
                                songEntity.name,
                                1,
                                1,
                                1,
                                songEntity.albumName,
                                songEntity.albumartUri,
                                songEntity.singer
                            )
                                 songs.add(newSong)
                        }
                    showSongs(songs.reversed())
                }
                Log.i("music","songs.size=${songs.size}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
    private fun showSongs(songs: List<Song>) {
        Log.i("music","show songs called")
        //layout manager
        //LinearLayoutManager layoutManager = new LigridSpanSizenearLayoutManager(this);
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation= LinearLayoutManager.VERTICAL
        recyclerview.layoutManager = layoutManager

        adapter = SongAdapter( player, requireContext())
        Log.i("music", "size of differ's list after deletion = ${adapter.differ.currentList.size}")
        adapter.differ.submitList(songs)
        recyclerview.adapter = adapter
        var job: Job?=null
        binding.favoriteSearch.addTextChangedListener {
                editable->
            job?.cancel()
            job= MainScope().launch {
                delay(300)
                editable.let {
                    if(editable.toString()!=null){
                        search(editable.toString())
                    }
                }
            }
        }
    }
    fun playerControls() {
        binding.bottomContainerF.setOnClickListener {
            if(SharedViewModel.initializedPlaying) {
//                val direction=PreviewFragmentDirections.
//                actionPreviewFragmentToPlayer(currentSong)
                findNavController().navigate(R.id.action_favoriteFragment_to_player)
                com.example.muiscplayerproject.fragments.Player.playerSong.playerCurrentSong =
                    PreviewFragment.playingSong.currentSong
                Log.i("music", "navigated by player controls function")
            }
            else
                Toast.makeText(requireContext(),"Please Select A Song", Toast.LENGTH_SHORT).show()
        }
        binding.nextF.setOnClickListener {
            if(SharedViewModel.initializedPlaying) {
                if (player.hasNextMediaItem()) {
                    player.seekToNext()
                }
            }
            else
                Toast.makeText(requireContext(),"Please Select A Song", Toast.LENGTH_SHORT).show()
        }
        binding.previousF.setOnClickListener {
            if(SharedViewModel.initializedPlaying) {
                if (player.hasPreviousMediaItem()) {
                    player.seekToPrevious()
                }
            }
            else
                Toast.makeText(requireContext(),"Please Select A Song", Toast.LENGTH_SHORT).show()
        }
        binding.playF.setOnClickListener {
            if(SharedViewModel.initializedPlaying) {
                if (player.isPlaying) {
                    player.pause()
                    binding.playF.setImageResource(R.drawable.baseline_play_circle_24)
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
                    binding.playF.setImageResource(R.drawable.baseline_pause_circle_24)
                }
            }
            else
                Toast.makeText(requireContext(),"Please Select A Song", Toast.LENGTH_SHORT).show()
        }

        //player listener
        playerListener()
    }
    private fun playerListener() {
        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.let {
                    binding.currentSongF.text = it.mediaMetadata?.title
                }
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    player.currentMediaItem?.mediaMetadata?.title?.let {
                        binding.currentSongF.text = it
                        binding.playF.setImageResource(R.drawable.baseline_pause_circle_24)
                    }
                }
            }
        })
    }
    override fun onPause() {
        super.onPause()
        Log.i("music","on pause mehtod of preview fragment called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("music","on destroy of favortie fragment called")
    }
}
package com.example.muiscplayerproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.a2ndproject.sharedViewModel.SharedViewModel

class MainActivity : AppCompatActivity() {
    lateinit var player:ExoPlayer
    val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(this)[SharedViewModel::class.java]
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = ExoPlayer.Builder(this).build()
        player.repeatMode = Player.REPEAT_MODE_ALL
        // Set the player instance in the SharedViewModel
       //val  sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        sharedViewModel.setPlayer(player)
        Log.i("music","player got set in main activity")
        setContentView(R.layout.activity_main)
    }
}
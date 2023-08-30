package com.example.muiscplayerproject

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.a2ndproject.sharedViewModel.SharedViewModel
import com.example.muiscplayerproject.service.MusicService

class MainActivity : AppCompatActivity() {
    lateinit var player:ExoPlayer
    val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(this)[SharedViewModel::class.java]
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(sharedViewModel.player.value==null)
        player = ExoPlayer.Builder(this).build()
        player.repeatMode = Player.REPEAT_MODE_ALL
        // Set the player instance in the SharedViewModel
       //val  sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        sharedViewModel.setPlayer(player)
        SharedViewModelHolder.sharedViewModel=sharedViewModel
//        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
//           ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
//           0)
//        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "running music",
                "running notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.i("music", "player got set in main activity")
        }


        setContentView(R.layout.activity_main)
    }
}
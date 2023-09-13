package com.example.muiscplayerproject

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun   onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        try{
            player=MusicService.sharedViewModel?.player?.value!!
        }
        catch (e:Exception) {
            player = ExoPlayer.Builder(this).build()
            player.repeatMode = Player.REPEAT_MODE_ALL
        }
        sharedViewModel.setPlayer(player)
        MusicService.sharedViewModel=sharedViewModel
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "running music",
                "running notification",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.i(MyTag, "player got set in main activity")
        }
        setContentView(R.layout.activity_main)
    }

    companion object{
        const val MyTag="music"
    }
}
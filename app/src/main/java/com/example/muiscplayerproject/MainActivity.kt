package com.example.muiscplayerproject

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.a2ndproject.sharedViewModel.SharedViewModel
import com.example.a2ndproject.sharedViewModel.SharedViewModel.Companion.permissionGranted
import com.example.muiscplayerproject.service.MusicService
import kotlinx.coroutines.sync.Semaphore
import java.util.Objects

class MainActivity : AppCompatActivity() {
    lateinit var player:ExoPlayer
    val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(this)[SharedViewModel::class.java]
    }
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun   onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try{
            player=MusicService.sharedViewModel?.player?.value!!
        }
        catch (e:Exception) {
            player = ExoPlayer.Builder(this).build()
            player.repeatMode = Player.REPEAT_MODE_ALL
        }
        sharedViewModel.setPlayer(player)
        MusicService.sharedViewModel=sharedViewModel
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
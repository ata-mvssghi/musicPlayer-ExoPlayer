package com.example.muiscplayerproject.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.DownloadService.startForeground
import androidx.media3.ui.PlayerNotificationManager.ACTION_NEXT
import androidx.media3.ui.PlayerNotificationManager.ACTION_PREVIOUS
import androidx.media3.ui.PlayerNotificationManager.ACTION_PLAY
import com.example.a2ndproject.sharedViewModel.SharedViewModel
import com.example.muiscplayerproject.R
import com.example.muiscplayerproject.SharedViewModelHolder
import com.example.muiscplayerproject.broadcastReceiver.NotificationDismissReceiver

//had to add this annotation hope it doesn't matter
@UnstableApi class MusicService : Service() {

    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notification: Notification
    private lateinit var player: ExoPlayer
    private lateinit var remoteViews:RemoteViews
    private var isPlaying = false

    override fun onCreate() {
        super.onCreate()
        setUp()

    }

    fun setUp(){
        val sharedViewModel: SharedViewModel? = SharedViewModelHolder.sharedViewModel

        remoteViews = RemoteViews(packageName, R.layout.notification_layout)

        val dismissIntent = Intent(this, NotificationDismissReceiver::class.java)
        dismissIntent.action = "notification_dismissed_action"
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            dismissIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

// Set the dismiss PendingIntent for your custom layout
        remoteViews.setOnClickPendingIntent(R.id.cancel, dismissPendingIntent)

        player= sharedViewModel?.player?.value!!
        remoteViews.setOnClickPendingIntent(R.id.play_notif, getPendingIntent(ACTION_PLAY))
        remoteViews.setOnClickPendingIntent(R.id.previous_notif, getPendingIntent(ACTION_PREVIOUS))
         remoteViews.setOnClickPendingIntent(R.id.nexy_notif, getPendingIntent(ACTION_NEXT))

        remoteViews.setTextViewText(R.id.songName_notif,player.currentMediaItem?.mediaMetadata?.title)
        remoteViews.setImageViewResource(R.id.previous_notif,R.drawable.player_previous)
        remoteViews.setImageViewResource(R.id.nexy_notif,R.drawable.player_next)
        remoteViews.setImageViewResource(R.id.play_notif,R.drawable.player_pause)
        remoteViews.setImageViewResource(R.id.cancel,R.drawable.crosss)


        notification=NotificationCompat.Builder(this,"running music")
            .setSmallIcon(R.drawable.baseline_audiotrack_24)
            .setContentTitle(player.currentMediaItem?.mediaMetadata?.title)
            .setCustomBigContentView(remoteViews)
            .setAutoCancel(true)
            .build()
        startForeground(1,notification)
        playerControls(player)

    }
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    fun playerControls(player: ExoPlayer){
        player.addListener(object : Player.Listener{
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                remoteViews.setImageViewResource(R.id.play_notif,R.drawable.player_pause)
                updateNotification()
            }

        })

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
//        val sharedViewModel: SharedViewModel? = SharedViewModelHolder.sharedViewModel
//        player= sharedViewModel?.player?.value!!
        // Handle the user interactions with the buttons here
        // (play, pause, next, previous)

        intent?.let {
            when (it.action) {
                ACTION_PLAY -> {
                    // Handle play button click
                    if(player.isPlaying){
                        player.pause()
                        remoteViews.setImageViewResource(R.id.play_notif,R.drawable.player_play)
                    }
                    else {
                        player.play()
                        remoteViews.setImageViewResource(R.id.play_notif,R.drawable.player_pause)
                    }
                      updateNotification()
                }
                ACTION_PREVIOUS -> {
                    if(player.hasPreviousMediaItem()){
                        player.seekToPrevious()
                        updateNotification()
                    }
                }
                ACTION_NEXT -> {
                   if(player.hasNextMediaItem()){
                       player.seekToNext()
                       updateNotification()
                   }
                }
                Actions.Stop.toString()->{
                    player.release()
                    stopSelf()
                }
            }
        }


        return START_STICKY
    }
        // Update the notification based on playback state and song info
       // updateNotification()




    fun start(){

    }

    private fun buildNotification(): Notification {
        // Build the notification using NotificationCompat.Builder
        // Add play-pause-next-previous buttons and set click intents
        // Set the notification content
        //val notification = builder.build()

        return notification
    }

    private fun updateNotification() {
        remoteViews.setTextViewText(R.id.songName_notif,player.currentMediaItem?.mediaMetadata?.title)
        notification=NotificationCompat.Builder(this,"running music")
            .setSmallIcon(com.example.muiscplayerproject.R.drawable.baseline_audiotrack_24)
            .setCustomBigContentView(remoteViews)
            .build()
        startForeground(NOTIFICATION_ID,notification)
    }
    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java)
        intent.action = action
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }


    override fun onDestroy() {
        super.onDestroy()
        SharedViewModel.isPaused.postValue(true)
        player.pause()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
    enum class Actions{
        Start,Stop
    }
}

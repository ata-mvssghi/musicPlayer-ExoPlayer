package com.example.muiscplayerproject.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaSession2Service.MediaNotification
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.Observer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.DownloadService.startForeground
import androidx.media3.ui.PlayerNotificationManager.ACTION_NEXT
import androidx.media3.ui.PlayerNotificationManager.ACTION_PREVIOUS
import androidx.media3.ui.PlayerNotificationManager.ACTION_PLAY
import com.bumptech.glide.Glide
import com.example.a2ndproject.sharedViewModel.SharedViewModel
import com.example.muiscplayerproject.MainActivity
import com.example.muiscplayerproject.R
import com.example.muiscplayerproject.broadcastReceiver.NotificationDismissReceiver
import java.lang.Exception
import androidx.media.app.NotificationCompat as MediaNotificationCompat

//had to add this annotation hope it doesn't matter
@UnstableApi class MusicService : Service() {
    private lateinit var notification: Notification
    lateinit var player: ExoPlayer
    private lateinit var remoteViews:RemoteViews
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

        // Set the dismiss PendingIntent for the custom layout
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
        remoteViews.setImageViewUri(R.id.albumPic,player.currentMediaItem?.mediaMetadata?.artworkUri)

        updateNotification()
        playerControls(player)
        SharedViewModel.isPaused.observeForever() { isPAused ->
            if(isPAused){
                remoteViews.setImageViewResource(R.id.play_notif,R.drawable.player_play)
                updateNotification()
            }
            else {
                remoteViews.setImageViewResource(R.id.play_notif,R.drawable.player_pause)
                updateNotification()
            }

        }

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
        intent?.let {
            when (it.action) {
                ACTION_PLAY -> {
                    // Handle play button click
                    if(player.isPlaying){
                        player.pause()
                        remoteViews.setImageViewResource(R.id.play_notif,R.drawable.player_play)
                        SharedViewModel.isPaused.postValue(true)
                    }
                    else {
                        player.play()
                        remoteViews.setImageViewResource(R.id.play_notif,R.drawable.player_pause)
                        SharedViewModel.isPaused.postValue(false)
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
                MusicService.Actions.Stop.toString()->{
                    player.release()
                    stopSelf()
                }
            }
        }


        return START_STICKY
    }

    private fun updateNotification() {
        remoteViews.setTextViewText(R.id.songName_notif,player.currentMediaItem?.mediaMetadata?.title)
        val artworkUri = player.currentMediaItem?.mediaMetadata?.artworkUri
        if (artworkUri != null) {
            val contentResolver: ContentResolver =this.contentResolver
            try {
                val inputStream = contentResolver.openInputStream(artworkUri)
                if (inputStream != null) {
                    // The URI points to a valid image
                    remoteViews.setImageViewUri(R.id.albumPic,artworkUri)
                } else {
                    // The URI doesn't point to a valid image
                    remoteViews.setImageViewResource(R.id.albumPic,R.drawable.headphones)
                }
            } catch (e: Exception) {
                remoteViews.setImageViewResource(R.id.albumPic,R.drawable.headphones)
            }
        } else {
            remoteViews.setImageViewResource(R.id.albumPic,R.drawable.headphones)
        }
        notification=NotificationCompat.Builder(this,"running music")
            .setSmallIcon(R.drawable.baseline_audiotrack_24)
            .setContentTitle(player.currentMediaItem?.mediaMetadata?.title)
            .setCustomBigContentView(remoteViews)
            .setStyle( NotificationCompat.DecoratedCustomViewStyle())
            .setAutoCancel(true)
            .build()
        startForeground(1,notification)
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

    companion object SharedViewModelHolder {
        var sharedViewModel: SharedViewModel? = null
    }
    enum class Actions{
        Start,Stop
    }
}

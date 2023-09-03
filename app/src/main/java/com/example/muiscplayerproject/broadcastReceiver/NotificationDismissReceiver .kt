package com.example.muiscplayerproject.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.muiscplayerproject.service.MusicService

class NotificationDismissReceiver : BroadcastReceiver() {
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "notification_dismissed_action") {
            // Stop your service here
            val serviceIntent = Intent(context, MusicService::class.java)
            context?.stopService(serviceIntent)
        }
    }
}
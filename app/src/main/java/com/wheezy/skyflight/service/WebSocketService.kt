package com.wheezy.skyflight.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.wheezy.skyflight.core.common.manager.WebSocketManager
import com.wheezy.skyflight.core.ui.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WebSocketService : LifecycleService() {

    @Inject
    lateinit var webSocketManager: WebSocketManager

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "websocket_service_channel"
        private var isServiceRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        if (!isServiceRunning) {
            isServiceRunning = true
            startForegroundService()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            if (notificationManager.areNotificationsEnabled().not()) {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        webSocketManager.disconnect()
        isServiceRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    private fun startForegroundService() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "WebSocket Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Maintains WebSocket connection for real-time updates"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SkyFlight")
            .setContentText("Connected for real-time updates")
            .setSmallIcon(R.drawable.sky_book)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }
}
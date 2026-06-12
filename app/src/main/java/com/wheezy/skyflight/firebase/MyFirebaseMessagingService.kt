package com.wheezy.skyflight.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wheezy.skyflight.core.common.manager.FCMTokenManager
import com.wheezy.skyflight.core.common.utils.NotificationEventBus
import com.wheezy.skyflight.core.ui.R
import com.wheezy.skyflight.presentation.screens.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var tokenManager: FCMTokenManager

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "flight_notifications_channel"
        private const val CHANNEL_NAME = "Flight Notifications"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        CoroutineScope(Dispatchers.IO).launch {
            tokenManager.sendTokenToServer(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received: ${message.data}")

        createNotificationChannel()

        message.notification?.let { notification ->
            Log.d(TAG, "Notification: ${notification.title} - ${notification.body}")
            showNotification(
                title = notification.title ?: "SkyFlight Notification",
                body = notification.body ?: ""
            )
        }

        message.data.let { data ->
            val title = data["title"] ?: "SkyFlight"
            val body = data["body"] ?: "You have a new notification"
            showNotification(title = title, body = body)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Flight booking and payment notifications"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
            enableLights(true)
            lightColor = Color.RED
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun showNotification(title: String, body: String) {
        val notificationId = System.currentTimeMillis().toInt()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.sky_book)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .build()

        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Notification shown: $title - $body")

        CoroutineScope(Dispatchers.Main).launch {
            NotificationEventBus.sendNotificationEvent("$title: $body", false)
        }

        val updateIntent = Intent("UPDATE_NOTIFICATIONS_COUNT")
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent)
    }
}
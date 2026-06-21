package com.wheezy.skyflight

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.work.*
import com.google.firebase.messaging.FirebaseMessaging
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.wheezy.skyflight.core.common.manager.FCMTokenManager
import com.wheezy.skyflight.core.common.manager.NetworkMonitor
import com.wheezy.skyflight.core.common.worker.CacheCleanupWorker
import com.wheezy.skyflight.core.common.worker.SyncBookingWorker
import com.wheezy.skyflight.core.config.Config
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {

    @Inject
    lateinit var tokenManager: FCMTokenManager

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        Config.STRIPE_PUBLISHABLE_KEY = BuildConfig.STRIPE_PUBLISHABLE_KEY
        Config.OPENWEATHER_API_KEY = BuildConfig.OPENWEATHER_API_KEY
        Config.GOOGLE_SERVER_CLIENT_ID = BuildConfig.GOOGLE_SERVER_CLIENT_ID

        createNotificationChannel()

        PaymentConfiguration.init(
            applicationContext,
            BuildConfig.STRIPE_PUBLISHABLE_KEY
        )

        if (isRooted()) {
            Log.e("Security", "Device is rooted!")
        }

        PaymentSheet.resetCustomer(applicationContext)

        networkMonitor.register()

        initFcm()

        setupWorkManager()
        setupCacheCleanup()

        observeNetworkForSync()

        applicationScope.launch {
            migrateOldDataStore()
        }
    }

    private fun setupCacheCleanup() {
        val cleanupRequest = PeriodicWorkRequestBuilder<CacheCleanupWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "cache_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
    }

    private fun isRooted(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys") ||
                File("/system/app/Superuser.apk").exists() ||
                File("/sbin/su").exists() ||
                File("/system/bin/su").exists() ||
                File("/system/xbin/su").exists() ||
                File("/data/local/xbin/su").exists() ||
                File("/data/local/bin/su").exists() ||
                File("/system/sd/xbin/su").exists() ||
                File("/system/bin/failsafe/su").exists() ||
                File("/data/local/su").exists()
    }

    private fun setupWorkManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncBookingWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                1, TimeUnit.MINUTES
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "sync_offline_bookings",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    private fun observeNetworkForSync() {
        applicationScope.launch {
            networkMonitor.isConnected.collect { isConnected ->
                if (isConnected) {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()

                    val syncRequest = OneTimeWorkRequestBuilder<SyncBookingWorker>()
                        .setConstraints(constraints)
                        .build()

                    WorkManager.getInstance(this@MyApplication)
                        .enqueueUniqueWork(
                            "sync_bookings_now",
                            ExistingWorkPolicy.REPLACE,
                            syncRequest
                        )
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "flight_notifications_channel",
            "Flight Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Flight booking and payment notifications"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun initFcm() {
        applicationScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                if (token.isNotEmpty()) {
                    tokenManager.sendTokenToServer(token)
                }
            } catch (e: Exception) {
                Log.e("FCM", "Failed to get FCM token", e)
            }
        }
    }

    private fun migrateOldDataStore() {
        try {
            val oldAuthFile = filesDir.resolve("datastore/auth_prefs.preferences_pb")
            val oldFcmFile = filesDir.resolve("datastore/fcm_prefs.preferences_pb")
            val oldWsFile = filesDir.resolve("datastore/websocket_prefs.preferences_pb")

            if (oldAuthFile.exists()) {
                oldAuthFile.delete()
                Log.d("Migration", "Deleted old auth_prefs")
            }
            if (oldFcmFile.exists()) {
                oldFcmFile.delete()
                Log.d("Migration", "Deleted old fcm_prefs")
            }
            if (oldWsFile.exists()) {
                oldWsFile.delete()
                Log.d("Migration", "Deleted old websocket_prefs")
            }
        } catch (e: Exception) {
            Log.e("Migration", "Error during migration", e)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        networkMonitor.unregister()
    }
}
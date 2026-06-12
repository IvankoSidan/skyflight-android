package com.wheezy.skyflight.feature.notifications.data.repository

import com.wheezy.skyflight.core.common.manager.NetworkMonitor
import com.wheezy.skyflight.feature.notifications.data.local.NotificationSettingsLocalDataSource
import com.wheezy.skyflight.feature.notifications.domain.model.NotificationSettings
import com.wheezy.skyflight.feature.notifications.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSettingsRepositoryImpl @Inject constructor(
    private val localDataSource: NotificationSettingsLocalDataSource,
    private val networkMonitor: NetworkMonitor
) : NotificationSettingsRepository {

    override fun getSettings(): Flow<NotificationSettings> {
        return localDataSource.settingsFlow
    }

    override suspend fun updateSettings(settings: NotificationSettings) {
        localDataSource.updateSettings(settings)
    }

    override suspend fun shouldSendNotification(
        notificationType: String,
        userId: Long
    ): Boolean {
        val settings = localDataSource.settingsFlow.first()

        if (!settings.pushEnabled) return false

        if (settings.isQuietHour()) return false

        return when (notificationType) {
            "booking_created" -> settings.bookingCreated
            "booking_confirmed" -> settings.bookingConfirmed
            "booking_cancelled" -> settings.bookingCancelled
            "payment_success" -> settings.paymentSuccess
            "payment_failed" -> settings.paymentFailed
            "reminder" -> settings.flightReminder
            "flight_status_update" -> settings.flightStatusUpdate
            "mass_promotion" -> settings.massPromotion
            "thank_you" -> settings.thankYouAfterFlight
            else -> true
        }
    }
}
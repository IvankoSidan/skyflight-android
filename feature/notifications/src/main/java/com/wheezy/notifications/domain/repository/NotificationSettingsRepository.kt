package com.wheezy.skyflight.feature.notifications.domain.repository

import com.wheezy.skyflight.feature.notifications.domain.model.NotificationSettings
import kotlinx.coroutines.flow.Flow

interface NotificationSettingsRepository {
    fun getSettings(): Flow<NotificationSettings>
    suspend fun updateSettings(settings: NotificationSettings)
    suspend fun shouldSendNotification(
        notificationType: String,
        userId: Long
    ): Boolean
}
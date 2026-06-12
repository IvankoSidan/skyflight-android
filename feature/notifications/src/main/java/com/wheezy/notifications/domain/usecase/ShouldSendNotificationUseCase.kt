package com.wheezy.skyflight.feature.notifications.domain.usecase

import com.wheezy.skyflight.feature.notifications.domain.repository.NotificationSettingsRepository
import javax.inject.Inject

class ShouldSendNotificationUseCase @Inject constructor(
    private val repository: NotificationSettingsRepository
) {
    suspend operator fun invoke(
        notificationType: String,
        userId: Long
    ): Boolean {
        return repository.shouldSendNotification(notificationType, userId)
    }
}
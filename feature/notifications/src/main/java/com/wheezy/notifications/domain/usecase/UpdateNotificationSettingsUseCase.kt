package com.wheezy.skyflight.feature.notifications.domain.usecase

import com.wheezy.skyflight.feature.notifications.domain.model.NotificationSettings
import com.wheezy.skyflight.feature.notifications.domain.repository.NotificationSettingsRepository
import javax.inject.Inject

class UpdateNotificationSettingsUseCase @Inject constructor(
    private val repository: NotificationSettingsRepository
) {
    suspend operator fun invoke(settings: NotificationSettings) {
        repository.updateSettings(settings)
    }
}
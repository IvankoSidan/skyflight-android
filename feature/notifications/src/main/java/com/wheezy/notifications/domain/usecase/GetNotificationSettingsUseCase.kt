package com.wheezy.skyflight.feature.notifications.domain.usecase

import com.wheezy.skyflight.feature.notifications.domain.model.NotificationSettings
import com.wheezy.skyflight.feature.notifications.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationSettingsUseCase @Inject constructor(
    private val repository: NotificationSettingsRepository
) {
    operator fun invoke(): Flow<NotificationSettings> {
        return repository.getSettings()
    }
}
package com.wheezy.skyflight.feature.notifications.domain.usecase

import com.wheezy.skyflight.feature.notifications.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetQuietHoursStatusUseCase @Inject constructor(
    private val repository: NotificationSettingsRepository
) {
    suspend operator fun invoke(): Boolean {
        val settings = repository.getSettings().first()
        return settings.isQuietHour()
    }
}
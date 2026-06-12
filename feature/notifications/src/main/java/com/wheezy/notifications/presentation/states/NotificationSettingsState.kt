package com.wheezy.skyflight.feature.notifications.presentation.states

import com.wheezy.skyflight.feature.notifications.domain.model.NotificationSettings

sealed class NotificationSettingsState {
    object Loading : NotificationSettingsState()
    data class Success(val settings: NotificationSettings) : NotificationSettingsState()
    data class Error(val message: String) : NotificationSettingsState()
}
package com.wheezy.skyflight.feature.notifications.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheezy.skyflight.feature.notifications.domain.model.NotificationSettings
import com.wheezy.skyflight.feature.notifications.domain.usecase.GetNotificationSettingsUseCase
import com.wheezy.skyflight.feature.notifications.domain.usecase.UpdateNotificationSettingsUseCase
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.feature.notifications.presentation.states.NotificationSettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetNotificationSettingsUseCase,
    private val updateSettingsUseCase: UpdateNotificationSettingsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<NotificationSettingsState>(NotificationSettingsState.Loading)
    val state: StateFlow<NotificationSettingsState> = _state.asStateFlow()

    private val _settings = MutableStateFlow<NotificationSettings?>(null)
    val settings: StateFlow<NotificationSettings?> = _settings.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        getSettingsUseCase()
            .onEach { settings ->
                _settings.value = settings
                _state.value = NotificationSettingsState.Success(settings)
            }
            .launchIn(viewModelScope)
    }

    fun updateSetting(update: (NotificationSettings) -> NotificationSettings) {
        viewModelScope.launch {
            val currentSettings = _settings.value ?: return@launch
            val newSettings = update(currentSettings)
            _settings.value = newSettings
            updateSettingsUseCase(newSettings)
            SnackbarHelper.showSuccess("Settings updated")
        }
    }

    fun toggleBookingCreated() {
        updateSetting { it.copy(bookingCreated = !it.bookingCreated) }
    }

    fun toggleBookingConfirmed() {
        updateSetting { it.copy(bookingConfirmed = !it.bookingConfirmed) }
    }

    fun toggleBookingCancelled() {
        updateSetting { it.copy(bookingCancelled = !it.bookingCancelled) }
    }

    fun togglePaymentSuccess() {
        updateSetting { it.copy(paymentSuccess = !it.paymentSuccess) }
    }

    fun togglePaymentFailed() {
        updateSetting { it.copy(paymentFailed = !it.paymentFailed) }
    }

    fun toggleFlightReminder() {
        updateSetting { it.copy(flightReminder = !it.flightReminder) }
    }

    fun toggleFlightStatusUpdate() {
        updateSetting { it.copy(flightStatusUpdate = !it.flightStatusUpdate) }
    }

    fun toggleMassPromotion() {
        updateSetting { it.copy(massPromotion = !it.massPromotion) }
    }

    fun toggleThankYouAfterFlight() {
        updateSetting { it.copy(thankYouAfterFlight = !it.thankYouAfterFlight) }
    }

    fun toggleQuietHours() {
        updateSetting { it.copy(quietHoursEnabled = !it.quietHoursEnabled) }
    }

    fun setQuietHoursStart(hour: Int) {
        updateSetting { it.copy(quietHoursStart = hour) }
    }

    fun setQuietHoursEnd(hour: Int) {
        updateSetting { it.copy(quietHoursEnd = hour) }
    }

    fun togglePushEnabled() {
        updateSetting { it.copy(pushEnabled = !it.pushEnabled) }
    }

    fun toggleEmailEnabled() {
        updateSetting { it.copy(emailEnabled = !it.emailEnabled) }
    }
}
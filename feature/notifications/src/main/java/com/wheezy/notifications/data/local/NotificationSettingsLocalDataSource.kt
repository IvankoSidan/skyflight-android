package com.wheezy.skyflight.feature.notifications.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.wheezy.skyflight.feature.notifications.domain.model.NotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSettingsLocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val BOOKING_CREATED = booleanPreferencesKey("booking_created")
        private val BOOKING_CONFIRMED = booleanPreferencesKey("booking_confirmed")
        private val BOOKING_CANCELLED = booleanPreferencesKey("booking_cancelled")
        private val PAYMENT_SUCCESS = booleanPreferencesKey("payment_success")
        private val PAYMENT_FAILED = booleanPreferencesKey("payment_failed")
        private val FLIGHT_REMINDER = booleanPreferencesKey("flight_reminder")
        private val FLIGHT_STATUS_UPDATE = booleanPreferencesKey("flight_status_update")
        private val MASS_PROMOTION = booleanPreferencesKey("mass_promotion")
        private val THANK_YOU_AFTER_FLIGHT = booleanPreferencesKey("thank_you_after_flight")
        private val QUIET_HOURS_ENABLED = booleanPreferencesKey("quiet_hours_enabled")
        private val QUIET_HOURS_START = intPreferencesKey("quiet_hours_start")
        private val QUIET_HOURS_END = intPreferencesKey("quiet_hours_end")
        private val PUSH_ENABLED = booleanPreferencesKey("push_enabled")
        private val EMAIL_ENABLED = booleanPreferencesKey("email_enabled")
    }

    val settingsFlow: Flow<NotificationSettings> = dataStore.data.map { prefs ->
        NotificationSettings(
            bookingCreated = prefs[BOOKING_CREATED] ?: true,
            bookingConfirmed = prefs[BOOKING_CONFIRMED] ?: true,
            bookingCancelled = prefs[BOOKING_CANCELLED] ?: true,
            paymentSuccess = prefs[PAYMENT_SUCCESS] ?: true,
            paymentFailed = prefs[PAYMENT_FAILED] ?: true,
            flightReminder = prefs[FLIGHT_REMINDER] ?: true,
            flightStatusUpdate = prefs[FLIGHT_STATUS_UPDATE] ?: true,
            massPromotion = prefs[MASS_PROMOTION] ?: false,
            thankYouAfterFlight = prefs[THANK_YOU_AFTER_FLIGHT] ?: true,
            quietHoursEnabled = prefs[QUIET_HOURS_ENABLED] ?: false,
            quietHoursStart = prefs[QUIET_HOURS_START] ?: 23,
            quietHoursEnd = prefs[QUIET_HOURS_END] ?: 8,
            pushEnabled = prefs[PUSH_ENABLED] ?: true,
            emailEnabled = prefs[EMAIL_ENABLED] ?: true
        )
    }

    suspend fun updateSettings(settings: NotificationSettings) {
        dataStore.edit { prefs ->
            prefs[BOOKING_CREATED] = settings.bookingCreated
            prefs[BOOKING_CONFIRMED] = settings.bookingConfirmed
            prefs[BOOKING_CANCELLED] = settings.bookingCancelled
            prefs[PAYMENT_SUCCESS] = settings.paymentSuccess
            prefs[PAYMENT_FAILED] = settings.paymentFailed
            prefs[FLIGHT_REMINDER] = settings.flightReminder
            prefs[FLIGHT_STATUS_UPDATE] = settings.flightStatusUpdate
            prefs[MASS_PROMOTION] = settings.massPromotion
            prefs[THANK_YOU_AFTER_FLIGHT] = settings.thankYouAfterFlight
            prefs[QUIET_HOURS_ENABLED] = settings.quietHoursEnabled
            prefs[QUIET_HOURS_START] = settings.quietHoursStart
            prefs[QUIET_HOURS_END] = settings.quietHoursEnd
            prefs[PUSH_ENABLED] = settings.pushEnabled
            prefs[EMAIL_ENABLED] = settings.emailEnabled
        }
    }
}
package com.wheezy.skyflight.feature.notifications.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationSettings(
    val bookingCreated: Boolean = true,
    val bookingConfirmed: Boolean = true,
    val bookingCancelled: Boolean = true,
    val paymentSuccess: Boolean = true,
    val paymentFailed: Boolean = true,
    val flightReminder: Boolean = true,
    val flightStatusUpdate: Boolean = true,
    val massPromotion: Boolean = false,
    val thankYouAfterFlight: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: Int = 23,
    val quietHoursEnd: Int = 8,
    val pushEnabled: Boolean = true,
    val emailEnabled: Boolean = true
) : Parcelable {
    fun isQuietHour(currentHour: Int = java.util.Calendar
        .getInstance().get(java.util.Calendar.HOUR_OF_DAY)): Boolean {
        if (!quietHoursEnabled) return false
        return if (quietHoursStart > quietHoursEnd) {
            currentHour >= quietHoursStart || currentHour < quietHoursEnd
        } else {
            currentHour in quietHoursStart until quietHoursEnd
        }
    }
}
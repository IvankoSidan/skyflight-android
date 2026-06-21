package com.wheezy.skyflight.feature.notifications.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Calendar

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

    fun isQuietHour(currentHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): Boolean {
        if (!quietHoursEnabled) return false
        if (quietHoursStart == quietHoursEnd) return false
        return if (quietHoursStart < quietHoursEnd) {
            currentHour in quietHoursStart until quietHoursEnd
        } else {
            currentHour in quietHoursStart..23 || currentHour in 0 until quietHoursEnd
        }
    }
}
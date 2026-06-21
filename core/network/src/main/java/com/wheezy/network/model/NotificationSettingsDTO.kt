package com.wheezy.skyflight.core.network.model

data class NotificationSettingsDTO(
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
)
package com.wheezy.skyflight.core.model

import java.time.LocalDateTime

data class Booking(
    val id: Long = 0,
    val userId: Long,
    val flightId: Long,
    val seatCount: Int = 1,
    val seatNumbers: String,
    var status: BookingStatus,
    val bookingDate: LocalDateTime = LocalDateTime.now()
)
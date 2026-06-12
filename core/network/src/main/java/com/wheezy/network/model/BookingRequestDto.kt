package com.wheezy.skyflight.core.network.model

data class BookingRequestDto(
    val flightId: Long,
    val seatNumber: String
)
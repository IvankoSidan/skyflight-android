package com.wheezy.skyflight.core.network.model

import com.wheezy.skyflight.core.model.BookingStatus

data class BookingStatusUpdateRequest(
    val status: BookingStatus
)
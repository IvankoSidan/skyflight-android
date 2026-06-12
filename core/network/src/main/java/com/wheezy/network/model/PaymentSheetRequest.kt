package com.wheezy.skyflight.core.network.model

data class PaymentSheetRequest(
    val bookingId: Long,
    val amount: Long,
    val currency: String,
    val promocodeId: Long? = null
)
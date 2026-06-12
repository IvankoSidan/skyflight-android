package com.wheezy.skyflight.core.network.model

data class PaymentSheetResponseDTO(
    val paymentIntentClientSecret: String,
    val ephemeralKey: String?,
    val customerId: String?,
    val publishableKey: String
)
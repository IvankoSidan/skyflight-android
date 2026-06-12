package com.wheezy.skyflight.core.network.model

data class PromocodeRequest(
    val code: String,
    val amount: Long,
    val currency: String = "USD"
)

data class PromocodeResponse(
    val id: Long?,
    val code: String,
    val discountPercent: Int?,
    val discountAmount: Long?,
    val discountedAmount: Long,
    val isValid: Boolean,
    val message: String?
)
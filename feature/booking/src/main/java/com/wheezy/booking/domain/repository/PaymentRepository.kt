package com.wheezy.skyflight.feature.booking.domain.repository

import com.wheezy.skyflight.core.network.model.PaymentSheetResponseDTO
import retrofit2.Response

interface PaymentRepository {
    suspend fun createPaymentSheet(
        bookingId: Long,
        amount: Long,
        currency: String
    ): Response<PaymentSheetResponseDTO>
}
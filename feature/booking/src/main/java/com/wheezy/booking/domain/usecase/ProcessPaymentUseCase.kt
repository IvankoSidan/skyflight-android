package com.wheezy.skyflight.feature.booking.domain.usecase

import com.wheezy.skyflight.feature.booking.domain.repository.PaymentRepository
import javax.inject.Inject

class ProcessPaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    sealed class Result {
        data class Success(
            val clientSecret: String,
            val customerId: String?,
            val ephemeralKey: String?
        ) : Result()
        data class Error(val message: String) : Result()
    }

    suspend operator fun invoke(bookingId: Long, amount: Long): Result {
        return try {
            val response = paymentRepository.createPaymentSheet(bookingId, amount, "RUB")
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.Success(
                    clientSecret = body.paymentIntentClientSecret,
                    customerId = body.customerId,
                    ephemeralKey = body.ephemeralKey
                )
            } else {
                Result.Error(response.message() ?: "Failed to create payment sheet")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
}
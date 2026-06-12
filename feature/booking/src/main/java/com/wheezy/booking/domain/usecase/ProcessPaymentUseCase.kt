package com.wheezy.skyflight.feature.booking.domain.usecase

import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.Seat
import com.wheezy.skyflight.feature.booking.domain.repository.PaymentRepository
import java.math.BigDecimal
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

    suspend operator fun invoke(
        bookingId: Long,
        flight: FlightModel,
        selectedSeats: List<Seat>
    ): Result {
        val amount = calculateAmount(flight.price, selectedSeats.size)

        return try {
            val response = paymentRepository.createPaymentSheet(bookingId, amount, "USD")
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.Success(
                    clientSecret = body.paymentIntentClientSecret,
                    customerId = body.customerId,
                    ephemeralKey = body.ephemeralKey
                )
            } else {
                Result.Error(response.message())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    private fun calculateAmount(price: BigDecimal, seatCount: Int): Long =
        price.multiply(BigDecimal(seatCount)).multiply(BigDecimal(100)).longValueExact()
}
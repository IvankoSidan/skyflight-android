package com.wheezy.skyflight.feature.booking.domain.usecase

import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.Seat
import com.wheezy.skyflight.core.network.model.BookingRequestDto
import com.wheezy.skyflight.feature.booking.domain.repository.FlightRepository
import javax.inject.Inject

class CreateBookingUseCase @Inject constructor(
    private val flightRepository: FlightRepository
) {
    data class Result(
        val success: Boolean,
        val bookingId: Long? = null,
        val errorMessage: String? = null
    )

    suspend operator fun invoke(
        flight: FlightModel,
        selectedSeats: List<Seat>
    ): Result {
        val seatNumbers = selectedSeats.joinToString(",") { it.name }
        val flightId = flight.flightId ?: return Result(false, errorMessage = "Invalid flight ID")

        val bookingDto = BookingRequestDto(flightId, seatNumbers)

        return try {
            val response = flightRepository.createBooking(bookingDto)
            if (response.isSuccessful) {
                Result(true, response.body()?.bookingId)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Authentication failed. Please login again."
                    404 -> "Flight not found. Please try again."
                    else -> "Booking failed: ${response.message()}"
                }
                Result(false, errorMessage = errorMsg)
            }
        } catch (e: Exception) {
            Result(false, errorMessage = "Booking error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
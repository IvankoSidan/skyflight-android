package com.wheezy.skyflight.feature.booking.domain.usecase

import com.wheezy.skyflight.core.network.model.BookingDetailsDTO
import com.wheezy.skyflight.feature.booking.domain.repository.BookingRepository
import javax.inject.Inject

class GetBookingByIdUseCase @Inject constructor(
    private val repository: BookingRepository
) {
    suspend operator fun invoke(bookingId: Long): Result<BookingDetailsDTO> {
        return try {
            val bookings = repository.getMyBookings()
            bookings.mapCatching { list ->
                list.find { it.bookingId == bookingId }
                    ?: throw Exception("Booking not found")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
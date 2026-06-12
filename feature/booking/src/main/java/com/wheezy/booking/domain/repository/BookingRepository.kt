package com.wheezy.skyflight.feature.booking.domain.repository

import com.wheezy.skyflight.core.model.BookingStatus
import com.wheezy.skyflight.core.network.model.BookingDetailsDTO

interface BookingRepository {
    suspend fun getMyBookings(): Result<List<BookingDetailsDTO>>
    suspend fun updateBookingStatus(bookingId: Long, status: BookingStatus): Result<Unit>
    suspend fun cancelBooking(bookingId: Long): Result<Unit>
    suspend fun deleteBooking(bookingId: Long): Result<Unit>
}
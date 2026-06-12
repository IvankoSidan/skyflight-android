package com.wheezy.skyflight.feature.booking.domain.repository

import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.network.model.BookingRequestDto
import com.wheezy.skyflight.core.network.model.BookingResponseDTO
import retrofit2.Response

interface FlightRepository {
    suspend fun createBooking(bookingDto: BookingRequestDto): Response<BookingResponseDTO>
    suspend fun getReservedSeats(flightId: Long): List<String>
    suspend fun getFlightById(flightId: Long): FlightModel?
}
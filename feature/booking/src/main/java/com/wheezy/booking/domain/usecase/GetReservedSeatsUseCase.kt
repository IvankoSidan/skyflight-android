package com.wheezy.skyflight.feature.booking.domain.usecase

import com.wheezy.skyflight.feature.booking.domain.repository.FlightRepository
import javax.inject.Inject

class GetReservedSeatsUseCase @Inject constructor(
    private val flightRepository: FlightRepository
) {
    suspend operator fun invoke(flightId: Long): List<String> {
        return flightRepository.getReservedSeats(flightId)
    }
}
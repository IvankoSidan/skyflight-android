package com.wheezy.skyflight.feature.booking.domain.usecase

import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.feature.booking.domain.repository.FlightRepository
import javax.inject.Inject

class GetFlightByIdUseCase @Inject constructor(
    private val flightRepository: FlightRepository
) {
    suspend operator fun invoke(flightId: Long): FlightModel? {
        return flightRepository.getFlightById(flightId)
    }
}
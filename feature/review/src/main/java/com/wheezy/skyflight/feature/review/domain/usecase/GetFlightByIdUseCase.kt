package com.wheezy.skyflight.feature.review.domain.usecase

import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.network.api.ApiService
import javax.inject.Inject

class GetFlightByIdUseCase @Inject constructor(
    private val apiService: ApiService
) {
    suspend operator fun invoke(flightId: Long): FlightModel? {
        return try {
            val response = apiService.getFlightById(flightId)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
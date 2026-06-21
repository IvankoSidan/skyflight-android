package com.wheezy.skyflight.core.common.usecase

import android.util.Log
import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.network.api.ApiService
import javax.inject.Inject

class GetFlightByIdUseCase @Inject constructor(
    private val apiService: ApiService
) {

    companion object {
        private const val TAG = "GetFlightByIdUseCase"
    }

    suspend operator fun invoke(flightId: Long): FlightModel? {
        return try {
            val response = apiService.getFlightById(flightId)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "Failed to get flight by id: $flightId, code: ${response.code()}, message: ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting flight by id: $flightId", e)
            null
        }
    }
}
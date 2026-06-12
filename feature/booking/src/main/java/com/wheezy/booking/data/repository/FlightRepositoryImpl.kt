package com.wheezy.skyflight.feature.booking.data.repository

import com.wheezy.skyflight.core.common.cache.DataCache
import com.wheezy.skyflight.core.common.network.SmartRetryPolicy
import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.network.api.ApiService
import com.wheezy.skyflight.core.network.model.BookingRequestDto
import com.wheezy.skyflight.core.network.model.BookingResponseDTO
import com.wheezy.skyflight.feature.booking.domain.repository.FlightRepository
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlightRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : FlightRepository {

    private val flightCache = DataCache<Long, FlightModel>()

    override suspend fun createBooking(bookingDto: BookingRequestDto): Response<BookingResponseDTO> {
        return SmartRetryPolicy.executeWithRetry {
            try {
                apiService.createBooking(bookingDto)
            } catch (e: Exception) {
                Response.error(500, "Error creating booking".toResponseBody())
            }
        }
    }

    override suspend fun getReservedSeats(flightId: Long): List<String> {
        val response = SmartRetryPolicy.executeWithRetry {
            apiService.getBookedSeats(flightId)
        }
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }

    override suspend fun getFlightById(flightId: Long): FlightModel? {
        return try {
            flightCache.get(flightId)?.let {
                return it
            }

            val response = SmartRetryPolicy.executeWithRetry {
                apiService.getFlightById(flightId)
            }

            if (response.isSuccessful && response.body() != null) {
                flightCache.put(flightId, response.body()!!, 3600)
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
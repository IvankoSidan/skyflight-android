package com.wheezy.skyflight.feature.search.domain.repository

import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.LocationModel

interface SearchRepository {
    suspend fun getLocations(): Result<List<LocationModel>>
    suspend fun getClassSeats(): Result<List<String>>
    suspend fun searchFlights(
        from: String,
        to: String,
        classType: String?
    ): Result<List<FlightModel>>
    suspend fun getReservedSeats(flightId: Long): Result<List<String>>
}
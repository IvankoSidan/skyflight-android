package com.wheezy.skyflight.feature.search.data.repository

import com.wheezy.skyflight.core.common.cache.DataCache
import com.wheezy.skyflight.core.common.network.SmartRetryPolicy
import com.wheezy.skyflight.core.database.dao.FlightDao
import com.wheezy.skyflight.core.database.entity.FlightEntity
import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.LocationModel
import com.wheezy.skyflight.core.network.api.ApiService
import com.wheezy.skyflight.feature.search.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val flightDao: FlightDao
) : SearchRepository {

    private val locationsCache = DataCache<String, List<LocationModel>>()
    private val classSeatsCache = DataCache<String, List<String>>()
    private val flightsCache = DataCache<String, List<FlightModel>>()

    override suspend fun getLocations(): Result<List<LocationModel>> {
        return try {
            locationsCache.get("locations")?.let {
                return Result.success(it)
            }

            val response = SmartRetryPolicy.executeWithRetry {
                apiService.getLocations()
            }

            if (response.isSuccessful && response.body() != null) {
                val locations = response.body()!!
                locationsCache.put("locations", locations, 3600)
                Result.success(locations)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getClassSeats(): Result<List<String>> {
        return try {
            classSeatsCache.get("class_seats")?.let {
                return Result.success(it)
            }

            val response = SmartRetryPolicy.executeWithRetry {
                apiService.getClassSeats()
            }

            if (response.isSuccessful && response.body() != null) {
                val classSeats = response.body()!!
                classSeatsCache.put("class_seats", classSeats, 3600)
                Result.success(classSeats)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchFlights(
        from: String,
        to: String,
        classType: String?
    ): Result<List<FlightModel>> {
        val cacheKey = "flights_${from}_${to}_$classType"

        flightsCache.get(cacheKey)?.let {
            return Result.success(it)
        }

        val cachedFlights = try {
            flightDao.getFlightsByQuerySuspend(cacheKey).map { it.toFlightModel() }
        } catch (e: Exception) {
            emptyList()
        }

        if (cachedFlights.isNotEmpty()) {
            flightsCache.put(cacheKey, cachedFlights, 300)
            return Result.success(cachedFlights)
        }

        return try {
            val response = SmartRetryPolicy.executeWithRetry {
                apiService.searchFlights(from, to, null, classType)
            }

            if (response.isSuccessful && response.body() != null) {
                val flights = response.body()!!

                val entities = flights.map { flight ->
                    FlightEntity(
                        flightId = flight.flightId ?: 0,
                        airlineLogo = flight.airlineLogo,
                        airlineName = flight.airlineName,
                        arriveTime = flight.arriveTime,
                        classSeat = flight.classSeat,
                        flightDate = flight.flightDate,
                        departureCity = flight.departureCity,
                        departureShort = flight.departureShort,
                        totalSeats = flight.totalSeats,
                        price = flight.price,
                        reservedSeats = flight.reservedSeats,
                        departureTime = flight.departureTime,
                        arrivalCity = flight.arrivalCity,
                        arrivalShort = flight.arrivalShort,
                        searchQueryKey = cacheKey
                    )
                }
                flightDao.insertFlights(entities)

                flightsCache.put(cacheKey, flights, 300)
                Result.success(flights)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            if (cachedFlights.isNotEmpty()) {
                Result.success(cachedFlights)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun getReservedSeats(flightId: Long): Result<List<String>> {
        return try {
            val response = SmartRetryPolicy.executeWithRetry {
                apiService.getBookedSeats(flightId)
            }

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }
}
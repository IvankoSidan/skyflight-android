package com.wheezy.skyflight.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wheezy.skyflight.core.model.FlightModel
import java.math.BigDecimal

@Entity(tableName = "cached_flights")
data class FlightEntity(
    @PrimaryKey
    val flightId: Long,
    val airlineLogo: String,
    val airlineName: String,
    val arriveTime: String,
    val classSeat: String,
    val flightDate: String,
    val departureCity: String,
    val departureShort: String,
    val totalSeats: Int,
    val price: BigDecimal,
    val reservedSeats: String,
    val departureTime: String,
    val arrivalCity: String,
    val arrivalShort: String,
    val searchQueryKey: String,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toFlightModel(): FlightModel = FlightModel(
        flightId = flightId,
        airlineLogo = airlineLogo,
        airlineName = airlineName,
        arriveTime = arriveTime,
        classSeat = classSeat,
        flightDate = flightDate,
        departureCity = departureCity,
        departureShort = departureShort,
        totalSeats = totalSeats,
        price = price,
        reservedSeats = reservedSeats,
        departureTime = departureTime,
        arrivalCity = arrivalCity,
        arrivalShort = arrivalShort,
        passenger = ""
    )
}
package com.wheezy.skyflight.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class FlightModel(
    val flightId: Long?,
    val airlineLogo: String,
    val airlineName: String,
    val arriveTime: String,
    val classSeat: String,
    val flightDate: String,
    val departureCity: String,
    val departureShort: String,
    val totalSeats: Int = 0,
    var price: BigDecimal,
    var passenger: String = "",
    val reservedSeats: String = "",
    val departureTime: String,
    val arrivalCity: String,
    val arrivalShort: String
) : Parcelable {
    val fullLogoUrl: String
        get() = "https://skyflightbooking.ru/api/logo/$airlineLogo"
}
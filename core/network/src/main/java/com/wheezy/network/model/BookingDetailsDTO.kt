package com.wheezy.skyflight.core.network.model

import com.wheezy.skyflight.core.model.Booking
import com.wheezy.skyflight.core.model.BookingStatus
import com.wheezy.skyflight.core.model.FlightModel
import java.math.BigDecimal
import java.time.LocalDateTime

data class BookingDetailsDTO(
    val bookingId: Long,
    val seatNumbers: String,
    val seatCount: Int,
    val status: BookingStatus,
    val bookingDate: String,
    val flightId: Long,
    val airlineName: String,
    val airlineLogo: String,
    val departureCity: String,
    val arrivalCity: String,
    val departureTime: String,
    val arriveTime: String,
    val flightDate: String,
    val classSeat: String,
    val price: BigDecimal,
    val paidAmount: BigDecimal? = null,
    val promocodeId: Long? = null,
    val promocodeCode: String? = null,
    val promocodeDiscountPercent: Int? = null,
    val promocodeDiscountAmount: Long? = null
)

fun BookingDetailsDTO.toBookingEntity(): Booking {
    return Booking(
        id = bookingId,
        userId = 0L,
        flightId = flightId,
        seatCount = seatCount,
        seatNumbers = seatNumbers,
        status = status,
        bookingDate = LocalDateTime.parse(bookingDate)
    )
}

fun BookingDetailsDTO.toFlightModel(): FlightModel {
    val displayPrice = paidAmount ?: price
    return FlightModel(
        flightId = flightId,
        airlineName = airlineName,
        airlineLogo = airlineLogo,
        departureCity = departureCity,
        arrivalCity = arrivalCity,
        departureTime = departureTime,
        arriveTime = arriveTime,
        flightDate = flightDate,
        classSeat = classSeat,
        price = displayPrice,
        departureShort = departureCity.take(3).uppercase(),
        arrivalShort = arrivalCity.take(3).uppercase()
    )
}
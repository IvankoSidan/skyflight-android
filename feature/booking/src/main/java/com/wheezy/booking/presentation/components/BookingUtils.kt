package com.wheezy.skyflight.feature.booking.presentation.components.booking

import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.wheezy.skyflight.core.model.Booking
import com.wheezy.skyflight.core.model.BookingStatus
import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import java.math.BigDecimal

fun getStatusText(status: BookingStatus): String = when (status) {
    BookingStatus.PENDING_PAYMENT -> "Pending Payment"
    BookingStatus.CONFIRMED -> "Confirmed"
    BookingStatus.PAID -> "Paid"
    BookingStatus.CANCELED -> "Canceled"
    BookingStatus.FAILED -> "Failed"
    BookingStatus.UNPAID -> "Unpaid"
}

fun calculateTotalPrice(price: BigDecimal, seatCount: Int): BigDecimal {
    return price.multiply(BigDecimal(seatCount))
}

fun shareBooking(context: Context, flight: FlightModel, booking: Booking, statusText: String) {
    val shareText = buildString {
        appendLine("✈️ SkyFlight Booking")
        appendLine()
        appendLine("Airline: ${flight.airlineName}")
        appendLine("From: ${flight.departureCity} → ${flight.arrivalCity}")
        appendLine("Date: ${flight.flightDate}")
        appendLine("Time: ${flight.departureTime}")
        appendLine("Seat(s): ${booking.seatNumbers}")
        appendLine("Booking ID: ${booking.id}")
        appendLine("Status: $statusText")
        appendLine()
        appendLine("View ticket: https://skyflightbooking.ru/ticket/${booking.id}")
        appendLine()
        appendLine("Download SkyFlight app: https://skyflightbooking.ru/download")
    }

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
}

fun copyBookingInfo(
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    flight: FlightModel,
    booking: Booking,
    statusText: String
) {
    val copyText = buildString {
        appendLine("✈️ SkyFlight Booking #${booking.id}")
        appendLine("${flight.airlineName}: ${flight.departureCity} → ${flight.arrivalCity}")
        appendLine("Date: ${flight.flightDate} at ${flight.departureTime}")
        appendLine("Seat(s): ${booking.seatNumbers}")
        appendLine("Status: $statusText")
        appendLine()
        appendLine("View: https://skyflightbooking.ru/ticket/${booking.id}")
    }
    clipboardManager.setText(AnnotatedString(copyText))
    SnackbarHelper.showInfo("Copied to clipboard")
}
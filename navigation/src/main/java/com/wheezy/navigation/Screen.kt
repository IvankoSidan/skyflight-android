package com.wheezy.skyflight.navigation

import android.net.Uri
import androidx.navigation.NavController

sealed class Screen(val route: String) {

    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    object Main : Screen("main")
    object SearchResult : Screen("searchResult")

    object FlightReviews : Screen("flight_reviews/{flightId}/{airlineName}") {
        @Suppress("unused")
        fun passArgs(flightId: Long, airlineName: String): String =
            "flight_reviews/$flightId/${Uri.encode(airlineName)}"
    }

    object AirlineReviews : Screen("airline_reviews/{airlineName}") {
        fun passArgs(airlineName: String): String = "airline_reviews/${Uri.encode(airlineName)}"
    }

    object MyReviews : Screen("my_reviews")
    object Loyalty : Screen("loyalty")
    object PointsHistory : Screen("points_history")
    object TicketDetail : Screen("ticketDetail")
    object BookingHistory : Screen("booking_history")
    object NotificationSettings : Screen("notification_settings")
    object Referral : Screen("referral")
    object SavedCards : Screen("saved_cards")
    object Invoices : Screen("invoices")

    object InvoiceDetail : Screen("invoice_detail/{bookingId}") {
        fun passBookingId(bookingId: Long): String = "invoice_detail/$bookingId"
    }

    object CreateReview : Screen("create_review/{bookingId}") {
        fun passBookingId(bookingId: Long): String = "create_review/$bookingId"
    }
}

fun NavController.navigateToCreateReview(bookingId: Long) {
    if (bookingId <= 0) return
    navigate(Screen.CreateReview.passBookingId(bookingId))
}

fun NavController.navigateAndClearStack(route: String) {
    navigate(route) {
        popUpTo(0) { inclusive = true }
    }
}

fun NavController.navigateToInvoiceDetail(bookingId: Long) {
    if (bookingId <= 0) return
    navigate(Screen.InvoiceDetail.passBookingId(bookingId))
}

fun NavController.navigateToAirlineReviews(airlineName: String) {
    if (airlineName.isBlank()) return
    navigate(Screen.AirlineReviews.passArgs(airlineName))
}
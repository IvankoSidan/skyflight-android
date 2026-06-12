package com.wheezy.skyflight.navigation

import android.net.Uri
import androidx.navigation.NavController

sealed class Screen(val route: String) {

    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    object Main : Screen("main")
    object SearchResult : Screen("searchResult")

    object SelectSeat : Screen("selectSeat/{flightId}") {
        fun passFlightId(flightId: Long): String = "selectSeat/$flightId"
    }

    object FlightReviews : Screen("flight_reviews/{flightId}/{airlineName}") {
        @Suppress("unused")
        fun passArgs(flightId: Long, airlineName: String): String =
            "flight_reviews/$flightId/${Uri.encode(airlineName)}"
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

    companion object {
        const val FLIGHT_ID_ARG = "flightId"
        const val BOOKING_ID_ARG = "bookingId"
    }
}

fun NavController.navigateToSelectSeat(flightId: Long) {
    if (flightId <= 0) return
    navigate(Screen.SelectSeat.passFlightId(flightId))
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
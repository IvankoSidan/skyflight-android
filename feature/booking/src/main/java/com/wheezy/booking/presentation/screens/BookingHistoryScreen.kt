package com.wheezy.skyflight.feature.booking.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.ImageLoader
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import com.wheezy.skyflight.core.model.BookingStatus
import com.wheezy.skyflight.core.network.model.toBookingEntity
import com.wheezy.skyflight.core.network.model.toFlightModel
import com.wheezy.skyflight.core.ui.components.EmptyStateScreen
import com.wheezy.skyflight.core.ui.components.MyBottomBar
import com.wheezy.skyflight.core.ui.components.WorldBackground
import com.wheezy.skyflight.feature.booking.presentation.components.BookingHistoryItem
import com.wheezy.skyflight.feature.booking.presentation.components.BookingTopBar
import com.wheezy.skyflight.feature.booking.presentation.components.PaymentErrorCard
import com.wheezy.skyflight.feature.booking.presentation.states.BookingListState
import com.wheezy.skyflight.feature.booking.presentation.states.PaymentState
import com.wheezy.skyflight.feature.booking.presentation.viewmodels.BookingViewModel
import com.wheezy.skyflight.feature.booking.presentation.viewmodels.PaymentViewModel
import com.wheezy.skyflight.navigation.Screen
import com.wheezy.skyflight.navigation.navigateAndClearStack
import com.wheezy.skyflight.navigation.navigateToAirlineReviews
import com.wheezy.skyflight.navigation.navigateToCreateReview
import com.wheezy.skyflight.navigation.navigateToInvoiceDetail
import kotlinx.coroutines.launch

@Composable
fun BookingHistoryScreen(
    navController: NavHostController,
    bookingViewModel: BookingViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val bookingListState by bookingViewModel.bookingListState.collectAsState()
    val loadingBookingIds by bookingViewModel.loadingBookingIds.collectAsState()
    val paymentState by paymentViewModel.paymentState.collectAsState()
    val offlineCount by bookingViewModel.offlineBookingsCount.collectAsState()
    val canReviewMap by bookingViewModel.canReviewMap.collectAsState()
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

    var pendingBookingId by remember { mutableStateOf<Long?>(null) }
    var isPaymentSheetPresented by remember { mutableStateOf(false) }

    val paymentSheet = rememberPaymentSheet(
        paymentResultCallback = { result ->
            isPaymentSheetPresented = false
            when (result) {
                is PaymentSheetResult.Completed -> {
                    paymentViewModel.handleSuccess()
                    val currentBookingId = pendingBookingId
                    if (currentBookingId != null) {
                        scope.launch {
                            bookingViewModel.updateBookingStatus(currentBookingId, BookingStatus.PAID)
                            bookingViewModel.loadMyBookings()
                        }
                    }
                    pendingBookingId = null
                    navController.navigateAndClearStack(Screen.Main.route)
                }
                is PaymentSheetResult.Canceled -> {
                    paymentViewModel.handleCancel()
                    pendingBookingId = null
                }
                is PaymentSheetResult.Failed -> {
                    paymentViewModel.handleFailure(result.error.localizedMessage ?: "Payment failed")
                    pendingBookingId = null
                }
            }
        }
    )

    LaunchedEffect(pendingBookingId, paymentState) {
        if (pendingBookingId != null && paymentState is PaymentState.Prepared && !isPaymentSheetPresented) {
            val prepared = paymentState as PaymentState.Prepared
            try {
                isPaymentSheetPresented = true
                paymentSheet.presentWithPaymentIntent(
                    prepared.clientSecret,
                    PaymentSheet.Configuration(
                        merchantDisplayName = "SkyFlight",
                        customer = PaymentSheet.CustomerConfiguration(prepared.customerId, prepared.ephemeralKey)
                    )
                )
            } catch (e: Exception) {
                isPaymentSheetPresented = false
                paymentViewModel.handleFailure(e.message ?: "Failed to open payment sheet")
            }
        }
    }

    LaunchedEffect(Unit) {
        bookingViewModel.loadMyBookings()
        bookingViewModel.loadOfflineBookingsCount()
    }

    LaunchedEffect(bookingListState) {
        if (bookingListState is BookingListState.Success) {
            val bookings = (bookingListState as BookingListState.Success).bookings
            bookings.forEach { booking ->
                bookingViewModel.checkCanReview(booking.bookingId)
            }
        }
    }

    Scaffold(
        bottomBar = { MyBottomBar(navController) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            WorldBackground(modifier = Modifier.align(Alignment.TopCenter))

            Column(modifier = Modifier.fillMaxSize()) {
                BookingTopBar(
                    title = "Booking History",
                    onBackClick = { navController.popBackStack() }
                )

                if (offlineCount > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📄", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "$offlineCount bookings pending sync",
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Will sync when internet is available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                if (paymentState is PaymentState.Error) {
                    PaymentErrorCard(
                        error = (paymentState as PaymentState.Error).message,
                        onDismiss = { paymentViewModel.clearError() }
                    )
                }

                when (val state = bookingListState) {
                    is BookingListState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is BookingListState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { bookingViewModel.loadMyBookings() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    is BookingListState.Success -> {
                        val bookings = state.bookings
                        if (bookings.isEmpty()) {
                            EmptyStateScreen("No bookings found.", modifier = Modifier.weight(1f))
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(items = bookings, key = { it.bookingId }) { item ->
                                    val flightModel = item.toFlightModel()
                                    val bookingEntity = item.toBookingEntity()
                                    val canReview = canReviewMap[item.bookingId] ?: false
                                    val showInvoice = bookingEntity.status == BookingStatus.PAID ||
                                            bookingEntity.status == BookingStatus.CONFIRMED

                                    BookingHistoryItem(
                                        flight = flightModel,
                                        booking = bookingEntity,
                                        onCancelClick = { b ->
                                            scope.launch {
                                                bookingViewModel.cancelOrDeleteBooking(b.id, b.status)
                                            }
                                        },
                                        onPayClick = { b ->
                                            paymentViewModel.clearError()
                                            pendingBookingId = b.id
                                            isPaymentSheetPresented = false
                                            paymentViewModel.processPayment(
                                                flight = flightModel,
                                                selectedSeats = emptyList(),
                                                bookingId = b.id
                                            )
                                        },
                                        onDeleteClick = { b ->
                                            scope.launch {
                                                bookingViewModel.cancelOrDeleteBooking(b.id, b.status)
                                            }
                                        },
                                        onReviewClick = {
                                            navController.navigateToCreateReview(item.bookingId)
                                        },
                                        onInvoiceClick = {
                                            navController.navigateToInvoiceDetail(item.bookingId)
                                        },
                                        onAirlineReviewsClick = {
                                            navController.navigateToAirlineReviews(flightModel.airlineName)
                                        },
                                        showReviewButton = canReview,
                                        showInvoiceButton = showInvoice,
                                        isPaying = pendingBookingId == item.bookingId && paymentState is PaymentState.Loading,
                                        isLoading = loadingBookingIds.contains(item.bookingId),
                                        paymentError = if (pendingBookingId == item.bookingId && paymentState is PaymentState.Error)
                                            (paymentState as PaymentState.Error).message else null,
                                        imageLoader = imageLoader
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
package com.wheezy.skyflight.feature.booking.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import com.wheezy.skyflight.core.common.contract.LoyaltyContract
import com.wheezy.skyflight.core.common.contract.SeatSelectionContract
import com.wheezy.skyflight.core.model.BookingStatus
import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.SeatStatus
import com.wheezy.skyflight.core.ui.components.BankLogo
import com.wheezy.skyflight.core.ui.components.GlassCard
import com.wheezy.skyflight.core.ui.components.GradientButton
import com.wheezy.skyflight.core.ui.components.WorldBackground
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.feature.booking.presentation.components.booking.LegendItem
import com.wheezy.skyflight.feature.booking.presentation.components.booking.SeatItem
import com.wheezy.skyflight.feature.booking.presentation.states.PaymentState
import com.wheezy.skyflight.feature.booking.presentation.viewmodels.BookingViewModel
import com.wheezy.skyflight.feature.booking.presentation.viewmodels.PaymentViewModel
import com.wheezy.common.state.PointsBalanceState
import com.wheezy.skyflight.navigation.Screen
import com.wheezy.skyflight.navigation.navigateAndClearStack
import kotlinx.coroutines.launch
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedBookingScreen(
    navController: NavController,
    flight: FlightModel,
    seatSelection: SeatSelectionContract,
    loyalty: LoyaltyContract,
    bookingViewModel: BookingViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    val seatList by seatSelection.seatList.collectAsState()
    val selectedSeats by seatSelection.selectedSeats.collectAsState()
    val totalPrice by seatSelection.totalPrice.collectAsState()
    val reservedSeats by seatSelection.reservedSeats.collectAsState()

    val paymentState by paymentViewModel.paymentState.collectAsState()
    val pointsBalanceState by loyalty.pointsBalanceState.collectAsState()
    val calculatedDiscountValue by loyalty.calculatedDiscount.collectAsState()
    val isLoyaltyLoading by loyalty.isLoading.collectAsState()
    val isCreatingBooking by bookingViewModel.isCreatingBooking.collectAsState()

    var usePoints by remember { mutableStateOf(false) }
    var pointsToUse by remember { mutableIntStateOf(0) }
    var showPointsDialog by remember { mutableStateOf(false) }
    var isPaymentSheetPresented by remember { mutableStateOf(false) }
    var currentBookingId by remember { mutableStateOf<Long?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val finalAmount = remember(totalPrice, usePoints, pointsToUse, calculatedDiscountValue) {
        val discount = calculatedDiscountValue
        if (usePoints && discount != null) {
            discount.finalAmount
        } else {
            totalPrice.multiply(java.math.BigDecimal(100)).toLong()
        }
    }

    val paymentSheet = rememberPaymentSheet(
        paymentResultCallback = { result ->
            isPaymentSheetPresented = false
            when (result) {
                is PaymentSheetResult.Completed -> {
                    if (currentBookingId != null && usePoints && pointsToUse > 0) {
                        loyalty.redeemPoints(pointsToUse, currentBookingId!!) {
                            paymentViewModel.handleSuccess()
                            bookingViewModel.updateBookingStatus(currentBookingId!!, BookingStatus.PAID)
                            showSuccessDialog = true
                        }
                    } else if (currentBookingId != null) {
                        scope.launch {
                            bookingViewModel.updateBookingStatus(currentBookingId!!, BookingStatus.PAID)
                            showSuccessDialog = true
                        }
                    }
                }
                is PaymentSheetResult.Canceled -> {
                    paymentViewModel.handleCancel()
                }
                is PaymentSheetResult.Failed -> {
                    paymentViewModel.handleFailure(result.error.localizedMessage ?: "Payment failed")
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        seatSelection.selectFlight(flight)
        loyalty.loadPointsBalance()
    }

    LaunchedEffect(usePoints, pointsToUse, totalPrice) {
        if (usePoints && pointsToUse > 0) {
            val amountInCents = totalPrice.multiply(java.math.BigDecimal(100)).toLong()
            loyalty.calculateDiscount(amountInCents, pointsToUse)
        } else {
            loyalty.clearCalculatedDiscount()
        }
    }

    LaunchedEffect(currentBookingId, finalAmount) {
        if (currentBookingId != null && finalAmount > 0 && paymentState == PaymentState.Idle) {
            paymentViewModel.processPaymentWithAmount(
                bookingId = currentBookingId!!,
                amount = finalAmount
            )
        }
    }

    LaunchedEffect(paymentState) {
        if (paymentState is PaymentState.Prepared && !isPaymentSheetPresented) {
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

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Payment Successful!") },
            text = { Text("Your booking has been confirmed. Check your email for details.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    navController.navigateAndClearStack(Screen.Main.route)
                }) {
                    Text("OK")
                }
            }
        )
    }

    if (showPointsDialog) {
        when (val state = pointsBalanceState) {
            is PointsBalanceState.Success -> {
                val balance = state.data
                AlertDialog(
                    onDismissRequest = { showPointsDialog = false },
                    title = { Text("Use Loyalty Points") },
                    text = {
                        Column {
                            Text("Available points: ${balance.balance}")
                            Text("100 points = 100 discount")
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = pointsToUse.toString(),
                                onValueChange = {
                                    pointsToUse =
                                        it.toIntOrNull()
                                            ?.coerceIn(0, balance.balance)
                                            ?: 0
                                },
                                label = { Text("Points to use") },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Discount: ${(pointsToUse / 100) * 100}",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (pointsToUse > 0) {
                                    usePoints = true
                                }
                                showPointsDialog = false
                            }
                        ) {
                            Text("Apply")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showPointsDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complete Booking") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            WorldBackground(modifier = Modifier.align(Alignment.TopCenter))
            Column(modifier = Modifier.fillMaxSize()) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "${flight.airlineName} - ${flight.classSeat}",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${flight.departureCity} → ${flight.arrivalCity}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${flight.flightDate} at ${flight.departureTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(seatList) { seatItem ->
                        val status = when {
                            reservedSeats.contains(seatItem.name) -> SeatStatus.UNAVAILABLE
                            selectedSeats.any { it.name == seatItem.name } -> SeatStatus.SELECTED
                            seatItem.status == SeatStatus.EMPTY -> SeatStatus.EMPTY
                            else -> SeatStatus.AVAILABLE
                        }
                        SeatItem(
                            seat = seatItem.copy(status = status),
                            onSeatClick = {
                                if (status == SeatStatus.AVAILABLE || status == SeatStatus.SELECTED) {
                                    seatSelection.selectSeat(seatItem)
                                }
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem("Available", MaterialTheme.colorScheme.primary)
                    LegendItem("Selected", MaterialTheme.colorScheme.tertiary)
                    LegendItem("Taken", MaterialTheme.colorScheme.surfaceVariant)
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${selectedSeats.size} seat(s) selected",
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                Text(
                                    text = selectedSeats.joinToString { it.name },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${totalPrice.setScale(0, RoundingMode.HALF_UP)}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BankLogo()
                            Text(
                                text = "Secure payment via Stripe",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        when (val state = pointsBalanceState) {
                            is PointsBalanceState.Success -> {
                                val balance = state.data
                                if (balance.balance > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Column {
                                                Text(
                                                    text = "Points balance: ${balance.balance}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                if (usePoints) {
                                                    calculatedDiscountValue?.let { discount ->
                                                        Text(
                                                            text = "Discount applied: ${discount.discountAmount / 100}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.tertiary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        TextButton(
                                            onClick = { showPointsDialog = true },
                                            enabled = !isLoyaltyLoading
                                        ) {
                                            Text(if (usePoints) "Change" else "Use points")
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        GradientButton(
                            onClick = {
                                if (selectedSeats.isEmpty()) {
                                    SnackbarHelper.showError("Please select at least one seat")
                                    return@GradientButton
                                }
                                scope.launch {
                                    bookingViewModel.createBooking(flight, selectedSeats) { success, bookingId ->
                                        if (success && bookingId != null) {
                                            currentBookingId = bookingId
                                        } else {
                                            SnackbarHelper.showError("Failed to create booking")
                                        }
                                    }
                                }
                            },
                            text = when {
                                isCreatingBooking -> "Creating booking..."
                                paymentState is PaymentState.Loading -> "Processing payment..."
                                isLoyaltyLoading -> "Processing points..."
                                else -> {
                                    val displayAmount = (finalAmount / 100)
                                    if (usePoints && calculatedDiscountValue != null) {
                                        "Pay $displayAmount (was ${totalPrice.toInt()})"
                                    } else {
                                        "Pay $displayAmount"
                                    }
                                }
                            },
                            enabled = selectedSeats.isNotEmpty() &&
                                    !isCreatingBooking &&
                                    !isLoyaltyLoading &&
                                    paymentState !is PaymentState.Loading &&
                                    paymentState !is PaymentState.Prepared
                        )

                        if (paymentState is PaymentState.Error) {
                            Text(
                                text = (paymentState as PaymentState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
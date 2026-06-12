package com.wheezy.skyflight.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.ImageLoader
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import com.wheezy.skyflight.core.common.utils.DebounceHelper
import com.wheezy.skyflight.core.model.BookingStatus
import com.wheezy.skyflight.core.ui.components.EmptyStateScreen
import com.wheezy.skyflight.core.ui.components.GlassCard
import com.wheezy.skyflight.core.ui.components.GradientButton
import com.wheezy.skyflight.core.ui.components.MyBottomBar
import com.wheezy.skyflight.core.ui.components.WorldBackground
import com.wheezy.skyflight.feature.booking.presentation.components.BookingTopBar
import com.wheezy.skyflight.feature.booking.presentation.components.PaymentErrorCard
import com.wheezy.skyflight.feature.booking.presentation.components.TicketDetailContent
import com.wheezy.skyflight.feature.booking.presentation.components.WeatherCard
import com.wheezy.skyflight.feature.booking.presentation.components.WeatherCardSkeleton
import com.wheezy.skyflight.feature.booking.presentation.states.PaymentState
import com.wheezy.skyflight.feature.booking.presentation.viewmodels.BookingViewModel
import com.wheezy.skyflight.feature.booking.presentation.viewmodels.PaymentViewModel
import com.wheezy.skyflight.feature.booking.presentation.viewmodels.WeatherViewModel
import com.wheezy.skyflight.feature.loyalty.presentation.states.PointsBalanceState
import com.wheezy.skyflight.feature.loyalty.presentation.states.RedeemPointsState
import com.wheezy.skyflight.feature.loyalty.presentation.viewmodels.LoyaltyViewModel
import com.wheezy.skyflight.navigation.Screen
import com.wheezy.skyflight.navigation.navigateAndClearStack
import kotlinx.coroutines.launch

@Composable
fun TicketDetailScreen(
    navController: NavHostController,
    onBackClick: () -> Unit
) {
    val bookingViewModel: BookingViewModel = hiltViewModel()
    val paymentViewModel: PaymentViewModel = hiltViewModel()
    val weatherViewModel: WeatherViewModel = hiltViewModel()
    val loyaltyViewModel: LoyaltyViewModel = hiltViewModel()

    val scope = rememberCoroutineScope()

    val selectedFlightState by bookingViewModel.selectedFlight.collectAsState()
    val selectedSeatsState by bookingViewModel.selectedSeats.collectAsState()
    val bookingIdState by bookingViewModel.bookingId.collectAsState()
    val paymentState by paymentViewModel.paymentState.collectAsState()
    val weatherState by weatherViewModel.weatherState.collectAsState()
    val pointsBalanceState by loyaltyViewModel.pointsBalanceState.collectAsState()
    val calculatedDiscount by loyaltyViewModel.calculatedDiscount.collectAsState()
    val redeemPointsState by loyaltyViewModel.redeemPointsState.collectAsState()

    val selectedFlight = selectedFlightState
    val selectedSeats = selectedSeatsState
    val bookingId = bookingIdState

    val payDebounce = remember { DebounceHelper(500L) }
    var isPaymentSheetPresented by remember { mutableStateOf(false) }
    var usePoints by remember { mutableStateOf(false) }
    var pointsToUse by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    val imageLoader = remember { ImageLoader.Builder(context).build() }

    LaunchedEffect(Unit) {
        loyaltyViewModel.loadPointsBalance()
    }

    LaunchedEffect(usePoints, pointsToUse, selectedFlight, selectedSeats) {
        if (usePoints && pointsToUse > 0 && selectedFlight != null && selectedSeats.isNotEmpty()) {
            val amount = selectedFlight.price.multiply(selectedSeats.size.toBigDecimal())
                .multiply(100.toBigDecimal()).toLong()
            loyaltyViewModel.calculateDiscount(amount, pointsToUse)
        } else {
            loyaltyViewModel.clearCalculatedDiscount()
        }
    }

    LaunchedEffect(redeemPointsState) {
        if (redeemPointsState is RedeemPointsState.Error) {
            loyaltyViewModel.clearRedeemState()
        }
    }

    val paymentSheet = rememberPaymentSheet(
        paymentResultCallback = { result ->
            isPaymentSheetPresented = false
            when (result) {
                is PaymentSheetResult.Completed -> {
                    if (usePoints && bookingId != null) {
                        val discount = calculatedDiscount
                        if (discount != null) {
                            loyaltyViewModel.redeemPoints(pointsToUse, bookingId) {
                                paymentViewModel.handleSuccess()
                                bookingViewModel.updateBookingStatus(bookingId, BookingStatus.PAID)
                                bookingViewModel.clearBooking()
                                navController.navigateAndClearStack(Screen.Main.route)
                            }
                        } else {
                            paymentViewModel.handleSuccess()
                            bookingViewModel.updateBookingStatus(bookingId, BookingStatus.PAID)
                            bookingViewModel.clearBooking()
                            navController.navigateAndClearStack(Screen.Main.route)
                        }
                    } else {
                        paymentViewModel.handleSuccess()
                        if (bookingId != null) {
                            scope.launch {
                                bookingViewModel.updateBookingStatus(bookingId, BookingStatus.PAID)
                                bookingViewModel.clearBooking()
                            }
                        }
                        navController.navigateAndClearStack(Screen.Main.route)
                    }
                }
                is PaymentSheetResult.Canceled -> {
                    paymentViewModel.handleCancel()
                    loyaltyViewModel.clearRedeemState()
                }
                is PaymentSheetResult.Failed -> {
                    paymentViewModel.handleFailure(result.error.localizedMessage ?: "Payment failed")
                    loyaltyViewModel.clearRedeemState()
                }
                else -> {
                    paymentViewModel.handleFailure("Unknown payment error")
                    loyaltyViewModel.clearRedeemState()
                }
            }
        }
    )

    LaunchedEffect(selectedFlight) {
        if (selectedFlight != null) {
            weatherViewModel.loadWeather(selectedFlight.arrivalCity)
        }
    }

    LaunchedEffect(bookingId, selectedSeats, selectedFlight) {
        if (bookingId != null && selectedSeats.isNotEmpty() && selectedFlight != null && paymentState == PaymentState.Idle) {
            paymentViewModel.processPayment(selectedFlight, selectedSeats, bookingId)
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

    Scaffold(
        bottomBar = { MyBottomBar(navController) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            WorldBackground(modifier = Modifier.align(Alignment.TopCenter))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                BookingTopBar(
                    title = "Ticket Detail",
                    onBackClick = onBackClick,
                    modifier = Modifier.padding(top = 16.dp)
                )

                if (paymentState is PaymentState.Error) {
                    PaymentErrorCard(
                        error = (paymentState as PaymentState.Error).message,
                        onDismiss = { paymentViewModel.clearError() }
                    )
                }

                if (redeemPointsState is RedeemPointsState.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                if (selectedFlight != null) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        blurRadius = 25f,
                        tintAlpha = 0.2f,
                        strokeAlpha = 0.2f,
                        cornerRadius = 24.dp,
                        enableGlow = true
                    ) {
                        TicketDetailContent(
                            flightModel = selectedFlight,
                            selectedSeats = selectedSeats,
                            modifier = Modifier.fillMaxWidth(),
                            imageLoader = imageLoader
                        )
                    }

                    // Loyalty Points Section
                    when (val state = pointsBalanceState) {
                        is PointsBalanceState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            }
                        }
                        is PointsBalanceState.Success -> {
                            val availablePoints = state.data.balance
                            if (availablePoints > 0) {
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 8.dp),
                                    blurRadius = 20f,
                                    tintAlpha = 0.15f,
                                    strokeAlpha = 0.2f,
                                    cornerRadius = 16.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "Loyalty Points",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "Balance: $availablePoints points",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Switch(
                                                checked = usePoints,
                                                onCheckedChange = { checked ->
                                                    usePoints = checked
                                                    if (checked) {
                                                        pointsToUse = availablePoints
                                                    } else {
                                                        pointsToUse = 0
                                                        loyaltyViewModel.clearCalculatedDiscount()
                                                    }
                                                },
                                                enabled = redeemPointsState !is RedeemPointsState.Loading,
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                                )
                                            )
                                        }

                                        val discount = calculatedDiscount
                                        if (usePoints && discount != null) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            HorizontalDivider()
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Points used:",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = "$pointsToUse",
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Discount:",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = "$${discount.discountAmount / 100}",
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.tertiary
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Final amount:",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                                )
                                                Text(
                                                    text = "$${discount.finalAmount / 100}",
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        if (redeemPointsState is RedeemPointsState.Error) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = (redeemPointsState as RedeemPointsState.Error).message,
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        is PointsBalanceState.Error -> {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Weather Section
                    when (val state = weatherState) {
                        is WeatherViewModel.WeatherUiState.Loading -> {
                            WeatherCardSkeleton()
                        }
                        is WeatherViewModel.WeatherUiState.Success -> {
                            WeatherCard(
                                weather = state.weather,
                                forecast = state.forecast,
                                isStale = state.isCached,
                                onRefresh = { weatherViewModel.refresh(selectedFlight.arrivalCity) }
                            )
                        }
                        is WeatherViewModel.WeatherUiState.Error -> {
                            val cachedData = state.cachedData
                            if (cachedData != null) {
                                WeatherCard(
                                    weather = cachedData,
                                    forecast = null,
                                    isStale = true,
                                    onRefresh = { weatherViewModel.refresh(selectedFlight.arrivalCity) }
                                )
                            } else {
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(24.dp)
                                )
                            }
                        }
                        else -> {
                            WeatherCardSkeleton()
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    GradientButton(
                        onClick = {
                            paymentViewModel.clearError()
                            loyaltyViewModel.clearRedeemState()
                            payDebounce.debounce {
                                if (paymentState is PaymentState.Prepared && !isPaymentSheetPresented) {
                                    val prepared = paymentState as PaymentState.Prepared
                                    try {
                                        isPaymentSheetPresented = true
                                        paymentSheet.presentWithPaymentIntent(
                                            prepared.clientSecret,
                                            PaymentSheet.Configuration(
                                                merchantDisplayName = "SkyFlight",
                                                customer = PaymentSheet.CustomerConfiguration(
                                                    prepared.customerId,
                                                    prepared.ephemeralKey
                                                )
                                            )
                                        )
                                    } catch (e: Exception) {
                                        isPaymentSheetPresented = false
                                        paymentViewModel.handleFailure(e.message ?: "Failed to open payment sheet")
                                    }
                                }
                            }
                        },
                        text = when {
                            paymentState is PaymentState.Loading -> "Processing..."
                            redeemPointsState is RedeemPointsState.Loading -> "Redeeming points..."
                            usePoints && calculatedDiscount != null -> {
                                val discount = calculatedDiscount
                                if (discount != null) "Pay $${discount.finalAmount / 100}" else "Pay for ticket"
                            }
                            else -> "Pay for ticket"
                        },
                        enabled = paymentState is PaymentState.Prepared &&
                                !isPaymentSheetPresented &&
                                redeemPointsState !is RedeemPointsState.Loading,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                } else {
                    EmptyStateScreen(
                        message = "Ticket data is missing. Please select a flight.",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
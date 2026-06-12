package com.wheezy.skyflight.feature.booking.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import com.wheezy.skyflight.core.model.SeatStatus
import com.wheezy.skyflight.core.ui.R
import com.wheezy.skyflight.core.ui.components.WorldBackground
import com.wheezy.skyflight.core.common.utils.DebounceHelper
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.feature.booking.presentation.components.BookingLoadingOverlay
import com.wheezy.skyflight.feature.booking.presentation.components.BookingTopBar
import com.wheezy.skyflight.feature.booking.presentation.components.LegendItem
import com.wheezy.skyflight.feature.booking.presentation.components.SeatItem
import com.wheezy.skyflight.feature.booking.presentation.states.SeatSelectionState
import com.wheezy.skyflight.feature.booking.presentation.viewmodels.BookingViewModel
import com.wheezy.skyflight.feature.booking.presentation.viewmodels.SeatSelectionViewModel
import com.wheezy.skyflight.navigation.Screen
import com.wheezy.skyflight.core.ui.components.GlassCard
import com.wheezy.skyflight.core.ui.components.GlassCardDefaults
import com.wheezy.skyflight.core.ui.components.GradientButton
import java.math.RoundingMode

@Composable
fun SeatListScreen(
    navController: NavController,
    flightId: Long,
    onBackClick: () -> Unit,
    seatSelectionViewModel: SeatSelectionViewModel = hiltViewModel(),
    bookingViewModel: BookingViewModel = hiltViewModel()
) {
    if (flightId <= 0) {
        LaunchedEffect(Unit) {
            onBackClick()
        }
        return
    }

    val context = LocalContext.current
    val imageLoader = remember { ImageLoader.Builder(context).build() }

    LaunchedEffect(Unit) {
        seatSelectionViewModel.loadFlight(flightId)
    }

    val seatSelectionState by seatSelectionViewModel.seatSelectionState.collectAsState()
    val selectedFlight by seatSelectionViewModel.selectedFlight.collectAsState()
    val seatList by seatSelectionViewModel.seatList.collectAsState()
    val selectedSeats by seatSelectionViewModel.selectedSeats.collectAsState()
    val totalPrice by seatSelectionViewModel.totalPrice.collectAsState()
    val reservedSeats by seatSelectionViewModel.reservedSeats.collectAsState()
    val isCreatingBooking by bookingViewModel.isCreatingBooking.collectAsState()

    val confirmDebounce = remember { DebounceHelper(500L) }
    val colors = MaterialTheme.colorScheme

    LaunchedEffect(seatSelectionState) {
        if (seatSelectionState is SeatSelectionState.Error) {
            SnackbarHelper.showError((seatSelectionState as SeatSelectionState.Error).message)
        }
    }

    when (seatSelectionState) {
        is SeatSelectionState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is SeatSelectionState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = (seatSelectionState as SeatSelectionState.Error).message,
                        color = colors.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { seatSelectionViewModel.loadFlight(flightId) }) {
                        Text("Retry")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onBackClick) {
                        Text("Go Back")
                    }
                }
            }
        }
        is SeatSelectionState.Success -> {
            Box(modifier = Modifier.fillMaxSize()) {
                WorldBackground(modifier = Modifier.align(Alignment.TopCenter))

                selectedFlight?.let { flight ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopCenter),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = flight.fullLogoUrl,
                                contentDescription = flight.airlineName,
                                imageLoader = imageLoader,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${flight.airlineName} - ${flight.classSeat}",
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                Text(
                                    text = "${flight.departureCity} → ${flight.arrivalCity}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "${flight.departureTime} - ${flight.arriveTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                    val (topSection, middleSection, bottomSection) = createRefs()

                    BookingTopBar(
                        title = "Select Seats",
                        onBackClick = onBackClick,
                        modifier = Modifier.constrainAs(topSection) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                    )

                    ConstraintLayout(
                        modifier = Modifier
                            .padding(top = 100.dp)
                            .constrainAs(middleSection) {
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                    ) {
                        val (airplane, seatGrid, legendRow) = createRefs()

                        Image(
                            painter = painterResource(id = R.drawable.airple_seat),
                            contentDescription = null,
                            modifier = Modifier.constrainAs(airplane) {
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier
                                .padding(top = 240.dp, start = 64.dp, end = 64.dp)
                                .constrainAs(seatGrid) {
                                    top.linkTo(parent.top)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }
                        ) {
                            items(items = seatList) { seatItem ->
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
                                            seatSelectionViewModel.selectSeat(seatItem)
                                        }
                                    }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .constrainAs(legendRow) {
                                    top.linkTo(seatGrid.bottom, margin = 16.dp)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                },
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            LegendItem("Available", colors.primary)
                            LegendItem("Selected", colors.tertiary)
                            LegendItem("Unavailable", colors.surfaceVariant)
                        }
                    }

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .constrainAs(bottomSection) {
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            },
                        config = GlassCardDefaults.medium
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${selectedSeats.size} seats selected",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = colors.onSurface,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = if (selectedSeats.isEmpty()) "-" else selectedSeats.joinToString { it.name },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = colors.onSurface
                                        )
                                    )
                                }

                                Text(
                                    text = "$${totalPrice.setScale(0, RoundingMode.HALF_UP)}",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        color = colors.tertiary,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            GradientButton(
                                onClick = {
                                    confirmDebounce.debounce {
                                        if (selectedSeats.isNotEmpty() && !isCreatingBooking) {
                                            selectedFlight?.let { flight ->
                                                bookingViewModel.createBooking(flight, selectedSeats) { success, id ->
                                                    if (success) {
                                                        bookingViewModel.setFlight(flight)
                                                        bookingViewModel.setSelectedSeats(selectedSeats)
                                                        bookingViewModel.setBookingId(id)
                                                        navController.navigate(Screen.TicketDetail.route)
                                                    }
                                                }
                                            }
                                        } else if (selectedSeats.isEmpty()) {
                                            SnackbarHelper.showError("Please select at least one seat")
                                        }
                                    }
                                },
                                text = "Confirm seats"
                            )
                        }
                    }
                }

                if (isCreatingBooking) {
                    BookingLoadingOverlay(message = "Creating booking...")
                }
            }
        }
    }
}
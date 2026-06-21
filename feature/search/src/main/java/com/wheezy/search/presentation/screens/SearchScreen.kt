package com.wheezy.skyflight.feature.search.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.wheezy.skyflight.core.model.Notification
import com.wheezy.skyflight.core.model.SeatSelectionState
import com.wheezy.skyflight.core.model.ThemeOption
import com.wheezy.skyflight.core.model.User
import com.wheezy.skyflight.core.ui.R
import com.wheezy.skyflight.core.ui.components.*
import com.wheezy.skyflight.feature.search.presentation.states.ClassSeatsUiState
import com.wheezy.skyflight.feature.search.presentation.states.FlightsUiState
import com.wheezy.skyflight.feature.search.presentation.states.SearchUiState
import com.wheezy.skyflight.feature.search.presentation.viewmodels.SearchViewModel
import com.wheezy.skyflight.navigation.navigateToAirlineReviews
import io.github.muddz.styleabletoast.StyleableToast

@Composable
fun SearchScreen(
    userState: User?,
    currentTheme: ThemeOption,
    notifications: List<Notification>,
    unreadCount: Int,
    onThemeChanged: (ThemeOption) -> Unit,
    onLogout: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToResult: (from: String, to: String, passengers: Int, selectedClass: String) -> Unit,
    bottomBar: @Composable () -> Unit,
    onClearAllNotifications: () -> Unit,
    searchViewModel: SearchViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val locationsState by searchViewModel.locationsState.collectAsState()
    val classSeatsState by searchViewModel.classSeatsState.collectAsState()
    val flightsState by searchViewModel.flightsState.collectAsState()
    val seatSelectionState by searchViewModel.seatSelectionState.collectAsState()

    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("") }
    var adultPassenger by remember { mutableStateOf("1") }
    var childPassenger by remember { mutableStateOf("0") }
    var isDrawerOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var isSeatLoading by remember { mutableStateOf(false) }
    var seatError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        searchViewModel.fetchLocations()
        searchViewModel.fetchClassSeats()
    }

    LaunchedEffect(seatSelectionState) {
        when (seatSelectionState) {
            is SeatSelectionState.Loading -> {
                isSeatLoading = true
                seatError = null
            }
            is SeatSelectionState.Success -> {
                isSeatLoading = false
                seatError = null
            }
            is SeatSelectionState.Error -> {
                isSeatLoading = false
                seatError = (seatSelectionState as SeatSelectionState.Error).message
                StyleableToast.makeText(
                    context,
                    (seatSelectionState as SeatSelectionState.Error).message,
                    R.style.errorToast
                ).show()
            }
        }
    }

    val locations = (locationsState as? SearchUiState.Success)?.locations.orEmpty()
    val locationNames = remember(locations) { locations.map { it.name } }
    val classItems = (classSeatsState as? ClassSeatsUiState.Success)?.classSeats.orEmpty()

    SideDrawer(
        isOpen = isDrawerOpen,
        onClose = { isDrawerOpen = false },
        currentTheme = currentTheme,
        onThemeSelected = onThemeChanged,
        onLogout = onLogout,
        onOpenBookingHistory = onNavigateToHistory
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    user = userState,
                    notifications = notifications,
                    unreadCount = unreadCount,
                    onOpenDrawer = { isDrawerOpen = true },
                    onClearAllNotifications = onClearAllNotifications
                )
            },
            bottomBar = bottomBar
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
                        config = GlassCardDefaults.heavy
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                            YellowTitle(text = "From")
                            when (locationsState) {
                                is SearchUiState.Loading -> {
                                    GlassCard(modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(56.dp)) {
                                        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                                    }
                                }
                                is SearchUiState.Error -> {
                                    GlassCard(modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(56.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = (locationsState as SearchUiState.Error).message,
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 12.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            TextButton(onClick = { searchViewModel.fetchLocations() }) {
                                                Text("Retry", color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                                is SearchUiState.Success -> {
                                    DropDownMenu(
                                        items = locationNames,
                                        leadingIcon = Icons.Default.FlightTakeoff,
                                        hint = "Select departure city",
                                        showLocationLoading = false,
                                        onItemSelected = { from = it }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            YellowTitle(text = "To")
                            when (locationsState) {
                                is SearchUiState.Loading -> {
                                    GlassCard(modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(56.dp)) {
                                        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                                    }
                                }
                                is SearchUiState.Error -> {
                                    GlassCard(modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(56.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = (locationsState as SearchUiState.Error).message,
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 12.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            TextButton(onClick = { searchViewModel.fetchLocations() }) {
                                                Text("Retry", color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                                is SearchUiState.Success -> {
                                    DropDownMenu(
                                        items = locationNames,
                                        leadingIcon = Icons.Default.FlightLand,
                                        hint = "Select arrival city",
                                        showLocationLoading = false,
                                        onItemSelected = { to = it }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            YellowTitle(text = "Passengers")
                            Row(modifier = Modifier.fillMaxWidth()) {
                                PassengerCounter(
                                    title = "Adult",
                                    modifier = Modifier.weight(1f),
                                    onItemSelected = { adultPassenger = it }
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                PassengerCounter(
                                    title = "Child",
                                    modifier = Modifier.weight(1f),
                                    onItemSelected = { childPassenger = it }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row {
                                YellowTitle(text = "Departure Date", modifier = Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(16.dp))
                                YellowTitle(text = "Return Date", modifier = Modifier.weight(1f))
                            }
                            DatePickerScreen(modifier = Modifier.fillMaxWidth())

                            Spacer(modifier = Modifier.height(16.dp))

                            YellowTitle(text = "Class")
                            when (classSeatsState) {
                                is ClassSeatsUiState.Loading -> {
                                    GlassCard(modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(56.dp)) {
                                        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                                    }
                                }
                                is ClassSeatsUiState.Error -> {
                                    GlassCard(modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(56.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = (classSeatsState as ClassSeatsUiState.Error).message,
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 12.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            TextButton(onClick = { searchViewModel.fetchClassSeats() }) {
                                                Text("Retry", color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                                is ClassSeatsUiState.Success -> {
                                    if (classItems.isEmpty()) {
                                        GlassCard(modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(56.dp)) {
                                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                                Text("No classes available", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    } else {
                                        var expanded by remember { mutableStateOf(false) }
                                        var selectedClassItem by remember { mutableStateOf("") }

                                        GlassCard(
                                            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                                            blurRadius = 12f,
                                            tintAlpha = 0.1f,
                                            cornerRadius = 10.dp
                                        ) {
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(56.dp)
                                                        .clickable { expanded = true }
                                                        .padding(horizontal = 16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Chair,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(
                                                        text = selectedClassItem.ifEmpty { "Select class" },
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowDropDown,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                DropdownMenu(
                                                    expanded = expanded,
                                                    onDismissRequest = { expanded = false },
                                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                                ) {
                                                    classItems.forEach { cls ->
                                                        DropdownMenuItem(
                                                            text = {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Chair,
                                                                        contentDescription = null,
                                                                        modifier = Modifier.size(18.dp),
                                                                        tint = MaterialTheme.colorScheme.primary
                                                                    )
                                                                    Spacer(modifier = Modifier.width(8.dp))
                                                                    Text(cls)
                                                                }
                                                            },
                                                            onClick = {
                                                                selectedClassItem = cls
                                                                selectedClass = cls
                                                                expanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            GradientButton(
                                onClick = {
                                    val numPassenger = (adultPassenger.toIntOrNull() ?: 0) + (childPassenger.toIntOrNull() ?: 0)
                                    if (from.isNotBlank() && to.isNotBlank() && numPassenger > 0) {
                                        onNavigateToResult(from, to, numPassenger, selectedClass)
                                    } else {
                                        StyleableToast.makeText(
                                            context,
                                            "Please select valid locations and at least one passenger",
                                            R.style.errorToast
                                        ).show()
                                    }
                                },
                                text = "Search"
                            )
                        }
                    }

                    if (isSeatLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Loading seats...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    if (seatError != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = seatError ?: "Failed to load seats",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = {
                                        // Retry loading seats
                                    }
                                ) {
                                    Text("Retry", color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                    }

                    when (val state = flightsState) {
                        is FlightsUiState.Success -> {
                            val flights = state.flights
                            if (flights.isNotEmpty()) {
                                val firstFlight = flights.first()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    TextButton(
                                        onClick = {
                                            navController.navigateToAirlineReviews(firstFlight.airlineName)
                                        }
                                    ) {
                                        Text(
                                            text = "View All ${firstFlight.airlineName} Reviews",
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
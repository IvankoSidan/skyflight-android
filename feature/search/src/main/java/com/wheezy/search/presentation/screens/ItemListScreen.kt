package com.wheezy.skyflight.feature.search.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.ImageLoader
import com.wheezy.skyflight.core.ui.components.BackButton
import com.wheezy.skyflight.core.ui.components.DropDownMenu
import com.wheezy.skyflight.core.ui.components.EmptyStateScreen
import com.wheezy.skyflight.core.ui.components.GlassCard
import com.wheezy.skyflight.core.ui.components.MyBottomBar
import com.wheezy.skyflight.core.ui.components.WorldBackground
import com.wheezy.skyflight.feature.search.presentation.components.FlightItem
import com.wheezy.skyflight.feature.search.presentation.states.FlightsUiState
import com.wheezy.skyflight.feature.search.presentation.viewmodels.SearchParamsViewModel
import com.wheezy.skyflight.feature.search.presentation.viewmodels.SearchViewModel

@Composable
fun ItemListScreen(
    navController: NavHostController,
    searchViewModel: SearchViewModel = hiltViewModel(),
    searchParamsViewModel: SearchParamsViewModel
) {
    val from by searchParamsViewModel.from.collectAsState()
    val to by searchParamsViewModel.to.collectAsState()
    val passengers by searchParamsViewModel.passengers.collectAsState()
    val selectedClass by searchParamsViewModel.selectedClass.collectAsState()

    val flightsState by searchViewModel.flightsState.collectAsState()
    val classSeatsState by searchViewModel.classSeatsState.collectAsState()
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

    var showFilter by remember { mutableStateOf(false) }
    var filterClass by remember { mutableStateOf("") }
    var filteredFlights by remember { mutableStateOf<List<com.wheezy.skyflight.core.model.FlightModel>?>(null) }

    val classItems = (classSeatsState as? com.wheezy.skyflight.feature.search.presentation.states.ClassSeatsUiState.Success)?.classSeats.orEmpty()

    LaunchedEffect(from, to, selectedClass) {
        if (from.isNotBlank() && to.isNotBlank() && passengers > 0) {
            searchViewModel.searchFlights(from, to, selectedClass)
        }
    }

    LaunchedEffect(flightsState) {
        if (flightsState is FlightsUiState.Success) {
            filteredFlights = null
            filterClass = ""
        }
    }

    BackHandler {
        navController.popBackStack()
    }

    Scaffold(
        bottomBar = { MyBottomBar(navController) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            WorldBackground(modifier = Modifier.align(Alignment.TopCenter))

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(top = 8.dp)
                ) {
                    BackButton(onClick = { navController.popBackStack() })
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Search Results",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )

                    // Кнопка фильтрации с DropDownMenu
                    IconButton(onClick = { showFilter = !showFilter }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (showFilter && classItems.isNotEmpty()) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Filter by Class",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            DropDownMenu(
                                items = listOf("All") + classItems,
                                leadingIcon = Icons.Default.Chair,
                                hint = "Select class",
                                showLocationLoading = false,
                                onItemSelected = { selected ->
                                    filterClass = if (selected == "All") "" else selected
                                    val currentState = flightsState
                                    if (currentState is FlightsUiState.Success) {
                                        filteredFlights = if (filterClass.isNotEmpty()) {
                                            currentState.flights.filter { it.classSeat == filterClass }
                                        } else {
                                            currentState.flights
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                when {
                    from.isBlank() || to.isBlank() || passengers <= 0 -> {
                        EmptyStateScreen(
                            message = "Please enter search parameters on the main screen first.",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    flightsState is FlightsUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Searching for flights...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    flightsState is FlightsUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = (flightsState as FlightsUiState.Error).message,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { searchViewModel.searchFlights(from, to, selectedClass) }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    flightsState is FlightsUiState.Success -> {
                        val displayFlights = filteredFlights ?: (flightsState as FlightsUiState.Success).flights
                        if (displayFlights.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (filterClass.isNotEmpty()) "No flights found for $filterClass" else "No flights found",
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Try changing your search criteria",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp)
                            ) {
                                itemsIndexed(displayFlights) { _, flight ->
                                    FlightItem(
                                        item = flight,
                                        onFlightClick = { clickedFlight ->
                                            clickedFlight.flightId?.let { id ->
                                                navController.navigate("unified_booking/$id")
                                            }
                                        },
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
package com.wheezy.skyflight.feature.search.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import com.wheezy.skyflight.core.ui.components.BackButton
import com.wheezy.skyflight.core.ui.components.EmptyStateScreen
import com.wheezy.skyflight.core.ui.components.MyBottomBar
import com.wheezy.skyflight.core.ui.components.WorldBackground
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.feature.search.presentation.components.FlightItem
import com.wheezy.skyflight.feature.search.presentation.states.FlightsUiState
import com.wheezy.skyflight.feature.search.presentation.viewmodels.SearchParamsViewModel
import com.wheezy.skyflight.feature.search.presentation.viewmodels.SearchViewModel
import com.wheezy.skyflight.navigation.navigateToSelectSeat

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
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

    LaunchedEffect(from, to, selectedClass) {
        if (from.isNotBlank() && to.isNotBlank() && passengers > 0) {
            searchViewModel.searchFlights(
                from = from,
                to = to,
                classType = selectedClass
            )
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
                        color = MaterialTheme.colorScheme.onBackground
                    )
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
                                Button(
                                    onClick = {
                                        searchViewModel.searchFlights(from, to, selectedClass)
                                    }
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    flightsState is FlightsUiState.Success -> {
                        val flights = (flightsState as FlightsUiState.Success).flights
                        if (flights.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "No flights found",
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
                                itemsIndexed(flights) { _, flight ->
                                    val flightId = flight.flightId
                                    FlightItem(
                                        item = flight,
                                        onFlightClick = {
                                            if (flightId != null && flightId > 0) {
                                                navController.navigateToSelectSeat(flightId)
                                            } else {
                                                SnackbarHelper.showError("Invalid flight data")
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
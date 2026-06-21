package com.wheezy.skyflight.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wheezy.skyflight.core.common.state.CanReviewState
import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.SeatSelectionState
import com.wheezy.skyflight.core.model.ThemeOption
import com.wheezy.skyflight.core.ui.components.MyBottomBar
import com.wheezy.skyflight.core.ui.viewmodel.TopBarViewModel
import com.wheezy.skyflight.feature.auth.presentation.screens.LoginScreen
import com.wheezy.skyflight.feature.auth.presentation.screens.RegisterScreen
import com.wheezy.skyflight.feature.auth.presentation.viewmodels.AuthViewModel
import com.wheezy.skyflight.feature.booking.presentation.screens.BookingHistoryScreen
import com.wheezy.skyflight.feature.booking.presentation.screens.UnifiedBookingScreen
import com.wheezy.skyflight.feature.booking.presentation.viewmodels.BookingViewModel
import com.wheezy.skyflight.feature.cards.presentation.screens.SavedCardsScreen
import com.wheezy.skyflight.feature.invoice.presentation.screens.InvoiceDetailScreen
import com.wheezy.skyflight.feature.invoice.presentation.screens.InvoicesScreen
import com.wheezy.skyflight.feature.loyalty.presentation.screens.LoyaltyScreen
import com.wheezy.skyflight.feature.loyalty.presentation.screens.PointsHistoryScreen
import com.wheezy.skyflight.feature.loyalty.presentation.viewmodels.LoyaltyViewModel
import com.wheezy.skyflight.feature.notifications.presentation.screens.NotificationSettingsScreen
import com.wheezy.skyflight.feature.referral.presentation.screens.ReferralScreen
import com.wheezy.skyflight.feature.review.presentation.screens.AirlineReviewsScreen
import com.wheezy.skyflight.feature.review.presentation.screens.CreateReviewScreen
import com.wheezy.skyflight.feature.review.presentation.screens.FlightReviewsScreen
import com.wheezy.skyflight.feature.review.presentation.screens.MyReviewsScreen
import com.wheezy.skyflight.feature.review.presentation.viewmodels.ReviewViewModel
import com.wheezy.skyflight.feature.search.presentation.screens.ItemListScreen
import com.wheezy.skyflight.feature.search.presentation.screens.SearchScreen
import com.wheezy.skyflight.feature.search.presentation.viewmodels.SearchParamsViewModel
import com.wheezy.skyflight.feature.search.presentation.viewmodels.SearchViewModel
import com.wheezy.skyflight.navigation.Screen
import com.wheezy.skyflight.navigation.navigateAndClearStack
import com.wheezy.skyflight.presentation.screens.SplashScreen
import com.wheezy.skyflight.presentation.screens.TicketDetailScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    searchParamsViewModel: SearchParamsViewModel,
    topBarViewModel: TopBarViewModel,
    currentTheme: ThemeOption,
    onThemeChanged: (ThemeOption) -> Unit,
    paddingValues: PaddingValues
) {
    val userState by authViewModel.user.collectAsState()
    val notifications by topBarViewModel.notifications.collectAsState()
    val unreadCount by topBarViewModel.unreadCount.collectAsState()

    val startDestination = if (userState == null) Screen.Splash.route else Screen.Main.route

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    userState = userState,
                    onGetStartedClick = {
                        val destination = if (userState != null) Screen.Main.route else Screen.Login.route
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onAutoNavigate = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateBack = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onLoginSuccess = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.SavedCards.route) {
                SavedCardsScreen(navController = navController)
            }

            composable("unified_booking/{flightId}") { backStackEntry ->
                val flightId = backStackEntry.arguments?.getString("flightId")?.toLongOrNull() ?: 0L
                val searchViewModel: SearchViewModel = hiltViewModel()
                val loyaltyViewModel = hiltViewModel<LoyaltyViewModel>()
                var flight by remember { mutableStateOf<FlightModel?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                var error by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(flightId) {
                    if (flightId > 0) {
                        try {
                            searchViewModel.loadFlight(flightId)
                            searchViewModel.state.collect { currentState ->
                                flight = currentState.selectedFlight
                                isLoading = currentState.seatSelectionState is SeatSelectionState.Loading
                                if (currentState.seatSelectionState is SeatSelectionState.Error) {
                                    error = (currentState.seatSelectionState as SeatSelectionState.Error).message
                                }
                            }
                        } catch (e: Exception) {
                            error = e.message ?: "Failed to load flight"
                            isLoading = false
                        }
                    } else {
                        error = "Invalid flight ID"
                        isLoading = false
                    }
                }

                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    error != null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = error ?: "Unknown error")
                                Button(onClick = { navController.popBackStack() }) {
                                    Text("Go Back")
                                }
                            }
                        }
                    }
                    flight != null -> {
                        UnifiedBookingScreen(
                            navController = navController,
                            flight = flight!!,
                            seatSelection = searchViewModel,
                            loyalty = loyaltyViewModel
                        )
                    }
                    else -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Flight not found")
                        }
                    }
                }
            }

            composable(Screen.Main.route) {
                val searchViewModel: SearchViewModel = hiltViewModel()

                SearchScreen(
                    userState = userState,
                    currentTheme = currentTheme,
                    notifications = notifications,
                    unreadCount = unreadCount,
                    onThemeChanged = onThemeChanged,
                    onLogout = {
                        authViewModel.logout()
                        searchParamsViewModel.clear()
                        navController.navigateAndClearStack(Screen.Login.route)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.BookingHistory.route)
                    },
                    onNavigateToResult = { from, to, passengers, selectedClass ->
                        searchParamsViewModel.setParams(from, to, passengers, selectedClass)
                        navController.navigate(Screen.SearchResult.route)
                    },
                    bottomBar = { MyBottomBar(navController) },
                    onClearAllNotifications = { topBarViewModel.clearAll() },
                    searchViewModel = searchViewModel,
                    navController = navController
                )
            }

            composable(Screen.SearchResult.route) {
                ItemListScreen(
                    navController = navController,
                    searchParamsViewModel = searchParamsViewModel
                )
            }

            composable(Screen.InvoiceDetail.route) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId")?.toLongOrNull() ?: 0L
                if (bookingId > 0) {
                    InvoiceDetailScreen(
                        navController = navController,
                        bookingId = bookingId
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Invalid booking ID")
                    }
                }
            }

            composable(Screen.Invoices.route) {
                InvoicesScreen(navController = navController)
            }

            composable(Screen.TicketDetail.route) {
                TicketDetailScreen(
                    navController = navController,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.BookingHistory.route) {
                BookingHistoryScreen(
                    navController = navController
                )
            }

            composable(Screen.NotificationSettings.route) {
                NotificationSettingsScreen(
                    navController = navController
                )
            }

            composable(Screen.Referral.route) {
                ReferralScreen(
                    navController = navController
                )
            }

            composable(Screen.Loyalty.route) {
                LoyaltyScreen(navController = navController)
            }

            composable(Screen.PointsHistory.route) {
                PointsHistoryScreen(navController = navController)
            }

            composable(Screen.CreateReview.route) { backStackEntry ->
                val bookingId = backStackEntry.arguments?.getString("bookingId")?.toLongOrNull() ?: 0L
                val bookingViewModel: BookingViewModel = hiltViewModel()
                val reviewViewModel: ReviewViewModel = hiltViewModel()

                var flightModel by remember { mutableStateOf<FlightModel?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                var error by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(bookingId) {
                    if (bookingId > 0) {
                        reviewViewModel.checkCanReview(bookingId)
                        bookingViewModel.getBookingById(bookingId)
                            .onSuccess { bookingDetails ->
                                flightModel = bookingViewModel.getFlightById(bookingDetails.flightId)
                                isLoading = false
                            }
                            .onFailure { e ->
                                error = e.message ?: "Failed to load booking"
                                isLoading = false
                            }
                    }
                }

                val canReviewState = reviewViewModel.canReviewState.collectAsState().value

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = error ?: "Unknown error")
                                Button(
                                    onClick = { navController.popBackStack() }
                                ) {
                                    Text("Go Back")
                                }
                            }
                        }
                    }
                    canReviewState is CanReviewState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = canReviewState.message)
                                Button(
                                    onClick = { navController.popBackStack() }
                                ) {
                                    Text("Go Back")
                                }
                            }
                        }
                    }
                    canReviewState is CanReviewState.Success -> {
                        if (!canReviewState.canReview) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "You have already reviewed this flight")
                                    Button(
                                        onClick = { navController.popBackStack() }
                                    ) {
                                        Text("Go Back")
                                    }
                                }
                            }
                        } else if (flightModel != null) {
                            CreateReviewScreen(
                                navController = navController,
                                flight = flightModel!!,
                                bookingId = bookingId,
                                reviewViewModel = reviewViewModel
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Booking not found")
                            }
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            composable(Screen.FlightReviews.route) { backStackEntry ->
                val flightId = backStackEntry.arguments?.getString("flightId")?.toLongOrNull() ?: 0L
                val airlineName = backStackEntry.arguments?.getString("airlineName")?.let {
                    android.net.Uri.decode(it)
                } ?: ""

                FlightReviewsScreen(
                    navController = navController,
                    flightId = flightId,
                    airlineName = airlineName
                )
            }

            composable(Screen.AirlineReviews.route) { backStackEntry ->
                val airlineName = backStackEntry.arguments?.getString("airlineName")?.let {
                    android.net.Uri.decode(it)
                } ?: ""
                AirlineReviewsScreen(
                    navController = navController,
                    airlineName = airlineName
                )
            }

            composable(Screen.MyReviews.route) {
                MyReviewsScreen(navController = navController)
            }
        }
    }
}
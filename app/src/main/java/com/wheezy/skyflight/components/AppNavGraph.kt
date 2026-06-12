package com.wheezy.skyflight.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.ThemeOption
import com.wheezy.skyflight.core.ui.components.MyBottomBar
import com.wheezy.skyflight.core.ui.viewmodel.TopBarViewModel
import com.wheezy.skyflight.feature.auth.presentation.screens.LoginScreen
import com.wheezy.skyflight.feature.auth.presentation.screens.RegisterScreen
import com.wheezy.skyflight.feature.auth.presentation.viewmodels.AuthViewModel
import com.wheezy.skyflight.feature.booking.presentation.screens.BookingHistoryScreen
import com.wheezy.skyflight.feature.booking.presentation.screens.SeatListScreen
import com.wheezy.skyflight.feature.booking.presentation.viewmodels.BookingViewModel
import com.wheezy.skyflight.feature.cards.presentation.screens.SavedCardsScreen
import com.wheezy.skyflight.feature.invoice.presentation.screens.InvoiceDetailScreen
import com.wheezy.skyflight.feature.invoice.presentation.screens.InvoicesScreen
import com.wheezy.skyflight.feature.loyalty.presentation.screens.LoyaltyScreen
import com.wheezy.skyflight.feature.loyalty.presentation.screens.PointsHistoryScreen
import com.wheezy.skyflight.feature.notifications.presentation.screens.NotificationSettingsScreen
import com.wheezy.skyflight.feature.referral.presentation.screens.ReferralScreen
import com.wheezy.skyflight.feature.review.presentation.screens.CreateReviewScreen
import com.wheezy.skyflight.feature.review.presentation.screens.FlightReviewsScreen
import com.wheezy.skyflight.feature.review.presentation.screens.MyReviewsScreen
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
                    searchViewModel = searchViewModel
                )
            }

            composable(Screen.SearchResult.route) {
                ItemListScreen(
                    navController = navController,
                    searchParamsViewModel = searchParamsViewModel
                )
            }

            composable(Screen.SelectSeat.route) { backStackEntry ->
                val flightId = backStackEntry.arguments?.getString(Screen.FLIGHT_ID_ARG)?.toLongOrNull() ?: 0L

                if (flightId <= 0) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Please select a flight first")
                    }
                    return@composable
                }

                SeatListScreen(
                    navController = navController,
                    flightId = flightId,
                    onBackClick = { navController.popBackStack() }
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
                val bookingId = backStackEntry.arguments?.getString(Screen.BOOKING_ID_ARG)?.toLongOrNull() ?: 0L
                val bookingViewModel: BookingViewModel = hiltViewModel()

                var flightModel by remember { mutableStateOf<FlightModel?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                var error by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(bookingId) {
                    if (bookingId > 0) {
                        isLoading = true
                        val bookingResult = bookingViewModel.getBookingById(bookingId)
                        bookingResult.onSuccess { bookingDetails ->
                            val flight = bookingViewModel.getFlightById(bookingDetails.flightId)
                            flightModel = flight
                            isLoading = false
                        }.onFailure { e ->
                            error = e.message ?: "Failed to load booking"
                            isLoading = false
                        }
                    }
                }

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
                                Text("Error: $error")
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { navController.popBackStack() }) {
                                    Text("Go Back")
                                }
                            }
                        }
                    }
                    flightModel != null -> {
                        CreateReviewScreen(
                            navController = navController,
                            flight = flightModel!!,
                            bookingId = bookingId
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Booking not found")
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

            composable(Screen.MyReviews.route) {
                MyReviewsScreen(navController = navController)
            }
        }
    }
}
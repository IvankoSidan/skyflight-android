package com.wheezy.skyflight.feature.notifications.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wheezy.skyflight.core.ui.components.BackButton
import com.wheezy.skyflight.core.ui.components.WorldBackground
import com.wheezy.skyflight.feature.notifications.presentation.components.SettingsSectionHeader
import com.wheezy.skyflight.feature.notifications.presentation.components.SettingsSwitchCard
import com.wheezy.skyflight.feature.notifications.presentation.components.TimeRangeCard
import com.wheezy.skyflight.feature.notifications.presentation.states.NotificationSettingsState
import com.wheezy.skyflight.feature.notifications.presentation.viewmodels.NotificationSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    navController: NavController,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
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

            when (state) {
                is NotificationSettingsState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is NotificationSettingsState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = (state as NotificationSettingsState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { /* retry */ }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is NotificationSettingsState.Success -> {
                    settings?.let { currentSettings ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item { SettingsSectionHeader(title = "General") }
                            item {
                                SettingsSwitchCard(
                                    title = "Push Notifications",
                                    subtitle = "Enable or disable all push notifications",
                                    icon = Icons.Default.Notifications,
                                    checked = currentSettings.pushEnabled,
                                    onCheckedChange = { viewModel.togglePushEnabled() }
                                )
                            }
                            item {
                                SettingsSwitchCard(
                                    title = "Email Notifications",
                                    subtitle = "Receive email notifications",
                                    icon = Icons.Default.Email,
                                    checked = currentSettings.emailEnabled,
                                    onCheckedChange = { viewModel.toggleEmailEnabled() }
                                )
                            }

                            item { SettingsSectionHeader(title = "Booking") }
                            item {
                                SettingsSwitchCard(
                                    title = "Booking Created",
                                    subtitle = "When a new booking is created",
                                    icon = Icons.Default.AddCircle,
                                    checked = currentSettings.bookingCreated,
                                    onCheckedChange = { viewModel.toggleBookingCreated() }
                                )
                            }
                            item {
                                SettingsSwitchCard(
                                    title = "Booking Confirmed",
                                    subtitle = "When booking is confirmed after payment",
                                    icon = Icons.Default.CheckCircle,
                                    checked = currentSettings.bookingConfirmed,
                                    onCheckedChange = { viewModel.toggleBookingConfirmed() }
                                )
                            }
                            item {
                                SettingsSwitchCard(
                                    title = "Booking Cancelled",
                                    subtitle = "When booking is cancelled",
                                    icon = Icons.Default.Cancel,
                                    checked = currentSettings.bookingCancelled,
                                    onCheckedChange = { viewModel.toggleBookingCancelled() }
                                )
                            }

                            item { SettingsSectionHeader(title = "Payment") }
                            item {
                                SettingsSwitchCard(
                                    title = "Payment Success",
                                    subtitle = "When payment is successful",
                                    icon = Icons.Default.Payment,
                                    checked = currentSettings.paymentSuccess,
                                    onCheckedChange = { viewModel.togglePaymentSuccess() }
                                )
                            }
                            item {
                                SettingsSwitchCard(
                                    title = "Payment Failed",
                                    subtitle = "When payment fails",
                                    icon = Icons.Default.Error,
                                    checked = currentSettings.paymentFailed,
                                    onCheckedChange = { viewModel.togglePaymentFailed() }
                                )
                            }

                            item { SettingsSectionHeader(title = "Flight") }
                            item {
                                SettingsSwitchCard(
                                    title = "Flight Reminder",
                                    subtitle = "24 hours before departure",
                                    icon = Icons.Default.AccessTime,
                                    checked = currentSettings.flightReminder,
                                    onCheckedChange = { viewModel.toggleFlightReminder() }
                                )
                            }
                            item {
                                SettingsSwitchCard(
                                    title = "Flight Status Update",
                                    subtitle = "Delays, cancellations, gate changes",
                                    icon = Icons.Default.Flight,
                                    checked = currentSettings.flightStatusUpdate,
                                    onCheckedChange = { viewModel.toggleFlightStatusUpdate() }
                                )
                            }
                            item {
                                SettingsSwitchCard(
                                    title = "Thank You After Flight",
                                    subtitle = "Feedback request after landing",
                                    icon = Icons.Default.Star,
                                    checked = currentSettings.thankYouAfterFlight,
                                    onCheckedChange = { viewModel.toggleThankYouAfterFlight() }
                                )
                            }

                            item { SettingsSectionHeader(title = "Marketing") }
                            item {
                                SettingsSwitchCard(
                                    title = "Promotions & Offers",
                                    subtitle = "Special deals and discount notifications",
                                    icon = Icons.Default.LocalOffer,
                                    checked = currentSettings.massPromotion,
                                    onCheckedChange = { viewModel.toggleMassPromotion() }
                                )
                            }

                            item { SettingsSectionHeader(title = "Quiet Hours") }
                            item {
                                SettingsSwitchCard(
                                    title = "Enable Quiet Hours",
                                    subtitle = "Don't send notifications during selected hours",
                                    icon = Icons.Default.Bedtime,
                                    checked = currentSettings.quietHoursEnabled,
                                    onCheckedChange = { viewModel.toggleQuietHours() }
                                )
                            }
                            if (currentSettings.quietHoursEnabled) {
                                item {
                                    TimeRangeCard(
                                        startHour = currentSettings.quietHoursStart,
                                        endHour = currentSettings.quietHoursEnd,
                                        onStartClick = { showStartTimePicker = true },
                                        onEndClick = { showEndTimePicker = true }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showStartTimePicker && settings != null) {
        TimePickerDialog(
            title = "Quiet Hours Start",
            currentHour = settings!!.quietHoursStart,
            onHourSelected = { viewModel.setQuietHoursStart(it) },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker && settings != null) {
        TimePickerDialog(
            title = "Quiet Hours End",
            currentHour = settings!!.quietHoursEnd,
            onHourSelected = { viewModel.setQuietHoursEnd(it) },
            onDismiss = { showEndTimePicker = false }
        )
    }
}

@Composable
fun TimePickerDialog(
    title: String,
    currentHour: Int,
    onHourSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(currentHour) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text("Select hour:")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = { selectedHour = (selectedHour - 1 + 24) % 24 }) {
                        Text("-", fontSize = 24.sp)
                    }
                    Text(
                        text = String.format("%02d:00", selectedHour),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    TextButton(onClick = { selectedHour = (selectedHour + 1) % 24 }) {
                        Text("+", fontSize = 24.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onHourSelected(selectedHour)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
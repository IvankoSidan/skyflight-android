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
import com.wheezy.skyflight.core.model.Notification
import com.wheezy.skyflight.core.model.ThemeOption
import com.wheezy.skyflight.core.model.User
import com.wheezy.skyflight.core.ui.R
import com.wheezy.skyflight.core.ui.components.*
import com.wheezy.skyflight.feature.search.presentation.states.ClassSeatsUiState
import com.wheezy.skyflight.feature.search.presentation.states.SearchUiState
import com.wheezy.skyflight.feature.search.presentation.viewmodels.SearchViewModel
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
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    val locationsState by searchViewModel.locationsState.collectAsState()
    val classSeatsState by searchViewModel.classSeatsState.collectAsState()

    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("") }
    var adultPassenger by remember { mutableStateOf("1") }
    var childPassenger by remember { mutableStateOf("0") }
    var isDrawerOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        searchViewModel.fetchLocations()
        searchViewModel.fetchClassSeats()
    }

    val locations = (locationsState as? SearchUiState.Success)?.locations ?: emptyList()
    val locationNames = remember(locations) { locations.map { it.name } }
    val classItems = (classSeatsState as? ClassSeatsUiState.Success)?.classSeats ?: emptyList()

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
                            // From
                            YellowTitle(text = "From")
                            when (locationsState) {
                                is SearchUiState.Loading -> {
                                    GlassCard(modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(56.dp)) {
                                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                                            CircularProgressIndicator()
                                        }
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
                                    LocationDropdown(
                                        value = from,
                                        onValueChange = { from = it },
                                        locations = locationNames,
                                        icon = Icons.Default.FlightTakeoff,
                                        hint = "Select departure city"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // To
                            YellowTitle(text = "To")
                            when (locationsState) {
                                is SearchUiState.Loading -> {
                                    GlassCard(modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(56.dp)) {
                                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                                            CircularProgressIndicator()
                                        }
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
                                    LocationDropdown(
                                        value = to,
                                        onValueChange = { to = it },
                                        locations = locationNames,
                                        icon = Icons.Default.FlightLand,
                                        hint = "Select arrival city"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Passengers
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

                            // Class
                            YellowTitle(text = "Class")
                            when (classSeatsState) {
                                is ClassSeatsUiState.Loading -> {
                                    GlassCard(modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(56.dp)) {
                                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                                            CircularProgressIndicator()
                                        }
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
                                        ClassDropdown(
                                            value = selectedClass,
                                            onValueChange = { selectedClass = it },
                                            classItems = classItems
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Search Button
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
                }
            }
        }
    }
}

@Composable
fun LocationDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    locations: List<String>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    hint: String
) {
    var expanded by remember { mutableStateOf(false) }

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
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (value.isEmpty()) hint else value,
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
                locations.forEach { city ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(city)
                            }
                        },
                        onClick = {
                            onValueChange(city)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ClassDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    classItems: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

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
                    text = if (value.isEmpty()) "Select class" else value,
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
                            onValueChange(cls)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
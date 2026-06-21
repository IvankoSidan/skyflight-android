package com.wheezy.skyflight.presentation.screens

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.wheezy.skyflight.core.common.manager.NetworkMonitor
import com.wheezy.skyflight.core.common.manager.NotificationSubscriptionManager
import com.wheezy.skyflight.core.common.security.SecurityManager
import com.wheezy.skyflight.core.ui.components.NetworkSnackbar
import com.wheezy.skyflight.core.ui.snackbar.AppSnackbar
import com.wheezy.skyflight.core.ui.snackbar.CustomSnackBarHost
import com.wheezy.skyflight.core.ui.snackbar.SnackbarController
import com.wheezy.skyflight.core.ui.snackbar.SnackbarManager
import com.wheezy.skyflight.core.ui.theme.MyAppTheme
import com.wheezy.skyflight.core.ui.viewmodel.ThemeViewModel
import com.wheezy.skyflight.core.ui.viewmodel.TopBarViewModel
import com.wheezy.skyflight.feature.auth.presentation.viewmodels.AuthViewModel
import com.wheezy.skyflight.feature.search.presentation.viewmodels.SearchParamsViewModel
import com.wheezy.skyflight.presentation.components.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var securityManager: SecurityManager

    @Inject
    lateinit var notificationSubscriptionManager: NotificationSubscriptionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        lifecycleScope.launch {
            securityManager.securityStatus.collect { status ->
                when (status) {
                    is SecurityManager.SecurityStatus.Compromised -> {
                        showSecurityWarning(status.reason)
                    }
                    is SecurityManager.SecurityStatus.Emulator -> {
                        showEmulatorWarning()
                    }
                    else -> { /* Secure */ }
                }
            }
        }

        if (!securityManager.isSecure()) {
            showSecurityDialog()
        }

        setContent {
            StatusTopBarColor()

            val themeViewModel: ThemeViewModel = hiltViewModel()
            val currentTheme by themeViewModel.currentTheme.collectAsState()
            val topBarViewModel: TopBarViewModel = hiltViewModel()
            val context = LocalContext.current
            val snackbarHostState = remember { SnackbarHostState() }
            var currentSnackbar by remember { mutableStateOf<AppSnackbar?>(null) }
            val scope = rememberCoroutineScope()

            val snackbarController = remember {
                SnackbarController(
                    hostState = snackbarHostState,
                    scope = scope
                )
            }

            DisposableEffect(Unit) {
                topBarViewModel.registerReceiver(context)
                onDispose {
                    topBarViewModel.unregisterReceiver(context)
                }
            }

            LaunchedEffect(Unit) {
                notificationSubscriptionManager.subscribeToNotifications()
                notificationSubscriptionManager.subscribeToBookingUpdates()

                launch {
                    SnackbarManager.events.collectLatest { snackbar ->
                        currentSnackbar = snackbar
                        snackbarController.process(snackbar)
                    }
                }
            }

            MyAppTheme(themeOption = currentTheme) {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = hiltViewModel()
                val searchParamsViewModel: SearchParamsViewModel = hiltViewModel()

                NetworkSnackbar(networkMonitor = networkMonitor)

                Scaffold(
                    snackbarHost = {
                        CustomSnackBarHost(
                            hostState = snackbarHostState,
                            currentSnackbar = currentSnackbar
                        )
                    }
                ) { paddingValues ->
                    AppNavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        searchParamsViewModel = searchParamsViewModel,
                        topBarViewModel = topBarViewModel,
                        currentTheme = currentTheme,
                        onThemeChanged = { theme ->
                            themeViewModel.setTheme(theme)
                        },
                        paddingValues = paddingValues
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationSubscriptionManager.unsubscribeFromAll()
    }

    private fun showSecurityDialog() {
        AlertDialog.Builder(this)
            .setTitle("Security Warning")
            .setMessage("Your device appears to be compromised (rooted or running modified software).\n\nFor your security, some features may be limited.\n\nContinue at your own risk.")
            .setPositiveButton("Continue") { _, _ -> }
            .setNegativeButton("Exit") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun showSecurityWarning(reason: String) {
        AlertDialog.Builder(this)
            .setTitle("Security Warning")
            .setMessage("Your device is compromised: $reason")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    private fun showEmulatorWarning() {
        AlertDialog.Builder(this)
            .setTitle("Emulator Detected")
            .setMessage("Running on emulator. Some features may be limited.")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }
}

@Composable
fun StatusTopBarColor() {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = Color.Transparent.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }
}
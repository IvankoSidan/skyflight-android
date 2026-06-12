package com.wheezy.skyflight.core.ui.components

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wheezy.skyflight.core.common.manager.NetworkMonitor
import com.wheezy.skyflight.core.ui.snackbar.AppSnackbar
import com.wheezy.skyflight.core.ui.snackbar.SnackbarManager
import com.wheezy.skyflight.core.ui.snackbar.SnackbarPriority
import com.wheezy.skyflight.core.ui.snackbar.SnackbarType

@Composable
fun NetworkSnackbar(
    networkMonitor: NetworkMonitor
) {
    val isConnected by networkMonitor.isConnected.collectAsStateWithLifecycle()
    var wasDisconnected by remember { mutableStateOf(false) }

    LaunchedEffect(isConnected) {
        if (!isConnected && !wasDisconnected) {
            wasDisconnected = true
            SnackbarManager.tryShow(
                AppSnackbar(
                    message = "No internet connection",
                    type = SnackbarType.ERROR,
                    priority = SnackbarPriority.HIGH
                )
            )
        } else if (isConnected && wasDisconnected) {
            wasDisconnected = false
            SnackbarManager.tryShow(
                AppSnackbar(
                    message = "Internet restored",
                    type = SnackbarType.SUCCESS,
                    priority = SnackbarPriority.HIGH
                )
            )
        }
    }
}
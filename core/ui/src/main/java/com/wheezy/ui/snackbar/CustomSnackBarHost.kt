package com.wheezy.skyflight.core.ui.snackbar

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CustomSnackBarHost(
    hostState: SnackbarHostState,
    currentSnackbar: AppSnackbar?,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    ) { snackbarData ->
        MaterialSnackbar(
            snackbar = currentSnackbar ?: AppSnackbar(
                message = snackbarData.visuals.message,
                type = SnackbarType.INFO
            )
        )
    }
}
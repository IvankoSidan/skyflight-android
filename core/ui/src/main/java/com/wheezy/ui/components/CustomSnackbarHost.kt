package com.wheezy.skyflight.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CustomSnackBarHost(hostState: SnackbarHostState) {
    SnackbarHost(
        hostState = hostState,
        modifier = Modifier,
        snackbar = { data ->
            val message = data.visuals.message
            val backgroundColor = when {
                message.contains("success", ignoreCase = true) -> MaterialTheme.colorScheme.primary
                message.contains("failed", ignoreCase = true) -> MaterialTheme.colorScheme.error
                message.contains("cancel", ignoreCase = true) -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val contentColor = when {
                message.contains("success", ignoreCase = true) -> MaterialTheme.colorScheme.onPrimary
                message.contains("failed", ignoreCase = true) -> MaterialTheme.colorScheme.onError
                message.contains("cancel", ignoreCase = true) -> MaterialTheme.colorScheme.onTertiary
                else -> MaterialTheme.colorScheme.onSurface
            }

            GlassCard(
                modifier = Modifier,
                config = GlassCardDefaults.subtle
            ) {
                Snackbar(
                    snackbarData = data,
                    containerColor = backgroundColor,
                    contentColor = contentColor,
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
    )
}
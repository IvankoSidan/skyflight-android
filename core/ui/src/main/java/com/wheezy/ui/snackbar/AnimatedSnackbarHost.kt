package com.wheezy.skyflight.core.ui.snackbar

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedSnackbarHost(
    hostState: SnackbarHostState,
    currentSnackbar: AppSnackbar?
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = hostState.currentSnackbarData != null && currentSnackbar != null,
            enter = slideInVertically(
                initialOffsetY = { it / 2 }
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it / 2 }
            ) + fadeOut()
        ) {
            currentSnackbar?.let {
                MaterialSnackbar(
                    snackbar = it,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(0.95f)
                )
            }
        }
    }
}
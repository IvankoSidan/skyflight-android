package com.wheezy.skyflight.core.ui.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration as MaterialDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SnackbarController(
    private val hostState: SnackbarHostState,
    private val scope: CoroutineScope
) {

    private var currentPriority: SnackbarPriority? = null

    fun process(snackbar: AppSnackbar) {
        scope.launch {
            if (currentPriority == SnackbarPriority.HIGH &&
                snackbar.priority != SnackbarPriority.HIGH
            ) {
                return@launch
            }

            currentPriority = snackbar.priority

            val duration = when (snackbar.duration) {
                SnackbarDuration.SHORT -> MaterialDuration.Short
                SnackbarDuration.LONG -> MaterialDuration.Long
                SnackbarDuration.INDEFINITE -> MaterialDuration.Indefinite
            }

            hostState.showSnackbar(
                message = snackbar.message,
                actionLabel = snackbar.actionLabel,
                duration = duration
            )

            currentPriority = null
        }
    }
}
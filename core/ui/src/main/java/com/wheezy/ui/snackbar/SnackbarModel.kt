package com.wheezy.skyflight.core.ui.snackbar

enum class SnackbarType {
    SUCCESS,
    ERROR,
    INFO
}

enum class SnackbarPriority {
    HIGH,
    NORMAL,
    LOW
}

enum class SnackbarDuration {
    SHORT,
    LONG,
    INDEFINITE
}

data class AppSnackbar(
    val message: String,
    val type: SnackbarType = SnackbarType.INFO,
    val priority: SnackbarPriority = SnackbarPriority.NORMAL,
    val actionLabel: String? = null,
    val duration: SnackbarDuration = SnackbarDuration.SHORT
)
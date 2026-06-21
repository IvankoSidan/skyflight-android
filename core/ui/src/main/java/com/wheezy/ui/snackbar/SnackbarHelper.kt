package com.wheezy.skyflight.core.ui.snackbar

object SnackbarHelper {

    fun showSuccess(message: String) {
        SnackbarManager.tryShow(
            AppSnackbar(
                message = message,
                type = SnackbarType.SUCCESS,
                priority = SnackbarPriority.NORMAL
            )
        )
    }

    fun showLowPriority(message: String) {
        SnackbarManager.tryShow(
            AppSnackbar(
                message = message,
                type = SnackbarType.INFO,
                priority = SnackbarPriority.LOW
            )
        )
    }

    fun showError(message: String) {
        SnackbarManager.tryShow(
            AppSnackbar(
                message = message,
                type = SnackbarType.ERROR,
                priority = SnackbarPriority.NORMAL
            )
        )
    }

    fun showInfo(message: String) {
        SnackbarManager.tryShow(
            AppSnackbar(
                message = message,
                type = SnackbarType.INFO,
                priority = SnackbarPriority.NORMAL
            )
        )
    }
}
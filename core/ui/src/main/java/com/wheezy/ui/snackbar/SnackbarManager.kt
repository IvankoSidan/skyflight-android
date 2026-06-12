package com.wheezy.skyflight.core.ui.snackbar

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object SnackbarManager {

    private val channel = Channel<AppSnackbar>(Channel.UNLIMITED)
    val events = channel.receiveAsFlow()

    suspend fun show(snackbar: AppSnackbar) {
        channel.send(snackbar)
    }

    fun tryShow(snackbar: AppSnackbar) {
        channel.trySend(snackbar)
    }
}
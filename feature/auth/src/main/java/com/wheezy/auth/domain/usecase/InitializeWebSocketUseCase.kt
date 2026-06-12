package com.wheezy.skyflight.feature.auth.domain.usecase

import com.wheezy.skyflight.core.common.manager.FCMTokenManager
import com.wheezy.skyflight.core.common.manager.WebSocketManager
import javax.inject.Inject

class InitializeWebSocketUseCase @Inject constructor(
    private val webSocketManager: WebSocketManager,
    private val fcmTokenManager: FCMTokenManager
) {
    suspend operator fun invoke() {
        webSocketManager.connect()
        webSocketManager.subscribeToNotifications()
        webSocketManager.subscribeToBookingUpdates()

        val fcmToken = fcmTokenManager.getLocalToken()
        if (!fcmToken.isNullOrEmpty()) {
            fcmTokenManager.sendTokenToServer(fcmToken)
        }
    }
}
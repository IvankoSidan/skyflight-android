package com.wheezy.skyflight.feature.auth.domain.usecase

import com.wheezy.skyflight.core.common.manager.FCMTokenManager
import com.wheezy.skyflight.core.common.manager.WebSocketManager
import com.wheezy.skyflight.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val fcmTokenManager: FCMTokenManager,
    private val webSocketManager: WebSocketManager
) {
    suspend operator fun invoke() {
        fcmTokenManager.unregisterToken()
        authRepository.logout()
        webSocketManager.disconnect()
    }
}
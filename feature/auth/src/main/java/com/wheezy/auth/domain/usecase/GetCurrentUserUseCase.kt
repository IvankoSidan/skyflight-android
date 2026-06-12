package com.wheezy.skyflight.feature.auth.domain.usecase

import com.wheezy.skyflight.core.model.User
import com.wheezy.skyflight.core.network.manager.TokenManager
import com.wheezy.skyflight.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(): User? {
        val token = tokenManager.getToken()
        return if (token != null) {
            authRepository.getCurrentUser()
        } else {
            null
        }
    }
}
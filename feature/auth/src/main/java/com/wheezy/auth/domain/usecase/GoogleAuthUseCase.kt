package com.wheezy.skyflight.feature.auth.domain.usecase

import com.wheezy.skyflight.core.model.User
import com.wheezy.skyflight.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class GoogleAuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(token: String): Result<User> {
        return authRepository.googleAuth(token)
    }
}
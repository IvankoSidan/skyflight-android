package com.wheezy.skyflight.feature.auth.domain.usecase

import com.wheezy.skyflight.core.model.User
import com.wheezy.skyflight.core.network.model.UserLoginDto
import com.wheezy.skyflight.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<User> {
        return authRepository.login(UserLoginDto(email, password))
    }
}
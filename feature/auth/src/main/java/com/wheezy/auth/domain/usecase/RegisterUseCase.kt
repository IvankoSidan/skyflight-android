package com.wheezy.skyflight.feature.auth.domain.usecase

import com.wheezy.skyflight.core.model.User
import com.wheezy.skyflight.core.network.model.UserRegisterDto
import com.wheezy.skyflight.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String
    ): Result<User> {
        return authRepository.register(UserRegisterDto(email, password, name))
    }
}
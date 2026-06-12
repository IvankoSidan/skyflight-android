package com.wheezy.skyflight.feature.auth.domain.repository

import com.wheezy.skyflight.core.model.User
import com.wheezy.skyflight.core.network.model.UserLoginDto
import com.wheezy.skyflight.core.network.model.UserRegisterDto

interface AuthRepository {
    suspend fun login(dto: UserLoginDto): Result<User>
    suspend fun register(dto: UserRegisterDto): Result<User>
    suspend fun googleAuth(token: String): Result<User>
    suspend fun getCurrentUser(): User?
    suspend fun logout()
    suspend fun refreshToken(): String?
}
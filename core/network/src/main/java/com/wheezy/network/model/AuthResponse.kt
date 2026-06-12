package com.wheezy.skyflight.core.network.model

data class AuthResponse(
    val user: UserResponseDto,
    val token: String
)
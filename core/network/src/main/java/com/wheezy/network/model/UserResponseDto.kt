package com.wheezy.skyflight.core.network.model

import com.wheezy.skyflight.core.model.User

data class UserResponseDto(
    val id: Long,
    val email: String,
    val name: String?,
    val profilePicture: String?
)

fun UserResponseDto.toDomain(): User = User(
    id = this.id,
    email = this.email,
    name = this.name,
    profilePicture = this.profilePicture
)
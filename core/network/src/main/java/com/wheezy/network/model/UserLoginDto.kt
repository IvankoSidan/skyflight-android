package com.wheezy.skyflight.core.network.model

import com.google.gson.annotations.SerializedName

data class UserLoginDto(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)
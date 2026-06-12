package com.wheezy.skyflight.core.network.model

import com.google.gson.annotations.SerializedName

data class UserRegisterDto(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("phone_number")
    val phoneNumber: String? = null
)
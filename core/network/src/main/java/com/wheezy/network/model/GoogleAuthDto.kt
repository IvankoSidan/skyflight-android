package com.wheezy.skyflight.core.network.model

import com.google.gson.annotations.SerializedName

data class GoogleAuthDto(
    @SerializedName("id_token")
    val idToken: String
)
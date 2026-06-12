package com.wheezy.skyflight.feature.search.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SearchParams(
    val from: String = "",
    val to: String = "",
    val passengers: Int = 1,
    val selectedClass: String = ""
) : Parcelable
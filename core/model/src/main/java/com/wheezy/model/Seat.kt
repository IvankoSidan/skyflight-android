package com.wheezy.skyflight.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Seat(
    var status: SeatStatus,
    var name: String
) : Parcelable
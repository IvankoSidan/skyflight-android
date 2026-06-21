package com.wheezy.skyflight.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.IgnoredOnParcel

@Parcelize
data class SavedCard(
    val id: Long,
    val stripePaymentMethodId: String,
    val cardLast4: String,
    val cardBrand: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val isDefault: Boolean
) : Parcelable {
    @IgnoredOnParcel
    val displayName: String = "$cardBrand •••• $cardLast4"

    @IgnoredOnParcel
    val expiryDate: String = "$expiryMonth/$expiryYear"
}
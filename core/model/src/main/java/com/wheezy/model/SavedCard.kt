    package com.wheezy.skyflight.core.model

    import android.os.Parcelable
    import kotlinx.parcelize.Parcelize

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
        val displayName: String = "$cardBrand •••• $cardLast4"
        val expiryDate: String = "$expiryMonth/$expiryYear"
    }
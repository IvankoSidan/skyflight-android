package com.wheezy.skyflight.feature.referral.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReferralCode(
    val code: String,
    val usageCount: Int,
    val maxUses: Int,
    val isValid: Boolean,
    val shareLink: String
) : Parcelable

@Parcelize
data class ReferredUser(
    val email: String,
    val name: String?,
    val registeredAt: String,
    val status: String
) : Parcelable

@Parcelize
data class ReferralInfo(
    val myCode: String,
    val myReferrals: List<ReferredUser>,
    val totalReferrals: Int,
    val totalDiscountEarned: Long
) : Parcelable

data class ApplyReferralResult(
    val success: Boolean,
    val message: String,
    val discountPercent: Int? = null,
    val discountAmount: Long? = null
)
package com.wheezy.skyflight.core.network.model

data class ReferralCodeResponse(
    val code: String,
    val usageCount: Int,
    val maxUses: Int,
    val isValid: Boolean,
    val shareLink: String
)

data class ReferralApplyRequest(
    val code: String
)

data class ReferralApplyResponse(
    val success: Boolean,
    val message: String,
    val discountPercent: Int?,
    val discountAmount: Long?
)

data class ReferralInfoResponse(
    val myCode: String,
    val myReferrals: List<ReferredUserDto>,
    val totalReferrals: Int,
    val totalDiscountEarned: Long
)

data class ReferredUserDto(
    val email: String,
    val name: String?,
    val registeredAt: String,
    val status: String
)
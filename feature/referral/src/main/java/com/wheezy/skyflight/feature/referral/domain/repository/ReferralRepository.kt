package com.wheezy.skyflight.feature.referral.domain.repository

import com.wheezy.skyflight.feature.referral.domain.model.ApplyReferralResult
import com.wheezy.skyflight.feature.referral.domain.model.ReferralCode
import com.wheezy.skyflight.feature.referral.domain.model.ReferralInfo

interface ReferralRepository {
    suspend fun getReferralCode(): Result<ReferralCode>
    suspend fun applyReferralCode(code: String): Result<ApplyReferralResult>
    suspend fun getMyReferrals(): Result<ReferralInfo>
}
package com.wheezy.skyflight.feature.referral.domain.usecase

import com.wheezy.skyflight.feature.referral.domain.model.ApplyReferralResult
import com.wheezy.skyflight.feature.referral.domain.repository.ReferralRepository
import javax.inject.Inject

class ApplyReferralCodeUseCase @Inject constructor(
    private val repository: ReferralRepository
) {
    suspend operator fun invoke(code: String): Result<ApplyReferralResult> {
        return repository.applyReferralCode(code)
    }
}
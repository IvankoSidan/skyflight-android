package com.wheezy.skyflight.feature.referral.domain.usecase

import com.wheezy.skyflight.feature.referral.domain.model.ReferralCode
import com.wheezy.skyflight.feature.referral.domain.repository.ReferralRepository
import javax.inject.Inject

class GetReferralCodeUseCase @Inject constructor(
    private val repository: ReferralRepository
) {
    suspend operator fun invoke(): Result<ReferralCode> {
        return repository.getReferralCode()
    }
}
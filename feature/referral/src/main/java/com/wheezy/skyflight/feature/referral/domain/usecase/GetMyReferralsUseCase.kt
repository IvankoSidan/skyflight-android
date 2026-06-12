package com.wheezy.skyflight.feature.referral.domain.usecase

import com.wheezy.skyflight.feature.referral.domain.model.ReferralInfo
import com.wheezy.skyflight.feature.referral.domain.repository.ReferralRepository
import javax.inject.Inject

class GetMyReferralsUseCase @Inject constructor(
    private val repository: ReferralRepository
) {
    suspend operator fun invoke(): Result<ReferralInfo> {
        return repository.getMyReferrals()
    }
}
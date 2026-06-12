package com.wheezy.skyflight.feature.loyalty.domain.usecase

import com.wheezy.skyflight.core.model.TierBenefit
import com.wheezy.skyflight.feature.loyalty.domain.repository.LoyaltyRepository
import javax.inject.Inject

class GetTiersUseCase @Inject constructor(
    private val repository: LoyaltyRepository
) {
    suspend operator fun invoke(): Result<List<TierBenefit>> {
        return repository.getTiers()
    }
}
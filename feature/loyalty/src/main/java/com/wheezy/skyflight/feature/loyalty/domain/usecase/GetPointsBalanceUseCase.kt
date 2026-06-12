package com.wheezy.skyflight.feature.loyalty.domain.usecase

import com.wheezy.skyflight.core.model.PointsBalance
import com.wheezy.skyflight.feature.loyalty.domain.repository.LoyaltyRepository
import javax.inject.Inject

class GetPointsBalanceUseCase @Inject constructor(
    private val repository: LoyaltyRepository
) {
    suspend operator fun invoke(): Result<PointsBalance> {
        return repository.getPointsBalance()
    }
}
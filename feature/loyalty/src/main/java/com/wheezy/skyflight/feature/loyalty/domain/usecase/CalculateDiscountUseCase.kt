package com.wheezy.skyflight.feature.loyalty.domain.usecase

import com.wheezy.skyflight.core.model.CalculateDiscountResponse
import com.wheezy.skyflight.feature.loyalty.domain.repository.LoyaltyRepository
import javax.inject.Inject

class CalculateDiscountUseCase @Inject constructor(
    private val repository: LoyaltyRepository
) {
    suspend operator fun invoke(amount: Long, points: Int): Result<CalculateDiscountResponse> {
        return repository.calculateDiscount(amount, points)
    }
}
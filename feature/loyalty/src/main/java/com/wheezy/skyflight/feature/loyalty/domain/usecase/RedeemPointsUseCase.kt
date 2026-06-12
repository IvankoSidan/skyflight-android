package com.wheezy.skyflight.feature.loyalty.domain.usecase

import com.wheezy.skyflight.core.model.RedeemPointsResponse
import com.wheezy.skyflight.feature.loyalty.domain.repository.LoyaltyRepository
import javax.inject.Inject

class RedeemPointsUseCase @Inject constructor(
    private val repository: LoyaltyRepository
) {
    suspend operator fun invoke(points: Int, bookingId: Long): Result<RedeemPointsResponse> {
        return repository.redeemPoints(points, bookingId)
    }
}
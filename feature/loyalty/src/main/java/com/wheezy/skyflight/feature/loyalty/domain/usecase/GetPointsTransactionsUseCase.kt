package com.wheezy.skyflight.feature.loyalty.domain.usecase

import com.wheezy.skyflight.core.model.PointsTransaction
import com.wheezy.skyflight.feature.loyalty.domain.repository.LoyaltyRepository
import javax.inject.Inject

class GetPointsTransactionsUseCase @Inject constructor(
    private val repository: LoyaltyRepository
) {
    suspend operator fun invoke(): Result<List<PointsTransaction>> {
        return repository.getTransactions()
    }
}
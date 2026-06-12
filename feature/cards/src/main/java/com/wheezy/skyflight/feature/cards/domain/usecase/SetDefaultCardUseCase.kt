package com.wheezy.skyflight.feature.cards.domain.usecase

import com.wheezy.skyflight.feature.cards.domain.repository.CardsRepository
import javax.inject.Inject

class SetDefaultCardUseCase @Inject constructor(
    private val repository: CardsRepository
) {
    suspend operator fun invoke(paymentMethodId: String): Result<Unit> {
        return repository.setDefaultCard(paymentMethodId)
    }
}
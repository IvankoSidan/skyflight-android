package com.wheezy.skyflight.feature.cards.domain.usecase

import com.wheezy.skyflight.core.model.SavedCard
import com.wheezy.skyflight.feature.cards.domain.repository.CardsRepository
import javax.inject.Inject

class GetSavedCardsUseCase @Inject constructor(
    private val repository: CardsRepository
) {
    suspend operator fun invoke(): Result<List<SavedCard>> {
        return repository.getSavedCards()
    }
}
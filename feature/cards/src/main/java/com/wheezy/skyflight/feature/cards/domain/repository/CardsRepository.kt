package com.wheezy.skyflight.feature.cards.domain.repository

import com.wheezy.skyflight.core.model.SavedCard

interface CardsRepository {
    suspend fun getSavedCards(): Result<List<SavedCard>>
    suspend fun deleteCard(paymentMethodId: String): Result<Unit>
    suspend fun setDefaultCard(paymentMethodId: String): Result<Unit>
}
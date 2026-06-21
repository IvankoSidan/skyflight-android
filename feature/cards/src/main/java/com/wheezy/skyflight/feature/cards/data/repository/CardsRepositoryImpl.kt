package com.wheezy.skyflight.feature.cards.data.repository

import android.util.Log
import com.wheezy.skyflight.core.model.SavedCard
import com.wheezy.skyflight.core.network.api.CardsApiService
import com.wheezy.skyflight.feature.cards.domain.repository.CardsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardsRepositoryImpl @Inject constructor(
    private val cardsApiService: CardsApiService
) : CardsRepository {

    companion object {
        private const val TAG = "CardsRepository"
    }

    override suspend fun getSavedCards(): Result<List<SavedCard>> {
        return try {
            val response = cardsApiService.getSavedCards()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getSavedCards error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteCard(paymentMethodId: String): Result<Unit> {
        return try {
            val response = cardsApiService.deleteCard(paymentMethodId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteCard error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun setDefaultCard(paymentMethodId: String): Result<Unit> {
        return try {
            val response = cardsApiService.setDefaultCard(paymentMethodId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "setDefaultCard error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
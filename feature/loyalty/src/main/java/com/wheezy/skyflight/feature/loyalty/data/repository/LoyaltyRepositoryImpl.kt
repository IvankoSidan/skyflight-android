package com.wheezy.skyflight.feature.loyalty.data.repository

import com.wheezy.skyflight.core.model.*
import com.wheezy.skyflight.core.network.api.LoyaltyApiService
import com.wheezy.skyflight.feature.loyalty.domain.repository.LoyaltyRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoyaltyRepositoryImpl @Inject constructor(
    private val loyaltyApiService: LoyaltyApiService
) : LoyaltyRepository {

    override suspend fun getPointsBalance(): Result<PointsBalance> {
        return try {
            val response = loyaltyApiService.getPointsBalance()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTransactions(): Result<List<PointsTransaction>> {
        return try {
            val response = loyaltyApiService.getTransactions()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTiers(): Result<List<TierBenefit>> {
        return try {
            val response = loyaltyApiService.getTiers()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun calculateDiscount(amount: Long, points: Int): Result<CalculateDiscountResponse> {
        return try {
            val response = loyaltyApiService.calculateDiscount(amount, points)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun redeemPoints(points: Int, bookingId: Long): Result<RedeemPointsResponse> {
        return try {
            val response = loyaltyApiService.redeemPoints(RedeemPointsRequest(points, bookingId))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
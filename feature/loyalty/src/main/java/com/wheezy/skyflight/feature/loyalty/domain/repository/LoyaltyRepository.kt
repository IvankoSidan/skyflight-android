package com.wheezy.skyflight.feature.loyalty.domain.repository

import com.wheezy.skyflight.core.model.*

interface LoyaltyRepository {
    suspend fun getPointsBalance(): Result<PointsBalance>
    suspend fun getTransactions(): Result<List<PointsTransaction>>
    suspend fun getTiers(): Result<List<TierBenefit>>
    suspend fun calculateDiscount(amount: Long, points: Int): Result<CalculateDiscountResponse>
    suspend fun redeemPoints(points: Int, bookingId: Long): Result<RedeemPointsResponse>
}
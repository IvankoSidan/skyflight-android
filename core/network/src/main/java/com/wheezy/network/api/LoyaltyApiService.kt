package com.wheezy.skyflight.core.network.api

import com.wheezy.skyflight.core.model.*
import retrofit2.Response
import retrofit2.http.*

interface LoyaltyApiService {

    @GET("/api/loyalty/points")
    suspend fun getPointsBalance(): Response<PointsBalance>

    @GET("/api/loyalty/transactions")
    suspend fun getTransactions(): Response<List<PointsTransaction>>

    @GET("/api/loyalty/calculate")
    suspend fun calculateDiscount(
        @Query("amount") amount: Long,
        @Query("points") points: Int
    ): Response<CalculateDiscountResponse>

    @POST("/api/loyalty/redeem")
    suspend fun redeemPoints(@Body request: RedeemPointsRequest): Response<RedeemPointsResponse>

    @GET("/api/loyalty/tiers")
    suspend fun getTiers(): Response<List<TierBenefit>>
}
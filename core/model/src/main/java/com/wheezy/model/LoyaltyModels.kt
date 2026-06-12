package com.wheezy.skyflight.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PointsBalance(
    val balance: Int,
    val lifetimePoints: Int,
    val tier: String,
    val nextTier: String?,
    val pointsToNextTier: Int,
    val cashbackPercent: Int
) : Parcelable {
    val progressToNextTier: Float
        get() = if (pointsToNextTier <= 0) 1f else {
            val currentTierPoints = when (tier) {
                "BRONZE" -> 0
                "SILVER" -> 1000
                "GOLD" -> 5000
                "PLATINUM" -> 20000
                else -> 0
            }
            val totalToNext = currentTierPoints + pointsToNextTier
            (currentTierPoints.toFloat() / totalToNext).coerceIn(0f, 1f)
        }
}

@Parcelize
data class PointsTransaction(
    val id: Long,
    val amount: Int,
    val type: String,
    val description: String?,
    val createdAt: String
) : Parcelable

@Parcelize
data class TierBenefit(
    val tier: String,
    val cashbackPercent: Int,
    val freeSeatSelection: Boolean,
    val priorityBoarding: Boolean,
    val freeBaggageKg: Int,
    val minPoints: Int
) : Parcelable

data class CalculateDiscountRequest(
    val amount: Long,
    val points: Int
)

data class CalculateDiscountResponse(
    val discountAmount: Long,
    val pointsUsed: Int,
    val finalAmount: Long
)

data class RedeemPointsRequest(
    val points: Int,
    val bookingId: Long
)

data class RedeemPointsResponse(
    val success: Boolean,
    val discountAmount: Long,
    val remainingPoints: Int,
    val message: String
)
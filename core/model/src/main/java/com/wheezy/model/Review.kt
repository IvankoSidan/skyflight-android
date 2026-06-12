package com.wheezy.skyflight.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class Review(
    val id: Long,
    val userId: Long,
    val userName: String?,
    val bookingId: Long,
    val flightId: Long,
    val airlineName: String,
    val rating: Int,
    val comment: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val canEdit: Boolean = false
) : Parcelable

@Parcelize
data class AirlineRating(
    val airlineName: String,
    val averageRating: Double,
    val totalReviews: Int,
    val ratingDistribution: Map<Int, Int>
) : Parcelable {
    val averageRatingFormatted: String = String.format("%.1f", averageRating)
    val starsCount: Int = averageRating.toInt()
    val hasHalfStar: Boolean = averageRating - starsCount >= 0.5
}

data class CreateReviewRequest(
    val bookingId: Long,
    val rating: Int,
    val comment: String?
)

data class UpdateReviewRequest(
    val rating: Int,
    val comment: String?
)

data class CanReviewResponse(
    val canReview: Boolean
)
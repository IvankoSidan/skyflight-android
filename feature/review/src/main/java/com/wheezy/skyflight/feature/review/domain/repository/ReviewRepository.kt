package com.wheezy.skyflight.feature.review.domain.repository

import com.wheezy.skyflight.core.model.AirlineRating
import com.wheezy.skyflight.core.model.Review
import com.wheezy.skyflight.feature.review.domain.model.ReviewsPageData

interface ReviewRepository {
    suspend fun createReview(bookingId: Long, rating: Int, comment: String?): Result<Review>
    suspend fun updateReview(reviewId: Long, rating: Int, comment: String?): Result<Review>
    suspend fun deleteReview(reviewId: Long): Result<Unit>
    suspend fun getFlightReviews(flightId: Long): Result<List<Review>>
    suspend fun getFlightReviewsPaginated(flightId: Long, page: Int, size: Int): Result<ReviewsPageData>
    suspend fun getAirlineRating(airlineName: String): Result<AirlineRating>
    suspend fun getMyReviews(): Result<List<Review>>
    suspend fun canReview(bookingId: Long): Result<Boolean>
}
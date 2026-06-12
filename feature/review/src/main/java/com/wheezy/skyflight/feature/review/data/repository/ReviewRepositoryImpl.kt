package com.wheezy.skyflight.feature.review.data.repository

import com.wheezy.skyflight.core.model.AirlineRating
import com.wheezy.skyflight.core.model.CreateReviewRequest
import com.wheezy.skyflight.core.model.Review
import com.wheezy.skyflight.core.model.UpdateReviewRequest
import com.wheezy.skyflight.core.network.api.ReviewApiService
import com.wheezy.skyflight.feature.review.domain.model.ReviewsPageData
import com.wheezy.skyflight.feature.review.domain.repository.ReviewRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val reviewApiService: ReviewApiService
) : ReviewRepository {

    override suspend fun createReview(bookingId: Long, rating: Int, comment: String?): Result<Review> {
        return try {
            val response = reviewApiService.createReview(CreateReviewRequest(bookingId, rating, comment))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReview(reviewId: Long, rating: Int, comment: String?): Result<Review> {
        return try {
            val response = reviewApiService.updateReview(reviewId, UpdateReviewRequest(rating, comment))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteReview(reviewId: Long): Result<Unit> {
        return try {
            val response = reviewApiService.deleteReview(reviewId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFlightReviews(flightId: Long): Result<List<Review>> {
        return try {
            val response = reviewApiService.getFlightReviews(flightId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFlightReviewsPaginated(flightId: Long, page: Int, size: Int): Result<ReviewsPageData> {
        return try {
            val response = reviewApiService.getFlightReviewsPaginated(flightId, page, size)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                @Suppress("UNCHECKED_CAST")
                val reviewsList = (body["reviews"] as? List<*>?)?.filterIsInstance<Review>() ?: emptyList()

                Result.success(
                    ReviewsPageData(
                        reviews = reviewsList,
                        currentPage = (body["currentPage"] as? Int) ?: 0,
                        totalPages = (body["totalPages"] as? Int) ?: 0,
                        totalItems = (body["totalItems"] as? Int) ?: 0,
                        airlineRating = body["airlineRating"] as? AirlineRating
                    )
                )
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAirlineRating(airlineName: String): Result<AirlineRating> {
        return try {
            val response = reviewApiService.getAirlineRating(airlineName)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyReviews(): Result<List<Review>> {
        return try {
            val response = reviewApiService.getMyReviews()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun canReview(bookingId: Long): Result<Boolean> {
        return try {
            val response = reviewApiService.canReview(bookingId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.canReview)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.success(false)
        }
    }
}
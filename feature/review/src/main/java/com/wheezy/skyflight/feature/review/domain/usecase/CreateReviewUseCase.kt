package com.wheezy.skyflight.feature.review.domain.usecase

import com.wheezy.skyflight.core.model.Review
import com.wheezy.skyflight.feature.review.domain.repository.ReviewRepository
import javax.inject.Inject

class CreateReviewUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(bookingId: Long, rating: Int, comment: String?): Result<Review> {
        return repository.createReview(bookingId, rating, comment)
    }
}
package com.wheezy.skyflight.feature.review.domain.usecase

import com.wheezy.skyflight.core.model.Review
import com.wheezy.skyflight.feature.review.domain.repository.ReviewRepository
import javax.inject.Inject

class GetFlightReviewsUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(flightId: Long): Result<List<Review>> {
        return repository.getFlightReviews(flightId)
    }
}
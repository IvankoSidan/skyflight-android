package com.wheezy.skyflight.feature.review.domain.usecase

import com.wheezy.skyflight.feature.review.domain.model.ReviewsPageData
import com.wheezy.skyflight.feature.review.domain.repository.ReviewRepository
import javax.inject.Inject

class GetFlightReviewsPaginatedUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(flightId: Long, page: Int, size: Int): Result<ReviewsPageData> {
        return repository.getFlightReviewsPaginated(flightId, page, size)
    }
}
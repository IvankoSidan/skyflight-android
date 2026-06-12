package com.wheezy.skyflight.feature.review.domain.model

import com.wheezy.skyflight.core.model.AirlineRating
import com.wheezy.skyflight.core.model.Review

data class ReviewsPageData(
    val reviews: List<Review>,
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val airlineRating: AirlineRating? = null
)
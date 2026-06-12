package com.wheezy.skyflight.core.network.api

import com.wheezy.skyflight.core.model.AirlineRating
import com.wheezy.skyflight.core.model.CanReviewResponse
import com.wheezy.skyflight.core.model.CreateReviewRequest
import com.wheezy.skyflight.core.model.Review
import com.wheezy.skyflight.core.model.UpdateReviewRequest
import retrofit2.Response
import retrofit2.http.*

interface ReviewApiService {

    @POST("/api/reviews")
    suspend fun createReview(@Body request: CreateReviewRequest): Response<Review>

    @PUT("/api/reviews/{id}")
    suspend fun updateReview(
        @Path("id") id: Long,
        @Body request: UpdateReviewRequest
    ): Response<Review>

    @DELETE("/api/reviews/{id}")
    suspend fun deleteReview(@Path("id") id: Long): Response<Unit>

    @GET("/api/reviews/flight/{flightId}")
    suspend fun getFlightReviews(@Path("flightId") flightId: Long): Response<List<Review>>

    @GET("/api/reviews/flight/{flightId}/paginated")
    suspend fun getFlightReviewsPaginated(
        @Path("flightId") flightId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<Map<String, Any>>

    @GET("/api/reviews/airline/{airlineName}")
    suspend fun getAirlineRating(@Path("airlineName") airlineName: String): Response<AirlineRating>

    @GET("/api/reviews/airline/{airlineName}/paginated")
    suspend fun getAirlineRatingPaginated(
        @Path("airlineName") airlineName: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<Map<String, Any>>

    @GET("/api/reviews/my")
    suspend fun getMyReviews(): Response<List<Review>>

    @GET("/api/reviews/can-review/{bookingId}")
    suspend fun canReview(@Path("bookingId") bookingId: Long): Response<CanReviewResponse>
}
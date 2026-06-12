package com.wheezy.skyflight.core.network.api

import com.wheezy.skyflight.core.model.SavedCard
import retrofit2.Response
import retrofit2.http.*

interface CardsApiService {

    @GET("/api/payments/cards")
    suspend fun getSavedCards(): Response<List<SavedCard>>

    @DELETE("/api/payments/cards/{paymentMethodId}")
    suspend fun deleteCard(@Path("paymentMethodId") paymentMethodId: String): Response<Unit>

    @PUT("/api/payments/cards/{paymentMethodId}/default")
    suspend fun setDefaultCard(@Path("paymentMethodId") paymentMethodId: String): Response<Unit>
}
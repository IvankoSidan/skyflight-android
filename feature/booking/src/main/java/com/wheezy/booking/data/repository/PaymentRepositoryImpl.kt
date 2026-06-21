package com.wheezy.skyflight.feature.booking.data.repository

import android.util.Log
import com.wheezy.skyflight.core.common.network.SmartRetryPolicy
import com.wheezy.skyflight.core.network.api.ApiService
import com.wheezy.skyflight.core.network.model.PaymentSheetRequest
import com.wheezy.skyflight.core.network.model.PaymentSheetResponseDTO
import com.wheezy.skyflight.feature.booking.domain.repository.PaymentRepository
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : PaymentRepository {

    companion object {
        private const val TAG = "PaymentRepository"
    }

    override suspend fun createPaymentSheet(
        bookingId: Long,
        amount: Long,
        currency: String
    ): Response<PaymentSheetResponseDTO> {
        val request = PaymentSheetRequest(bookingId, amount, currency)
        return SmartRetryPolicy.executeWithRetry {
            try {
                apiService.createPaymentSheet(request)
            } catch (e: Exception) {
                Log.e(TAG, "createPaymentSheet error", e)
                throw e
            }
        }
    }
}
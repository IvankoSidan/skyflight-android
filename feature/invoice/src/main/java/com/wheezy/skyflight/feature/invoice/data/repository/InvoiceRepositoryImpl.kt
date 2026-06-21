package com.wheezy.skyflight.feature.invoice.data.repository

import android.util.Log
import com.wheezy.skyflight.core.model.Invoice
import com.wheezy.skyflight.core.model.InvoiceListResponse
import com.wheezy.skyflight.core.model.TaxRate
import com.wheezy.skyflight.core.network.api.InvoiceApiService
import com.wheezy.skyflight.feature.invoice.domain.repository.InvoiceRepository
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepositoryImpl @Inject constructor(
    private val invoiceApiService: InvoiceApiService
) : InvoiceRepository {

    companion object {
        private const val TAG = "InvoiceRepository"
    }

    override suspend fun getInvoiceByBookingId(bookingId: Long): Result<Invoice> {
        return try {
            val response = invoiceApiService.getInvoiceByBookingId(bookingId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getInvoiceByBookingId error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getMyInvoices(page: Int, size: Int): Result<InvoiceListResponse> {
        return try {
            val response = invoiceApiService.getMyInvoices(page, size)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getMyInvoices error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun downloadInvoice(invoiceId: Long): Result<ResponseBody> {
        return try {
            val response = invoiceApiService.downloadInvoice(invoiceId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "downloadInvoice error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun resendInvoiceEmail(bookingId: Long): Result<Unit> {
        return try {
            val response = invoiceApiService.resendInvoiceEmail(bookingId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "resendInvoiceEmail error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getTaxRates(): Result<List<TaxRate>> {
        return try {
            val response = invoiceApiService.getTaxRates()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getTaxRates error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
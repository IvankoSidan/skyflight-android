package com.wheezy.skyflight.core.network.api

import com.wheezy.skyflight.core.model.Invoice
import com.wheezy.skyflight.core.model.InvoiceListResponse
import com.wheezy.skyflight.core.model.TaxRate
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface InvoiceApiService {

    @GET("/api/invoices/booking/{bookingId}")
    suspend fun getInvoiceByBookingId(
        @Path("bookingId") bookingId: Long
    ): Response<Invoice>

    @GET("/api/invoices/my")
    suspend fun getMyInvoices(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<InvoiceListResponse>

    @GET("/api/invoices/{invoiceId}/download")
    @Streaming
    suspend fun downloadInvoice(
        @Path("invoiceId") invoiceId: Long
    ): Response<ResponseBody>

    @POST("/api/invoices/booking/{bookingId}/resend")
    suspend fun resendInvoiceEmail(
        @Path("bookingId") bookingId: Long
    ): Response<Map<String, String>>

    @GET("/api/invoices/tax-rates")
    suspend fun getTaxRates(): Response<List<TaxRate>>
}
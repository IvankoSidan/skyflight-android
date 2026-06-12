package com.wheezy.skyflight.feature.invoice.domain.repository

import com.wheezy.skyflight.core.model.Invoice
import com.wheezy.skyflight.core.model.InvoiceListResponse
import com.wheezy.skyflight.core.model.TaxRate
import okhttp3.ResponseBody

interface InvoiceRepository {
    suspend fun getInvoiceByBookingId(bookingId: Long): Result<Invoice>
    suspend fun getMyInvoices(page: Int, size: Int): Result<InvoiceListResponse>
    suspend fun downloadInvoice(invoiceId: Long): Result<ResponseBody>
    suspend fun resendInvoiceEmail(bookingId: Long): Result<Unit>
    suspend fun getTaxRates(): Result<List<TaxRate>>
}
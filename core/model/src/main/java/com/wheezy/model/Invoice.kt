package com.wheezy.skyflight.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class Invoice(
    val id: Long,
    val invoiceNumber: String,
    val bookingId: Long,
    val issueDate: String,
    val dueDate: String,
    val currency: String,
    val subtotal: BigDecimal,
    val discountAmount: BigDecimal,
    val taxRate: BigDecimal,
    val taxAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val status: String,
    val pdfUrl: String?,
    val downloadUrl: String?
) : Parcelable

@Parcelize
data class InvoiceListResponse(
    val invoices: List<Invoice>,
    val totalCount: Int,
    val totalPages: Int,
    val currentPage: Int
) : Parcelable

@Parcelize
data class TaxRate(
    val countryCode: String,
    val countryName: String,
    val taxName: String,
    val taxRate: BigDecimal
) : Parcelable
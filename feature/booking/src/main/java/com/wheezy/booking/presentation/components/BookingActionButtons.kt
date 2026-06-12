package com.wheezy.skyflight.feature.booking.presentation.components.booking

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wheezy.skyflight.core.model.Booking
import com.wheezy.skyflight.core.model.canBeCancelled
import com.wheezy.skyflight.core.model.canBeDeleted
import com.wheezy.skyflight.core.model.canBePaid
import com.wheezy.skyflight.core.ui.components.GradientButton

@Composable
fun BookingActionButtons(
    booking: Booking,
    showReviewButton: Boolean,
    showInvoiceButton: Boolean,
    isPaying: Boolean,
    isLoading: Boolean,
    onShareClick: () -> Unit,
    onCopyClick: () -> Unit,
    onReviewClick: (() -> Unit)?,
    onInvoiceClick: (() -> Unit)?,
    onPayClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка Share
        IconButton(
            onClick = onShareClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Кнопка Copy
        IconButton(
            onClick = onCopyClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Кнопка Invoice (чек)
        if (showInvoiceButton) {
            IconButton(
                onClick = { onInvoiceClick?.invoke() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = "Invoice",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Кнопка Review (отзыв)
        if (showReviewButton) {
            GradientButton(
                onClick = { onReviewClick?.invoke() },
                text = "Write Review",
                enabled = true,
                colors = listOf(Color(0xFFFF9800), Color(0xFFF57C00)),
                modifier = Modifier.wrapContentWidth()
            )
        }

        // Кнопки действий (Pay, Cancel, Delete)
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
            isPaying -> {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
            booking.status.canBePaid() -> {
                GradientButton(
                    onClick = onPayClick,
                    text = "Pay",
                    enabled = true,
                    colors = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
                )
            }
            booking.status.canBeCancelled() -> {
                GradientButton(
                    onClick = onCancelClick,
                    text = "Cancel",
                    enabled = true,
                    colors = listOf(MaterialTheme.colorScheme.tertiary, Color(0xFFF57C00))
                )
            }
            booking.status.canBeDeleted() -> {
                GradientButton(
                    onClick = onDeleteClick,
                    text = "Delete",
                    enabled = true,
                    colors = listOf(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error)
                )
            }
        }
    }
}
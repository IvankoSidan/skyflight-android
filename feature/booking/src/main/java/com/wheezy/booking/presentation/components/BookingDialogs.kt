package com.wheezy.skyflight.feature.booking.presentation.components.booking

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
fun PayDeleteDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onPayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Booking Options") },
            text = { Text("Would you like to pay for or delete this booking?") },
            confirmButton = {
                TextButton(onClick = onPayClick) { Text("Pay") }
            },
            dismissButton = {
                TextButton(onClick = onDeleteClick) { Text("Delete") }
            }
        )
    }
}

@Composable
fun CancelDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Cancel Booking") },
            text = { Text("Are you sure you want to cancel this booking?") },
            confirmButton = {
                TextButton(onClick = onConfirm) { Text("Yes, Cancel") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("No") }
            }
        )
    }
}

@Composable
fun DeleteDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Delete Booking") },
            text = { Text("Do you really want to delete this booking permanently?") },
            confirmButton = {
                TextButton(onClick = onConfirm) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        )
    }
}
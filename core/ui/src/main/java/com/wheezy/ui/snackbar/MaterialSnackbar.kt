package com.wheezy.skyflight.core.ui.snackbar

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MaterialSnackbar(
    snackbar: AppSnackbar,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val containerColor = when (snackbar.type) {
        SnackbarType.SUCCESS -> colorScheme.primaryContainer
        SnackbarType.ERROR -> colorScheme.errorContainer
        SnackbarType.INFO -> colorScheme.inverseSurface
    }

    val contentColor = when (snackbar.type) {
        SnackbarType.SUCCESS -> colorScheme.onPrimaryContainer
        SnackbarType.ERROR -> colorScheme.onErrorContainer
        SnackbarType.INFO -> colorScheme.inverseOnSurface
    }

    val icon = when (snackbar.type) {
        SnackbarType.SUCCESS -> Icons.Default.CheckCircle
        SnackbarType.ERROR -> Icons.Default.Error
        SnackbarType.INFO -> Icons.Default.Info
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = contentColor)

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = snackbar.message,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            snackbar.actionLabel?.let {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = it,
                    color = colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
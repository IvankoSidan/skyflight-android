package com.wheezy.skyflight.feature.booking.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheezy.skyflight.core.model.Seat
import com.wheezy.skyflight.core.model.SeatStatus
import com.wheezy.skyflight.core.ui.components.GlassCard
import com.wheezy.skyflight.core.ui.components.GlassCardDefaults

@Composable
fun SeatItem(
    seat: Seat,
    onSeatClick: () -> Unit
) {
    val isClickable = seat.status == SeatStatus.AVAILABLE || seat.status == SeatStatus.SELECTED
    val colors = MaterialTheme.colorScheme

    val (backgroundColor, textColor) = when (seat.status) {
        SeatStatus.AVAILABLE -> colors.primary to colors.onPrimary
        SeatStatus.UNAVAILABLE -> colors.surfaceVariant to colors.onSurfaceVariant
        SeatStatus.SELECTED -> colors.tertiary to colors.onTertiary
        SeatStatus.EMPTY -> Color.Transparent to Color.Transparent
    }

    if (seat.status == SeatStatus.EMPTY) {
        Box(
            modifier = Modifier
                .padding(4.dp)
                .size(28.dp)
        )
    } else {
        GlassCard(
            modifier = Modifier
                .padding(4.dp)
                .size(28.dp)
                .then(if (isClickable) Modifier.clickable { onSeatClick() } else Modifier),
            config = GlassCardDefaults.subtle
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = seat.name,
                    color = textColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp
                )
            }
        }
    }
}
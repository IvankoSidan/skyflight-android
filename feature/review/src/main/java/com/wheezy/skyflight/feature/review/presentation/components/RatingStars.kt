package com.wheezy.skyflight.feature.review.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wheezy.skyflight.core.model.AirlineRating

@Composable
fun RatingStars(
    rating: Int,
    maxStars: Int = 5,
    size: Int = 24,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(maxStars) { index ->
            Icon(
                imageVector = if (index < rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(size.dp)
            )
            if (index < maxStars - 1) {
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}

@Composable
fun RatingStarsInteractive(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    maxStars: Int = 5,
    size: Int = 32,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(maxStars) { index ->
            val starNumber = index + 1
            Icon(
                imageVector = if (starNumber <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "Rate $starNumber stars",
                tint = if (starNumber <= rating) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(size.dp)
                    .padding(4.dp)
                    .clickable { onRatingChanged(starNumber) }
            )
        }
    }
}

@Composable
fun AverageRatingDisplay(
    rating: AirlineRating,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = rating.averageRatingFormatted,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        RatingStars(
            rating = rating.starsCount,
            size = 16
        )
        Text(
            text = "Based on ${rating.totalReviews} reviews",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RatingDistributionChart(
    ratingDistribution: Map<Int, Int>,
    totalReviews: Int,
    modifier: Modifier = Modifier
) {
    if (totalReviews == 0) return

    Column(modifier = modifier) {
        (5 downTo 1).forEach { stars ->
            val count = ratingDistribution[stars] ?: 0
            val percentage = if (totalReviews > 0) (count.toFloat() / totalReviews) * 100f else 0f

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = "$stars ★",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(32.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(percentage / 100f)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                shape = RoundedCornerShape(6.dp)
                            )
                    )
                }

                Text(
                    text = "$count",
                    fontSize = 12.sp,
                    modifier = Modifier.width(32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
    }
}
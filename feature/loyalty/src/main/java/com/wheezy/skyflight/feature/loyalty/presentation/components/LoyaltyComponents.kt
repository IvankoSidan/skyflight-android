package com.wheezy.skyflight.feature.loyalty.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wheezy.skyflight.core.model.TierBenefit

@Composable
fun TierCard(
    tier: TierBenefit,
    modifier: Modifier = Modifier,
    isCurrent: Boolean = false
) {
    val backgroundColor = when (tier.tier) {
        "BRONZE" -> Color(0xFFCD7F32)
        "SILVER" -> Color(0xFFC0C0C0)
        "GOLD" -> Color(0xFFFFD700)
        "PLATINUM" -> Color(0xFFE5E4E2)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) {
                backgroundColor.copy(alpha = 0.3f)
            } else {
                backgroundColor.copy(alpha = 0.15f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrent) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isCurrent) {
                Text(
                    text = "★ CURRENT ★",
                    style = MaterialTheme.typography.labelSmall,
                    color = backgroundColor,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Text(
                text = tier.tier,
                style = MaterialTheme.typography.titleLarge,
                color = backgroundColor,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Text(
                text = "${tier.cashbackPercent}% cashback",
                style = MaterialTheme.typography.bodyMedium
            )

            if (tier.freeSeatSelection) {
                Text("✓ Free seat selection", style = MaterialTheme.typography.bodySmall)
            }
            if (tier.priorityBoarding) {
                Text("✓ Priority boarding", style = MaterialTheme.typography.bodySmall)
            }
            if (tier.freeBaggageKg > 0) {
                Text("✓ ${tier.freeBaggageKg}kg free baggage", style = MaterialTheme.typography.bodySmall)
            }

            Text(
                text = "Min ${tier.minPoints} points",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
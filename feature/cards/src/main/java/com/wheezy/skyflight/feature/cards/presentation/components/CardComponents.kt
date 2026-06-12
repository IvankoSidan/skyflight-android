package com.wheezy.skyflight.feature.cards.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.wheezy.skyflight.core.model.SavedCard
import com.wheezy.skyflight.core.ui.components.GlassCard

data class CardBrandInfo(
    val icon: ImageVector,
    val color: Color
)

@Composable
fun getCardBrandInfo(cardBrand: String): CardBrandInfo {
    return when (cardBrand.lowercase()) {
        "visa" -> CardBrandInfo(
            icon = Icons.Filled.CreditCard,
            color = Color(0xFF1A1F71)
        )
        "mastercard" -> CardBrandInfo(
            icon = Icons.Filled.Payment,
            color = Color(0xFFEB001B)
        )
        "amex" -> CardBrandInfo(
            icon = Icons.Filled.AccountBalance,
            color = Color(0xFF006FCF)
        )
        else -> CardBrandInfo(
            icon = Icons.Filled.CreditCard,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun CardItem(
    card: SavedCard,
    onDeleteClick: () -> Unit,
    onSetDefaultClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brandInfo = getCardBrandInfo(card.cardBrand)

    GlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = brandInfo.icon,
                contentDescription = card.cardBrand,
                tint = brandInfo.color,
                modifier = Modifier.size(44.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = "Expires: ${card.expiryDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (card.isDefault) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Default card",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Default",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(onClick = onSetDefaultClick) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Set as default",
                        tint = if (card.isDefault)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete card",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
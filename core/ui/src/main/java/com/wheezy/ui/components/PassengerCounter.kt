package com.wheezy.skyflight.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PassengerCounter(
    title: String,
    modifier: Modifier = Modifier,
    onItemSelected: (String) -> Unit
) {
    var passengerCount by remember { mutableStateOf(1) }

    GlassCard(
        modifier = modifier
            .padding(top = 8.dp)
            .height(60.dp),
        blurRadius = 12f,
        tintAlpha = 0.12f,
        strokeAlpha = 0.15f,
        cornerRadius = 10.dp,
        enableGlow = false
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp).padding(start = 4.dp)
            )
            Box(
                modifier = Modifier.fillMaxHeight().weight(1f).clickable {
                    if (passengerCount > 0) {
                        passengerCount--
                        onItemSelected(passengerCount.toString())
                    }
                },
                contentAlignment = Alignment.Center
            ) {
                Text("-", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "$passengerCount $title",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier.fillMaxHeight().weight(1f).clickable {
                    passengerCount++
                    onItemSelected(passengerCount.toString())
                },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
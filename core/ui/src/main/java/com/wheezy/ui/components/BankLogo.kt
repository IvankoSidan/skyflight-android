package com.wheezy.skyflight.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BankLogo() {
    GlassCard(
        modifier = Modifier.size(40.dp),
        blurRadius = 12f,
        tintAlpha = 0.2f,
        strokeAlpha = 0.15f,
        cornerRadius = 20.dp,
        enableGlow = false
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface, CircleShape)
        )
    }
}
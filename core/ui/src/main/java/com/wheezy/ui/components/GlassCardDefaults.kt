package com.wheezy.skyflight.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GlassCardDefaults {
    val subtle = GlassCardConfig(
        blurRadius = 10f,
        tintAlpha = 0.10f,
        strokeAlpha = 0.15f,
        cornerRadius = 12.dp,
        strokeWidth = 1.dp,
        enableGradient = false,
        enableGlow = false
    )

    val light = GlassCardConfig(
        blurRadius = 18f,
        tintAlpha = 0.20f,
        strokeAlpha = 0.25f,
        cornerRadius = 16.dp,
        strokeWidth = 1.dp,
        enableGradient = true,
        enableGlow = false
    )

    val medium = GlassCardConfig(
        blurRadius = 24f,
        tintAlpha = 0.25f,
        strokeAlpha = 0.30f,
        cornerRadius = 20.dp,
        strokeWidth = 1.dp,
        enableGradient = true,
        enableGlow = true
    )

    val heavy = GlassCardConfig(
        blurRadius = 30f,
        tintAlpha = 0.35f,
        strokeAlpha = 0.40f,
        cornerRadius = 28.dp,
        strokeWidth = 1.dp,
        enableGradient = true,
        enableGlow = true
    )
}

data class GlassCardConfig(
    val blurRadius: Float,
    val tintAlpha: Float,
    val strokeAlpha: Float,
    val cornerRadius: Dp,
    val strokeWidth: Dp,
    val enableGradient: Boolean,
    val enableGlow: Boolean
)

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    config: GlassCardConfig = GlassCardDefaults.medium,
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = modifier,
        blurRadius = config.blurRadius,
        tintAlpha = config.tintAlpha,
        strokeAlpha = config.strokeAlpha,
        cornerRadius = config.cornerRadius,
        strokeWidth = config.strokeWidth,
        enableGradient = config.enableGradient,
        enableGlow = config.enableGlow,
        content = content
    )
}
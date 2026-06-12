package com.wheezy.skyflight.core.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

fun Modifier.glassBlur(
    blurRadius: Float = 20f,
    tintColor: Color,
    strokeColor: Color,
    glowColor: Color = Color.White.copy(alpha = 0.2f),
    cornerRadius: Float = 20f,
    strokeWidth: Float = 1f,
    enableGradient: Boolean = true,
    enableGlow: Boolean = true
): Modifier = this.drawBehind {
    drawIntoCanvas { canvas ->
        val backgroundPaint = Paint().apply {
            asFrameworkPaint().apply {
                color = tintColor.toArgb()
                setMaskFilter(BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL))
            }
        }

        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = cornerRadius,
            radiusY = cornerRadius,
            paint = backgroundPaint
        )

        if (enableGradient) {
            val gradientPaint = Paint().apply {
                asFrameworkPaint().apply {
                    val gradientColors = intArrayOf(
                        tintColor.copy(alpha = 0.15f).toArgb(),
                        Color.Transparent.toArgb(),
                        tintColor.copy(alpha = 0.05f).toArgb()
                    )
                    val shader = android.graphics.LinearGradient(
                        0f, 0f,
                        size.width, size.height,
                        gradientColors,
                        floatArrayOf(0f, 0.5f, 1f),
                        android.graphics.Shader.TileMode.CLAMP
                    )
                    setShader(shader)
                    setMaskFilter(BlurMaskFilter(blurRadius * 0.5f, BlurMaskFilter.Blur.NORMAL))
                }
            }

            canvas.drawRoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                radiusX = cornerRadius,
                radiusY = cornerRadius,
                paint = gradientPaint
            )
        }

        val strokePaint = Paint().apply {
            this.color = strokeColor
            this.style = PaintingStyle.Stroke
            this.strokeWidth = strokeWidth.dp.toPx()
            asFrameworkPaint().apply {
                setMaskFilter(BlurMaskFilter(2f, BlurMaskFilter.Blur.SOLID))
            }
        }

        canvas.drawRoundRect(
            left = strokeWidth.dp.toPx() / 2,
            top = strokeWidth.dp.toPx() / 2,
            right = size.width - (strokeWidth.dp.toPx() / 2),
            bottom = size.height - (strokeWidth.dp.toPx() / 2),
            radiusX = cornerRadius - strokeWidth.dp.toPx() / 2,
            radiusY = cornerRadius - strokeWidth.dp.toPx() / 2,
            paint = strokePaint
        )

        if (enableGlow) {
            val glowPaint = Paint().apply {
                this.color = glowColor
                asFrameworkPaint().apply {
                    setMaskFilter(BlurMaskFilter(8f, BlurMaskFilter.Blur.OUTER))
                }
            }

            canvas.drawRoundRect(
                left = (-2).dp.toPx(),
                top = (-2).dp.toPx(),
                right = size.width + 2.dp.toPx(),
                bottom = size.height + 2.dp.toPx(),
                radiusX = cornerRadius + 2.dp.toPx(),
                radiusY = cornerRadius + 2.dp.toPx(),
                paint = glowPaint
            )
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    blurRadius: Float = 20f,
    tintAlpha: Float = 0.2f,
    strokeAlpha: Float = 0.3f,
    cornerRadius: Dp = 20.dp,
    strokeWidth: Dp = 1.dp,
    enableGradient: Boolean = true,
    enableGlow: Boolean = true,
    content: @Composable () -> Unit
) {
    val tintColor = MaterialTheme.colorScheme.surface.copy(alpha = tintAlpha)
    val strokeColor = MaterialTheme.colorScheme.outline.copy(alpha = strokeAlpha)
    val glowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .glassBlur(
                blurRadius = blurRadius,
                tintColor = tintColor,
                strokeColor = strokeColor,
                glowColor = glowColor,
                cornerRadius = cornerRadius.value,
                strokeWidth = strokeWidth.value,
                enableGradient = enableGradient,
                enableGlow = enableGlow
            )
    ) {
        content()
    }
}
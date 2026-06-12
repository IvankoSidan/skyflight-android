package com.wheezy.skyflight.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wheezy.skyflight.core.model.ThemeOption

val LocalTheme = staticCompositionLocalOf { ThemeOption.Auto }

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun MyAppTheme(
    themeOption: ThemeOption = ThemeOption.Auto,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeOption) {
        ThemeOption.Auto -> isSystemInDarkTheme()
        ThemeOption.Light -> false
        ThemeOption.Dark -> true
    }

    val colors = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF915FFF),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF2A1652),

            secondary = Color(0xFFFB4B87),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFF5A102D),

            tertiary = Color(0xFFFFB300),
            onTertiary = Color(0xFF241A00),
            tertiaryContainer = Color(0xFF634500),

            background = Color(0xFF0B0E14),
            onBackground = Color(0xFFE2E2E6),

            surface = Color(0xFF161B22),
            onSurface = Color(0xFFF0F0F5),

            surfaceVariant = Color(0xFF21262D),
            onSurfaceVariant = Color(0xFF8B949E),

            error = Color(0xFFFF6B6B),
            outline = Color(0xFF30363D)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6C38FF),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFEBE4FF),

            secondary = Color(0xFFE91E63),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFD9E2),

            tertiary = Color(0xFFF57C00),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFFFF0E1),

            background = Color(0xFFF5F7FA),
            onBackground = Color(0xFF1A1C1E),

            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A1C1E),

            surfaceVariant = Color(0xFFEBEEF2),
            onSurfaceVariant = Color(0xFF5D6671),

            error = Color(0xFFBA1A1A),
            outline = Color(0xFFD1D9E0)
        )
    }

    CompositionLocalProvider(
        LocalTheme provides themeOption
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
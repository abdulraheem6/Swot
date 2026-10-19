package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalAppTheme = staticCompositionLocalOf { ThemeRegistry.getTheme("bright_light") }

@Composable
fun MyApplicationTheme(
    themeId: String = "bright_light",
    content: @Composable () -> Unit
) {
    val themeConfig = ThemeRegistry.getTheme(themeId)
    val colorScheme = if (themeConfig.isLight) {
        lightColorScheme(
            primary = themeConfig.primary,
            onPrimary = Color.White,
            primaryContainer = themeConfig.primaryContainer,
            onPrimaryContainer = themeConfig.primary,
            secondary = themeConfig.primaryLight,
            onSecondary = Color.White,
            tertiary = SafeGreen,
            background = themeConfig.background,
            onBackground = themeConfig.textPrimary,
            surface = themeConfig.surface,
            onSurface = themeConfig.textPrimary,
            surfaceVariant = themeConfig.surfaceVariant,
            onSurfaceVariant = themeConfig.textSecondary,
            outline = themeConfig.borderSubtle,
            error = UrgentRed,
            onError = Color.White
        )
    } else {
        darkColorScheme(
            primary = themeConfig.primary,
            onPrimary = Color.White,
            primaryContainer = themeConfig.primaryContainer,
            onPrimaryContainer = themeConfig.primaryLight,
            secondary = themeConfig.primaryLight,
            onSecondary = Color.White,
            tertiary = SafeGreen,
            background = themeConfig.background,
            onBackground = themeConfig.textPrimary,
            surface = themeConfig.surface,
            onSurface = themeConfig.textPrimary,
            surfaceVariant = themeConfig.surfaceVariant,
            onSurfaceVariant = themeConfig.textSecondary,
            outline = themeConfig.borderSubtle,
            error = UrgentRed,
            onError = Color.White
        )
    }

    CompositionLocalProvider(LocalAppTheme provides themeConfig) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

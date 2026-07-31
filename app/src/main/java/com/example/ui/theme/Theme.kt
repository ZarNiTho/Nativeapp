package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    LIGHT, DARK, GOLD_CHARCOAL
}

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E2340),
    secondary = BrandSky,
    tertiary = BrandLime,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkElevated,
    onBackground = Color(0xFFEDF2F9),
    onSurface = Color(0xFFEDF2F9),
    onSurfaceVariant = Color(0xFFC8D2E0)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandPrimaryLight,
    secondary = BrandSky,
    tertiary = BrandRose,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightElevated,
    onBackground = Color(0xFF0F1A2E),
    onSurface = Color(0xFF0F1A2E),
    onSurfaceVariant = Color(0xFF2D3A4F)
)

private val GoldCharcoalColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = GoldCharcoalBg,
    primaryContainer = Color(0xFF2A2416),
    secondary = BrandLime,
    tertiary = BrandGold,
    background = GoldCharcoalBg,
    surface = GoldCharcoalSurface,
    surfaceVariant = GoldCharcoalElevated,
    onBackground = Color(0xFFF0ECE0),
    onSurface = Color(0xFFF0ECE0),
    onSurfaceVariant = Color(0xFFC8C0B0)
)

@Composable
fun MobileAnswerTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.GOLD_CHARCOAL -> GoldCharcoalColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

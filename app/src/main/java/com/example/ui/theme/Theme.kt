package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = CyberEquals,
    onPrimary = CyberOnEquals,
    primaryContainer = CyberSurfaceVariant,
    onPrimaryContainer = CyberEquals,
    secondary = CyberOperator,
    onSecondary = Color.Black,
    secondaryContainer = CyberOperatorContainer,
    onSecondaryContainer = CyberOperator,
    tertiary = CyberMemory,
    onTertiary = Color.White,
    background = CyberBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = CyberSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    error = CyberClear,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightEquals,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = LightEquals,
    secondary = LightOperator,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEDD5),
    onSecondaryContainer = LightOperator,
    tertiary = LightMemory,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF64748B),
    error = LightClear,
    onError = Color.White
)

@Composable
fun CalculatorTheme(
    themeMode: String = "CYBER",
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "LIGHT" -> false
        "DARK", "CYBER" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) CyberColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

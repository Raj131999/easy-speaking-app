package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AppLightColorScheme = lightColorScheme(
    primary = PolishTeal,
    onPrimary = Color.White,
    primaryContainer = PolishTealLight,
    onPrimaryContainer = PolishTealDark,
    secondary = PolishIndigoText,
    onSecondary = Color.White,
    secondaryContainer = PolishIndigoBg,
    onSecondaryContainer = PolishIndigoText,
    tertiary = Orange600,
    onTertiary = Color.White,
    tertiaryContainer = Orange50,
    onTertiaryContainer = Orange600,
    background = PolishBg,
    onBackground = PolishNavy,
    surface = Color.White,
    onSurface = PolishNavy,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,
    outline = Slate300,
    outlineVariant = Slate200,
    error = Red500,
    onError = Color.White,
    errorContainer = Red50,
    onErrorContainer = Red700
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppLightColorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}

package com.doseyenc.wavelift.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── WaveLift Color Palette ──────────────────────────────────────────

// Midnight Theme (Dark)
private val WavePrimary = Color(0xFF1E88E5)        // Vibrant Blue
private val WavePrimaryDark = Color(0xFF1565C0)     // Deep Blue
private val WaveSecondary = Color(0xFF00BCD4)       // Cyan accent
private val WaveSurface = Color(0xFF161B22)         // GitHub-like dark surface
private val WaveBackground = Color(0xFF0D1117)      // Deep dark background
private val WaveSurfaceVariant = Color(0xFF21262D)   // Slightly lighter surface
private val WaveOnPrimary = Color(0xFFFFFFFF)
private val WaveOnSurface = Color(0xFFE6EDF3)       // Light text on dark
private val WaveOnSurfaceVariant = Color(0xFF8B949E) // Muted text
private val WaveError = Color(0xFFFF6B6B)           // Soft red
private val WaveSuccess = Color(0xFF4CAF50)         // Green for completed

// Light Theme
private val WavePrimaryLight = Color(0xFF1976D2)
private val WaveSurfaceLight = Color(0xFFF6F8FA)
private val WaveBackgroundLight = Color(0xFFFFFFFF)
private val WaveSurfaceVariantLight = Color(0xFFE8ECF0)
private val WaveOnSurfaceLight = Color(0xFF1F2328)
private val WaveOnSurfaceVariantLight = Color(0xFF656D76)

private val WaveLiftDarkColorScheme = darkColorScheme(
    primary = WavePrimary,
    onPrimary = WaveOnPrimary,
    primaryContainer = WavePrimaryDark,
    onPrimaryContainer = WaveOnPrimary,
    secondary = WaveSecondary,
    onSecondary = WaveOnPrimary,
    surface = WaveSurface,
    onSurface = WaveOnSurface,
    surfaceVariant = WaveSurfaceVariant,
    onSurfaceVariant = WaveOnSurfaceVariant,
    background = WaveBackground,
    onBackground = WaveOnSurface,
    error = WaveError,
    onError = WaveOnPrimary,
    outline = Color(0xFF30363D),
    outlineVariant = Color(0xFF21262D)
)

private val WaveLiftLightColorScheme = lightColorScheme(
    primary = WavePrimaryLight,
    onPrimary = WaveOnPrimary,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = WaveSecondary,
    onSecondary = WaveOnPrimary,
    surface = WaveSurfaceLight,
    onSurface = WaveOnSurfaceLight,
    surfaceVariant = WaveSurfaceVariantLight,
    onSurfaceVariant = WaveOnSurfaceVariantLight,
    background = WaveBackgroundLight,
    onBackground = WaveOnSurfaceLight,
    error = Color(0xFFD32F2F),
    onError = WaveOnPrimary,
    outline = Color(0xFFD0D7DE),
    outlineVariant = Color(0xFFE8ECF0)
)

// ── Typography ───────────────────────────────────────────────────────

private val WaveLiftTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

// ── Shapes ─────────────────────────────────────────────────────────

private val WaveLiftShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// ── Public Colors ──────────────────────────────────────────────────

object WaveLiftColors {
    val Success = WaveSuccess
    val GradientStart = WavePrimary
    val GradientEnd = WaveSecondary
    val Primary = WavePrimary
}

// ── Theme Composable ───────────────────────────────────────────────

@Composable
fun WaveLiftTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) WaveLiftDarkColorScheme else WaveLiftLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WaveLiftTypography,
        shapes = WaveLiftShapes,
        content = content
    )
}

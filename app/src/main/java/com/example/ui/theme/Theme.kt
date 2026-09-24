package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class NanoCustomColors(
    val bg: Color,
    val bgGradientEnd: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val headerBg: Color,
    val headerText: Color,
    val headerSubtext: Color,
    val border: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val badgeSuccessBg: Color,
    val badgeSuccessText: Color,
    val badgeDangerBg: Color,
    val badgeDangerText: Color,
    val badgeBlueBg: Color,
    val badgeBlueText: Color,
    val badgeAmberBg: Color,
    val badgeAmberText: Color,
    val badgeOrangeBg: Color,
    val badgeOrangeText: Color,
    val navBg: Color,
    val navBorder: Color,
    val navPillActive: Color
)

val LocalNanoColors = staticCompositionLocalOf {
    SoftDarkThemeColors
}

// Backwards-compatible alias for existing composables
val LocalAratdarColors get() = LocalNanoColors

val SoftDarkThemeColors = NanoCustomColors(
    bg = SoftDarkBg,
    bgGradientEnd = SoftDarkBgGradientEnd,
    surface = SoftDarkCardSurface,
    surfaceElevated = SoftDarkCardElevated,
    headerBg = SoftDarkBg,
    headerText = SoftDarkTextPrimary,
    headerSubtext = SoftDarkTextMuted,
    border = SoftDarkCardBorder,
    borderSubtle = SoftDarkCardBorderSubtle,
    textPrimary = SoftDarkTextPrimary,
    textSecondary = SoftDarkTextSecondary,
    textMuted = SoftDarkTextMuted,
    primary = SoftDarkTeal,
    onPrimary = Color(0xFF03221E),
    primaryContainer = SoftDarkTealBg,
    onPrimaryContainer = SoftDarkTealText,
    badgeSuccessBg = SoftDarkEmeraldBg,
    badgeSuccessText = SoftDarkEmeraldText,
    badgeDangerBg = SoftDarkRoseBg,
    badgeDangerText = SoftDarkRoseText,
    badgeBlueBg = SoftDarkBlueBg,
    badgeBlueText = SoftDarkBlueText,
    badgeAmberBg = SoftDarkAmberBg,
    badgeAmberText = SoftDarkAmberText,
    badgeOrangeBg = SoftDarkOrangeBg,
    badgeOrangeText = SoftDarkOrangeText,
    navBg = SoftDarkNavBg,
    navBorder = SoftDarkNavBorder,
    navPillActive = SoftDarkNavPillActive
)

private val DarkColorScheme = darkColorScheme(
    primary = SoftDarkTeal,
    onPrimary = Color(0xFF03221E),
    primaryContainer = SoftDarkTealBg,
    onPrimaryContainer = SoftDarkTealText,
    secondary = SoftDarkOrange,
    onSecondary = Color.Black,
    secondaryContainer = SoftDarkOrangeBg,
    onSecondaryContainer = SoftDarkOrangeText,
    tertiary = SoftDarkEmerald,
    background = SoftDarkBg,
    onBackground = SoftDarkTextPrimary,
    surface = SoftDarkCardSurface,
    onSurface = SoftDarkTextPrimary,
    surfaceVariant = SoftDarkCardElevated,
    onSurfaceVariant = SoftDarkTextSecondary,
    outline = SoftDarkCardBorder,
    error = SoftDarkRose,
    errorContainer = SoftDarkRoseBg
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalNanoColors provides SoftDarkThemeColors) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = Typography,
            content = content
        )
    }
}

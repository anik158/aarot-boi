package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
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

val CreamThemeColors = NanoCustomColors(
    bg = CreamBg,
    bgGradientEnd = CreamBgGradientEnd,
    surface = CreamCardSurface,
    surfaceElevated = CreamCardElevated,
    headerBg = CreamCardSurface,
    headerText = CreamTextPrimary,
    headerSubtext = CreamTextMuted,
    border = CreamCardBorder,
    borderSubtle = CreamCardBorderSubtle,
    textPrimary = CreamTextPrimary,
    textSecondary = CreamTextSecondary,
    textMuted = CreamTextMuted,
    primary = CreamGreen,
    onPrimary = Color(0xFFFFFBF5),
    primaryContainer = CreamGreenBg,
    onPrimaryContainer = CreamGreenText,
    badgeSuccessBg = CreamEmeraldBg,
    badgeSuccessText = CreamEmeraldText,
    badgeDangerBg = CreamTerracottaBg,
    badgeDangerText = CreamTerracottaText,
    badgeBlueBg = CreamBlueBg,
    badgeBlueText = CreamBlueText,
    badgeAmberBg = CreamAmberBg,
    badgeAmberText = CreamAmberText,
    badgeOrangeBg = CreamOrangeBg,
    badgeOrangeText = CreamOrangeText,
    navBg = CreamNavBg,
    navBorder = CreamNavBorder,
    navPillActive = CreamNavPillActive
)

/** Old name kept so existing call sites still compile. */
val SoftDarkThemeColors = CreamThemeColors

val LocalNanoColors = staticCompositionLocalOf {
    CreamThemeColors
}

val LocalAratdarColors get() = LocalNanoColors

private val CreamColorScheme = lightColorScheme(
    primary = CreamGreen,
    onPrimary = Color(0xFFFFFBF5),
    primaryContainer = CreamGreenBg,
    onPrimaryContainer = CreamGreenText,
    secondary = CreamOrange,
    onSecondary = Color(0xFFFFFBF5),
    secondaryContainer = CreamOrangeBg,
    onSecondaryContainer = CreamOrangeText,
    tertiary = CreamBlue,
    background = CreamBg,
    onBackground = CreamTextPrimary,
    surface = CreamCardSurface,
    onSurface = CreamTextPrimary,
    surfaceVariant = CreamCardElevated,
    onSurfaceVariant = CreamTextSecondary,
    outline = CreamCardBorder,
    error = CreamTerracotta,
    errorContainer = CreamTerracottaBg
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalNanoColors provides CreamThemeColors) {
        MaterialTheme(
            colorScheme = CreamColorScheme,
            typography = Typography,
            content = content
        )
    }
}

package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private fun style(size: TextUnit, line: TextUnit, weight: FontWeight = FontWeight.Normal) =
  TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = size,
    lineHeight = line,
    letterSpacing = 0.sp,
  )

// Larger-than-default Material styles so components without an explicit
// fontSize (TextField, dialogs, buttons) are readable for older users.
val Typography =
  Typography(
    headlineSmall = style(AppText.Display, 36.sp, FontWeight.Bold),
    titleLarge = style(AppText.Heading, 32.sp, FontWeight.Bold),
    titleMedium = style(AppText.Title, 26.sp, FontWeight.SemiBold),
    titleSmall = style(AppText.BodyLg, 24.sp, FontWeight.SemiBold),
    bodyLarge = style(AppText.BodyLg, 26.sp),
    bodyMedium = style(AppText.Body, 24.sp),
    bodySmall = style(AppText.Small, 22.sp),
    labelLarge = style(AppText.BodyLg, 24.sp, FontWeight.SemiBold),
    labelMedium = style(AppText.Small, 22.sp, FontWeight.Medium),
    labelSmall = style(AppText.Caption, 20.sp, FontWeight.Medium),
  )

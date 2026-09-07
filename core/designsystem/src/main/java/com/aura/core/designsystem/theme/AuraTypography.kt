package com.aura.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Editorial typography system.
// Display faces use the system serif for a calm, premium musical voice.
// A bundled open-licensed serif can replace FontFamily.Serif later
// without changing any call sites.
// UI faces stay on the platform sans for readability.
object AuraTypography {

    val display = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Light,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    )

    val headline = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Light,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
    )

    val title = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.1.sp,
    )

    val body = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    )

    val metadata = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
    )

    // Wide-tracked micro labels for section headers.
    // Call sites apply uppercase to the string itself.
    val label = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.5.sp,
    )

    // Maps AURA tokens onto Material 3 slots so built-in components
    // inherit the same voice.
    fun asMaterial3(): Typography = Typography(
        displayLarge = display,
        displayMedium = display.copy(fontSize = 28.sp, lineHeight = 34.sp),
        displaySmall = display.copy(fontSize = 24.sp, lineHeight = 30.sp),
        headlineLarge = headline,
        headlineMedium = headline.copy(fontSize = 22.sp, lineHeight = 28.sp),
        headlineSmall = headline.copy(fontSize = 20.sp, lineHeight = 26.sp),
        titleLarge = title,
        titleMedium = title.copy(fontSize = 16.sp, lineHeight = 22.sp),
        titleSmall = title.copy(fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = body,
        bodyMedium = body.copy(fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = metadata,
        labelLarge = label.copy(fontSize = 12.sp, letterSpacing = 0.5.sp),
        labelMedium = label,
        labelSmall = label.copy(fontSize = 10.sp, letterSpacing = 1.sp),
    )
}
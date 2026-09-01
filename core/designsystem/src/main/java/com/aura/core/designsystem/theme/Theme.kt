package com.aura.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ObsidianDarkColorScheme = darkColorScheme(
    primary = ObsidianPrimary,
    secondary = ObsidianSecondary,
    background = ObsidianBackground,
    surface = ObsidianSurface,
    onPrimary = ObsidianBackground,
    onSecondary = ObsidianBackground,
    onBackground = IvorySurface,
    onSurface = IvorySurface,
)

private val IvoryLightColorScheme = lightColorScheme(
    primary = IvoryPrimary,
    secondary = ObsidianSecondary,
    background = IvoryBackground,
    surface = IvorySurface,
    onPrimary = IvorySurface,
    onSecondary = ObsidianBackground,
    onBackground = ObsidianBackground,
    onSurface = ObsidianBackground,
)

@Composable
fun AuraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) ObsidianDarkColorScheme else IvoryLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

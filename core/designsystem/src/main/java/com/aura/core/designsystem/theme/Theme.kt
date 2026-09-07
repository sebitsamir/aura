package com.aura.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// The AURA Obsidian theme.
// Obsidian is the default identity for the first alpha.
// Theme switching (System, Ivory, AMOLED, Atmosphere) arrives later
// and will wrap this composable without changing call sites.
@Composable
fun AuraTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = auraObsidianColorScheme(),
        typography = AuraTypography.asMaterial3(),
        shapes = auraShapes(),
        content = content,
    )
}

private fun auraObsidianColorScheme() = darkColorScheme(
    primary = AuraColors.auraRed,
    onPrimary = AuraColors.onAuraRed,
    primaryContainer = AuraColors.auraRedSoft,
    onPrimaryContainer = AuraColors.textPrimary,
    secondary = AuraColors.textSecondary,
    onSecondary = AuraColors.background,
    tertiary = AuraColors.auraRed,
    onTertiary = AuraColors.onAuraRed,
    background = AuraColors.background,
    onBackground = AuraColors.textPrimary,
    surface = AuraColors.surface,
    onSurface = AuraColors.textPrimary,
    surfaceVariant = AuraColors.surfaceElevated,
    onSurfaceVariant = AuraColors.textSecondary,
    // No red tint on surfaces. Layers stay charcoal.
    surfaceTint = Color.Transparent,
    outline = AuraColors.borderSubtle,
    outlineVariant = AuraColors.borderSubtle,
    error = AuraColors.error,
    onError = AuraColors.onError,
    inverseSurface = AuraColors.textPrimary,
    inverseOnSurface = AuraColors.background,
)

private fun auraShapes() = Shapes(
    small = AuraShape.small,
    medium = AuraShape.medium,
    large = AuraShape.large,
    extraLarge = AuraShape.large,
)
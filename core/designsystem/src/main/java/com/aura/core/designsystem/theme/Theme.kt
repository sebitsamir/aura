package com.aura.core.designsystem.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.aura.core.model.AuraAppearance

private val ObsidianColorScheme = darkColorScheme(
    primary = AuraRed,
    onPrimary = Color.White,
    primaryContainer = AuraRedDeep,
    onPrimaryContainer = Color.White,

    secondary = AuraRedSoft,
    onSecondary = Color.White,

    background = ObsidianBackground,
    onBackground = ObsidianTextPrimary,

    surface = ObsidianSurface,
    onSurface = ObsidianTextPrimary,

    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = ObsidianTextSecondary,

    outline = ObsidianBorder,
    outlineVariant = ObsidianBorderSoft,

    error = AuraErrorDark,
    onError = Color.Black,
)

private val AmoledColorScheme = darkColorScheme(
    primary = AuraRed,
    onPrimary = Color.White,
    primaryContainer = AuraRedDeep,
    onPrimaryContainer = Color.White,

    secondary = AuraRedSoft,
    onSecondary = Color.White,

    background = AmoledBackground,
    onBackground = AmoledTextPrimary,

    surface = AmoledSurface,
    onSurface = AmoledTextPrimary,

    surfaceVariant = AmoledSurfaceElevated,
    onSurfaceVariant = AmoledTextSecondary,

    outline = AmoledBorder,
    outlineVariant = AmoledBorder,

    error = AuraErrorDark,
    onError = Color.Black,
)

private val IvoryColorScheme = lightColorScheme(
    primary = IvoryAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),

    secondary = AuraRed,
    onSecondary = Color.White,

    background = IvoryBackground,
    onBackground = IvoryTextPrimary,

    surface = IvorySurface,
    onSurface = IvoryTextPrimary,

    surfaceVariant = IvorySurfaceElevated,
    onSurfaceVariant = IvoryTextSecondary,

    outline = IvoryBorder,
    outlineVariant = IvoryBorderSoft,

    error = AuraErrorLight,
    onError = Color.White,
)

/**
 * The selected appearance requested by the user.
 *
 * SYSTEM remains SYSTEM here even though the actual rendered color scheme
 * resolves to Obsidian or Ivory depending on Android's current appearance.
 */
val LocalAuraAppearance = staticCompositionLocalOf {
    AuraAppearance.OBSIDIAN
}

object AuraThemeInfo {

    val appearance: AuraAppearance
        @Composable
        @ReadOnlyComposable
        get() = LocalAuraAppearance.current
}

/**
 * Main AURA design-system entry point.
 *
 * ATMOSPHERE intentionally uses the Obsidian base scheme at the application
 * root for now. Album-derived atmosphere belongs to the player/visual
 * atmosphere layer where contrast can be validated against real artwork.
 */
@Composable
fun AuraTheme(
    appearance: AuraAppearance = AuraAppearance.OBSIDIAN,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()

    val colorScheme: ColorScheme = when (appearance) {
        AuraAppearance.SYSTEM -> {
            if (systemDark) {
                ObsidianColorScheme
            } else {
                IvoryColorScheme
            }
        }

        AuraAppearance.OBSIDIAN -> ObsidianColorScheme

        AuraAppearance.IVORY -> IvoryColorScheme

        AuraAppearance.AMOLED -> AmoledColorScheme

        AuraAppearance.ATMOSPHERE -> ObsidianColorScheme
    }

    val isLightAppearance = when (appearance) {
        AuraAppearance.IVORY -> true

        AuraAppearance.SYSTEM -> !systemDark

        AuraAppearance.OBSIDIAN,
        AuraAppearance.AMOLED,
        AuraAppearance.ATMOSPHERE -> false
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()

            if (activity != null) {
                val controller = WindowCompat.getInsetsController(
                    activity.window,
                    view,
                )

                controller.isAppearanceLightStatusBars = isLightAppearance
                controller.isAppearanceLightNavigationBars = isLightAppearance
            }
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalAuraAppearance provides appearance,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this

        is ContextWrapper -> {
            val base = baseContext

            if (base === this) {
                null
            } else {
                base.findActivity()
            }
        }

        else -> null
    }
}
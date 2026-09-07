
package com.aura.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.staticCompositionLocalOf

// Motion language for AURA.
// Motion supports meaning: artwork changes, playback state, selection,
// navigation, sheets, confirmation. No decorative bouncing or glowing.
object AuraMotion {

    object Duration {
        const val FastMs = 150
        const val StandardMs = 300
        const val SlowMs = 500
        const val ArtworkMs = 400
        const val AtmosphereMs = 600
    }

    // Renamed to Easings to avoid shadowing androidx.compose.animation.core.Easing
    object Easings {
        val Standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        val Decelerate: Easing = CubicBezierEasing(0f, 0f, 0.2f, 1f)
    }

    // Provided by the app once the Reduce Motion setting exists.
    // Until then it defaults to false.
    val LocalAuraReduceMotion = staticCompositionLocalOf { false }

    // Standard tween helper. Call sites switch to a snap spec
    // when LocalAuraReduceMotion is true.
    fun <T> standard(durationMillis: Int = Duration.StandardMs): FiniteAnimationSpec<T> =
        tween(durationMillis = durationMillis, easing = Easings.Standard)
}
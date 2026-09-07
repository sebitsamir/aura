package com.aura.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// AURA Obsidian color tokens.
// Obsidian is the default identity. Ivory, AMOLED, and Atmosphere
// arrive later with theme switching and the atmosphere engine.
//
// Rule: auraRed is a signature, not a surface. It is used only for
// active navigation, progress, selected controls, current track,
// and small brand details. Most of the app stays charcoal and silver.
object AuraColors {

    // Layered dark surfaces with soft tonal separation.
    val background = Color(0xFF0B0B0D)
    val surface = Color(0xFF121215)
    val surfaceElevated = Color(0xFF1A1A1F)
    val borderSubtle = Color(0xFF26262B)

    // Text hierarchy. Hierarchy comes from type and spacing, not color alone.
    val textPrimary = Color(0xFFF5F4F2)
    val textSecondary = Color(0xFFA6A4AD)
    val textMuted = Color(0xFF6E6C75)

    // Brand accent.
    val auraRed = Color(0xFFE23A3A)
    val auraRedSoft = Color(0x33E23A3A)
    val onAuraRed = Color(0xFFFFFFFF)

    // Placeholder until the Atmosphere engine provides album-derived accents.
    val dynamicAccent = auraRed

    // Semantic.
    val error = Color(0xFFFFB4AB)
    val onError = Color(0xFF690005)
}
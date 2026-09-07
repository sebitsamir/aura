package com.aura.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Restrained, consistent corner radii.
// AURA avoids cards everywhere. Shapes group content only when needed.
object AuraShape {
    val small = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(12.dp)
    val large = RoundedCornerShape(20.dp)
    val pill = RoundedCornerShape(50)

    // Album and track artwork treatment.
    val artwork = medium
}
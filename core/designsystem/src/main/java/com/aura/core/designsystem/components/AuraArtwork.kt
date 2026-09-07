package com.aura.core.designsystem.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.aura.core.designsystem.theme.AuraColors
import com.aura.core.designsystem.theme.AuraShape
import com.aura.core.designsystem.theme.AuraTypography

// Cached artwork surface used by every screen.
// Coil provides memory and disk caching, so large embedded covers
// are never decoded repeatedly.
// Deterministic fallback: a quiet serif A on an elevated surface.
@Composable
fun AuraArtwork(
    artworkUri: Uri?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = AuraShape.artwork,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(AuraColors.surfaceElevated),
        contentAlignment = Alignment.Center,
    ) {
        if (artworkUri != null) {
            AsyncImage(
                model = artworkUri,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        } else {
            androidx.compose.material3.Text(
                text = "A",
                style = AuraTypography.headline,
                color = AuraColors.textMuted,
            )
        }
    }
}
package com.aura.core.designsystem.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.core.designsystem.theme.AuraColors
import com.aura.core.designsystem.theme.AuraShape
import com.aura.core.designsystem.theme.AuraSpacing
import com.aura.core.designsystem.theme.AuraTypography

@Composable
fun AuraMiniPlayer(
    title: String,
    artist: String,
    artworkUri: Uri?,
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AuraColors.surface)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(AuraColors.borderSubtle)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(AuraColors.auraRed)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AuraSpacing.base, vertical = AuraSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuraArtwork(
                artworkUri = artworkUri,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                shape = AuraShape.small
            )
            Spacer(modifier = Modifier.width(AuraSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AuraTypography.title.copy(fontSize = 14.sp),
                    color = AuraColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artist,
                    style = AuraTypography.metadata,
                    color = AuraColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(AuraSpacing.sm))
            
            IconButton(onClick = onPrevious) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", tint = AuraColors.textPrimary)
            }
            IconButton(onClick = onPlayPause) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = AuraColors.auraRed
                )
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = AuraColors.textPrimary)
            }
        }
    }
}
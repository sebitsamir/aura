package com.aura.feature.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.core.designsystem.components.AuraArtwork
import com.aura.core.designsystem.components.AuraBrandMark
import com.aura.core.designsystem.components.AuraEmptyState
import com.aura.core.designsystem.theme.AuraColors
import com.aura.core.designsystem.theme.AuraShape
import com.aura.core.designsystem.theme.AuraSpacing
import com.aura.core.designsystem.theme.AuraTypography
import com.aura.core.model.RepeatMode
import com.aura.core.playback.PlaybackUiState
import kotlin.math.max

@Composable
fun PlayerRoute(
    onBackClick: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PlayerScreen(uiState, viewModel::onEvent, onBackClick)
}

@Composable
fun PlayerScreen(
    uiState: PlayerScreenUiState,
    onEvent: (PlayerScreenEvent) -> Unit,
    onBackClick: () -> Unit,
) {
    val playback = uiState.playback
    val song = playback.currentSong
    if (song == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(AuraColors.background),
            contentAlignment = Alignment.Center,
        ) {
            AuraEmptyState(
                title = "Nothing playing",
                message = "Choose a song from Home or Library to begin.",
                actionLabel = "Go back",
                onAction = onBackClick,
            )
        }
        return
    }

    val duration = max(playback.durationMs, 1L)
    val artworkUri = "content://media/external/audio/albumart/${song.albumId}".toUri()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraColors.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = AuraSpacing.lg),
    ) {
        PlayerHeader(onBackClick)
        Box(Modifier.fillMaxWidth().height(1.dp).background(AuraColors.auraRed))
        AuraArtwork(
            artworkUri = artworkUri,
            contentDescription = "${song.album} artwork",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AuraSpacing.lg, vertical = AuraSpacing.lg)
                .aspectRatio(1f),
            shape = AuraShape.artwork,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = AuraSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    song.title,
                    style = AuraTypography.display.copy(fontSize = 30.sp, lineHeight = 36.sp),
                    color = AuraColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(song.artist, style = AuraTypography.body, color = AuraColors.textSecondary, maxLines = 1)
                Text(
                    song.album,
                    style = AuraTypography.body.copy(fontFamily = FontFamily.Serif),
                    color = AuraColors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                Icons.Default.FavoriteBorder,
                contentDescription = "Favorites arrive in phase 5",
                tint = AuraColors.textMuted,
                modifier = Modifier.size(34.dp),
            )
        }
        Spacer(Modifier.height(AuraSpacing.md))
        Slider(
            value = playback.positionMs.toFloat().coerceIn(0f, duration.toFloat()),
            onValueChange = { onEvent(PlayerScreenEvent.SeekTo(it.toLong())) },
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier.padding(horizontal = AuraSpacing.lg),
            colors = SliderDefaults.colors(
                thumbColor = AuraColors.textPrimary,
                activeTrackColor = AuraColors.auraRed,
                inactiveTrackColor = AuraColors.borderSubtle,
            ),
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = AuraSpacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(formatDuration(playback.positionMs), style = AuraTypography.metadata, color = AuraColors.textSecondary)
            Text("-${formatDuration((duration - playback.positionMs).coerceAtLeast(0))}", style = AuraTypography.metadata, color = AuraColors.textSecondary)
        }
        Spacer(Modifier.height(AuraSpacing.lg))
        PlaybackControls(playback, onEvent)
        Spacer(Modifier.height(AuraSpacing.xl))
        PlayerDestinations()
    }
}

@Composable
private fun PlayerHeader(onBackClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = AuraSpacing.sm, vertical = AuraSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Default.KeyboardArrowDown, "Close player", tint = AuraColors.textPrimary)
        }
        AuraBrandMark(Modifier.size(64.dp), "AURA")
        Icon(Icons.Default.MoreVert, "Options arrive with the player phase", tint = AuraColors.textMuted)
    }
}

@Composable
private fun PlaybackControls(playback: PlaybackUiState, onEvent: (PlayerScreenEvent) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = AuraSpacing.base),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerControl(Icons.Default.Shuffle, "Shuffle", playback.shuffleEnabled) {
            onEvent(PlayerScreenEvent.ToggleShuffle)
        }
        PlayerControl(Icons.Default.SkipPrevious, "Previous") { onEvent(PlayerScreenEvent.Previous) }
        IconButton(
            onClick = { onEvent(PlayerScreenEvent.TogglePlayPause) },
            modifier = Modifier.size(76.dp).background(AuraColors.surfaceElevated, CircleShape),
        ) {
            Icon(
                if (playback.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                if (playback.isPlaying) "Pause" else "Play",
                tint = AuraColors.textPrimary,
                modifier = Modifier.size(42.dp),
            )
        }
        PlayerControl(Icons.Default.SkipNext, "Next") { onEvent(PlayerScreenEvent.Next) }
        PlayerControl(
            if (playback.repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
            playback.repeatMode.label(),
            playback.repeatMode != RepeatMode.OFF,
        ) { onEvent(PlayerScreenEvent.CycleRepeatMode) }
    }
}

@Composable
private fun PlayerControl(
    icon: ImageVector,
    description: String,
    active: Boolean = false,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(icon, description, tint = if (active) AuraColors.auraRed else AuraColors.textPrimary)
    }
}

@Composable
private fun PlayerDestinations() {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = AuraSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(AuraSpacing.sm),
    ) {
        PlayerDestination(Icons.AutoMirrored.Filled.QueueMusic, "Queue", Modifier.weight(1f))
        PlayerDestination(Icons.Default.Lyrics, "Lyrics", Modifier.weight(1f))
        PlayerDestination(Icons.Default.Equalizer, "Audio", Modifier.weight(1f))
    }
}

@Composable
private fun PlayerDestination(icon: ImageVector, label: String, modifier: Modifier) {
    OutlinedButton(
        onClick = { },
        enabled = false,
        modifier = modifier,
        border = BorderStroke(1.dp, AuraColors.borderSubtle),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(20.dp))
            Text(label, style = AuraTypography.metadata)
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000).coerceAtLeast(0)
    return "%d:%02d".format(seconds / 60, seconds % 60)
}

package com.aura.feature.player

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.core.model.RepeatMode
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerRoute(
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result.values.all { it }
        viewModel.onPermissionResult(granted)
    }

    val requestPermissions: () -> Unit = {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissions)
    }

    PlayerScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onRequestPermission = requestPermissions,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    uiState: PlayerScreenUiState,
    onEvent: (PlayerScreenEvent) -> Unit,
    onRequestPermission: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AURA") },
            )
        },
        bottomBar = {
            NowPlayingBar(
                uiState = uiState,
                onEvent = onEvent,
            )
        },
    ) { padding ->
        when {
            !uiState.hasAudioPermission -> {
                PermissionContent(
                    modifier = Modifier.padding(padding),
                    onRequestPermission = onRequestPermission,
                )
            }
            uiState.isLoadingLibrary -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.libraryError != null -> {
                ErrorContent(
                    modifier = Modifier.padding(padding),
                    message = uiState.libraryError,
                    onRetry = { onEvent(PlayerScreenEvent.RefreshLibrary) },
                )
            }
            else -> {
                SongList(
                    modifier = Modifier.padding(padding),
                    songs = uiState.songs,
                    currentSongId = uiState.playback.currentSong?.id,
                    onSongClick = { index -> onEvent(PlayerScreenEvent.PlaySong(index)) },
                )
            }
        }
    }
}

@Composable
private fun PermissionContent(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Let AURA find your music",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "AURA needs access to your audio library so it can organize and play your music. Your music stays on your device.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Allow access")
        }
    }
}

@Composable
private fun ErrorContent(
    modifier: Modifier = Modifier,
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Try again")
        }
    }
}

@Composable
private fun SongList(
    modifier: Modifier = Modifier,
    songs: List<com.aura.core.model.Song>,
    currentSongId: Long?,
    onSongClick: (Int) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
            val isPlaying = song.id == currentSongId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSongClick(index) }
                    .padding(vertical = 12.dp)
                    .semantics {
                        contentDescription = "Play ${song.title} by ${song.artist}"
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (isPlaying) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isPlaying) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    Text(
                        text = "${song.artist} • ${song.album}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = formatDuration(song.durationMs),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun NowPlayingBar(
    uiState: PlayerScreenUiState,
    onEvent: (PlayerScreenEvent) -> Unit,
) {
    val playback = uiState.playback
    val currentSong = playback.currentSong ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = currentSong.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = currentSong.artist,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val duration = max(playback.durationMs, 1L)
        val progress = (playback.positionMs.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )
        Slider(
            value = playback.positionMs.toFloat(),
            onValueChange = { onEvent(PlayerScreenEvent.SeekTo(it.toLong())) },
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "${formatDuration(playback.positionMs)} / ${formatDuration(playback.durationMs)}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { onEvent(PlayerScreenEvent.ToggleShuffle) },
                modifier = Modifier.semantics {
                    contentDescription = if (playback.shuffleEnabled) "Disable shuffle" else "Enable shuffle"
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = null,
                    tint = if (playback.shuffleEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            IconButton(onClick = { onEvent(PlayerScreenEvent.Previous) }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
            }
            IconButton(
                onClick = { onEvent(PlayerScreenEvent.TogglePlayPause) },
                modifier = Modifier.size(56.dp),
            ) {
                Icon(
                    imageVector = if (playback.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (playback.isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(40.dp),
                )
            }
            IconButton(onClick = { onEvent(PlayerScreenEvent.Next) }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next")
            }
            IconButton(
                onClick = { onEvent(PlayerScreenEvent.CycleRepeatMode) },
                modifier = Modifier.semantics {
                    contentDescription = playback.repeatMode.label()
                },
            ) {
                Icon(
                    imageVector = when (playback.repeatMode) {
                        RepeatMode.ONE -> Icons.Default.RepeatOne
                        RepeatMode.ALL, RepeatMode.OFF -> Icons.Default.Repeat
                    },
                    contentDescription = null,
                    tint = if (playback.repeatMode == RepeatMode.OFF) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

package com.aura.feature.library

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.core.designsystem.components.AuraArtwork
import com.aura.core.designsystem.components.AuraBrandMark
import com.aura.core.designsystem.components.AuraEmptyState
import com.aura.core.designsystem.components.AuraErrorState
import com.aura.core.designsystem.components.AuraLoadingState
import com.aura.core.designsystem.theme.*
import com.aura.core.model.*

@Composable
fun LibraryRoute(
    onSearchClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        viewModel.onPermissionResult(result.values.all { it })
    }

    LibraryScreen(
        uiState = uiState,
        onTabSelected = viewModel::onTabSelected,
        onSearchClick = onSearchClick,
        onRequestPermission = {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            permissionLauncher.launch(permissions)
        },
        onRetry = viewModel::retry,
        onSongClick = onSongClick,
        onAlbumClick = onAlbumClick,
        onArtistClick = onArtistClick,
        modifier = modifier
    )
}

@Composable
internal fun LibraryScreen(
    uiState: LibraryUiState,
    onTabSelected: (LibraryTab) -> Unit,
    onSearchClick: () -> Unit,
    onRequestPermission: () -> Unit,
    onRetry: () -> Unit,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemCount = when (uiState.selectedTab) {
        LibraryTab.Songs -> uiState.songs.size
        LibraryTab.Albums -> uiState.albums.size
        LibraryTab.Artists -> uiState.artists.size
        LibraryTab.Genres -> uiState.genres.size
        LibraryTab.Folders -> uiState.folders.size
        LibraryTab.Playlists -> 0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraColors.background)
    ) {
        // 1. Top Bar
        LibraryTopBar(onSearchClick = onSearchClick)

        // 2. Tabs
        LibraryTabs(
            selectedTab = uiState.selectedTab,
            onTabSelected = onTabSelected
        )

        // 3. Filter/Sort Header
        FilterSortHeader(count = itemCount)

        // 4. Content
        Box(modifier = Modifier.weight(1f)) {
            if (!uiState.hasAudioPermission) {
                LibraryPermissionContent(
                    modifier = Modifier.align(Alignment.Center),
                    onRequestPermission = onRequestPermission
                )
            } else if (uiState.isLoading) {
                AuraLoadingState(message = "Reading your music library")
            } else if (uiState.errorMessage != null) {
                AuraErrorState(
                    title = "Library unavailable",
                    message = uiState.errorMessage,
                    retryLabel = "Try again",
                    onRetry = onRetry,
                )
            } else if (itemCount == 0) {
                val playlistsSelected = uiState.selectedTab == LibraryTab.Playlists
                AuraEmptyState(
                    title = if (playlistsSelected) "Playlists are coming next" else "AURA hasn't found any ${uiState.selectedTab.name.lowercase()} yet",
                    message = if (playlistsSelected) "Playlist management arrives in its dedicated product phase." else "Scan your device again after adding music.",
                    actionLabel = if (playlistsSelected) null else "Scan again",
                    onAction = if (playlistsSelected) null else onRetry
                )
            } else {
                LibraryContent(
                    uiState = uiState,
                    onSongClick = onSongClick,
                    onAlbumClick = onAlbumClick,
                    onArtistClick = onArtistClick
                )
            }
        }
    }
}

@Composable
private fun LibraryPermissionContent(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(AuraSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = AuraColors.auraRed,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(AuraSpacing.base))
        Text("Let AURA find your music", style = AuraTypography.headline, color = AuraColors.textPrimary)
        Spacer(modifier = Modifier.height(AuraSpacing.sm))
        Text(
            "Allow audio access to organize and play music stored on this device.",
            style = AuraTypography.body,
            color = AuraColors.textSecondary
        )
        Spacer(modifier = Modifier.height(AuraSpacing.lg))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(containerColor = AuraColors.auraRed)
        ) {
            Text("Allow access")
        }
    }
}

@Composable
private fun LibraryTopBar(onSearchClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AuraSpacing.base),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AuraBrandMark(modifier = Modifier.size(44.dp), contentDescription = "AURA")
            Spacer(modifier = Modifier.width(AuraSpacing.base))
            Text(
                text = "Library",
                style = AuraTypography.display.copy(fontSize = 32.sp),
                color = AuraColors.textPrimary
            )
        }
        IconButton(onClick = onSearchClick) {
            Icon(Icons.Rounded.Search, contentDescription = "Search library", tint = AuraColors.textPrimary)
        }
    }
}

@Composable
private fun LibraryTabs(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = AuraColors.background,
        contentColor = AuraColors.auraRed,
        edgePadding = AuraSpacing.base,
        divider = {},
        indicator = { tabPositions ->
            if (selectedTab.ordinal < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = AuraColors.auraRed
                )
            }
        }
    ) {
        LibraryTab.entries.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.name,
                        style = AuraTypography.label,
                        color = if (selectedTab == tab) AuraColors.textPrimary else AuraColors.textMuted
                    )
                }
            )
        }
    }
}

@Composable
private fun FilterSortHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AuraSpacing.base, vertical = AuraSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$count items",
            style = AuraTypography.metadata,
            color = AuraColors.textMuted
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = null, tint = AuraColors.textMuted, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(AuraSpacing.xs))
            Text("Title order", style = AuraTypography.metadata, color = AuraColors.textMuted)
        }
    }
}

@Composable
private fun LibraryContent(
    uiState: LibraryUiState,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AuraSpacing.max)
    ) {
        when (uiState.selectedTab) {
            LibraryTab.Songs -> {
                items(uiState.songs, key = { it.auraUuid }) { song ->
                    SongRow(song = song, onClick = { onSongClick(song) })
                }
            }
            LibraryTab.Albums -> {
                items(uiState.albums, key = { it.albumUuid }) { album ->
                    AlbumRow(album = album, onClick = { onAlbumClick(album) })
                }
            }
            LibraryTab.Artists -> {
                items(uiState.artists, key = { it.artistUuid }) { artist ->
                    ArtistRow(artist = artist, onClick = { onArtistClick(artist) })
                }
            }
            LibraryTab.Genres -> {
                items(uiState.genres, key = { it.genreUuid }) { genre ->
                    LibraryLabelRow(Icons.Rounded.Style, genre.name, "${genre.trackCount} songs")
                }
            }
            LibraryTab.Folders -> {
                items(uiState.folders, key = { it.folderUuid }) { folder ->
                    LibraryLabelRow(Icons.Rounded.Folder, folder.name, "${folder.trackCount} songs")
                }
            }
            LibraryTab.Playlists -> Unit
        }
    }
}

@Composable
private fun SongRow(song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AuraSpacing.base, vertical = AuraSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AuraArtwork(
            artworkUri = "content://media/external/audio/albumart/${song.albumId}".toUri(),
            contentDescription = song.title,
            modifier = Modifier.size(48.dp),
            shape = AuraShape.small
        )
        Column(
            modifier = Modifier
                .padding(start = AuraSpacing.base)
                .weight(1f)
        ) {
            Text(text = song.title, style = AuraTypography.body, color = AuraColors.textPrimary, maxLines = 1)
            Text(text = song.artist, style = AuraTypography.metadata, color = AuraColors.textSecondary, maxLines = 1)
        }
        Text(formatDuration(song.durationMs), style = AuraTypography.metadata, color = AuraColors.textMuted)
    }
}

@Composable
private fun LibraryLabelRow(icon: ImageVector, title: String, metadata: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AuraSpacing.base, vertical = AuraSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = AuraShape.small,
            color = AuraColors.surfaceElevated
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = AuraColors.auraRed, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(modifier = Modifier.width(AuraSpacing.base))
        Column {
            Text(title, style = AuraTypography.body, color = AuraColors.textPrimary, maxLines = 1)
            Text(metadata, style = AuraTypography.metadata, color = AuraColors.textSecondary)
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

@Composable
private fun AlbumRow(album: Album, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AuraSpacing.base, vertical = AuraSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AuraArtwork(
            artworkUri = "content://media/external/audio/albumart/${album.mediaStoreAlbumId}".toUri(),
            contentDescription = album.name,
            modifier = Modifier.size(56.dp),
            shape = AuraShape.small
        )
        Column(
            modifier = Modifier
                .padding(start = AuraSpacing.base)
                .weight(1f)
        ) {
            Text(text = album.name, style = AuraTypography.body, color = AuraColors.textPrimary, maxLines = 1)
            Text(text = album.artistName.ifBlank { "Various Artists" }, style = AuraTypography.metadata, color = AuraColors.textSecondary, maxLines = 1)
        }
    }
}

@Composable
private fun ArtistRow(artist: Artist, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AuraSpacing.base, vertical = AuraSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = AuraColors.surfaceElevated,
            border = BorderStroke(1.dp, AuraColors.borderSubtle)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(artist.name.take(1).uppercase(), style = AuraTypography.title, color = AuraColors.textSecondary)
            }
        }
        Column(
            modifier = Modifier
                .padding(start = AuraSpacing.base)
                .weight(1f)
        ) {
            Text(text = artist.name, style = AuraTypography.body, color = AuraColors.textPrimary, maxLines = 1)
            Text(text = "${artist.albumCount} albums • ${artist.trackCount} songs", style = AuraTypography.metadata, color = AuraColors.textSecondary, maxLines = 1)
        }
    }
}

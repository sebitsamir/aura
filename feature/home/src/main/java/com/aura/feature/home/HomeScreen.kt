package com.aura.feature.home

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.core.designsystem.components.AuraArtwork
import com.aura.core.designsystem.components.AuraBrandMark
import com.aura.core.designsystem.components.AuraLoadingState
import com.aura.core.designsystem.theme.*
import com.aura.core.model.Song

@Composable
fun HomeRoute(
    onSongClick: (Song) -> Unit,
    onFlowClick: () -> Unit,
    onViewAllClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onSongClick = onSongClick,
        onFlowClick = onFlowClick,
        onViewAllClick = onViewAllClick,
        onProfileClick = onProfileClick,
        modifier = modifier
    )
}

@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onSongClick: (Song) -> Unit,
    onFlowClick: () -> Unit,
    onViewAllClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AuraColors.background),
        contentPadding = PaddingValues(bottom = AuraSpacing.max),
        verticalArrangement = Arrangement.spacedBy(AuraSpacing.xxl)
    ) {
        // 1. Branding and Profile
        item {
            TopBrandingRow(onProfileClick = onProfileClick)
        }

        // 2. Greeting
        item {
            HomeHeader(greeting = uiState.greeting)
        }

        // 3. Mood Shortcuts
        item {
            MoodShortcuts(onMoodClick = onFlowClick)
        }

        // 4. Continue Listening (Hero Card)
        if (uiState.continueListening != null) {
            item {
                ContinueListeningSection(
                    song = uiState.continueListening,
                    progress = uiState.continueListeningProgress,
                    timeText = uiState.continueListeningTime,
                    onClick = { onSongClick(uiState.continueListening) },
                    onViewAllClick = { onViewAllClick("continue") }
                )
            }
        }

        // 5. Flow
        item {
            FlowShortcutSection(onClick = onFlowClick)
        }

        // 6. Rediscover
        item {
            HorizontalSongSection(
                title = "Rediscover",
                description = "Albums and tracks you loved, waiting to be heard again.",
                songs = uiState.rediscover,
                onSongClick = onSongClick,
                onViewAllClick = { onViewAllClick("rediscover") }
            )
        }

        // 7. Recently Played
        if (uiState.recentlyPlayed.isNotEmpty()) {
            item {
                HorizontalSongSection(
                    title = "Recently Played",
                    songs = uiState.recentlyPlayed,
                    onSongClick = onSongClick,
                    onViewAllClick = { onViewAllClick("recent") }
                )
            }
        } else if (!uiState.isLoading) {
            item {
                Text(
                    text = "Your recently played music will appear here.",
                    style = AuraTypography.metadata,
                    color = AuraColors.textMuted,
                    modifier = Modifier.padding(horizontal = AuraSpacing.base)
                )
            }
        }

        if (uiState.isLoading) {
            item { AuraLoadingState(message = "Preparing your library") }
        }
    }
}

@Composable
private fun TopBrandingRow(onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = AuraSpacing.base, end = AuraSpacing.base, top = AuraSpacing.base),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AuraBrandMark(modifier = Modifier.size(42.dp), contentDescription = "AURA")
            Spacer(modifier = Modifier.width(AuraSpacing.md))
            Text(
                text = "A U R A",
                style = AuraTypography.label.copy(letterSpacing = 6.sp, fontWeight = FontWeight.Bold),
                color = AuraColors.textPrimary
            )
        }

        Surface(
            modifier = Modifier
                .size(32.dp)
                .clickable(onClick = onProfileClick),
            shape = CircleShape,
            color = AuraColors.surfaceElevated,
            border = BorderStroke(1.dp, AuraColors.borderSubtle)
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "Profile",
                tint = AuraColors.textSecondary,
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}

@Composable
private fun HomeHeader(greeting: String) {
    Column(modifier = Modifier.padding(horizontal = AuraSpacing.base)) {
        Text(
            text = greeting,
            style = AuraTypography.display,
            color = AuraColors.textPrimary
        )
        Text(
            text = "What sounds right?",
            style = AuraTypography.headline.copy(fontSize = 18.sp),
            color = AuraColors.textSecondary
        )
    }
}

@Composable
private fun MoodShortcuts(onMoodClick: () -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = AuraSpacing.base),
        horizontalArrangement = Arrangement.spacedBy(AuraSpacing.sm)
    ) {
        item { MoodChip("Calm", Icons.Rounded.Waves, onMoodClick) }
        item { MoodChip("Focus", Icons.Rounded.Adjust, onMoodClick) }
        item { MoodChip("Energy", Icons.Rounded.Bolt, onMoodClick) }
        item { MoodChip("Nostalgia", Icons.Rounded.History, onMoodClick) }
    }
}

@Composable
private fun MoodChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = AuraColors.surface,
        shape = CircleShape,
        border = BorderStroke(1.dp, AuraColors.borderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AuraSpacing.base, vertical = AuraSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AuraColors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(AuraSpacing.sm))
            Text(
                text = label,
                style = AuraTypography.body.copy(fontSize = 14.sp),
                color = AuraColors.textPrimary
            )
        }
    }
}

@Composable
private fun ContinueListeningSection(
    song: Song,
    progress: Float,
    timeText: String,
    onClick: () -> Unit,
    onViewAllClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = AuraSpacing.base)) {
        SectionHeaderRow(title = "Continue Listening", onViewAllClick = onViewAllClick)
        Spacer(modifier = Modifier.height(AuraSpacing.base))

        Surface(
            onClick = onClick,
            color = AuraColors.surface,
            shape = AuraShape.medium,
            border = BorderStroke(1.dp, AuraColors.borderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(AuraSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuraArtwork(
                    artworkUri = "content://media/external/audio/albumart/${song.albumId}".toUri(),
                    contentDescription = song.title,
                    modifier = Modifier.size(120.dp),
                    shape = AuraShape.small
                )

                Column(
                    modifier = Modifier
                        .padding(start = AuraSpacing.base)
                        .weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = song.title, style = AuraTypography.title, color = AuraColors.textPrimary, maxLines = 1)
                            Text(text = song.artist, style = AuraTypography.body, color = AuraColors.textSecondary, maxLines = 1)
                        }
                        Icon(Icons.Rounded.MoreVert, contentDescription = null, tint = AuraColors.textMuted)
                    }

                    Spacer(modifier = Modifier.height(AuraSpacing.lg))

                    Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(AuraColors.borderSubtle)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .height(2.dp)
                                .background(AuraColors.auraRed)
                        )
                    }

                    Spacer(modifier = Modifier.height(AuraSpacing.sm))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = timeText, style = AuraTypography.metadata, color = AuraColors.textMuted)
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = AuraColors.surfaceElevated,
                            border = BorderStroke(1.dp, AuraColors.borderSubtle)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", tint = AuraColors.textPrimary, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowShortcutSection(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = AuraColors.surface,
        shape = AuraShape.medium,
        border = BorderStroke(1.dp, AuraColors.borderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AuraSpacing.base)
    ) {
        Row(
            modifier = Modifier.padding(AuraSpacing.base),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = AuraColors.background,
                border = BorderStroke(1.dp, AuraColors.auraRedSoft)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AuraBrandMark(modifier = Modifier.size(40.dp))
                }
            }

            Column(modifier = Modifier.padding(start = AuraSpacing.base).weight(1f)) {
                Text(text = "Flow", style = AuraTypography.title, color = AuraColors.textPrimary)
                Text(
                    text = "Build a listening session from your mood.",
                    style = AuraTypography.metadata,
                    color = AuraColors.textSecondary
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = AuraColors.auraRed
            )
        }
    }
}

@Composable
private fun HorizontalSongSection(
    title: String,
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onViewAllClick: () -> Unit,
    description: String? = null
) {
    Column {
        Column(modifier = Modifier.padding(horizontal = AuraSpacing.base)) {
            SectionHeaderRow(title = title, onViewAllClick = onViewAllClick)
            if (description != null) {
                Text(
                    text = description,
                    style = AuraTypography.metadata,
                    color = AuraColors.textMuted,
                    modifier = Modifier.padding(top = AuraSpacing.xs)
                )
            }
        }

        Spacer(modifier = Modifier.height(AuraSpacing.base))

        LazyRow(
            contentPadding = PaddingValues(horizontal = AuraSpacing.base),
            horizontalArrangement = Arrangement.spacedBy(AuraSpacing.base)
        ) {
            items(songs) { song ->
                Column(
                    modifier = Modifier
                        .width(140.dp)
                        .clickable { onSongClick(song) }
                ) {
                    AuraArtwork(
                        artworkUri = "content://media/external/audio/albumart/${song.albumId}".toUri(),
                        contentDescription = song.title,
                        modifier = Modifier.size(140.dp),
                        shape = AuraShape.small
                    )
                    Spacer(modifier = Modifier.height(AuraSpacing.sm))
                    Text(
                        text = song.title,
                        style = AuraTypography.body.copy(fontSize = 14.sp),
                        color = AuraColors.textPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = song.artist,
                        style = AuraTypography.metadata,
                        color = AuraColors.textSecondary,
                        maxLines = 1
                    )
                }
            }
        }
        if (songs.isEmpty()) {
            Text(
                text = "Keep listening. AURA will surface meaningful returns here.",
                style = AuraTypography.metadata,
                color = AuraColors.textMuted,
                modifier = Modifier.padding(horizontal = AuraSpacing.base)
            )
        }
    }
}

@Composable
private fun SectionHeaderRow(title: String, onViewAllClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = AuraTypography.label.copy(letterSpacing = 1.5.sp),
            color = AuraColors.textMuted
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onViewAllClick)
        ) {
            Text(
                text = "View all",
                style = AuraTypography.metadata.copy(fontWeight = FontWeight.Bold),
                color = AuraColors.auraRed
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = AuraColors.auraRed,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

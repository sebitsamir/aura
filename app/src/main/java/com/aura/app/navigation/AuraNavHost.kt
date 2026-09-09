package com.aura.app.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aura.core.designsystem.components.AuraBottomNavigation
import com.aura.core.designsystem.components.AuraBottomNavItem
import com.aura.core.designsystem.components.AuraMiniPlayer
import com.aura.core.designsystem.components.AuraScaffold
import com.aura.core.designsystem.theme.AuraColors
import com.aura.core.designsystem.theme.AuraTypography
import com.aura.core.playback.PlaybackCommand
import com.aura.domain.playback.PlaybackRepository
import com.aura.feature.player.PlayerRoute
import kotlinx.coroutines.launch

@Composable
fun AuraNavHost(
    playbackRepository: PlaybackRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val playbackState by playbackRepository.playbackState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val isTopLevelRoute = currentRoute in listOf(
        AuraNavigationRoute.Home::class.qualifiedName,
        AuraNavigationRoute.Library::class.qualifiedName,
        AuraNavigationRoute.Search::class.qualifiedName,
        AuraNavigationRoute.Settings::class.qualifiedName
    )

    AuraScaffold(
        modifier = modifier,
        bottomBar = {
            if (isTopLevelRoute) {
                Column {
                    if (playbackState.currentSong != null) {
                        AuraMiniPlayer(
                            title = playbackState.currentSong?.title ?: "",
                            artist = playbackState.currentSong?.artist ?: "",
                            artworkUri = playbackState.currentSong?.let {
                                Uri.parse("content://media/external/audio/albumart/${it.albumId}")
                            },
                            isPlaying = playbackState.isPlaying,
                            progress = if (playbackState.durationMs > 0)
                                playbackState.positionMs.toFloat() / playbackState.durationMs.toFloat() else 0f,
                            onPlayPause = {
                                scope.launch { playbackRepository.send(PlaybackCommand.TogglePlayPause) }
                            },
                            onNext = { scope.launch { playbackRepository.send(PlaybackCommand.Next) } },
                            onPrevious = { scope.launch { playbackRepository.send(PlaybackCommand.Previous) } },
                            onClick = { navController.navigate(AuraNavigationRoute.Player) }
                        )
                    }
                    AuraBottomNavigation {
                        AuraBottomNavItem(
                            selected = currentRoute == AuraNavigationRoute.Home::class.qualifiedName,
                            onClick = { navigateToTopLevel(navController, AuraNavigationRoute.Home) },
                            icon = Icons.Filled.Home,
                            label = "Home"
                        )
                        AuraBottomNavItem(
                            selected = currentRoute == AuraNavigationRoute.Library::class.qualifiedName,
                            onClick = { navigateToTopLevel(navController, AuraNavigationRoute.Library) },
                            icon = Icons.Filled.LibraryMusic,
                            label = "Library"
                        )
                        AuraBottomNavItem(
                            selected = currentRoute == AuraNavigationRoute.Search::class.qualifiedName,
                            onClick = { navigateToTopLevel(navController, AuraNavigationRoute.Search) },
                            icon = Icons.Filled.Search,
                            label = "Search"
                        )
                        AuraBottomNavItem(
                            selected = currentRoute == AuraNavigationRoute.Settings::class.qualifiedName,
                            onClick = { navigateToTopLevel(navController, AuraNavigationRoute.Settings) },
                            icon = Icons.Filled.Settings,
                            label = "Settings"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AuraNavigationRoute.Home,
            modifier = Modifier.padding(paddingValues)
        ) {
            // PlayerRoute owns the permission launcher and library loading.
            composable<AuraNavigationRoute.Home> { PlayerRoute() }
            composable<AuraNavigationRoute.Library> { PlayerRoute() }
            composable<AuraNavigationRoute.Search> { PlaceholderScreen("Search") }
            composable<AuraNavigationRoute.Settings> { PlaceholderScreen("Settings") }
            composable<AuraNavigationRoute.Player> { PlayerRoute() }
        }
    }
}

private fun navigateToTopLevel(
    navController: androidx.navigation.NavController,
    route: AuraNavigationRoute
) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$name (Coming in Stage 3.2+)",
            style = AuraTypography.headline,
            color = AuraColors.textPrimary
        )
    }
}

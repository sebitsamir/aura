package com.aura.app.navigation

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
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aura.core.designsystem.components.AuraBottomNavigation
import com.aura.core.designsystem.components.AuraBottomNavItem
import com.aura.core.designsystem.components.AuraMiniPlayer
import com.aura.core.designsystem.components.AuraScaffold
import com.aura.core.designsystem.components.AuraIcons
import com.aura.core.designsystem.theme.AuraColors
import com.aura.core.designsystem.theme.AuraTypography
import com.aura.core.playback.PlaybackCommand
import com.aura.domain.playback.PlaybackRepository
import com.aura.feature.flow.FlowRoute
import com.aura.feature.home.HomeRoute
import com.aura.feature.library.LibraryRoute
import com.aura.feature.player.PlayerRoute
import com.aura.feature.settings.SettingsRoute
import kotlinx.coroutines.launch

@Composable
fun AuraNavHost(
    playbackRepository: PlaybackRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val playbackState by playbackRepository.playbackState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isTopLevelRoute = currentDestination?.let { dest ->
        dest.hasRoute<AuraNavigationRoute.Home>() ||
        dest.hasRoute<AuraNavigationRoute.Library>() ||
        dest.hasRoute<AuraNavigationRoute.Search>() ||
        dest.hasRoute<AuraNavigationRoute.Flow>() ||
        dest.hasRoute<AuraNavigationRoute.Settings>()
    } ?: false

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
                                "content://media/external/audio/albumart/${it.albumId}".toUri()
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
                            selected = currentDestination?.hasRoute<AuraNavigationRoute.Home>() == true,
                            onClick = { navigateToTopLevel(navController, AuraNavigationRoute.Home) },
                            icon = Icons.Filled.Home,
                            label = "Home"
                        )
                        AuraBottomNavItem(
                            selected = currentDestination?.hasRoute<AuraNavigationRoute.Library>() == true,
                            onClick = { navigateToTopLevel(navController, AuraNavigationRoute.Library) },
                            icon = Icons.Filled.LibraryMusic,
                            label = "Library"
                        )
                        AuraBottomNavItem(
                            selected = currentDestination?.hasRoute<AuraNavigationRoute.Search>() == true,
                            onClick = { navigateToTopLevel(navController, AuraNavigationRoute.Search) },
                            icon = Icons.Filled.Search,
                            label = "Search"
                        )
                        AuraBottomNavItem(
                            selected = currentDestination?.hasRoute<AuraNavigationRoute.Flow>() == true,
                            onClick = { navigateToTopLevel(navController, AuraNavigationRoute.Flow) },
                            icon = AuraIcons.Flow,
                            label = "Flow"
                        )
                        AuraBottomNavItem(
                            selected = currentDestination?.hasRoute<AuraNavigationRoute.Settings>() == true,
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
            composable<AuraNavigationRoute.Home> {
                HomeRoute(
                    onSongClick = { song ->
                        scope.launch {
                            playbackRepository.send(PlaybackCommand.PlayQueue(listOf(song)))
                            navController.navigate(AuraNavigationRoute.Player)
                        }
                    },
                    onFlowClick = {
                        navController.navigate(AuraNavigationRoute.Flow)
                    },
                    onViewAllClick = { category ->
                        navController.navigate(AuraNavigationRoute.Library)
                    },
                    onProfileClick = {
                        navController.navigate(AuraNavigationRoute.Settings)
                    }
                )
            }
            composable<AuraNavigationRoute.Library> {
                LibraryRoute(
                    onSearchClick = { navController.navigate(AuraNavigationRoute.Search) },
                    onSongClick = { song ->
                        scope.launch {
                            playbackRepository.send(PlaybackCommand.PlayQueue(listOf(song)))
                            navController.navigate(AuraNavigationRoute.Player)
                        }
                    },
                    onAlbumClick = { album ->
                        // Navigate to album detail
                    },
                    onArtistClick = { artist ->
                        // Navigate to artist detail
                    }
                )
            }
            composable<AuraNavigationRoute.Search> {
                PlaceholderScreen("Search")
            }
            composable<AuraNavigationRoute.Flow> {
                FlowRoute()
            }
            composable<AuraNavigationRoute.Settings> {
                SettingsRoute()
            }
            composable<AuraNavigationRoute.Player> {
                PlayerRoute(onBackClick = navController::popBackStack)
            }
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
            text = "$name (Coming in Stage 3.x)",
            style = AuraTypography.headline,
            color = AuraColors.textPrimary
        )
    }
}

package com.aura.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aura.feature.player.PlayerRoute

@Composable
fun AuraNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuraNavigationRoute.Player.route,
    ) {
        composable(route = AuraNavigationRoute.Player.route) {
            PlayerRoute()
        }
    }
}

package com.aura.app.navigation

sealed class AuraNavigationRoute(val route: String) {
    data object Player : AuraNavigationRoute("player")
}

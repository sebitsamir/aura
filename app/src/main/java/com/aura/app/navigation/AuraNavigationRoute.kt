package com.aura.app.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AuraNavigationRoute {
    @Serializable data object Home : AuraNavigationRoute
    @Serializable data object Library : AuraNavigationRoute
    @Serializable data object Search : AuraNavigationRoute
    @Serializable data object Settings : AuraNavigationRoute
    @Serializable data object Player : AuraNavigationRoute
}

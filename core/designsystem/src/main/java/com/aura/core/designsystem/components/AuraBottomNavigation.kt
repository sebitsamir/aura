package com.aura.core.designsystem.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.aura.core.designsystem.theme.AuraColors
import com.aura.core.designsystem.theme.AuraTypography

@Composable
fun AuraBottomNavigation(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    NavigationBar(
        modifier = modifier,
        containerColor = AuraColors.background,
        contentColor = AuraColors.textPrimary,
        tonalElevation = 0.dp,
        content = content
    )
}

@Composable
fun RowScope.AuraBottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(imageVector = icon, contentDescription = label) },
        label = { 
            Text(
                text = label.uppercase(),
                style = AuraTypography.label,
                maxLines = 1
            ) 
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = AuraColors.auraRed,
            selectedTextColor = AuraColors.auraRed,
            unselectedIconColor = AuraColors.textMuted,
            unselectedTextColor = AuraColors.textMuted,
            indicatorColor = Color.Transparent
        ),
        modifier = modifier
    )
}
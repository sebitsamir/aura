package com.aura.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aura.core.designsystem.theme.AuraColors

@Composable
fun AuraScaffold(
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = AuraColors.background,
        contentColor = AuraColors.textPrimary,
        bottomBar = bottomBar,
        content = content
    )
}
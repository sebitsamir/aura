package com.aura.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aura.core.designsystem.theme.AuraColors
import com.aura.core.designsystem.theme.AuraSpacing
import com.aura.core.designsystem.theme.AuraTypography

// Shared loading state. Never leave a blank screen.
@Composable
fun AuraLoadingState(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AuraSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(28.dp)
                .semantics { contentDescription = message ?: "Loading" },
            color = AuraColors.auraRed,
            strokeWidth = 2.dp,
        )
        if (message != null) {
            Text(
                text = message,
                style = AuraTypography.metadata,
                color = AuraColors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = AuraSpacing.md),
            )
        }
    }
}
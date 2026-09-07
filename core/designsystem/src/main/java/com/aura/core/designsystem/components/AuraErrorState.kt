package com.aura.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.aura.core.designsystem.theme.AuraColors
import com.aura.core.designsystem.theme.AuraSpacing
import com.aura.core.designsystem.theme.AuraTypography

// Error state with a recovery path. Never fail silently.
@Composable
fun AuraErrorState(
    title: String,
    message: String,
    retryLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AuraSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = AuraTypography.headline,
            color = AuraColors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = AuraTypography.metadata,
            color = AuraColors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = AuraSpacing.sm),
        )
        TextButton(
            onClick = onRetry,
            modifier = Modifier.padding(top = AuraSpacing.base),
        ) {
            Text(
                text = retryLabel,
                style = AuraTypography.title.copy(fontSize = 14.sp),
                color = AuraColors.auraRed,
            )
        }
    }
}
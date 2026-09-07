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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.aura.core.designsystem.theme.AuraColors
import com.aura.core.designsystem.theme.AuraSpacing
import com.aura.core.designsystem.theme.AuraTypography

// Empty states must teach the user what to do next.
// Example: "AURA has not found any music yet." plus a Scan action.
@Composable
fun AuraEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
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
        if (actionLabel != null && onAction != null) {
            TextButton(
                onClick = onAction,
                modifier = Modifier.padding(top = AuraSpacing.base),
            ) {
                Text(
                    text = actionLabel,
                    style = AuraTypography.title.copy(fontSize = 14.sp),
                    color = AuraColors.auraRed,
                )
            }
        }
    }
}
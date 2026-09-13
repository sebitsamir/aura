package com.aura.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.core.designsystem.components.AuraBrandMark
import com.aura.core.designsystem.theme.*

@Composable
fun SettingsRoute(modifier: Modifier = Modifier) {
    SettingsScreen(modifier = modifier)
}

@Composable
internal fun SettingsScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = AuraSpacing.base,
            top = AuraSpacing.xl,
            end = AuraSpacing.base,
            bottom = AuraSpacing.max
        ),
        verticalArrangement = Arrangement.spacedBy(AuraSpacing.lg)
    ) {
        item {
            Text(
                text = "Settings",
                style = AuraTypography.display.copy(fontSize = 38.sp, lineHeight = 44.sp),
                color = AuraColors.textPrimary
            )
        }

        item {
            Column {
                SettingCategory("Theme")
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AuraColors.surface,
                    shape = AuraShape.medium,
                    border = BorderStroke(1.dp, AuraColors.borderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(AuraSpacing.base),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(88.dp),
                            color = AuraColors.background,
                            shape = AuraShape.medium,
                            border = BorderStroke(1.dp, AuraColors.auraRed)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                AuraBrandMark(modifier = Modifier.size(58.dp), contentDescription = "Obsidian theme")
                            }
                        }
                        Spacer(modifier = Modifier.width(AuraSpacing.base))
                        Column {
                            Text("Obsidian", style = AuraTypography.title, color = AuraColors.textPrimary)
                            Text("Current AURA appearance", style = AuraTypography.metadata, color = AuraColors.auraRed)
                            Text("Additional appearances arrive in a later phase.", style = AuraTypography.metadata, color = AuraColors.textMuted)
                        }
                    }
                }
            }
        }

        item {
            Column {
                SettingCategory("Available now")
                SettingsGroup(
                    rows = listOf(
                        SettingRowData(Icons.Rounded.LibraryMusic, "Library", "Local MediaStore and Room library"),
                        SettingRowData(Icons.Rounded.Security, "Privacy", "Offline-first, with no account required"),
                        SettingRowData(Icons.Rounded.Info, "About", "AURA 0.3.2 · Stage 3.2")
                    )
                )
            }
        }
    }
}

@Composable
private fun SettingCategory(title: String) {
    Text(
        text = title.uppercase(),
        style = AuraTypography.label,
        color = AuraColors.textSecondary,
        modifier = Modifier.padding(start = AuraSpacing.xs, bottom = AuraSpacing.sm)
    )
}

private data class SettingRowData(
    val icon: ImageVector,
    val title: String,
    val description: String
)

@Composable
private fun SettingsGroup(rows: List<SettingRowData>) {
    Surface(
        color = AuraColors.surface,
        shape = AuraShape.medium,
        border = BorderStroke(1.dp, AuraColors.borderSubtle)
    ) {
        Column {
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AuraSpacing.base, vertical = AuraSpacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = row.icon,
                        contentDescription = null,
                        tint = AuraColors.auraRed,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(AuraSpacing.base))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(row.title, style = AuraTypography.body, color = AuraColors.textPrimary)
                        Text(row.description, style = AuraTypography.metadata, color = AuraColors.textSecondary)
                    }
                }
                if (index != rows.lastIndex) {
                    HorizontalDivider(color = AuraColors.borderSubtle, modifier = Modifier.padding(start = 54.dp))
                }
            }
        }
    }
}

package com.aura.feature.flow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.core.designsystem.components.AuraBrandMark
import com.aura.core.designsystem.theme.*

@Composable
fun FlowRoute(modifier: Modifier = Modifier) {
    FlowScreen(modifier = modifier)
}

@Composable
internal fun FlowScreen(modifier: Modifier = Modifier) {
    var mood by remember { mutableFloatStateOf(0.5f) }
    var familiarity by remember { mutableFloatStateOf(0.4f) }
    var vocals by remember { mutableFloatStateOf(0.65f) }
    var tempo by remember { mutableFloatStateOf(0.5f) }
    var selectedDuration by remember { mutableStateOf("60 min") }
    var selectedEra by remember { mutableStateOf("Any") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = AuraSpacing.max),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(AuraSpacing.xl))
        AuraBrandMark(modifier = Modifier.size(104.dp), contentDescription = "AURA")
        Text(
            text = "Flow",
            style = AuraTypography.display.copy(fontSize = 46.sp, lineHeight = 52.sp),
            color = AuraColors.textPrimary
        )
        Text(
            text = "Build your listening experience",
            style = AuraTypography.body,
            color = AuraColors.textSecondary
        )

        Spacer(modifier = Modifier.height(AuraSpacing.xl))
        Column(
            modifier = Modifier.padding(horizontal = AuraSpacing.base),
            verticalArrangement = Arrangement.spacedBy(AuraSpacing.md)
        ) {
            FlowSlider("Mood", Icons.Rounded.Cloud, mood, { mood = it }, "Dark", "Uplifting")
            FlowSlider("Familiarity", Icons.Rounded.AutoAwesome, familiarity, { familiarity = it }, "Familiar", "Discover")
            FlowSlider("Vocals", Icons.Rounded.Mic, vocals, { vocals = it }, "Instrumental", "Vocal")
            FlowSlider("Tempo", Icons.Rounded.Speed, tempo, { tempo = it }, "Slow", "Fast")
        }

        FlowChoiceSection(
            title = "Duration",
            choices = listOf("15 min", "30 min", "45 min", "60 min", "90 min", "Unlimited"),
            selected = selectedDuration,
            onSelected = { selectedDuration = it }
        )
        FlowChoiceSection(
            title = "Era",
            choices = listOf("Any", "Classic", "Modern", "Future"),
            selected = selectedEra,
            onSelected = { selectedEra = it }
        )

        Spacer(modifier = Modifier.height(AuraSpacing.xl))
        Button(
            onClick = {},
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AuraSpacing.xl)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = AuraColors.auraRedSoft,
                disabledContentColor = AuraColors.textPrimary
            ),
            shape = AuraShape.large,
            border = BorderStroke(1.dp, AuraColors.auraRed)
        ) {
            Icon(Icons.Rounded.GraphicEq, contentDescription = null)
            Spacer(modifier = Modifier.size(AuraSpacing.sm))
            Text("START FLOW", style = AuraTypography.title.copy(fontWeight = FontWeight.Bold))
        }
        Text(
            text = "Flow generation unlocks when the recommendation engine is ready.",
            style = AuraTypography.metadata,
            color = AuraColors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = AuraSpacing.huge, vertical = AuraSpacing.md)
        )
    }
}

@Composable
private fun FlowSlider(
    label: String,
    icon: ImageVector,
    value: Float,
    onValueChange: (Float) -> Unit,
    startLabel: String,
    endLabel: String
) {
    Surface(
        color = AuraColors.surface,
        shape = AuraShape.medium,
        border = BorderStroke(1.dp, AuraColors.borderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(AuraSpacing.base),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = AuraColors.auraRed, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.size(AuraSpacing.base))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = AuraTypography.title.copy(fontSize = 16.sp), color = AuraColors.textPrimary)
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    colors = SliderDefaults.colors(
                        thumbColor = AuraColors.textPrimary,
                        activeTrackColor = AuraColors.auraRed,
                        inactiveTrackColor = AuraColors.borderSubtle
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(startLabel, style = AuraTypography.metadata, color = AuraColors.textMuted)
                    Text(endLabel, style = AuraTypography.metadata, color = AuraColors.textMuted)
                }
            }
        }
    }
}

@Composable
private fun FlowChoiceSection(
    title: String,
    choices: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = AuraSpacing.xl)) {
        Text(
            text = title,
            style = AuraTypography.title.copy(fontSize = 16.sp),
            color = AuraColors.textPrimary,
            modifier = Modifier.padding(horizontal = AuraSpacing.base)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = AuraSpacing.base),
            horizontalArrangement = Arrangement.spacedBy(AuraSpacing.sm)
        ) {
            items(choices) { choice ->
                FilterChip(
                    selected = choice == selected,
                    onClick = { onSelected(choice) },
                    label = { Text(choice) },
                    shape = AuraShape.medium,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = AuraColors.surface,
                        labelColor = AuraColors.textSecondary,
                        selectedContainerColor = AuraColors.auraRedSoft,
                        selectedLabelColor = AuraColors.textPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = choice == selected,
                        borderColor = AuraColors.borderSubtle,
                        selectedBorderColor = AuraColors.auraRed
                    )
                )
            }
        }
    }
}

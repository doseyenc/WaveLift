package com.doseyenc.wavelift.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.doseyenc.wavelift.model.AudioQuality
import com.doseyenc.wavelift.ui.i18n.Strings
import com.doseyenc.wavelift.ui.theme.WaveLiftColors

@Composable
fun QualitySelector(
    selectedQuality: AudioQuality,
    onQualitySelected: (AudioQuality) -> Unit,
    strings: Strings,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = strings.qualitySectionTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AudioQuality.entries.forEach { quality ->
                QualityCard(
                    quality = quality,
                    isSelected = quality == selectedQuality,
                    onClick = { onQualitySelected(quality) },
                    label = when (quality) {
                        AudioQuality.LOW -> strings.qualityLow
                        AudioQuality.MEDIUM -> strings.qualityMedium
                        AudioQuality.HIGH -> strings.qualityHigh
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QualityCard(
    quality: AudioQuality,
    isSelected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        else
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300)
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected)
            WaveLiftColors.Primary
        else
            MaterialTheme.colorScheme.outline,
        animationSpec = tween(300)
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 0.dp,
        animationSpec = tween(300)
    )

    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${quality.kbps}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected)
                    WaveLiftColors.Primary
                else
                    MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "kbps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected)
                    WaveLiftColors.Primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

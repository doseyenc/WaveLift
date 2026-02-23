package com.doseyenc.wavelift.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.doseyenc.wavelift.model.DownloadItem
import com.doseyenc.wavelift.model.DownloadState
import com.doseyenc.wavelift.ui.i18n.Strings
import com.doseyenc.wavelift.ui.theme.WaveLiftColors

@Composable
fun DownloadListItem(
    item: DownloadItem,
    strings: Strings,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusIcon(state = item.state)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title.ifEmpty { strings.stateDownloading },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusText(state = item.state, strings = strings)

                when (val state = item.state) {
                    is DownloadState.Downloading -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = WaveLiftColors.Primary,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                    is DownloadState.Analyzing, is DownloadState.Converting -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = WaveLiftColors.Primary,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = item.quality.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusIcon(state: DownloadState) {
    val icon = when (state) {
        is DownloadState.Idle -> "⏳"
        is DownloadState.Analyzing -> "🔍"
        is DownloadState.Downloading -> "⬇️"
        is DownloadState.Converting -> "🔄"
        is DownloadState.Completed -> "✅"
        is DownloadState.Error -> "❌"
    }
    Text(text = icon, style = MaterialTheme.typography.titleLarge)
}

@Composable
private fun StatusText(state: DownloadState, strings: Strings) {
    val text = when (state) {
        is DownloadState.Idle -> strings.stateIdle
        is DownloadState.Analyzing -> strings.stateAnalyzing
        is DownloadState.Downloading -> {
            buildString {
                append("${strings.stateDownloading} %${(state.progress * 100).toInt()}")
                if (state.speed.isNotEmpty()) append(" • ${state.speed}")
                if (state.eta.isNotEmpty()) append(" • ETA ${state.eta}")
            }
        }
        is DownloadState.Converting -> strings.stateConverting
        is DownloadState.Completed -> strings.stateCompleted
        is DownloadState.Error -> state.message
    }

    val color = when (state) {
        is DownloadState.Completed -> WaveLiftColors.Success
        is DownloadState.Error -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

package com.doseyenc.wavelift.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.doseyenc.wavelift.model.UpdateState
import com.doseyenc.wavelift.ui.i18n.Strings
import com.doseyenc.wavelift.ui.theme.WaveLiftColors

/**
 * Shows the state of an app update. Renders nothing while idle or checking.
 */
@Composable
fun UpdateBanner(
    state: UpdateState,
    strings: Strings,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onOpenRelease: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state is UpdateState.Idle || state is UpdateState.Checking) return

    val isError = state is UpdateState.Failed

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isError) {
            MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isError) "⚠️" else "🚀",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title(state, strings),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )

                when (state) {
                    is UpdateState.Downloading -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = WaveLiftColors.Primary,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                    is UpdateState.Installing -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = WaveLiftColors.Primary,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                    is UpdateState.Ready -> {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.updateReadyDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            when (state) {
                is UpdateState.Available -> {
                    if (state.canInstall) {
                        TextButton(onClick = { onOpenRelease(state.releaseUrl) }) {
                            Text(strings.updateReleaseNotesButton)
                        }
                        Button(onClick = onDownload) {
                            Text(strings.updateDownloadButton)
                        }
                    } else {
                        Button(onClick = { onOpenRelease(state.releaseUrl) }) {
                            Text(strings.updateOpenPageButton)
                        }
                    }
                }
                is UpdateState.Ready -> {
                    TextButton(onClick = { onOpenRelease(state.releaseUrl) }) {
                        Text(strings.updateReleaseNotesButton)
                    }
                    Button(onClick = onInstall) {
                        Text(strings.updateInstallButton)
                    }
                }
                is UpdateState.Failed -> {
                    TextButton(onClick = { onOpenRelease(state.releaseUrl) }) {
                        Text(strings.updateOpenPageButton)
                    }
                    Button(onClick = onDownload) {
                        Text(strings.updateRetryButton)
                    }
                }
                else -> {}
            }
        }
    }
}

private fun title(state: UpdateState, strings: Strings): String = when (state) {
    is UpdateState.Available -> "${strings.updateAvailableTitle}: ${state.version}"
    is UpdateState.Downloading -> "${strings.updateDownloadingTitle}: ${state.version} • %${(state.progress * 100).toInt()}"
    is UpdateState.Ready -> "${strings.updateReadyTitle}: ${state.version}"
    is UpdateState.Installing -> strings.updateInstallingTitle
    is UpdateState.Failed -> "${strings.updateFailedTitle}: ${state.version}"
    is UpdateState.Idle, is UpdateState.Checking -> ""
}

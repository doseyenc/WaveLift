package com.doseyenc.wavelift.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import com.doseyenc.wavelift.model.AudioQuality
import com.doseyenc.wavelift.ui.components.DownloadListItem
import com.doseyenc.wavelift.ui.components.QualitySelector
import com.doseyenc.wavelift.ui.components.SettingsToggles
import com.doseyenc.wavelift.ui.i18n.Strings
import com.doseyenc.wavelift.ui.i18n.getStrings
import com.doseyenc.wavelift.viewmodel.UiState

@Composable
fun MainScreen(
    uiState: UiState,
    onUrlChanged: (String) -> Unit,
    onQualitySelected: (AudioQuality) -> Unit,
    onEmbedThumbnailChanged: (Boolean) -> Unit,
    onAddMetadataChanged: (Boolean) -> Unit,
    onSelectOutputDirectory: () -> Unit,
    onAnalyzeUrl: () -> Unit,
    onStartDownload: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onCancelDownload: (String) -> Unit,
    onSnackbarDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val strings = getStrings(uiState.language)
    var showAboutDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            onSnackbarDismissed()
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(strings.aboutTitle) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${strings.developerTitle}: Çağrı Döseyen",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { uriHandler.openUri("https://github.com/doseyenc") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("GitHub")
                    }
                    Button(
                        onClick = { uriHandler.openUri("https://linkedin.com/in/cagridoseyen") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("LinkedIn")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HeaderSection(
                    strings = strings,
                    isDarkTheme = uiState.isDarkTheme,
                    languageCode = uiState.language.code,
                    onToggleTheme = onToggleTheme,
                    onToggleLanguage = onToggleLanguage,
                    onShowAbout = { showAboutDialog = true }
                )
            }

            item {
                UrlInputSection(
                    url = uiState.url,
                    onUrlChanged = onUrlChanged,
                    onAnalyze = onAnalyzeUrl,
                    isAnalyzing = uiState.isAnalyzing,
                    playlistInfo = uiState.playlistInfo,
                    strings = strings
                )
            }

            item {
                QualitySelector(
                    selectedQuality = uiState.selectedQuality,
                    onQualitySelected = onQualitySelected,
                    strings = strings
                )
            }

            item {
                SettingsToggles(
                    embedThumbnail = uiState.embedThumbnail,
                    onEmbedThumbnailChange = onEmbedThumbnailChanged,
                    addMetadata = uiState.addMetadata,
                    onAddMetadataChange = onAddMetadataChanged,
                    strings = strings
                )
            }

            item {
                OutputDirectorySection(
                    outputDirectory = uiState.outputDirectory,
                    onSelectDirectory = onSelectOutputDirectory,
                    strings = strings
                )
            }

            item {
                DownloadButton(
                    onClick = onStartDownload,
                    enabled = uiState.url.isNotBlank(),
                    strings = strings
                )
            }

            if (uiState.downloads.isNotEmpty()) {
                item {
                    Text(
                        text = strings.downloadsSectionTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                items(
                    items = uiState.downloads,
                    key = { it.id }
                ) { item ->
                    DownloadListItem(
                        item = item,
                        strings = strings,
                        onCancel = { onCancelDownload(item.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun HeaderSection(
    strings: Strings,
    isDarkTheme: Boolean,
    languageCode: String,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onShowAbout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = strings.appTitle,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = strings.appSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            // About button
            IconButton(onClick = onShowAbout) {
                Text(
                    text = "ℹ️",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            // Language toggle
            TextButton(onClick = onToggleLanguage) {
                Text(
                    text = languageCode.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            // Theme toggle
            IconButton(onClick = onToggleTheme) {
                Text(
                    text = if (isDarkTheme) "☀️" else "🌙",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
private fun UrlInputSection(
    url: String,
    onUrlChanged: (String) -> Unit,
    onAnalyze: () -> Unit,
    isAnalyzing: Boolean,
    playlistInfo: String?,
    strings: Strings
) {
    Column {
        Text(
            text = strings.urlSectionTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = strings.urlPlaceholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            trailingIcon = {
                if (url.isNotEmpty()) {
                    TextButton(
                        onClick = onAnalyze,
                        enabled = !isAnalyzing
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = strings.analyzeButton,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )

        AnimatedVisibility(
            visible = playlistInfo != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            playlistInfo?.let {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "🎵 $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OutputDirectorySection(
    outputDirectory: String,
    onSelectDirectory: () -> Unit,
    strings: Strings
) {
    Column {
        Text(
            text = strings.outputSectionTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            onClick = onSelectDirectory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📁 $outputDirectory",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = strings.outputChangeButton,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DownloadButton(
    onClick: () -> Unit,
    enabled: Boolean,
    strings: Strings
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
    ) {
        Text(
            text = strings.downloadButton,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

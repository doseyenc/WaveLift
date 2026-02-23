package com.doseyenc.wavelift

import androidx.compose.runtime.*
import com.doseyenc.wavelift.ui.MainScreen
import com.doseyenc.wavelift.ui.theme.WaveLiftTheme
import com.doseyenc.wavelift.viewmodel.DownloadViewModel

@Composable
fun App(viewModel: DownloadViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    WaveLiftTheme(darkTheme = uiState.isDarkTheme) {
        MainScreen(
            uiState = uiState,
            onUrlChanged = viewModel::onUrlChanged,
            onQualitySelected = viewModel::onQualitySelected,
            onEmbedThumbnailChanged = viewModel::onEmbedThumbnailChanged,
            onAddMetadataChanged = viewModel::onAddMetadataChanged,
            onSelectOutputDirectory = viewModel::selectOutputDirectory,
            onAnalyzeUrl = viewModel::analyzeUrl,
            onStartDownload = viewModel::startDownload,
            onToggleTheme = viewModel::onToggleTheme,
            onToggleLanguage = viewModel::onToggleLanguage,
            onCancelDownload = viewModel::cancelDownload,
            onSnackbarDismissed = viewModel::onSnackbarDismissed
        )
    }
}
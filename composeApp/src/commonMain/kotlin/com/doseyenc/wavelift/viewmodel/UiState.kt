package com.doseyenc.wavelift.viewmodel

import com.doseyenc.wavelift.model.AudioQuality
import com.doseyenc.wavelift.model.DownloadItem
import com.doseyenc.wavelift.ui.i18n.Language

data class UiState(
    val url: String = "",
    val selectedQuality: AudioQuality = AudioQuality.HIGH,
    val embedThumbnail: Boolean = true,
    val addMetadata: Boolean = true,
    val outputDirectory: String = "",
    val downloads: List<DownloadItem> = emptyList(),
    val snackbarMessage: String? = null,
    val isAnalyzing: Boolean = false,
    val playlistInfo: String? = null,
    val isDarkTheme: Boolean = true,
    val language: Language = Language.TURKISH
)

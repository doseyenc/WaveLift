package com.doseyenc.wavelift.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doseyenc.wavelift.engine.DownloadEngine
import com.doseyenc.wavelift.engine.NotificationManager
import com.doseyenc.wavelift.model.AudioQuality
import com.doseyenc.wavelift.model.DownloadItem
import com.doseyenc.wavelift.model.DownloadState
import com.doseyenc.wavelift.ui.i18n.Language
import com.doseyenc.wavelift.ui.i18n.Strings
import com.doseyenc.wavelift.ui.i18n.getStrings
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.swing.JFileChooser

class DownloadViewModel : ViewModel() {

    private val engine = DownloadEngine()
    private val _uiState = MutableStateFlow(
        UiState(outputDirectory = System.getProperty("user.home") + "/Music")
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Track active download jobs for cancellation
    private val activeJobs = mutableMapOf<String, Job>()

    private val strings get() = getStrings(_uiState.value.language)

    fun onUrlChanged(url: String) {
        _uiState.update { it.copy(url = url, playlistInfo = null) }
    }

    fun onQualitySelected(quality: AudioQuality) {
        _uiState.update { it.copy(selectedQuality = quality) }
    }

    fun onEmbedThumbnailChanged(value: Boolean) {
        _uiState.update { it.copy(embedThumbnail = value) }
    }

    fun onAddMetadataChanged(value: Boolean) {
        _uiState.update { it.copy(addMetadata = value) }
    }

    fun onToggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun onToggleLanguage() {
        _uiState.update {
            val newLang = if (it.language == Language.TURKISH) Language.ENGLISH else Language.TURKISH
            it.copy(language = newLang)
        }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun selectOutputDirectory() {
        val chooser = JFileChooser(_uiState.value.outputDirectory).apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = strings.directoryPickerTitle
            isAcceptAllFileFilterUsed = false
            putClientProperty("FileChooser.useShellFolder", false)
        }
        val result = chooser.showSaveDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedDir = chooser.selectedFile
            if (!selectedDir.exists()) {
                selectedDir.mkdirs()
            }
            _uiState.update {
                it.copy(outputDirectory = selectedDir.absolutePath)
            }
        }
    }

    fun analyzeUrl() {
        val url = _uiState.value.url.trim()
        if (url.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = strings.emptyUrlError) }
            return
        }
        if (!isValidUrl(url)) {
            _uiState.update { it.copy(snackbarMessage = strings.invalidUrlError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, playlistInfo = null) }
            engine.analyzePlaylist(url).collect { state ->
                when (state) {
                    is DownloadState.Analyzing -> {
                        _uiState.update { it.copy(playlistInfo = state.message) }
                    }
                    is DownloadState.Error -> {
                        _uiState.update {
                            it.copy(
                                snackbarMessage = localizeError(state.message),
                                isAnalyzing = false,
                                playlistInfo = null
                            )
                        }
                    }
                    else -> {}
                }
            }
            _uiState.update { it.copy(isAnalyzing = false) }
        }
    }

    fun cancelDownload(itemId: String) {
        activeJobs[itemId]?.cancel()
        activeJobs.remove(itemId)
        _uiState.update { uiState ->
            val updatedDownloads = uiState.downloads.map { download ->
                if (download.id == itemId) {
                    download.copy(state = DownloadState.Error(strings.downloadCancelled))
                } else {
                    download
                }
            }
            uiState.copy(downloads = updatedDownloads)
        }
    }

    fun startDownload() {
        val currentState = _uiState.value
        val url = currentState.url.trim()

        if (url.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = strings.emptyUrlError) }
            return
        }
        if (!isValidUrl(url)) {
            _uiState.update { it.copy(snackbarMessage = strings.invalidUrlError) }
            return
        }

        val item = DownloadItem(
            title = extractTitleFromUrl(url),
            url = url,
            quality = currentState.selectedQuality,
            state = DownloadState.Idle,
            outputPath = currentState.outputDirectory
        )

        // Add item to list
        _uiState.update {
            it.copy(
                downloads = listOf(item) + it.downloads,
                url = "" // Clear URL field
            )
        }

        // Start download and track the job
        val job = viewModelScope.launch {
            engine.download(
                url = url,
                quality = currentState.selectedQuality,
                outputDir = currentState.outputDirectory,
                embedThumbnail = currentState.embedThumbnail,
                addMetadata = currentState.addMetadata
            ).collect { state ->
                _uiState.update { uiState ->
                    val updatedDownloads = uiState.downloads.map { download ->
                        if (download.id == item.id) {
                            // Localize error messages
                            val localizedState = if (state is DownloadState.Error) {
                                DownloadState.Error(localizeError(state.message))
                            } else {
                                state
                            }
                            download.copy(state = localizedState)
                        } else {
                            download
                        }
                    }

                    // Send desktop notification on completion
                    if (state is DownloadState.Completed) {
                        NotificationManager.showNotification(
                            title = strings.notificationTitle,
                            message = strings.notificationMessage
                        )
                    }

                    uiState.copy(
                        downloads = updatedDownloads,
                        snackbarMessage = when (state) {
                            is DownloadState.Completed -> strings.downloadComplete
                            is DownloadState.Error -> localizeError(state.message)
                            else -> uiState.snackbarMessage
                        }
                    )
                }
            }
        }

        activeJobs[item.id] = job
        job.invokeOnCompletion { activeJobs.remove(item.id) }
    }

    /**
     * Maps raw yt-dlp error messages to user-friendly localized messages.
     */
    private fun localizeError(rawError: String): String {
        val lower = rawError.lowercase()
        val s = strings
        return when {
            lower.contains("does not exist") || lower.contains("playlist does not exist") ->
                s.errorPlaylistNotFound
            lower.contains("private video") || lower.contains("this video is private") ->
                s.errorPrivateVideo
            lower.contains("video unavailable") || lower.contains("this video is unavailable") ||
            lower.contains("is not available") ->
                s.errorVideoUnavailable
            lower.contains("geo") || lower.contains("not available in your country") ->
                s.errorGeoRestricted
            lower.contains("sign in") || lower.contains("login") || lower.contains("age") ->
                s.errorLoginRequired
            lower.contains("copyright") || lower.contains("dmca") || lower.contains("blocked") ->
                s.errorCopyright
            lower.contains("urlopen") || lower.contains("connection") ||
            lower.contains("timed out") || lower.contains("network") ->
                s.errorNetwork
            else -> rawError
        }
    }

    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    private fun extractTitleFromUrl(url: String): String {
        return when {
            "youtube.com" in url || "youtu.be" in url -> "YouTube Video"
            "soundcloud.com" in url -> "SoundCloud Track"
            else -> "Media Download"
        }
    }
}

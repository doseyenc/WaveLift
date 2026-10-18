package com.doseyenc.wavelift.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doseyenc.wavelift.engine.DownloadEngine
import com.doseyenc.wavelift.engine.NotificationManager
import com.doseyenc.wavelift.engine.ReleaseInfo
import com.doseyenc.wavelift.engine.UpdateManager
import com.doseyenc.wavelift.model.AudioQuality
import com.doseyenc.wavelift.model.DownloadItem
import com.doseyenc.wavelift.model.DownloadState
import com.doseyenc.wavelift.model.UpdateState
import com.doseyenc.wavelift.ui.i18n.Language
import com.doseyenc.wavelift.ui.i18n.Strings
import com.doseyenc.wavelift.ui.i18n.getStrings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.prefs.Preferences
import javax.swing.JFileChooser
import kotlin.system.exitProcess

class DownloadViewModel : ViewModel() {

    private val engine = DownloadEngine()
    private val prefs = Preferences.userNodeForPackage(DownloadViewModel::class.java)
    private val _uiState = MutableStateFlow(
        UiState(
            outputDirectory = System.getProperty("user.home") + "/Music",
            appVersion = UpdateManager.currentVersion,
            autoUpdate = prefs.getBoolean(PREF_AUTO_UPDATE, true)
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Track active download jobs for cancellation
    private val activeJobs = mutableMapOf<String, Job>()

    private val strings get() = getStrings(_uiState.value.language)

    private var updateJob: Job? = null
    private var latestRelease: ReleaseInfo? = null
    private var downloadedInstaller: File? = null

    init {
        // Packaged builds only; there is nothing to update when running from Gradle
        if (UpdateManager.currentVersion != null) {
            checkForUpdates(manual = false)
        }
    }

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

    private var analysisJob: Job? = null

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

        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, playlistInfo = null) }
            try {
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
            } finally {
                _uiState.update { it.copy(isAnalyzing = false) }
            }
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

    fun onAutoUpdateChanged(enabled: Boolean) {
        prefs.putBoolean(PREF_AUTO_UPDATE, enabled)
        _uiState.update { it.copy(autoUpdate = enabled) }
    }

    fun checkForUpdates(manual: Boolean = true) {
        if (updateJob?.isActive == true) return
        val currentVersion = UpdateManager.currentVersion
        if (currentVersion == null) {
            _uiState.update { it.copy(snackbarMessage = strings.updateDevBuild) }
            return
        }

        updateJob = viewModelScope.launch {
            _uiState.update { it.copy(update = UpdateState.Checking) }
            val release = try {
                withContext(Dispatchers.IO) { UpdateManager.fetchLatestRelease() }
            } catch (e: Exception) {
                println("[WaveLift] Update check failed: ${e.message}")
                _uiState.update {
                    it.copy(
                        update = UpdateState.Idle,
                        snackbarMessage = if (manual) strings.updateCheckFailed else it.snackbarMessage
                    )
                }
                return@launch
            }

            if (!UpdateManager.isNewer(release.version, currentVersion)) {
                withContext(Dispatchers.IO) { UpdateManager.clearDownloads() }
                _uiState.update {
                    it.copy(
                        update = UpdateState.Idle,
                        snackbarMessage = if (manual) strings.updateUpToDate else it.snackbarMessage
                    )
                }
                return@launch
            }

            latestRelease = release
            if (release.asset != null && _uiState.value.autoUpdate) {
                downloadRelease(release)
            } else {
                _uiState.update {
                    it.copy(update = UpdateState.Available(release.version, release.releaseUrl, release.asset != null))
                }
            }
        }
    }

    fun downloadUpdate() {
        val release = latestRelease ?: return
        if (updateJob?.isActive == true) return
        updateJob = viewModelScope.launch { downloadRelease(release) }
    }

    private suspend fun downloadRelease(release: ReleaseInfo) {
        val asset = release.asset ?: return
        _uiState.update { it.copy(update = UpdateState.Downloading(release.version, release.releaseUrl)) }
        try {
            downloadedInstaller = UpdateManager.download(asset) { progress ->
                _uiState.update { it.copy(update = UpdateState.Downloading(release.version, release.releaseUrl, progress)) }
            }
            _uiState.update { it.copy(update = UpdateState.Ready(release.version, release.releaseUrl)) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("[WaveLift] Update download failed: ${e.message}")
            _uiState.update { it.copy(update = UpdateState.Failed(release.version, release.releaseUrl)) }
        }
    }

    fun installUpdate() {
        val installer = downloadedInstaller ?: return
        val ready = _uiState.value.update as? UpdateState.Ready ?: return
        if (activeJobs.isNotEmpty()) {
            _uiState.update { it.copy(snackbarMessage = strings.updateWaitForDownloads) }
            return
        }

        _uiState.update { it.copy(update = UpdateState.Installing) }
        viewModelScope.launch {
            try {
                val mustQuit = withContext(Dispatchers.IO) { UpdateManager.install(installer) }
                if (mustQuit) exitProcess(0)
                // The OS installer took over (e.g. .deb on Linux); keep the banner so it can be reopened
                _uiState.update { it.copy(update = ready) }
            } catch (e: Exception) {
                println("[WaveLift] Update install failed: ${e.message}")
                _uiState.update { it.copy(update = UpdateState.Failed(ready.version, ready.releaseUrl)) }
            }
        }
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

    private companion object {
        const val PREF_AUTO_UPDATE = "autoUpdate"
    }
}

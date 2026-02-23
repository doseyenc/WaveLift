package com.doseyenc.wavelift.model

sealed class DownloadState {
    data object Idle : DownloadState()
    data class Analyzing(val message: String = "Analiz ediliyor...") : DownloadState()
    data class Downloading(
        val progress: Float = 0f,
        val speed: String = "",
        val eta: String = ""
    ) : DownloadState()
    data class Converting(val message: String = "MP3'e dönüştürülüyor...") : DownloadState()
    data class Completed(val outputPath: String = "") : DownloadState()
    data class Error(val message: String) : DownloadState()
}

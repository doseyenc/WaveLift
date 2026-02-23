package com.doseyenc.wavelift.engine

import com.doseyenc.wavelift.model.DownloadState

object ProgressParser {

    // [download]  45.2% of 5.23MiB at 1.20MiB/s ETA 00:03
    private val downloadProgressRegex =
        Regex("""\[download\]\s+(\d+\.?\d*)%(?:\s+of\s+\S+)?(?:\s+at\s+(\S+))?(?:\s+ETA\s+(\S+))?""")

    // [download] Downloading item 3 of 45
    private val playlistItemRegex =
        Regex("""\[download\]\s+Downloading item \d+ of (\d+)""")

    // [ExtractAudio] Destination: /path/to/file.mp3
    private val extractAudioRegex =
        Regex("""\[ExtractAudio\]\s+Destination:\s+(.+)""")

    // ERROR: [youtube] ...: ...
    private val errorRegex =
        Regex("""ERROR:\s+(.+)""")

    // [download] Destination: Title.webm
    private val destinationRegex =
        Regex("""\[download\]\s+Destination:\s+(.+)""")

    fun parseProgress(line: String): DownloadState? {
        // Check for errors first — return raw error, localization handled by ViewModel
        errorRegex.find(line)?.let { match ->
            return DownloadState.Error(match.groupValues[1].trim())
        }

        // Check for download progress
        downloadProgressRegex.find(line)?.let { match ->
            val progress = match.groupValues[1].toFloatOrNull()?.div(100f) ?: 0f
            val speed = match.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() } ?: ""
            val eta = match.groupValues.getOrNull(3)?.takeIf { it.isNotEmpty() } ?: ""
            return DownloadState.Downloading(progress = progress, speed = speed, eta = eta)
        }

        // Check for audio extraction (conversion phase)
        extractAudioRegex.find(line)?.let {
            return DownloadState.Converting("Converting...")
        }

        // 100% completed
        if (line.contains("[download] 100%") || line.contains("has already been downloaded")) {
            return DownloadState.Downloading(progress = 1f)
        }

        return null
    }

    fun parsePlaylistCount(line: String): Int? {
        playlistItemRegex.find(line)?.let { match ->
            return match.groupValues[1].toIntOrNull()
        }
        return null
    }

    fun parseTitleFromDestination(line: String): String? {
        destinationRegex.find(line)?.let { match ->
            val path = match.groupValues[1].trim()
            val fileName = path.substringAfterLast("/").substringAfterLast("\\")
            return fileName.substringBeforeLast(".")
        }
        return null
    }
}

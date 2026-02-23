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
        // Check for errors first
        errorRegex.find(line)?.let { match ->
            val rawError = match.groupValues[1].trim()
            val friendlyMessage = mapToFriendlyError(rawError)
            return DownloadState.Error(friendlyMessage)
        }

        // Check for download progress  
        downloadProgressRegex.find(line)?.let { match ->
            val progress = match.groupValues[1].toFloatOrNull()?.div(100f) ?: 0f
            val speed = match.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() } ?: ""
            val eta = match.groupValues.getOrNull(3)?.takeIf { it.isNotEmpty() } ?: ""
            return DownloadState.Downloading(progress = progress, speed = speed, eta = eta)
        }

        // Check for audio extraction (conversion phase)
        extractAudioRegex.find(line)?.let { match ->
            return DownloadState.Converting("MP3'e dönüştürülüyor...")
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

    private fun mapToFriendlyError(rawError: String): String {
        val lower = rawError.lowercase()
        return when {
            // Playlist doesn't exist or is private
            lower.contains("does not exist") ||
            lower.contains("playlist does not exist") ||
            lower.contains("the playlist does not exist") ->
                "🔒 Bu oynatma listesi bulunamadı!\n" +
                "Olası sebepler:\n" +
                "• Oynatma listesi silinmiş olabilir\n" +
                "• Oynatma listesi gizli (private) olabilir\n" +
                "• URL hatalı olabilir\n\n" +
                "Lütfen linki kontrol edip tekrar deneyin."

            // Private or unavailable video
            lower.contains("private video") ||
            lower.contains("this video is private") ->
                "🔒 Bu video gizli (private)!\n" +
                "Video sahibi tarafından gizlenmiş. Herkese açık bir video deneyin."

            // Video unavailable
            lower.contains("video unavailable") ||
            lower.contains("this video is unavailable") ||
            lower.contains("is not available") ->
                "⚠️ Bu video kullanılamıyor!\n" +
                "Video kaldırılmış veya bölgenizde erişime kapalı olabilir."

            // Geo-restricted
            lower.contains("geo") || lower.contains("not available in your country") ->
                "🌍 Bu video bölgenizde erişime kapalı!\n" +
                "İçerik coğrafi kısıtlama nedeniyle izlenemiyor."

            // Login required
            lower.contains("sign in") || lower.contains("login") ||
            lower.contains("age") ->
                "🔐 Bu içerik için giriş yapılması gerekiyor!\n" +
                "Yaş kısıtlamalı veya üye-only içerik olabilir."

            // Copyright / DMCA
            lower.contains("copyright") || lower.contains("dmca") ||
            lower.contains("blocked") ->
                "©️ Bu içerik telif hakkı nedeniyle engellenmiş!\n" +
                "İçerik sahibi tarafından indirme engellenmiş olabilir."

            // Network errors
            lower.contains("urlopen") || lower.contains("connection") ||
            lower.contains("timed out") || lower.contains("network") ->
                "🌐 Bağlantı hatası!\n" +
                "İnternet bağlantınızı kontrol edin ve tekrar deneyin."

            // Fallback: show raw error
            else -> rawError
        }
    }
}

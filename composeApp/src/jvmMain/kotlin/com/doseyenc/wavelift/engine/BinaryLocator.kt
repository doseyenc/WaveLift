package com.doseyenc.wavelift.engine

import java.io.File

/**
 * Resolves the paths to bundled yt-dlp and ffmpeg binaries.
 * 
 * Priority:
 * 1. Bundled in appResources (packaged app)
 * 2. System PATH fallback (development mode)
 */
object BinaryLocator {

    private val isWindows = System.getProperty("os.name").lowercase().contains("win")

    /**
     * Returns the absolute path to yt-dlp binary, or "yt-dlp" for PATH fallback.
     */
    fun ytDlpPath(): String {
        val bundled = findBundledBinary("yt-dlp")
        return bundled?.absolutePath ?: "yt-dlp"
    }

    /**
     * Returns the absolute path to ffmpeg binary, or "ffmpeg" for PATH fallback.
     */
    fun ffmpegPath(): String {
        val bundled = findBundledBinary("ffmpeg")
        return bundled?.absolutePath ?: "ffmpeg"
    }

    /**
     * Returns the ffmpeg directory for yt-dlp's --ffmpeg-location flag, or null if not bundled.
     */
    fun ffmpegDir(): String? {
        val bundled = findBundledBinary("ffmpeg")
        return bundled?.parentFile?.absolutePath
    }

    /**
     * Checks if the binaries are bundled (packaged mode) or using system PATH.
     */
    fun isBundled(): Boolean {
        return findBundledBinary("yt-dlp") != null
    }

    private fun findBundledBinary(name: String): File? {
        val resourcesDir = System.getProperty("compose.application.resources.dir") ?: return null
        val binaryName = if (isWindows) "$name.exe" else name
        val file = File(resourcesDir, binaryName)

        if (file.exists()) {
            // Ensure executable permission on macOS/Linux
            if (!isWindows && !file.canExecute()) {
                file.setExecutable(true)
            }
            return file
        }
        return null
    }
}

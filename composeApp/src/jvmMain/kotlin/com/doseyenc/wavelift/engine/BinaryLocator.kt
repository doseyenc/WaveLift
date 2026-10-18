package com.doseyenc.wavelift.engine

import java.io.File

/**
 * Resolves the paths to bundled yt-dlp and ffmpeg binaries.
 *
 * Priority:
 * 1. Bundled in appResources (packaged app)
 * 2. System install (PATH + common package manager locations)
 *
 * Lookups are not cached, so a binary installed while the app is running is picked up
 * on the next download without a restart.
 */
object BinaryLocator {

    private val isWindows = System.getProperty("os.name").lowercase().contains("win")

    /**
     * Install locations that are missing from PATH when the app is launched from
     * Finder/Dock (macOS gives GUI apps only /usr/bin:/bin:/usr/sbin:/sbin) or when
     * a package manager updated PATH after the app was started.
     */
    private val extraSearchDirs: List<String> by lazy {
        val home = System.getProperty("user.home")
        if (isWindows) {
            listOfNotNull(
                System.getenv("LOCALAPPDATA")?.let { "$it\\Microsoft\\WinGet\\Links" },
                "$home\\scoop\\shims"
            )
        } else {
            listOf(
                "/opt/homebrew/bin",
                "/usr/local/bin",
                "/opt/local/bin",
                "$home/.local/bin",
                "/usr/bin",
                "/bin"
            )
        }
    }

    /**
     * Returns the absolute path to yt-dlp binary, or "yt-dlp" for PATH fallback.
     */
    fun ytDlpPath(): String {
        return resolve("yt-dlp")?.absolutePath ?: "yt-dlp"
    }

    /**
     * Returns the absolute path to ffmpeg binary, or "ffmpeg" for PATH fallback.
     */
    fun ffmpegPath(): String {
        return resolve("ffmpeg")?.absolutePath ?: "ffmpeg"
    }

    /**
     * Returns the ffmpeg directory for yt-dlp's --ffmpeg-location flag, or null if not found.
     */
    fun ffmpegDir(): String? {
        return resolve("ffmpeg")?.parentFile?.absolutePath
    }

    /**
     * Checks if the binaries are bundled (packaged mode) or using system PATH.
     */
    fun isBundled(): Boolean {
        return findBundledBinary("yt-dlp") != null
    }

    /**
     * PATH for child processes, so yt-dlp can find its own helpers (deno, ffprobe)
     * even when the app was launched with a minimal PATH.
     */
    fun searchPath(): String {
        return searchDirs().joinToString(File.pathSeparator)
    }

    private fun resolve(name: String): File? {
        return findBundledBinary(name) ?: findSystemBinary(name)
    }

    private fun findBundledBinary(name: String): File? {
        val resourcesDir = System.getProperty("compose.application.resources.dir") ?: return null

        // Compose copies appResources/<os>-<arch>/ flat into the resources dir when packaging
        val file = File(resourcesDir, binaryName(name))

        if (file.exists()) {
            // Ensure executable permission on macOS/Linux
            if (!isWindows && !file.canExecute()) {
                file.setExecutable(true)
            }
            return file
        }
        return null
    }

    private fun findSystemBinary(name: String): File? {
        return searchDirs()
            .map { File(it, binaryName(name)) }
            .firstOrNull { it.isFile && it.canExecute() }
    }

    private fun searchDirs(): List<String> {
        val pathDirs = System.getenv("PATH").orEmpty().split(File.pathSeparator)
        return (pathDirs + extraSearchDirs).filter { it.isNotBlank() }.distinct()
    }

    private fun binaryName(name: String): String = if (isWindows) "$name.exe" else name
}

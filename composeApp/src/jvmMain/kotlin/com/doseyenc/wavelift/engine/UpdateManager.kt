package com.doseyenc.wavelift.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

data class ReleaseAsset(
    val name: String,
    val downloadUrl: String,
    val size: Long,
    val sha256: String?
)

data class ReleaseInfo(
    val version: String,
    val releaseUrl: String,
    /** Installer for the current platform, or null if the release has none. */
    val asset: ReleaseAsset?
)

/**
 * Checks GitHub Releases for a newer version, downloads the installer for the
 * current platform and hands it over to the OS installer.
 */
object UpdateManager {

    private const val LATEST_RELEASE_API = "https://api.github.com/repos/doseyenc/WaveLift/releases/latest"

    private val os = System.getProperty("os.name").lowercase()
    private val arch = System.getProperty("os.arch").lowercase()
    private val updateDir = File(System.getProperty("java.io.tmpdir"), "WaveLift-updates")

    /** Version of the installed app, or null when running from Gradle instead of a packaged build. */
    val currentVersion: String? = System.getProperty("jpackage.app-version")

    fun fetchLatestRelease(): ReleaseInfo {
        val connection = openConnection(LATEST_RELEASE_API).apply {
            setRequestProperty("Accept", "application/vnd.github+json")
        }
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("GitHub API returned HTTP ${connection.responseCode}")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val release = Json.parseToJsonElement(body).jsonObject

            val assets = release["assets"]?.jsonArray.orEmpty().map { element ->
                val asset = element.jsonObject
                ReleaseAsset(
                    name = asset.getValue("name").jsonPrimitive.content,
                    downloadUrl = asset.getValue("browser_download_url").jsonPrimitive.content,
                    size = asset.getValue("size").jsonPrimitive.long,
                    sha256 = asset["digest"]?.jsonPrimitive?.content
                        ?.takeIf { it.startsWith("sha256:") }
                        ?.removePrefix("sha256:")
                )
            }

            return ReleaseInfo(
                version = release.getValue("tag_name").jsonPrimitive.content.removePrefix("v"),
                releaseUrl = release.getValue("html_url").jsonPrimitive.content,
                asset = installerExtension()?.let { ext -> assets.firstOrNull { it.name.endsWith(ext) } }
            )
        } finally {
            connection.disconnect()
        }
    }

    /** Compares dotted versions numerically, e.g. "1.10.0" is newer than "1.9.2". */
    fun isNewer(latest: String, current: String): Boolean {
        val a = parseVersion(latest)
        val b = parseVersion(current)
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }

    /**
     * Downloads [asset] and verifies its size and SHA-256 digest.
     * A previously completed download of the same file is reused.
     */
    suspend fun download(asset: ReleaseAsset, onProgress: (Float) -> Unit): File = withContext(Dispatchers.IO) {
        updateDir.mkdirs()
        val target = File(updateDir, asset.name)
        if (target.isFile && target.length() == asset.size && matchesDigest(target, asset)) {
            onProgress(1f)
            return@withContext target
        }

        val partial = File(updateDir, "${asset.name}.part")
        val connection = openConnection(asset.downloadUrl)
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("Download returned HTTP ${connection.responseCode}")
            }
            connection.inputStream.use { input ->
                partial.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var downloaded = 0L
                    var lastPercent = -1
                    while (true) {
                        ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        val percent = (downloaded * 100 / asset.size).toInt()
                        if (percent != lastPercent) {
                            lastPercent = percent
                            onProgress(downloaded.toFloat() / asset.size)
                        }
                    }
                }
            }
        } finally {
            connection.disconnect()
        }

        if (partial.length() != asset.size || !matchesDigest(partial, asset)) {
            partial.delete()
            throw IOException("Downloaded update is corrupt")
        }
        Files.move(partial.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        target
    }

    /**
     * Starts installing [installer] in a separate process.
     * Returns true when the app must quit so the installer can replace it.
     */
    fun install(installer: File): Boolean {
        return when {
            os.contains("mac") -> installOnMac(installer)
            os.contains("win") -> installOnWindows(installer)
            else -> {
                // .deb needs root, so let the system's package installer take over
                Desktop.getDesktop().open(installer)
                false
            }
        }
    }

    /** Removes downloaded installers once the app is up to date. */
    fun clearDownloads() {
        updateDir.deleteRecursively()
    }

    private fun installOnMac(dmg: File): Boolean {
        val launcher = System.getProperty("jpackage.app-path")?.let(::File)
        val appBundle = generateSequence(launcher) { it.parentFile }.firstOrNull { it.name.endsWith(".app") }

        // Without write access to the install location, fall back to opening the DMG in Finder
        if (appBundle == null || !appBundle.parentFile.canWrite()) {
            Desktop.getDesktop().open(dmg)
            return false
        }

        val script = File(updateDir, "install-macos.sh")
        UpdateManager::class.java.getResourceAsStream("/update/install-macos.sh")!!.use { input ->
            script.outputStream().use { input.copyTo(it) }
        }
        ProcessBuilder(
            "/bin/sh", script.absolutePath,
            ProcessHandle.current().pid().toString(),
            dmg.absolutePath,
            appBundle.absolutePath
        )
            .redirectErrorStream(true)
            .redirectOutput(File(updateDir, "install.log"))
            .start()
        return true
    }

    private fun installOnWindows(msi: File): Boolean {
        val launcher = System.getProperty("jpackage.app-path").orEmpty()
        val script = File(updateDir, "install-windows.cmd")
        // MSI upgrades in place thanks to upgradeUuid; relaunch the app once it succeeds
        script.writeText(
            "@echo off\r\n" +
            "msiexec /i \"%~1\" /passive /norestart\r\n" +
            "if not errorlevel 1 if not \"%~2\"==\"\" start \"\" \"%~2\"\r\n"
        )
        ProcessBuilder("cmd", "/c", script.absolutePath, msi.absolutePath, launcher)
            .redirectErrorStream(true)
            .redirectOutput(File(updateDir, "install.log"))
            .start()
        return true
    }

    private fun installerExtension(): String? = when {
        // Release DMGs are built on Apple Silicon runners only
        os.contains("mac") -> if (arch == "aarch64") ".dmg" else null
        os.contains("win") -> ".msi"
        os.contains("linux") -> ".deb"
        else -> null
    }

    private fun matchesDigest(file: File, asset: ReleaseAsset): Boolean {
        val expected = asset.sha256 ?: return true
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }.equals(expected, ignoreCase = true)
    }

    private fun parseVersion(version: String): List<Int> =
        version.removePrefix("v").split('.').map { part -> part.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }

    private fun openConnection(url: String): HttpURLConnection =
        (URI(url).toURL().openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("User-Agent", "WaveLift/${currentVersion ?: "dev"}")
        }
}

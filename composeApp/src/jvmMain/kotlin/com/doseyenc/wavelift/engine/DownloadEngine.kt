package com.doseyenc.wavelift.engine

import com.doseyenc.wavelift.model.AudioQuality
import com.doseyenc.wavelift.model.DownloadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.coroutines.coroutineContext

class DownloadEngine {

    fun download(
        url: String,
        quality: AudioQuality,
        outputDir: String,
        embedThumbnail: Boolean = true,
        addMetadata: Boolean = true
    ): Flow<DownloadState> = flow {
        emit(DownloadState.Analyzing("Analyzing link..."))

        val command = buildCommand(url, quality, outputDir, embedThumbnail, addMetadata)
        println("[WaveLift] Running: ${command.joinToString(" ")}")

        val process: Process
        try {
            val processBuilder = ProcessBuilder(command)
                .redirectErrorStream(true)
                .directory(java.io.File(outputDir))

            process = processBuilder.start()
        } catch (e: java.io.IOException) {
            emit(DownloadState.Error(mapStartError(e)))
            return@flow
        }

        val outputLines = mutableListOf<String>()
        try {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.useLines { lines ->
                for (line in lines) {
                    if (!coroutineContext.isActive) break
                    println("[yt-dlp] $line")
                    outputLines.add(line)

                    ProgressParser.parseProgress(line)?.let { emit(it) }
                }
            }

            if (coroutineContext.isActive) {
                val exitCode = process.waitFor()
                if (exitCode == 0) {
                    emit(DownloadState.Completed(outputDir))
                } else {
                    val errorContext = outputLines.takeLast(5).joinToString("\n")
                    emit(DownloadState.Error("yt-dlp error ($exitCode):\n$errorContext"))
                }
            }
        } finally {
            if (process.isAlive) {
                println("[WaveLift] Cleaning up process...")
                process.destroyForcibly()
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun mapStartError(e: java.io.IOException): String {
        return when {
            e.message?.contains("No such file") == true ||
            e.message?.contains("Cannot run program") == true ->
                "yt-dlp not found! Please make sure yt-dlp is installed.\n" +
                "macOS: brew install yt-dlp\n" +
                "Windows: winget install yt-dlp"
            else -> "Download error: ${e.message}"
        }
    }

    fun analyzePlaylist(url: String): Flow<DownloadState> = flow {
        emit(DownloadState.Analyzing("Analyzing playlist..."))

        val command = buildList {
            add(BinaryLocator.ytDlpPath())
            add("--flat-playlist")
            add("--print")
            add("title")
            add("--no-warnings")
            BinaryLocator.ffmpegDir()?.let {
                add("--ffmpeg-location")
                add(it)
            }
            add(url)
        }

        val process: Process
        try {
            val processBuilder = ProcessBuilder(command).redirectErrorStream(true)
            process = processBuilder.start()
        } catch (e: Exception) {
            emit(DownloadState.Error("Failed to start analysis: ${e.message}"))
            return@flow
        }

        val titles = mutableListOf<String>()
        try {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.useLines { lines ->
                for (line in lines) {
                    if (!coroutineContext.isActive) break
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty() && !trimmed.startsWith("ERROR")) {
                        titles.add(trimmed)
                    }
                }
            }

            if (coroutineContext.isActive) {
                val exitCode = process.waitFor()
                if (exitCode == 0 && titles.isNotEmpty()) {
                    emit(DownloadState.Analyzing("${titles.size} songs found, ready to download!"))
                } else if (titles.isEmpty()) {
                    emit(DownloadState.Analyzing("Single video detected."))
                } else {
                    emit(DownloadState.Error("Playlist analysis failed."))
                }
            }
        } finally {
            if (process.isAlive) {
                process.destroyForcibly()
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun buildCommand(
        url: String,
        quality: AudioQuality,
        outputDir: String,
        embedThumbnail: Boolean,
        addMetadata: Boolean
    ): List<String> {
        return buildList {
            add(BinaryLocator.ytDlpPath())
            add("-x")
            add("--audio-format")
            add("mp3")
            add("--audio-quality")
            add(quality.ytDlpValue)
            add("--newline")
            add("--no-check-certificates")
            add("--no-cache-dir")
            add("--force-ipv4")
            add("--ignore-errors")  // Skip failed items in playlists
            add("--retries")
            add("3")
            BinaryLocator.ffmpegDir()?.let {
                add("--ffmpeg-location")
                add(it)
            }
            if (embedThumbnail) {
                add("--embed-thumbnail")
            }
            if (addMetadata) {
                add("--embed-metadata")
            }
            add("-o")
            add("$outputDir/%(title)s.%(ext)s")
            // Allow playlists — no --no-playlist flag
            add(url)
        }
    }
}


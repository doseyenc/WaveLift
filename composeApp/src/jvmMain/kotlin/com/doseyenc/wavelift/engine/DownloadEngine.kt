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

        try {
            val processBuilder = ProcessBuilder(command)
                .redirectErrorStream(true)
                .directory(java.io.File(outputDir))

            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))

            var lastTitle: String? = null
            val outputLines = mutableListOf<String>()

            reader.useLines { lines ->
                for (line in lines) {
                    if (!coroutineContext.isActive) {
                        process.destroyForcibly()
                        return@useLines
                    }

                    println("[yt-dlp] $line") // Debug log
                    outputLines.add(line)

                    // Try to extract title from destination line
                    ProgressParser.parseTitleFromDestination(line)?.let {
                        lastTitle = it
                    }

                    // Parse and emit progress updates
                    ProgressParser.parseProgress(line)?.let { state ->
                        emit(state)
                    }
                }
            }

            val exitCode = process.waitFor()
            if (exitCode == 0) {
                emit(DownloadState.Completed(outputDir))
            } else {
                // Collect last few output lines for error context
                val errorContext = outputLines.takeLast(5).joinToString("\n")
                emit(DownloadState.Error("yt-dlp exit code: $exitCode\n$errorContext"))
            }
        } catch (e: java.io.IOException) {
            val message = when {
                e.message?.contains("No such file") == true ||
                e.message?.contains("Cannot run program") == true ->
                    "yt-dlp not found! Please make sure yt-dlp is installed.\n" +
                    "macOS: brew install yt-dlp\n" +
                    "Windows: winget install yt-dlp"
                else -> "Download error: ${e.message}"
            }
            emit(DownloadState.Error(message))
        } catch (e: Exception) {
            emit(DownloadState.Error("Unexpected error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

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

        try {
            val processBuilder = ProcessBuilder(command)
                .redirectErrorStream(true)

            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val titles = mutableListOf<String>()

            reader.useLines { lines ->
                for (line in lines) {
                    if (!coroutineContext.isActive) {
                        process.destroyForcibly()
                        return@useLines
                    }
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty() && !trimmed.startsWith("ERROR")) {
                        titles.add(trimmed)
                    }
                }
            }

            val exitCode = process.waitFor()
            if (exitCode == 0 && titles.isNotEmpty()) {
                emit(DownloadState.Analyzing("${titles.size} songs found, ready to download!"))
            } else if (titles.isEmpty()) {
                emit(DownloadState.Analyzing("Single video detected."))
            } else {
                emit(DownloadState.Error("Playlist analysis failed."))
            }
        } catch (e: java.io.IOException) {
            emit(DownloadState.Error("yt-dlp not found! Please make sure it's installed."))
        } catch (e: Exception) {
            emit(DownloadState.Error("Analysis error: ${e.message}"))
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


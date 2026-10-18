package com.doseyenc.wavelift.model

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()

    /** A newer release exists but hasn't been downloaded (auto-download off, or no installer for this platform). */
    data class Available(
        val version: String,
        val releaseUrl: String,
        val canInstall: Boolean
    ) : UpdateState()

    data class Downloading(
        val version: String,
        val releaseUrl: String,
        val progress: Float = 0f
    ) : UpdateState()

    data class Ready(val version: String, val releaseUrl: String) : UpdateState()
    data object Installing : UpdateState()
    data class Failed(val version: String, val releaseUrl: String) : UpdateState()
}

package com.doseyenc.wavelift.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class DownloadItem @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toString(),
    val title: String,
    val url: String,
    val quality: AudioQuality,
    val state: DownloadState = DownloadState.Idle,
    val outputPath: String = ""
)

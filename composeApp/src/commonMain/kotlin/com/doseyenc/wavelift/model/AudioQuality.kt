package com.doseyenc.wavelift.model

enum class AudioQuality(val kbps: Int, val label: String) {
    LOW(128, "128 kbps"),
    MEDIUM(192, "192 kbps"),
    HIGH(320, "320 kbps");

    val ytDlpValue: String get() = kbps.toString()
}

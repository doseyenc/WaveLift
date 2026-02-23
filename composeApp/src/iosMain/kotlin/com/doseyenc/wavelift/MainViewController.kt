package com.doseyenc.wavelift

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    MaterialTheme {
        Text("WaveLift — Desktop only")
    }
}
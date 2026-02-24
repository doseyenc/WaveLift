package com.doseyenc.wavelift

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.doseyenc.wavelift.viewmodel.DownloadViewModel

fun main() = application {
    val viewModel = DownloadViewModel()

    Window(
        onCloseRequest = ::exitApplication,
        title = "WaveLift — Media Converter",
        icon = painterResource("WaveLift.png"),
        state = WindowState(
            size = DpSize(width = 900.dp, height = 750.dp)
        )
    ) {
        App(viewModel)
    }
}
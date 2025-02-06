package icu.bluedream.gameinstaller

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import icu.bluedream.gameinstaller.ui.screen.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "GameInstaller",
    ) {
        App()
    }
}
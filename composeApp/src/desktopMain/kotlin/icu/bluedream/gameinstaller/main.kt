package icu.bluedream.gameinstaller

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cafe.adriel.voyager.navigator.Navigator
import icu.bluedream.gameinstaller.core.storage.SelfStorage
import icu.bluedream.gameinstaller.ui.theme.GameSetupTheme
import icu.bluedream.gameinstaller.ui.view.HomeView

fun main() {
    println(SelfStorage.getCacheFolder())
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "GameSetup",
        ) {
            GameSetupTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigator(HomeView())
                }
            }
        }
    }
}
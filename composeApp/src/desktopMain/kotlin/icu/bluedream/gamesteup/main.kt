package icu.bluedream.gamesteup

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cafe.adriel.voyager.navigator.Navigator
import icu.bluedream.gamesteup.core.utils.FileSystemUtil
import icu.bluedream.gamesteup.ui.screen.HomeScreen
import icu.bluedream.gamesteup.ui.theme.GameSetupAppTheme

fun main() = application {
    println("We have attached cache folder: ${FileSystemUtil.getCacheRoot()}")
    Window(
        onCloseRequest = ::exitApplication,
        title = "GameSetup",
    ) {
        GameSetupAppTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Navigator(HomeScreen())
            }
        }
    }
}
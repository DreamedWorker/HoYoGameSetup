package icu.bluedream.gameinstaller.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import gameinstaller.composeapp.generated.resources.*
import gameinstaller.composeapp.generated.resources.Res
import gameinstaller.composeapp.generated.resources.app_ok
import gameinstaller.composeapp.generated.resources.app_window_title
import gameinstaller.composeapp.generated.resources.home_menu_about
import icu.bluedream.gameinstaller.data.HomeViewUiState
import icu.bluedream.gameinstaller.data.UiParts.*
import icu.bluedream.gameinstaller.ui.theme.GameInstallerTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.awt.Desktop
import java.net.URI

var configuration: HomeViewUiState = HomeViewUiState()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var showMoreMenu by remember { mutableStateOf(false) }
    var uiPart by remember { mutableStateOf(CONFIGURATION) }
    var showAbout by remember { mutableStateOf(false) }
    GameInstallerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize().systemBarsPadding(),
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(Res.string.app_window_title)) },
                        actions = {
                            IconButton({ showMoreMenu = true }) {
                                Icon(Icons.Default.MoreVert, "more option button")
                                DropdownMenu(showMoreMenu, { showMoreMenu = !showMoreMenu }) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.home_menu_update)) },
                                        onClick = {
                                            if (Desktop.isDesktopSupported()) {
                                                Desktop.getDesktop().browse(URI.create("https://github.com/DreamedWorker/HoYoGameSetup"))
                                            }
                                        },
                                        leadingIcon = { Icon(Icons.Default.Update, null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.home_menu_about)) },
                                        onClick = { showAbout = true },
                                        leadingIcon = { Icon(Icons.Default.AccountBalance, null) }
                                    )
                                }
                            }
                        }
                    )
                }
            ) { pd ->
                Column(modifier = Modifier.padding(pd)) {
                    when(uiPart) {
                        CONFIGURATION -> ConfigurationPart(
                            changeUI = { a, b ->
                                configuration = b
                                uiPart = a
                            }
                        )
                        DATA_ALREADY_EXISTS -> UseExistingData(
                            configuration,
                            changeUI = {
                                uiPart = it
                            }
                        )
                        INSTALL_BRAND_NEW -> BrandNewInstall(
                            configuration,
                            changeUI = {
                                uiPart = it
                            }
                        )
                        ALL_DONE -> {
                            AllDoneAndPlayNow(configuration)
                        }
                    }
                }
                if (showAbout) {
                    AlertDialog(
                        onDismissRequest = { showAbout = !showAbout },
                        confirmButton = {
                            TextButton({showAbout = !showAbout}) {
                                Text(stringResource(Res.string.app_ok))
                            }
                        },
                        title = { Text(stringResource(Res.string.home_menu_about)) },
                        icon = { Image(painterResource(Res.drawable.app_logo), null) },
                        text = { AboutUs() }
                    )
                }
            }
        }
    }
}
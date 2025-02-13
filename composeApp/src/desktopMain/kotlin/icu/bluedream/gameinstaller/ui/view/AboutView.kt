package icu.bluedream.gameinstaller.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import gamesetup.composeapp.generated.resources.*
import icu.bluedream.gameinstaller.core.storage.SelfStorage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.awt.Desktop
import java.io.File
import java.net.URI

class AboutView : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.about_title)) },
                    navigationIcon = {
                        IconButton({navigator.pop()}) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                        }
                    }
                )
            }
        ) { pd ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(pd).systemBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painterResource(Res.drawable.app_logo), null,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                    Text(
                        stringResource(Res.string.app_name),
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(stringResource(Res.string.about_version))
                }
                Column {
                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.about_license)) },
                        leadingContent = { Icon(Icons.Default.Policy, null) },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                        },
                        modifier = Modifier.clickable {
                            if (Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().browse(URI.create("https://github.com/GameplayInMac/HoYoGameSetup/blob/main/LICENSE"))
                            }
                        }.padding(vertical = 4.dp)
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.about_release)) },
                        leadingContent = { Icon(Icons.Default.Update, null) },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                        },
                        modifier = Modifier.clickable {
                            if (Desktop.isDesktopSupported()) {
                                Desktop.getDesktop().browse(URI.create("https://github.com/GameplayInMac/HoYoGameSetup/releases"))
                            }
                        }.padding(vertical = 4.dp)
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.about_delete_caches)) },
                        leadingContent = { Icon(Icons.Default.DeleteForever, null) },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                        },
                        modifier = Modifier.clickable {
                            with(File(SelfStorage.getCacheFolder())) {
                                deleteRecursively()
                                mkdirs()
                            }
                        }.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
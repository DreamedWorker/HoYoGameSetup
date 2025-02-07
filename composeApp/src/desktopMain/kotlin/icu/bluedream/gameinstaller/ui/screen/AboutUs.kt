package icu.bluedream.gameinstaller.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gameinstaller.composeapp.generated.resources.Res
import gameinstaller.composeapp.generated.resources.about_license
import gameinstaller.composeapp.generated.resources.about_tip
import gameinstaller.composeapp.generated.resources.about_version
import org.jetbrains.compose.resources.stringResource
import java.awt.Desktop
import java.net.URI

@Composable
fun AboutUs() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(Res.string.about_version), style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp))
        Spacer(Modifier.size(8.dp))
        Card {
            ListItem(
                headlineContent = { Text(stringResource(Res.string.about_license)) },
                leadingContent = {
                    Icon(Icons.Default.Policy, null)
                },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                },
                modifier = Modifier.clickable {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop()
                            .browse(URI.create("https://github.com/DreamedWorker/HoYoGameSetup/blob/main/LICENSE"))
                    }
                }
            )
        }
        Spacer(Modifier.size(8.dp))
        Row {
            Icon(Icons.Outlined.Info, null)
            Spacer(Modifier.weight(1f))
        }
        Text(stringResource(Res.string.about_tip), style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
    }
}
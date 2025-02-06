package icu.bluedream.gameinstaller.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gameinstaller.composeapp.generated.resources.Res
import gameinstaller.composeapp.generated.resources.all_done
import gameinstaller.composeapp.generated.resources.all_done_close
import gameinstaller.composeapp.generated.resources.all_done_open
import icu.bluedream.gameinstaller.data.Constants
import icu.bluedream.gameinstaller.data.HomeViewUiState
import icu.bluedream.gameinstaller.helper.GameHelper
import org.jetbrains.compose.resources.stringResource
import java.io.File
import kotlin.system.exitProcess

@Composable
fun AllDoneAndPlayNow(installConfig: HomeViewUiState) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(Icons.Default.Check, null, modifier = Modifier.size(72.dp))
        Spacer(Modifier.size(8.dp))
        Text(stringResource(Res.string.all_done), style = MaterialTheme.typography.headlineLarge)
        Row(
            modifier = Modifier.padding(top = 8.dp)
        ) {
            OutlinedButton({ exitProcess(0) }) {
                Text(stringResource(Res.string.all_done_close))
            }
            Spacer(Modifier.width(8.dp))
            Button({
                val name = GameHelper.getGameFinderName(installConfig.selectedGameType)
                val app = File(Constants.MAC_APPLICATION_PATH, name).absolutePath
                val pb = ProcessBuilder("/usr/bin/open", app)
                pb.start().waitFor()
                exitProcess(0)
            }) {
                Text(stringResource(Res.string.all_done_open))
            }
        }
    }
}
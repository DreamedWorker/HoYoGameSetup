package icu.bluedream.gameinstaller.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import gamesetup.composeapp.generated.resources.*
import gamesetup.composeapp.generated.resources.Res
import gamesetup.composeapp.generated.resources.done_close
import gamesetup.composeapp.generated.resources.done_exp
import gamesetup.composeapp.generated.resources.done_title
import icu.bluedream.gameinstaller.data.types.GameType
import org.jetbrains.compose.resources.stringResource
import kotlin.system.exitProcess

class AllDoneView(private val type: GameType) : Screen {

    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize().systemBarsPadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                Icons.Default.Check, null,
                modifier = Modifier.size(78.dp)
            )
            Text(
                stringResource(Res.string.done_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(stringResource(Res.string.done_exp))
            Row(modifier = Modifier.padding(top = 8.dp)) {
                ElevatedButton({ exitProcess(0) }) {
                    Text(stringResource(Res.string.done_close))
                }
                Box(modifier = Modifier.size(8.dp))
                Button({
                    val openPb = ProcessBuilder(
                        "/usr/bin/open", "-a",
                        GameType.getAppTranslationName(type)
                    )
                    openPb.start().waitFor()
                    exitProcess(0)
                }) {
                    Text(stringResource(Res.string.done_open))
                }
            }
        }
    }
}
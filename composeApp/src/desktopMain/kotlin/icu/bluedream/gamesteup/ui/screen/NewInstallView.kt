package icu.bluedream.gamesteup.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import gamesetup.composeapp.generated.resources.*
import gamesetup.composeapp.generated.resources.Res
import gamesetup.composeapp.generated.resources.YuanShenPortingIcon
import gamesetup.composeapp.generated.resources.Zenless_Zone_Zero
import gamesetup.composeapp.generated.resources.brand_title
import icu.bluedream.gamesteup.data.state.HomeUiState
import icu.bluedream.gamesteup.data.types.GameType
import icu.bluedream.gamesteup.ui.composition.TaskIndicator
import icu.bluedream.gamesteup.viewmodel.BrandNewViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class NewInstallView(private val installConfig: HomeUiState) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel { BrandNewViewModel() }
        val state by viewModel.uiState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.brand_title)) },
                    navigationIcon = {
                        IconButton({navigator.pop()}) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                        }
                    }
                )
            },
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
                            painterResource(
                                when (installConfig.selectedGame) {
                                    GameType.GI_CN, GameType.GI_OS -> Res.drawable.YuanShenPortingIcon
                                    GameType.ZZZ_CN, GameType.ZZZ_OS -> Res.drawable.Zenless_Zone_Zero
                                }
                            ), null,
                            modifier = Modifier.size(72.dp)
                        )
                        CircularProgressIndicator(
                            progress = { state.totalTaskProgress },
                            modifier = Modifier.size(105.dp)
                        )
                    }
                    Text(
                        stringResource(Res.string.brand_tip),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text(
                        stringResource(Res.string.home_steps),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    TaskIndicator(
                        taskImg = Icons.Default.AttachFile,
                        taskName = Res.string.brand_step_download_prepared_pack,
                        taskState = state.fetchMetaState
                    )
                    TaskIndicator(
                        taskImg = Icons.Default.CloudDownload,
                        taskName = Res.string.brand_step_fetch_game_pack_info,
                        taskState = state.downloadGamePathState,
                        isGamePackInfo = true,
                        packInfo = state.packInfo
                    )
                    TaskIndicator(
                        taskImg = Icons.Default.Downloading,
                        taskName = Res.string.brand_step_download_game_packs,
                        taskState = state.downloadGamePacksState,
                        isDownloadTask = true,
                        currentFile = state.currentFile,
                        totalFiles = state.totalFile,
                        speed = state.speed
                    )
                    TaskIndicator(
                        taskImg = Icons.Default.RocketLaunch,
                        taskName = Res.string.brand_step_merge_data,
                        taskState = state.modifyBatState
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Info, null)
                    Text(
                        stringResource(Res.string.brand_have_a_break),
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                    )
                }
            }
            if (state.showError) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissDialog() },
                    confirmButton = {
                        TextButton({ viewModel.dismissDialog() }){
                            Text(stringResource(Res.string.app_ok))
                        }
                    },
                    icon = { Icon(Icons.Default.Warning, null) },
                    title = { Text(stringResource(Res.string.app_error)) },
                    text = { Text(state.errorMessage) }
                )
            }
        }
        LaunchedEffect(Unit) {
            viewModel.downloadTask(installConfig) {
                navigator.push(AllDoneView(installConfig.selectedGame))
            }
        }
    }
}
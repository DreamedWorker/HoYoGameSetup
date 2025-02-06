package icu.bluedream.gameinstaller.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import gameinstaller.composeapp.generated.resources.*
import gameinstaller.composeapp.generated.resources.Res
import gameinstaller.composeapp.generated.resources.YuanShenPortingIcon
import gameinstaller.composeapp.generated.resources.Zenless_Zone_Zero
import gameinstaller.composeapp.generated.resources.brand_label
import icu.bluedream.gameinstaller.data.*
import icu.bluedream.gameinstaller.helper.FileHelper
import icu.bluedream.gameinstaller.helper.GameHelper
import icu.bluedream.gameinstaller.ui.composition.TaskIndicator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.io.File
import kotlin.system.exitProcess

lateinit var thatJob: Job

@Composable
fun UseExistingData(
    installConfig: HomeViewUiState,
    changeUI: (UiParts) -> Unit,
    viewModel: DataExistPartViewModel = viewModel<DataExistPartViewModel>()
) {
    val state by viewModel.appUiState.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            contentAlignment = Alignment.Center
        ) { // 总进度条
            Image(
                painterResource(
                    if (installConfig.selectedGameType.name.contains("Gen"))
                        Res.drawable.YuanShenPortingIcon else Res.drawable.Zenless_Zone_Zero
                ),
                null,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp))
            )
            CircularProgressIndicator(
                progress = { state.totalProgress },
                modifier = Modifier.size(115.dp),
                trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        ) { // 任务状态指示
            Text(
                stringResource(Res.string.brand_label, GameHelper.getGameFinderName(installConfig.selectedGameType)),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 2.dp)
            )
            TaskIndicator(
                taskImg = Icons.Default.Apps,
                taskTitle = Res.string.brand_task_writeApp,
                taskState = state.writeAppState
            )
            TaskIndicator(
                taskImg = Icons.Default.Code,
                taskTitle = Res.string.exists_processBat,
                taskState = state.mergeFilesState
            )
            Text(
                stringResource(Res.string.brand_title),
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        Row {
            Spacer(Modifier.weight(1f))
            OutlinedButton({
                thatJob.cancel("任务由用户手动取消")
                exitProcess(0) }
            ) {
                Text(stringResource(Res.string.brand_cancel))
            }
        }
    }
    LaunchedEffect(Unit) {
        Thread.sleep(1200)
        thatJob = viewModel.startTask(installConfig) { changeUI(it) }
    }
}

class DataExistPartViewModel : ViewModel() {
    private val _appUiState = MutableStateFlow(BrandNewInstallViewUiState())
    val appUiState: StateFlow<BrandNewInstallViewUiState> get() = _appUiState.asStateFlow()

    fun startTask(taskConfig: HomeViewUiState, goNext: (UiParts) -> Unit): Job {
        return viewModelScope.launch(Dispatchers.Main) {
            var canGoNext = true
            val jobCopyApp = launch(Dispatchers.IO) {
                if (isActive) {
                    val file = File(taskConfig.gameInstallationPath)
                    if (!file.isDirectory || !file.exists()) {
                        file.mkdir()
                    }
                    updateUiState { copy(writeAppState = TaskState.ONGOING) }
                    FileHelper.copyFile2AppDir(taskConfig.gameInstallationPath, taskConfig.selectedGameType) {
                        canGoNext = it
                        updateUiState { copy(writeAppState = if (it) TaskState.FINISHED else TaskState.FAILED) }
                        if (it) {
                            updateUiState { copy(totalProgress = 0.5f) }
                        }
                    }
                }
            }
            jobCopyApp.join()
            val jobChangeLauncherArgs = launch(Dispatchers.IO) {
                updateUiState { copy(mergeFilesState = TaskState.ONGOING) }
                updateUiState { copy(totalProgress = 1f) }
                Thread.sleep(1200)
                if (canGoNext && isActive) {
                    val launchBatPath =
                        "${Constants.MAC_APPLICATION_PATH}${GameHelper.getGameFinderName(taskConfig.selectedGameType)}/${Constants.WINE_DRIVE_C_PATH}${Constants.LAUNCH_BAT}"
                    val launchString = "\"${Constants.WINE_MAC_ROOT_PATH}${taskConfig.gameInstallationPath}${
                        GameHelper.getExeFileName(taskConfig.selectedGameType)
                    }\"" + if (GameHelper.isGS(taskConfig.selectedGameType)) " " + Constants.GS_CLOUD_GAME_PARAM else ""
                    val launchBatFile = File(launchBatPath)
                    launchBatFile.writeText(launchString)
                    updateUiState { copy(mergeFilesState = TaskState.FINISHED) }
                }
            }
            jobChangeLauncherArgs.join()
            withContext(Dispatchers.Main) {
                if (canGoNext) {
                    goNext(UiParts.ALL_DONE)
                }
            }
        }
    }

    private fun updateUiState(update: BrandNewInstallViewUiState.() -> BrandNewInstallViewUiState) {
        updateUiState(_appUiState.value.update())
    }

    private fun updateUiState(uiState: BrandNewInstallViewUiState) {
        _appUiState.update { uiState }
    }
}
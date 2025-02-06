package icu.bluedream.gameinstaller.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.FolderZip
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
import icu.bluedream.gameinstaller.data.*
import icu.bluedream.gameinstaller.helper.FileHelper
import icu.bluedream.gameinstaller.helper.GameHelper
import icu.bluedream.gameinstaller.helper.NetworkHelper
import icu.bluedream.gameinstaller.helper.NetworkHelper.fetch
import icu.bluedream.gameinstaller.ui.composition.TaskIndicator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.io.File
import java.net.URI
import kotlin.system.exitProcess

lateinit var thisJob: Job

@Composable
fun BrandNewInstall(
    installConfig: HomeViewUiState,
    changeUI: (UiParts) -> Unit,
    viewModel: BrandNewInstallPartViewModel = viewModel<BrandNewInstallPartViewModel>()
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
                taskImg = Icons.Default.DocumentScanner,
                taskTitle = Res.string.brand_task_fetchInfo,
                taskState = state.fetchInfoState
            )
            TaskIndicator(
                taskImg = Icons.Default.Downloading,
                taskTitle = Res.string.brand_task_downloadFiles,
                taskState = state.downloadState,
                isDownloadTask = true,
                totalDownloads = state.totalDownloads,
                currentDownload = state.currentDownload,
                currentDownloadProgress = state.singleFileDownloadState,
                speed = state.speed
            )
            TaskIndicator(
                taskImg = Icons.Default.FolderZip,
                taskTitle = Res.string.brand_task_merge,
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
                thisJob.cancel("任务由用户手动取消")
                exitProcess(0) }
            ) {
                Text(stringResource(Res.string.brand_cancel))
            }
        }
    }
    LaunchedEffect(Unit) {
        Thread.sleep(1200)
        thisJob = viewModel.startTask(installConfig) { changeUI(it) }
    }
}

class BrandNewInstallPartViewModel : ViewModel() {
    private val _appUiState = MutableStateFlow(BrandNewInstallViewUiState())
    val appUiState: StateFlow<BrandNewInstallViewUiState> get() = _appUiState.asStateFlow()

    fun startTask(taskConfig: HomeViewUiState, goNext: (UiParts) -> Unit): Job {
        return viewModelScope.launch(Dispatchers.Main) {
            var version = "0.0.0"
            val pkgUrls = mutableListOf<String>()
            val pkgMd5s = mutableListOf<String>()
            var canGoNext = true
            var pkgZipFileName: String? = null
            val splitPkgZipFileName = mutableListOf<String>()
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
                            updateUiState { copy(totalProgress = 0.25f) }
                        }
                    }
                }
            }
            jobCopyApp.join()
            val jobFetchInfo = launch(Dispatchers.IO) {
                if (canGoNext && isActive) {
                    updateUiState { copy(fetchInfoState = TaskState.ONGOING) }
                    val url = URI.create(Constants.getGameURL(taskConfig.selectedGameType)).toURL()
                    val packageInfo = url.fetch<GameManifest>()
                    version = packageInfo.data.gamePackages[0].main.major?.version ?: "0.0.0"
                    if (version == "0.0.0") {
                        canGoNext = false
                        updateUiState { copy(fetchInfoState = TaskState.FAILED) }
                    } else {
                        for (pkg in packageInfo.data.gamePackages[0].main.major?.gamePkgs!!) {
                            pkgUrls.add(pkg.url)
                            pkgMd5s.add(pkg.md5.uppercase())
                        }
                        canGoNext = true
                        updateUiState { copy(totalDownloads = pkgUrls.size) }
                        updateUiState { copy(fetchInfoState = TaskState.FINISHED) }
                        updateUiState { copy(totalProgress = 0.5f) }
                    }
                } else {
                    canGoNext = false
                    updateUiState { copy(fetchInfoState = TaskState.FAILED) }
                }
            }
            jobFetchInfo.join()
            val jobDownloadFiles = launch(Dispatchers.IO) {
                if (canGoNext && isActive) {
                    updateUiState { copy(downloadState = TaskState.ONGOING) }
                    if (version == "0.0.0") {
                        canGoNext = false
                        updateUiState { copy(downloadState = TaskState.FAILED) }
                    } else {
                        for ((pkgCount, url) in pkgUrls.withIndex()) {
                            updateUiState { copy(currentDownload = pkgCount) }
                            while (!NetworkHelper.downloadFile(
                                    pkgUrl = url,
                                    destPath = taskConfig.gameInstallationPath,
                                    pkgCount = appUiState.value.totalDownloads,
                                    pkgZipFileName = pkgZipFileName,
                                    changePkgZipFileInnerName = {
                                        pkgZipFileName = it
                                    },
                                    pkgMd5s = pkgMd5s,
                                    splitPkgZipFileName = splitPkgZipFileName,
                                    addZipFileName2Split = {
                                        if(!splitPkgZipFileName.contains(it)) {
                                            splitPkgZipFileName.add(it)
                                        }
                                    },
                                    printDownloadProgress = { current, total, speed ->
                                        if (total <= 0f) {
                                            updateUiState { copy(singleFileDownloadState = 0f) }
                                        } else {
                                            updateUiState {
                                                copy(singleFileDownloadState = (current / total)
                                                    .coerceIn(0L, 1L).toFloat(), speed = speed
                                                )
                                            }
                                        }
                                    }
                                )) {
                                Thread.sleep(2000)
                            }
                            canGoNext = true
                            updateUiState { copy(downloadState = TaskState.FINISHED) }
                            updateUiState { copy(totalProgress = 0.75f) }
                        }
                    }
                } else {
                    canGoNext = false
                    updateUiState { copy(downloadState = TaskState.FAILED) }
                }
            }
            jobDownloadFiles.join()
            val jobUnzipFiles = launch(Dispatchers.IO) {
                if (canGoNext && isActive) {
                    updateUiState { copy(mergeFilesState = TaskState.ONGOING) }
                    FileHelper.catAllSplitZipFiles(taskConfig.gameInstallationPath, splitPkgZipFileName, pkgZipFileName)
                    if (FileHelper.unzipGame(pkgZipFileName, File(taskConfig.gameInstallationPath), taskConfig.gameInstallationPath)) {
                        canGoNext = true
                        updateUiState { copy(mergeFilesState = TaskState.FINISHED) }
                        updateUiState { copy(totalProgress = 1f) }
                    } else {
                        canGoNext = false
                        updateUiState { copy(mergeFilesState = TaskState.FAILED) }
                    }
                } else {
                    canGoNext = false
                    updateUiState { copy(mergeFilesState = TaskState.FAILED) }
                }
            }
            jobUnzipFiles.join()
            val jobChangeLauncherArgs = launch(Dispatchers.IO) {
                Thread.sleep(1200)
                if (canGoNext && isActive) {
                    val launchBatPath =
                        "${Constants.MAC_APPLICATION_PATH}${GameHelper.getGameFinderName(taskConfig.selectedGameType)}/${Constants.WINE_DRIVE_C_PATH}${Constants.LAUNCH_BAT}"
                    val launchString = "\"${Constants.WINE_MAC_ROOT_PATH}${taskConfig.gameInstallationPath}${
                        GameHelper.getExeFileName(taskConfig.selectedGameType)
                    }\"" + if (GameHelper.isGS(taskConfig.selectedGameType)) " " + Constants.GS_CLOUD_GAME_PARAM else ""
                    val launchBatFile = File(launchBatPath)
                    launchBatFile.writeText(launchString)
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
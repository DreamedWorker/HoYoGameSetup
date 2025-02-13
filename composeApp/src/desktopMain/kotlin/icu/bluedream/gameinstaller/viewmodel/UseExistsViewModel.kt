package icu.bluedream.gameinstaller.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import icu.bluedream.gameinstaller.core.helper.ContentUtil
import icu.bluedream.gameinstaller.core.helper.FileUtil
import icu.bluedream.gameinstaller.core.helper.HttpUtil
import icu.bluedream.gameinstaller.core.storage.SelfStorage
import icu.bluedream.gameinstaller.data.Constants
import icu.bluedream.gameinstaller.data.state.BrandNewUiState
import icu.bluedream.gameinstaller.data.state.HomeUiState
import icu.bluedream.gameinstaller.data.types.ActionState
import icu.bluedream.gameinstaller.data.types.GameType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class UseExistsViewModel : ViewModel() {
    private val _state = MutableStateFlow(BrandNewUiState())
    val uiState: StateFlow<BrandNewUiState> get() = _state.asStateFlow()

    fun startDownload(config: HomeUiState, goNext: () -> Unit) {
        try {
            viewModelScope.launch(Dispatchers.Main) {
                val jobFetchMeta = launch(Dispatchers.IO) {
                    var shallGo = true
                    val metaName = GameType.getMetaFileName(config.selectedGame)
                    val localFile = File(SelfStorage.getCacheFileFolder(), "transfer_$metaName")
                    fun mergeFile() {
                        val installPb = ProcessBuilder(
                            "/usr/bin/ditto", "-x", "-k",
                            "${SelfStorage.getCachePackageFolder()}/temp_app.zip",
                            Constants.MAC_APPLICATION_PATH
                        )
                        if (installPb.start().waitFor() == 0) {
                            File("${Constants.MAC_APPLICATION_PATH}${GameType.getGameAppName(config.selectedGame)}")
                                .renameTo(File("${Constants.MAC_APPLICATION_PATH}${GameType.getAppTranslationName(config.selectedGame)}"))
                            val oldFile = "${Constants.MAC_APPLICATION_PATH}${GameType.getGameAppName(config.selectedGame)}"
                            val rmPb = ProcessBuilder(
                                "/bin/rm", "-rf", oldFile
                            )
                            if (rmPb.start().waitFor() == 0) {
                                updateUIState { copy(fetchMetaState = ActionState.FINISHED, totalTaskProgress = 0.5f) }
                            } else {
                                updateUIState { copy(fetchMetaState = ActionState.FAILED) }
                            }
                        } else {
                            updateUIState { copy(fetchMetaState = ActionState.FAILED) }
                        }
                    }

                    fun processFiles() {
                        if (shallGo) {
                            val downloadLinks = getDownloadLinks(config.selectedGame, localFile.absolutePath)
                            val md5map = ContentUtil.readMd5FileToMap(localFile.absolutePath)
                            val tempPreDown = File(SelfStorage.getCachePackageFolder(), "tempPreDown")
                            if (!tempPreDown.exists()) {
                                tempPreDown.mkdirs()
                            }
                            if (File("${SelfStorage.getCachePackageFolder()}/temp_app.zip").exists()) {
                                mergeFile()
                            } else {
                                HttpUtil.downloadFiles(
                                    downloadLinks,
                                    tempPreDown,
                                    checkMd5 = {
                                        val md5 = FileUtil.calculateFileMD5(it.absolutePath)
                                        val name = it.nameWithoutExtension
                                        if (md5map.keys.contains(name)) {
                                            val storage = md5map[name] ?: ""
                                            storage == md5
                                        } else {
                                            false
                                        }
                                    },
                                    { _ ->
                                        val tempName = GameType.getSplitFileName(config.selectedGame)
                                        try {
                                            FileUtil.mergeSplitFiles(
                                                tempPreDown.absolutePath,
                                                "${SelfStorage.getCachePackageFolder()}/temp_app.zip",
                                                tempName
                                            )
                                            mergeFile()
                                        } catch (e: Exception) {
                                            updateUIState { copy(fetchMetaState = ActionState.FAILED) }
                                        }
                                    }
                                )
                            }
                        }
                    }
                    updateUIState { copy(fetchMetaState = ActionState.ONGOING) }
                    if (!localFile.exists()) {
                        try {
                            HttpUtil.downloadFile(
                                "https://ftp1.bluedream.icu/$metaName",
                                metaName,
                                SelfStorage.getCacheFileFolder()
                            )
                            ContentUtil.reformatMd5File(
                                "${SelfStorage.getCacheFileFolder()}/${metaName}",
                                localFile.absolutePath
                            )
                            shallGo = true
                            processFiles()
                        } catch (e: Exception) {
                            updateUIState { copy(fetchMetaState = ActionState.FAILED) }
                            shallGo = false
                        }
                    } else {
                        shallGo = true
                        processFiles()
                    }
                }
                jobFetchMeta.join()
                val jobMergeFiles = launch(Dispatchers.IO) {
                    if (_state.value.fetchMetaState == ActionState.FAILED) {
                        updateUIState { copy(modifyBatState = ActionState.FAILED) }
                    } else {
                        updateUIState { copy(modifyBatState = ActionState.ONGOING) }
                        ContentUtil.modifyLaunchBat(config.selectedGame, config.selectedPath)
                        updateUIState { copy(modifyBatState = ActionState.FINISHED, totalTaskProgress = 1f) }
                        Thread.sleep(1200)
                    }
                }
                jobMergeFiles.join()
                withContext(Dispatchers.Main) {
                    if (_state.value.modifyBatState == ActionState.FINISHED)
                        goNext.invoke()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            updateUIState {
                copy(errorMessage = e.localizedMessage, showError = true)
            }
        }
    }

    fun dismissDialog() {
        updateUIState {
            copy(showError = false, errorMessage = "")
        }
    }

    private fun getDownloadLinks(gameType: GameType, file: String): List<String> {
        val oriList = ContentUtil.getLeftPartsList(file)
        val url = when(gameType) {
            GameType.GI_OS, GameType.GI_CN -> "https://ftp1.bluedream.icu/GS/"
            GameType.ZZZ_OS, GameType.ZZZ_CN -> "https://ftp1.bluedream.icu/ZZZ/"
        }
        for ((index, name) in oriList.withIndex()) {
            oriList[index] = "$url$name"
        }
        return oriList.toList()
    }

    private fun updateUIState(update: BrandNewUiState.() -> BrandNewUiState) {
        updateUIState(_state.value.update())
    }

    private fun updateUIState(uiState: BrandNewUiState) {
        _state.update { uiState }
    }
}
package icu.bluedream.gamesteup.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import icu.bluedream.gamesteup.core.downloadGameDataPack
import icu.bluedream.gamesteup.core.downloadGameMetaFile
import icu.bluedream.gamesteup.core.downloadNecessaryFile
import icu.bluedream.gamesteup.core.pkgUrls
import icu.bluedream.gamesteup.data.state.BrandNewUiState
import icu.bluedream.gamesteup.data.state.HomeUiState
import icu.bluedream.gamesteup.data.types.ActionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BrandNewViewModel : ScreenModel {
    private val _state = MutableStateFlow(BrandNewUiState())
    val uiState: StateFlow<BrandNewUiState> get() = _state.asStateFlow()

    fun downloadTask(installConfig: HomeUiState, finished: () -> Unit) {
        try {
            screenModelScope.launch(Dispatchers.Main) {
                val jobDownload = launch(Dispatchers.IO) {
                    downloadNecessaryFile(
                        installConfig.selectedGame
                    ) {
                        updateUIState { copy(fetchMetaState = it) }
                        if (it == ActionState.FINISHED) {
                            updateUIState { copy(totalTaskProgress = 0.25f) }
                        }
                    }
                }
                jobDownload.join()
                val jobDownloadGameMeta = launch(Dispatchers.IO) {
                    if (_state.value.fetchMetaState == ActionState.FINISHED) {
                        downloadGameMetaFile(
                            installConfig.selectedGame,
                            {
                                updateUIState { copy(downloadGamePathState = it) }
                                if (it == ActionState.FINISHED) {
                                    updateUIState {
                                        copy(totalTaskProgress = 0.5f, totalFile = pkgUrls.size, currentFile = 0)
                                    }
                                }
                            },
                            { updateUIState { copy(packInfo = "Current game version is: V$it") } }
                        )
                    } else {
                        updateUIState { copy(downloadGamePathState = ActionState.FAILED) }
                    }
                }
                jobDownloadGameMeta.join()
                val jobDownloadAndMerge = launch(Dispatchers.IO) {
                    if (_state.value.downloadGamePathState == ActionState.FINISHED) {
                        downloadGameDataPack(
                            config = installConfig,
                            updateTaskState = { updateUIState { copy(downloadGamePacksState = it) } },
                            updateDownloadState = { index: Int, speed: Float ->
                                updateUIState { copy(currentFile = index, speed = speed) }
                            },
                            theLastTaskState = { updateUIState { copy(modifyBatState = it) } },
                            updateTotalProgress = { updateUIState { copy(totalTaskProgress = it) } }
                        )
                    } else {
                        updateUIState {
                            copy(
                                downloadGamePacksState = ActionState.FAILED,
                                modifyBatState = ActionState.FAILED
                            )
                        }
                    }
                }
                jobDownloadAndMerge.join()
                withContext(Dispatchers.Main) {
                    if (_state.value.modifyBatState == ActionState.FINISHED) {
                        finished.invoke()
                    }
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

    private fun updateUIState(update: BrandNewUiState.() -> BrandNewUiState) {
        updateUIState(_state.value.update())
    }

    private fun updateUIState(uiState: BrandNewUiState) {
        _state.update { uiState }
    }
}
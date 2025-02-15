package icu.bluedream.gamesteup.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import icu.bluedream.gamesteup.core.downloadNecessaryFile
import icu.bluedream.gamesteup.core.utils.ContentUtil
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

class UseExistsViewModel : ScreenModel {
    private val _state = MutableStateFlow(BrandNewUiState())
    val uiState: StateFlow<BrandNewUiState> get() = _state.asStateFlow()

    fun startTask(installConfig: HomeUiState, finished: () -> Unit) {
        try {
            screenModelScope.launch(Dispatchers.Main) {
                val jobDownload = launch(Dispatchers.IO) {
                    downloadNecessaryFile(
                        installConfig.selectedGame
                    ) {
                        updateUIState { copy(fetchMetaState = it) }
                        if (it == ActionState.FINISHED) {
                            updateUIState { copy(totalTaskProgress = 0.5f) }
                        }
                    }
                }
                jobDownload.join()
                val jobMergeFile = launch(Dispatchers.IO) {
                    if (_state.value.fetchMetaState == ActionState.FINISHED) {
                        ContentUtil.modifyLaunchBat(installConfig.selectedGame, installConfig.selectedPath)
                        updateUIState { copy(modifyBatState = ActionState.FINISHED) }
                        updateUIState { copy(totalTaskProgress = 1f) }
                        Thread.sleep(1200)
                    } else {
                        updateUIState { copy(modifyBatState = ActionState.FAILED) }
                    }
                }
                jobMergeFile.join()
                withContext(Dispatchers.Main) {
                    finished.invoke()
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
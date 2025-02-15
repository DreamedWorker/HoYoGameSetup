package icu.bluedream.gamesteup.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import icu.bluedream.gamesteup.core.utils.FileUtil
import icu.bluedream.gamesteup.data.state.HomeUiState
import icu.bluedream.gamesteup.data.types.GameType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ScreenModel {
    private val _state = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> get() = _state.asStateFlow()

    fun chooseDir() {
        screenModelScope.launch {
            val choice = FileUtil.chooseGamePath {
                updateUIState { copy(isInstalled = it) }
            }
            updateUIState { copy(selectedPath = choice) }
        }
    }

    fun updateSelectedGame(type: GameType) {
        updateUIState { copy(selectedGame = type) }
    }

    private fun updateUIState(update: HomeUiState.() -> HomeUiState) {
        updateUIState(_state.value.update())
    }

    private fun updateUIState(uiState: HomeUiState) {
        _state.update { uiState }
    }
}
package icu.bluedream.gameinstaller.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import icu.bluedream.gameinstaller.core.helper.FileUtil
import icu.bluedream.gameinstaller.data.state.HomeUiState
import icu.bluedream.gameinstaller.data.types.GameType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> get() = _state.asStateFlow()

    fun chooseDir() {
        viewModelScope.launch {
            val choice = FileUtil.chooseGamePath()
            if (choice.endsWith("HoYoGamePacks/")) {
                updateUIState { copy(isInstalled = false) }
            } else {
                updateUIState { copy(isInstalled = true) }
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
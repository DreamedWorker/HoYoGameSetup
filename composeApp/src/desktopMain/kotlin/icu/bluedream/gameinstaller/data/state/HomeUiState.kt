package icu.bluedream.gameinstaller.data.state

import icu.bluedream.gameinstaller.data.types.GameType

data class HomeUiState(
    var selectedGame: GameType = GameType.GI_CN,
    var selectedPath: String = "",
    var isInstalled: Boolean = false
)

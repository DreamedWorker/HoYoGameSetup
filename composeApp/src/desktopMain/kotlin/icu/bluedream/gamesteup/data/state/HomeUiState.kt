package icu.bluedream.gamesteup.data.state

import icu.bluedream.gamesteup.data.types.GameType

data class HomeUiState(
    var selectedGame: GameType = GameType.GI_CN,
    var selectedPath: String = "",
    var isInstalled: Boolean = false
)

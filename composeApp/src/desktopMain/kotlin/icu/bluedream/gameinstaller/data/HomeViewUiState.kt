package icu.bluedream.gameinstaller.data

import kotlinx.serialization.Serializable

@Serializable
data class HomeViewUiState(
    var selectedGameType: GameType = GameType.GenshinCN,
    var gameInstallationPath: String = "",
    var installMethod: InstallationAction = InstallationAction.InstallBrandNew,
    var installationLog: String = "Follow the guides above to start downloading your game(s).",
    var needShowDialog: Boolean = false,
    var dialogMsg: String = ""
)

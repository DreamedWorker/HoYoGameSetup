package icu.bluedream.gamesteup.data.state

import icu.bluedream.gamesteup.data.types.ActionState

data class BrandNewUiState(
    var fetchMetaState: ActionState = ActionState.WAITING,
    var downloadGamePathState: ActionState = ActionState.WAITING,
    var downloadGamePacksState: ActionState = ActionState.WAITING,
    var modifyBatState: ActionState = ActionState.WAITING,
    var totalTaskProgress: Float = 0f,
    var currentFile: Int = 0,
    var totalFile: Int = 0,
    var speed: Float = 0f,
    var packInfo: String = "Waiting for fetching...",
    var showError: Boolean = false,
    var errorMessage: String = ""
)

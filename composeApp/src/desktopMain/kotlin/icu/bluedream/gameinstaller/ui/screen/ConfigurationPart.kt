package icu.bluedream.gameinstaller.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import gameinstaller.composeapp.generated.resources.*
import icu.bluedream.gameinstaller.data.GameType
import icu.bluedream.gameinstaller.data.HomeViewUiState
import icu.bluedream.gameinstaller.data.UiParts
import io.github.vinceglb.filekit.core.FileKit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import java.io.File

@Composable
fun ConfigurationPart(
    changeUI: (UiParts, HomeViewUiState) -> Unit,
    viewModel: ConfigurationPartViewModel = viewModel<ConfigurationPartViewModel>()
) {
    val state by viewModel.appUiState.collectAsState()
    var openSelector by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) { // 头部
            Image(
                painterResource(Res.drawable.app_logo), null,
                modifier = Modifier.size(72.dp)
            )
            Text(
                stringResource(Res.string.home_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(stringResource(Res.string.home_subtitle), style = MaterialTheme.typography.bodyMedium)
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) { // 安装步骤区
            Text(
                stringResource(Res.string.home_label_step),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Card( // 选择游戏类型
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.gameSelector_action_title)) },
                    leadingContent = { Icon(Icons.Default.Gamepad, null) },
                    supportingContent = { Text(stringResource(Res.string.gameSelector_supporting)) },
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(GameType.getLiteralName(state.selectedGameType)),
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                            DropdownMenu(openSelector, { openSelector = !openSelector }) {
                                for (name in stringArrayResource(Res.array.gameType_list)) {
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            viewModel.updateSelectedGame(GameType.getGameType(name))
                                            openSelector = !openSelector
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.clickable { openSelector = !openSelector }
                )
            }
            Card( // 选择游戏数据位置
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.pathSelector_action_title)) },
                    leadingContent = {
                        Icon(
                            Icons.Default.Storage,
                            "game path selector icon"
                        )
                    },
                    modifier = Modifier.clickable {
                        viewModel.updateSelectedPath()
                    },
                    supportingContent = {
                        Text(
                            text = state.gameInstallationPath.ifBlank { stringResource(Res.string.pathSelector_supporting) },
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = if (state.gameInstallationPath.isNotBlank()) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer),
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    }
                )
            }
            Spacer(Modifier.height(8.dp).width(1.dp))
            Icon(Icons.Outlined.Info, null)
            Text(
                stringResource(Res.string.pathSelector_tip),
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
            )
        }
        Spacer(Modifier.weight(1f))
        Row {
            Spacer(Modifier.weight(1f))
            Button({
                if (viewModel.startDownload()) {
                    val files = File(state.gameInstallationPath).list()
                    if (files == null) {
                        changeUI(UiParts.INSTALL_BRAND_NEW, state)
                    } else {
                        if (files.contains("YuanShen.exe") || files.contains("ZenlessZoneZero.exe")) {
                            changeUI(UiParts.DATA_ALREADY_EXISTS, state)
                        } else {
                            changeUI(UiParts.INSTALL_BRAND_NEW, state)
                        }
                    }
                }
            }) {
                Text(stringResource(Res.string.home_startTask))
            }
        }
    }
    if (state.needShowDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeDialog() },
            confirmButton = {
                TextButton({ viewModel.closeDialog() }) {
                    Text(stringResource(Res.string.app_ok))
                }
            },
            icon = { Icon(Icons.Outlined.Warning, "warning dialog") },
            title = { Text(stringResource(Res.string.app_warning)) },
            text = { Text(state.dialogMsg) }
        )
    }
}

class ConfigurationPartViewModel : ViewModel() {
    private val _appUiState = MutableStateFlow(HomeViewUiState())
    val appUiState: StateFlow<HomeViewUiState> get() = _appUiState.asStateFlow()

    fun updateSelectedGame(type: GameType) {
        updateUiState { copy(selectedGameType = type) }
    }

    fun updateSelectedPath() {
        viewModelScope.launch {
            val path = checkDir(chooseFile())
            updateSelectedPath(path)
        }
    }

    fun startDownload(): Boolean {
        if (_appUiState.value.gameInstallationPath.isBlank()) {
            viewModelScope.launch {
                _appUiState.update {
                    appUiState.value.copy(
                        needShowDialog = true,
                        dialogMsg = getString(Res.string.home_error_noPath)
                    )
                }
            }
            return false
        } else {
            return true
        }
    }

    fun closeDialog() {
        updateUiState { copy(needShowDialog = false, dialogMsg = "") }
    }

    private fun updateSelectedPath(path: String) {
        updateUiState { copy(gameInstallationPath = path) }
    }

    private fun updateUiState(update: HomeViewUiState.() -> HomeViewUiState) {
        updateUiState(_appUiState.value.update())
    }

    private fun updateUiState(uiState: HomeViewUiState) {
        _appUiState.update { uiState }
    }

    private fun checkDir(path: String): String {
        if (path.isBlank()) return ""
        val files = File(path).list() ?: return "${path}/HoYoGamePacks/"
        return if (files.contains("YuanShen.exe") || files.contains("ZenlessZoneZero.exe")) {
            "${path}/"
        } else {
            "${path}/HoYoGamePacks/"
        }
    }

    private suspend fun chooseFile(): String {
        val dir = FileKit.pickDirectory(title = getString(Res.string.pathSelector_supporting)) ?: return ""
        return if (dir.file.canWrite()) {
            dir.file.absolutePath
        } else {
            ""
        }
    }
}
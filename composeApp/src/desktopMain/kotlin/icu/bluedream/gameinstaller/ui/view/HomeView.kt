package icu.bluedream.gameinstaller.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import gamesetup.composeapp.generated.resources.*
import gamesetup.composeapp.generated.resources.Res
import gamesetup.composeapp.generated.resources.app_logo
import gamesetup.composeapp.generated.resources.app_name
import gamesetup.composeapp.generated.resources.home_title
import icu.bluedream.gameinstaller.core.storage.SelfStorage
import icu.bluedream.gameinstaller.data.types.GameType
import icu.bluedream.gameinstaller.viewmodel.HomeViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import java.io.File

class HomeView : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = viewModel<HomeViewModel>()
        val state = viewModel.uiState.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        var showGameSelector by remember { mutableStateOf(false) }
        var showMoreAction by remember { mutableStateOf(false) }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.app_name)) },
                    actions = {
                        IconButton({ showMoreAction = true }){
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(showMoreAction, { showMoreAction = false }){
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.home_menu_cache_clear)) },
                                leadingIcon = {
                                    Icon(Icons.Default.Cached, null)
                                },
                                onClick = {
                                    with(File(SelfStorage.getCacheFileFolder())) {
                                        println(deleteRecursively())
                                        mkdirs()
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.home_menu_about)) },
                                onClick = {
                                    navigator.push(AboutView())
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.AccountBalance, null)
                                }
                            )
                        }
                    }
                )
            }
        ) { pd ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(pd).systemBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { // 欢迎标语
                    Image(
                        painterResource(Res.drawable.app_logo), null,
                        modifier = Modifier.size(92.dp)
                    )
                    Text(
                        stringResource(Res.string.home_title),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
                    )
                    Text(
                        stringResource(Res.string.home_exp),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                Column { // 操作区
                    Text(
                        stringResource(Res.string.home_steps),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    // 游戏类型选择器
                    Card {
                        ListItem(
                            headlineContent = { Text(stringResource(Res.string.home_select_game)) },
                            leadingContent = { Icon(Icons.Default.Gamepad, null) },
                            trailingContent = {
                                Row {
                                    Text(
                                        stringResource(GameType.getGameName(state.value.selectedGame)),
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                                    )
                                    DropdownMenu(
                                        expanded = showGameSelector,
                                        { showGameSelector = false }
                                    ) {
                                        for ((index, name) in stringArrayResource(Res.array.game_types).withIndex()) {
                                            DropdownMenuItem(
                                                text = { Text(name) },
                                                onClick = {
                                                    when (index) {
                                                        0 -> viewModel.updateSelectedGame(GameType.GI_CN)
                                                        1 -> viewModel.updateSelectedGame(GameType.GI_OS)
                                                        2 -> viewModel.updateSelectedGame(GameType.ZZZ_CN)
                                                        3 -> viewModel.updateSelectedGame(GameType.ZZZ_OS)
                                                        else -> viewModel.updateSelectedGame(GameType.GI_CN)
                                                    }
                                                    showGameSelector = false
                                                }
                                            )
                                        }
                                    }
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.clickable { showGameSelector = true }
                        )
                    }
                    // 游戏路径选择器
                    Card(
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        ListItem(
                            headlineContent = { Text(stringResource(Res.string.home_select_path)) },
                            supportingContent = {
                                Text(state.value.selectedPath.ifBlank { stringResource(Res.string.home_select_tip) })
                            },
                            modifier = Modifier.clickable {
                                viewModel.chooseDir()
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = if (state.value.selectedPath.isNotBlank())
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            leadingContent = {
                                Icon(Icons.Default.Storage, null)
                            }
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Row {
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = {
                            if (!state.value.isInstalled) {
                                navigator.push(NewInstallView(state.value))
                            } else {
                                navigator.push(UseExistsView(state.value))
                            }
                        },
                        enabled = state.value.selectedPath.isNotBlank()
                    ) {
                        Text(stringResource(Res.string.home_select_do))
                    }
                }
            }
        }
    }
}
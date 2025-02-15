package icu.bluedream.gamesteup.ui.screen

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
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import gamesetup.composeapp.generated.resources.*
import gamesetup.composeapp.generated.resources.Res
import gamesetup.composeapp.generated.resources.app_name
import gamesetup.composeapp.generated.resources.home_menu_about
import gamesetup.composeapp.generated.resources.home_menu_cache_clear
import icu.bluedream.gamesteup.core.utils.FileSystemUtil
import icu.bluedream.gamesteup.data.types.GameType
import icu.bluedream.gamesteup.viewmodel.HomeViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import java.io.File

class HomeScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel { HomeViewModel() }
        val navigator = LocalNavigator.currentOrThrow
        val state by viewModel.uiState.collectAsState()
        var openTopMenu by remember { mutableStateOf(false) }
        var showGameSelector by remember { mutableStateOf(false) }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.app_name)) },
                    actions = {
                        IconButton({ openTopMenu = true }){
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(openTopMenu, { openTopMenu = !openTopMenu }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.home_menu_cache_clear)) },
                                onClick = {
                                    with(File(FileSystemUtil.getCommonFileDir())) {
                                        deleteRecursively()
                                        mkdirs()
                                    }
                                    openTopMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Cached, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.home_menu_about)) },
                                onClick = { navigator.push(AboutView()) },
                                leadingIcon = { Icon(Icons.Default.AccountBalance, null) }
                            )
                        }
                    }
                )
            }
        ) { pd ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .systemBarsPadding().padding(pd).padding(16.dp),
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
                                        stringResource(GameType.getGameName(state.selectedGame)),
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
                                Text(state.selectedPath.ifBlank { stringResource(Res.string.home_select_tip) })
                            },
                            modifier = Modifier.clickable {
                                viewModel.chooseDir()
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = if (state.selectedPath.isNotBlank())
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
                            if (!state.isInstalled) {
                                navigator.push(NewInstallView(state))
                            } else {
                                navigator.push(UseExistsView(state))
                            }
                        },
                        enabled = state.selectedPath.isNotBlank()
                    ) {
                        Text(stringResource(Res.string.home_select_do))
                    }
                }
            }
        }
    }
}
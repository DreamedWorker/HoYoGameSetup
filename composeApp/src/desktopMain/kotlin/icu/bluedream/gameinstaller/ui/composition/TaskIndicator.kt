package icu.bluedream.gameinstaller.ui.composition

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import icu.bluedream.gameinstaller.data.types.ActionState
import icu.bluedream.gameinstaller.data.types.ActionState.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TaskIndicator(
    taskImg: ImageVector,
    taskName: StringResource,
    taskState: ActionState,
    isDownloadTask: Boolean = false,
    currentFile: Int = 0,
    totalFiles: Int = 0,
    speed: Float = 0f,
    isGamePackInfo: Boolean = false,
    packInfo: String = ""
) {
    Card(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        ListItem(
            headlineContent = { Text(stringResource(taskName)) },
            leadingContent = { Icon(taskImg, null) },
            trailingContent = { Text(taskState.name.lowercase()) },
            supportingContent = {
                if (isDownloadTask) {
                    Text("Downloading $currentFile of $totalFiles, Speed: $speed MB/s")
                } else if (isGamePackInfo) {
                    Text(packInfo)
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = when(taskState) {
                    ONGOING, WAITING -> MaterialTheme.colorScheme.tertiaryContainer
                    FINISHED -> MaterialTheme.colorScheme.primaryContainer
                    FAILED -> MaterialTheme.colorScheme.errorContainer
                }
            )
        )
    }
}
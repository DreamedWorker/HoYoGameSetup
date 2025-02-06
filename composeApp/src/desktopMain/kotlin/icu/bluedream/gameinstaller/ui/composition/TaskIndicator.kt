package icu.bluedream.gameinstaller.ui.composition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import gameinstaller.composeapp.generated.resources.*
import icu.bluedream.gameinstaller.data.TaskState
import icu.bluedream.gameinstaller.data.TaskState.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TaskIndicator(
    taskImg: ImageVector,
    taskTitle: StringResource,
    taskState: TaskState,
    isDownloadTask: Boolean = false,
    totalDownloads: Int = 0,
    currentDownload: Int = 0,
    currentDownloadProgress: Float = 0f,
    speed: Float = 0f
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
    ) {
        ListItem(
            headlineContent = { Text(stringResource(taskTitle)) },
            leadingContent = {
                Icon(taskImg, null)
            },
            supportingContent = {
                if (isDownloadTask) {
                    Text("正在下载第 $currentDownload 个文件，总共 $totalDownloads 个文件。当前下载速度 $speed MB/s。")
                }
            },
            trailingContent = {
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isDownloadTask) {
                        CircularProgressIndicator(
                            progress = { currentDownloadProgress },
                            trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    }
                    when(taskState) {
                        WAITING -> Text(stringResource(Res.string.brand_state_waiting), style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
                        ONGOING -> Text(stringResource(Res.string.brand_state_ongoing), style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
                        FINISHED -> Text(stringResource(Res.string.brand_state_finished), style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
                        FAILED -> Text(stringResource(Res.string.brand_state_failed), style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline))
                    }
                }
            },
            modifier = Modifier.clickable {},
            colors = ListItemDefaults.colors(
                containerColor = when (taskState) {
                    FINISHED -> MaterialTheme.colorScheme.tertiaryContainer
                    FAILED -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.secondaryContainer
                }
            )
        )
    }
}
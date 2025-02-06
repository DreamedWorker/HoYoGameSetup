package icu.bluedream.gameinstaller.data

data class BrandNewInstallViewUiState(
    var totalProgress: Float = 0.1f,
    var writeAppState: TaskState = TaskState.WAITING,
    var fetchInfoState: TaskState = TaskState.WAITING,
    var downloadState: TaskState = TaskState.WAITING,
    var mergeFilesState: TaskState = TaskState.WAITING,
    var totalDownloads: Int = 0,
    var currentDownload: Int = 0,
    var singleFileDownloadState: Float = 0f,
    var speed: Float = 0f
)
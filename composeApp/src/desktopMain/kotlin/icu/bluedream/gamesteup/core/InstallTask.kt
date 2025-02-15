package icu.bluedream.gamesteup.core

import icu.bluedream.gamesteup.core.utils.ContentUtil
import icu.bluedream.gamesteup.core.utils.DownloadUtil
import icu.bluedream.gamesteup.core.utils.FileSystemUtil
import icu.bluedream.gamesteup.core.utils.FileUtil
import icu.bluedream.gamesteup.data.GameMeta
import icu.bluedream.gamesteup.data.MAC_APPLICATION_PATH
import icu.bluedream.gamesteup.data.state.HomeUiState
import icu.bluedream.gamesteup.data.types.ActionState
import icu.bluedream.gamesteup.data.types.GameType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.FileInputStream

val pkgUrls = mutableListOf<String>()
val pkgMd5s = mutableListOf<String>()

fun downloadNecessaryFile(
    installGameType: GameType,
    updateTaskState: (ActionState) -> Unit
) {
    updateTaskState(ActionState.ONGOING)
    val preparedManifestFile = File("${FileSystemUtil.getCommonFileDir()}/${installGameType.name}_transfer.txt")
    val metaName = GameType.getMetaFileName(installGameType)
    if (preparedManifestFile.exists()) {
        downloadPrebuildApp(installGameType, updateTaskState)
    } else {
        try {
            DownloadUtil.downloadFiles(
                urls = listOf("https://ftp1.bluedream.icu/$metaName"),
                saveDir = File(FileSystemUtil.getCommonFileDir()),
                checkMd5 = { _, _ -> false },
                onAllComplete = { files ->
                    val originalFile = files.first()
                    ContentUtil.reformatMd5File(originalFile.absolutePath, preparedManifestFile.absolutePath)
                    downloadPrebuildApp(installGameType, updateTaskState)
                },
                onProgressUpdate = { _, _ -> }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            updateTaskState(ActionState.FAILED)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun downloadGameMetaFile(
    installGameType: GameType,
    updateTaskState: (ActionState) -> Unit,
    gameVersionViewer: (String) -> Unit
) {
    updateTaskState(ActionState.ONGOING)
    val url = GameType.getPackInfoDownloadLink(installGameType)
    val gameMetaFile = File("${FileSystemUtil.getCommonFileDir()}/${installGameType.name}_meta.txt")
    val fileContext: GameMeta
    if (gameMetaFile.exists()) {
        fileContext = try {
            Json.decodeFromStream<GameMeta>(FileInputStream(gameMetaFile))
        } catch (e: Exception) {
            DownloadUtil.downloadFile(
                url = url,
                fileName = "${installGameType.name}_meta.txt",
                outputDir = FileSystemUtil.getCommonFileDir()
            )
            Json.decodeFromStream<GameMeta>(FileInputStream(gameMetaFile))
        }
    } else {
        DownloadUtil.downloadFile(
            url = url,
            fileName = "${installGameType.name}_meta.txt",
            outputDir = FileSystemUtil.getCommonFileDir()
        )
       fileContext = Json.decodeFromStream<GameMeta>(FileInputStream(gameMetaFile))
    }
    gameVersionViewer(fileContext.data.gamePackages[0].main.major!!.version)
    for (pkg in fileContext.data.gamePackages[0].main.major!!.gamePkgs) {
        pkgUrls.add(pkg.url)
        pkgMd5s.add(pkg.md5.uppercase())
    }
    updateTaskState(ActionState.FINISHED)
}

fun downloadGameDataPack(
    config: HomeUiState,
    updateTaskState: (ActionState) -> Unit,
    updateDownloadState: (Int, Float) -> Unit,
    theLastTaskState: (ActionState) -> Unit,
    updateTotalProgress: (Float) -> Unit
) {
    updateTaskState(ActionState.ONGOING)
    val selectedDir = File(config.selectedPath)
    if (!selectedDir.exists()) {
        selectedDir.mkdirs()
    }
    try {
        DownloadUtil.downloadFiles(
            urls = pkgUrls,
            saveDir = selectedDir,
            checkMd5 = { file: File, nameIndex: Int ->
                val md5 = FileUtil.calculateFileMD5(file.absolutePath) ?: ""
                val storage = pkgMd5s[nameIndex]
                md5 == storage
            },
            onProgressUpdate = { count: Int, speed: Double ->
                updateDownloadState(count, "%.2f".format(speed.toFloat()).toFloat())
            },
            onAllComplete = {
                if (it.size == pkgUrls.size) {
                    updateTaskState(ActionState.FINISHED)
                    updateTotalProgress(0.75f)
                    val tempGameZip = File(config.selectedPath, "tempGameZip.zip")
                    if (!tempGameZip.exists()) {
                        tempGameZip.createNewFile()
                    }
                    FileUtil.mergeSplitFiles(
                        inputDir = config.selectedPath,
                        outputFile = tempGameZip.absolutePath,
                        prefix = GameType.getPackDownloadPrefix(config.selectedGame)
                    )
                    FileUtil.unzipAndDeleteArchive(tempGameZip.absolutePath)
                    theLastTaskState(ActionState.ONGOING)
                    ContentUtil.modifyLaunchBat(config.selectedGame, config.selectedPath)
                    theLastTaskState(ActionState.FINISHED)
                    updateTotalProgress(1f)
                    Thread.sleep(1200)
                } else {
                    updateTaskState(ActionState.FAILED)
                    theLastTaskState(ActionState.FAILED)
                }
            }
        )
    } catch (e: Exception) {
        updateTaskState(ActionState.FAILED)
    }
}

private fun downloadPrebuildApp(
    installGameType: GameType,
    updateTaskState: (ActionState) -> Unit
) {
    val prebuildAppFile = File("${FileSystemUtil.getPrebuildDir()}/${installGameType.name}_temp_prebuild.zip")
    if (prebuildAppFile.exists()) {
        if (checkIfIsOk(prebuildAppFile)) {
            if (installFile2Application(prebuildAppFile, installGameType) == 0) {
                updateTaskState(ActionState.FINISHED)
            } else {
                updateTaskState(ActionState.FAILED)
            }
        } else {
            downloadAppSplit(
                installGameType,
                { installFile2Application(prebuildAppFile, installGameType) },
                { updateTaskState(it) }
            )
        }
    } else {
        downloadAppSplit(
            installGameType,
            { installFile2Application(prebuildAppFile, installGameType) },
            { updateTaskState(it) }
        )
    }
}

private fun installFile2Application(prebuildAppFile: File, installGameType: GameType): Int {
    val installPb = ProcessBuilder(
        "/usr/bin/ditto", "-x", "-k",
        prebuildAppFile.absolutePath,
        MAC_APPLICATION_PATH
    ).start().waitFor()
    if (installPb == 0) {
        File("${MAC_APPLICATION_PATH}${GameType.getGameAppName(installGameType)}")
            .renameTo(File("${MAC_APPLICATION_PATH}${GameType.getAppTranslationName(installGameType)}"))
        val oldFile = "${MAC_APPLICATION_PATH}${GameType.getGameAppName(installGameType)}"
        val rmPb = ProcessBuilder(
            "/bin/rm", "-rf", oldFile
        )
        return rmPb.start().waitFor()
    } else {
        return installPb
    }
}

private fun downloadAppSplit(
    installGameType: GameType,
    downloadFinshed: () -> Int,
    updateTaskState: (ActionState) -> Unit
) {
    val processedFilePath = "${FileSystemUtil.getCommonFileDir()}/${installGameType.name}_transfer.txt"
    val md5map = ContentUtil.readMd5FileToMap(processedFilePath)
    try {
        DownloadUtil.downloadFiles(
            urls = getSplitDownlaodLink(installGameType, processedFilePath),
            saveDir = File(FileSystemUtil.getPrebuildDir()),
            checkMd5 = { file: File, _ ->
                val md5 = FileUtil.calculateFileMD5(file.absolutePath) ?: ""
                val name = file.nameWithoutExtension
                if (md5map.keys.contains(name)) {
                    val storage = md5map[name] ?: ""
                    storage == md5
                } else {
                    false
                }
            },
            onProgressUpdate = { _, _ -> },
            onAllComplete = {
                FileUtil.mergeSplitFiles(
                    FileSystemUtil.getPrebuildDir(),
                    "${FileSystemUtil.getPrebuildDir()}/${installGameType.name}_temp_prebuild.zip",
                    GameType.getSplitFileName(installGameType)
                )
                if (downloadFinshed() == 0) {
                    updateTaskState(ActionState.FINISHED)
                } else {
                    updateTaskState(ActionState.FAILED)
                }
            }
        )
    } catch (e: Exception) {
        e.printStackTrace()
        updateTaskState(ActionState.FAILED)
    }
}

private fun checkIfIsOk(file: File): Boolean {
    val fileSizeInBytes = file.length()
    val fileSizeLimitInBytes = 500 * 1024 * 1024
    return fileSizeInBytes > fileSizeLimitInBytes
}

private fun getSplitDownlaodLink(type: GameType, processedFile: String): List<String> {
    val oriList = ContentUtil.getLeftPartsList(processedFile)
    val url = when (type) {
        GameType.GI_OS, GameType.GI_CN -> "https://ftp1.bluedream.icu/GS/"
        GameType.ZZZ_OS, GameType.ZZZ_CN -> "https://ftp1.bluedream.icu/ZZZ/"
    }
    for ((index, name) in oriList.withIndex()) {
        oriList[index] = "$url$name"
    }
    return oriList.toList()
}
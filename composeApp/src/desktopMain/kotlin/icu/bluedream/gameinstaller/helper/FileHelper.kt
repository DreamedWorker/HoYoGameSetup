package icu.bluedream.gameinstaller.helper

import gameinstaller.composeapp.generated.resources.Res
import icu.bluedream.gameinstaller.data.Constants
import icu.bluedream.gameinstaller.data.GameType
import icu.bluedream.gameinstaller.data.GameType.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

object FileHelper {
    @OptIn(ExperimentalResourceApi::class)
    suspend fun copyFile2AppDir(selectedPath: String, type: GameType, canGoNext: (Boolean) -> Unit) =
        withContext(Dispatchers.IO) {
            val fileNameInner = when (type) {
                GenshinCN, GenshinOS -> Res.readBytes("files/GS.zip")
                ZzzCN, ZzzOS -> Res.readBytes("files/ZZZ.zip")
            }
            val outerTempFile = File(selectedPath, "tempFile.zip")
            outerTempFile.createNewFile()
            outerTempFile.outputStream().use { it.write(fileNameInner) }
            val copyPb = ProcessBuilder(
                "/usr/bin/ditto", "-xk", outerTempFile.absolutePath, Constants.MAC_APPLICATION_PATH
            )
            val appName = GameHelper.getGameFinderName(type)
            val appFileName = GameHelper.getGameFileNameInner(type)
            val destFolder = File(Constants.MAC_APPLICATION_PATH)
            if (copyPb.start().waitFor() == 0) {
                outerTempFile.delete()
                val destFile = File(destFolder, appName)
                if (appFileName.contains("GS")) {
                    File(destFolder, "GS.app").renameTo(destFile)
                } else {
                    File(destFolder, "ZZZ.app").renameTo(destFile)
                }
                canGoNext(true)
            } else {
                canGoNext(false)
            }
        }

    fun calculateMD5(file: File): String {
        val digest = MessageDigest.getInstance("MD5")
        val inputStream = FileInputStream(file)
        val buffer = ByteArray(8192)
        var read: Int
        while (inputStream.read(buffer).also { read = it } > 0) {
            digest.update(buffer, 0, read)
        }
        val md5sum = digest.digest()
        val hexString = StringBuilder()
        for (i in md5sum.indices) {
            hexString.append(String.format("%02x", md5sum[i]))
        }
        inputStream.close()
        return hexString.toString().uppercase()
    }

    fun catAllSplitZipFiles(gamePkgsBaseDir: String, splitPkgZipFileName: List<String>, pkgZipFileName: String?) {
        val splitZipPkgs = mutableListOf<File>()
        for (name in splitPkgZipFileName) {
            val pkg = File(gamePkgsBaseDir + name)
            if (pkg.exists()) {
                splitZipPkgs.add(pkg)
            }
        }
        pkgZipFileName?.let {
            try {
                val destFile = File(gamePkgsBaseDir + it)
                if (splitZipPkgs.size == splitPkgZipFileName.size) {
                    FileOutputStream(destFile).channel.use { outputChannel ->
                        for (splitPkg in splitZipPkgs) {
                            println("正在合并资源文件${splitPkg.name} ... ")
                            val inputChannel = FileInputStream(splitPkg).channel
                            outputChannel.transferFrom(inputChannel, outputChannel.size(), inputChannel.size())
                            inputChannel.close()
                        }
                        outputChannel.close()
                    }
                    for (splitPkg in splitZipPkgs) {
                        //合包完成后删除所有分包
                        splitPkg.delete()
                    }
                } else {
                    if (splitZipPkgs.size != 0) {
                        println("资源包缺失，请重新下载！")
                    }
                }
            } catch (e: Exception) {
                println("Error merging files: ${e.message}")
            }
        }
    }

    fun unzipGame(pkgZipFileName: String?, outputDirFile: File, gamePkgsBaseDir: String): Boolean {
        if (pkgZipFileName != null) {
            val unzipArgs = mutableListOf<String>()
            unzipArgs.add(pkgZipFileName)
            unzipArgs.add("-d")
            unzipArgs.add(outputDirFile.absolutePath)
            val processBuilder = ProcessBuilder("/usr/bin/unzip", *unzipArgs.toTypedArray())
                .directory(File(gamePkgsBaseDir))
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
            val process = processBuilder.start()
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                File(gamePkgsBaseDir + pkgZipFileName).delete()
                return true
            } else {
                return false
            }
        } else {
            return false
        }
    }
}
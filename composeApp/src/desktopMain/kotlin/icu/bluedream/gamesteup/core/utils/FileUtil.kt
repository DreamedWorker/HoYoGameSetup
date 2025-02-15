package icu.bluedream.gamesteup.core.utils

import gamesetup.composeapp.generated.resources.Res
import gamesetup.composeapp.generated.resources.home_select_path_tip
import io.github.vinceglb.filekit.core.FileKit
import org.jetbrains.compose.resources.getString
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object FileUtil {
    private suspend fun choosePath(): String {
        val path = FileKit.pickDirectory(title = getString(Res.string.home_select_path_tip))?.path ?: return ""
        val file = File(path)
        return if (!file.canWrite()) {
            ""
        } else {
            path
        }
    }

    suspend fun chooseGamePath(
        isNewInstallation: (Boolean) -> Unit
    ): String {
        val selected = choosePath()
        if (selected.isBlank()) {
            return ""
        }
        val files = File(selected).list()
        if (files != null) {
            return if (files.contains("YuanShen.exe") || files.contains("ZenlessZoneZero.exe")) {
                isNewInstallation(true)
                "$selected/"
            } else {
                isNewInstallation(false)
                "$selected/HoYoGamePacks/"
            }
        } else {
            File(selected).mkdirs()
            isNewInstallation(false)
            return "$selected/HoYoGamePacks/"
        }
    }

    fun calculateFileMD5(filePath: String): String? {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val file = File(filePath)
            file.inputStream().buffered().use { inputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                do {
                    bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        md.update(buffer, 0, bytesRead)
                    }
                } while (bytesRead != -1)
            }
            bytesToHex(md.digest())
        } catch (e: Exception) {
            null
        }
    }

    fun mergeSplitFiles(inputDir: String, outputFile: String, prefix: String) {
        require(outputFile.endsWith(".zip")) { "输出文件必须是 .zip 格式" }
        val dir = File(inputDir).takeIf { it.isDirectory }
            ?: throw IllegalArgumentException("输入路径不是有效目录")
        val splitFiles = dir.listFiles { _, name ->
            name.startsWith(prefix) && name.length > prefix.length
        }?.sortedBy { it.name }
            ?: throw IllegalStateException("未找到分割文件")
        require(splitFiles.isNotEmpty()) { "目录中没有分割文件" }
        FileOutputStream(outputFile).use { outputStream ->
            splitFiles.forEach { file ->
                file.inputStream().buffered().use { input ->
                    input.copyTo(outputStream)
                }
                println("已合并: ${file.name} (${file.length()} bytes)")
                if (file.delete()) {
                    println("已删除: ${file.name}")
                } else {
                    println("删除失败: ${file.name}")
                }
            }
        }
    }

    fun unzipAndDeleteArchive(archivePath: String) {
        val archiveFile = File(archivePath)
        require(archiveFile.exists() && archiveFile.isFile) { "压缩包路径无效或不是一个文件" }
        require(archivePath.endsWith(".zip")) { "压缩包必须是 .zip 格式" }
        val parentDir = archiveFile.parentFile ?: throw IllegalArgumentException("无法获取压缩包所在目录")
        FileInputStream(archiveFile).use { fis ->
            ZipInputStream(fis).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val filePath = File(parentDir, entry.name).absolutePath
                    if (!entry.isDirectory) {
                        val parentFile = File(filePath).parentFile
                        if (parentFile != null && !parentFile.exists()) {
                            parentFile.mkdirs()
                        }
                        FileOutputStream(filePath).use { fos ->
                            zis.copyTo(fos)
                        }
                        println("已解压: ${entry.name} (${File(filePath).length()} bytes)")
                    } else {
                        File(filePath).mkdirs()
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }
        if (archiveFile.delete()) {
            println("已删除: ${archiveFile.name}")
        } else {
            println("删除失败: ${archiveFile.name}")
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexString = StringBuilder(bytes.size * 2)
        for (byte in bytes) {
            val hex = String.format("%02x", byte)
            hexString.append(hex)
        }
        return hexString.toString()
    }

}
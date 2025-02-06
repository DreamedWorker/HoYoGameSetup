package icu.bluedream.gameinstaller.helper

import kotlinx.serialization.json.Json
import java.io.*
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLConnection

object NetworkHelper {
    inline fun <reified T> URL.fetch(): T {
        val connection = this.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val `is` = connection.inputStream
            val reader = BufferedReader(InputStreamReader(`is`))
            val data = reader.use { it.readLine() }
            println(data)
            return Json.decodeFromString<T>(data)
        } else {
            throw Exception("Something went wrong when while we are fetching package info, ${connection.responseMessage}")
        }
    }

    private fun getFileName(url: String, connection: URLConnection): String {
        var fileName = ""
        // 从 URL 中获取文件名
        val urlPath = URI.create(url).toURL().path
        val slashIndex = urlPath.lastIndexOf('/')
        if (slashIndex >= 0 && slashIndex < urlPath.length - 1) {
            fileName = urlPath.substring(slashIndex + 1)
        }
        // 如果获取不到，从连接中获取 Content-Disposition 中的文件名
        if (fileName.isEmpty()) {
            val disposition = connection.getHeaderField("Content-Disposition")
            if (disposition != null && disposition.contains("filename=")) {
                fileName = disposition.substring(disposition.indexOf("filename=") + 9)
                fileName = fileName.replace("\"", "")
            }
        }
        return fileName
    }

    fun downloadFile(
        pkgUrl: String, destPath: String, pkgCount: Int,
        pkgZipFileName: String?, changePkgZipFileInnerName: (String) -> Unit,
        pkgMd5s: List<String>,
        splitPkgZipFileName: List<String>, addZipFileName2Split: (String) -> Unit,
        printDownloadProgress: (Long, Long, Float) -> Unit
    ): Boolean {
        fun initPkgZipFileName(splitName: String) {
            val lastDotIndex = splitName.lastIndexOf(".")
            if (pkgZipFileName.isNullOrEmpty()) {
                changePkgZipFileInnerName(splitName.substring(0, lastDotIndex))
            }
        }
        fun checkShouldDownload(pkgName: String, pkgCount: Int): Boolean {
            //当某个序号的分包不存在或者存在但md5校验不通过时需要重新下载
            val targetFile = File(destPath + pkgName)
            if (!targetFile.exists()) {
                return true
            }
            val targetMd5 = FileHelper.calculateMD5(targetFile)
            if (targetMd5 != pkgMd5s[pkgCount - 1]) {
                targetFile.delete()
                return true
            }
            return false
        }
        try {
            val url = URI.create(pkgUrl).toURL()
            val connection = url.openConnection()
            connection.connect()
            var startTime: Long
            var currentTime: Long
            var lastDownloadSize = 0L
            var downloadedSize: Long = 0
            // 获取文件名
            val fileName = getFileName(pkgUrl, connection)
            if (fileName.isEmpty()) {
                return false
            }
            if(!splitPkgZipFileName.contains(fileName)) {
                addZipFileName2Split(fileName)
            }
            initPkgZipFileName(fileName)
            if (File(destPath + pkgZipFileName).exists()) {
                //已存在待解压的合并包，不下载数据
                return true
            }
            val destFilePath = destPath + fileName
            if (!checkShouldDownload(fileName, pkgCount)) {
                return true
            }
            // 获取文件总大小
            val fileSize = connection.contentLengthLong
            startTime = System.currentTimeMillis()
            BufferedInputStream(connection.getInputStream()).use { input ->
                FileOutputStream(destFilePath).use { output ->
                    val buffer = ByteArray(1024 * 32)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedSize += bytesRead
                        currentTime = System.currentTimeMillis()
                        if (currentTime - startTime >= 1000L) {
                            printDownloadProgress(downloadedSize, fileSize, ((downloadedSize - lastDownloadSize) / 1024f / 1024f))
                            startTime = System.currentTimeMillis()
                            lastDownloadSize = downloadedSize
                        }
                    }
                }
            }
            val verifyDownload = checkShouldDownload(fileName, pkgCount)
            if (verifyDownload) {
                File(destFilePath).delete()
            }
            return !verifyDownload
        } catch (e: Exception) {
            return false
        }
    }
}
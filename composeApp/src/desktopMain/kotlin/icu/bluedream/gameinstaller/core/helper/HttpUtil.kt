package icu.bluedream.gameinstaller.core.helper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI

object HttpUtil {
    fun downloadFiles(
        urls: List<String>,
        saveDir: File,
        checkMd5: (File) -> Boolean,
        onAllComplete: (List<File>) -> Unit
    ) {
        val client = OkHttpClient()
        saveDir.mkdirs()
        val successFiles = mutableListOf<File>()
        urls.forEach { url ->
            val fileName = url.substringAfterLast('/').takeIf { it.isNotBlank() }
                ?: "unnamed_${System.currentTimeMillis()}"
            val destFile = File(saveDir, fileName)
            try {
                if (destFile.exists()) {
                    if (checkMd5(destFile)) {
                        successFiles.add(destFile)
                        return@forEach
                    }
                    destFile.delete()
                }
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                    response.body?.let { body ->
                        destFile.outputStream().use { output ->
                            body.byteStream().copyTo(output)
                        }
                        successFiles.add(destFile)
                    } ?: throw IOException("Empty response body")
                }
            } catch (e: Exception) {
                destFile.delete()
                println("下载失败 [$url]: ${e.message}")
            }
        }
        onAllComplete(successFiles)
    }

    fun downloadFile(url: String, fileName: String, outputDir: String) {
        val connection = URI.create(url).toURL().openConnection() as HttpURLConnection
        connection.apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 15000
        }
        try {
            if (connection.responseCode !in 200..299) {
                throw IOException("HTTP request failed with code ${connection.responseCode}")
            }
            val outputDirFile = File(outputDir).apply { mkdirs() }
            val outputFile = File(outputDirFile, fileName)
            connection.inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    fun downloadFiles(
        urls: List<String>,
        saveDir: File,
        checkMd5: (File, Int) -> Boolean,
        onAllComplete: (List<File>) -> Unit,
        onProgressUpdate: (Int, Double) -> Unit // 文件索引和当前下载速度（MB/s）
    ) = CoroutineScope(Dispatchers.IO).launch {
        val client = OkHttpClient()
        saveDir.mkdirs()
        val successFiles = mutableListOf<File>()

        for ((index, url) in urls.withIndex()) {
            val fileName = url.substringAfterLast('/').takeIf { it.isNotBlank() }
                ?: "unnamed_${System.currentTimeMillis()}"
            val destFile = File(saveDir, fileName)

            try {
                if (destFile.exists()) {
                    if (checkMd5(destFile, urls.indexOf(fileName))) {
                        successFiles.add(destFile)
                        continue
                    }
                    destFile.delete()
                }

                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                    response.body?.let { body ->
                        body.contentLength()
                        var bytesCopied: Long = 0
                        var startTime = System.currentTimeMillis()
                        destFile.outputStream().use { output ->
                            body.byteStream().buffered().use { input ->
                                val buffer = ByteArray(8 * 1024)
                                while (true) {
                                    val read = input.read(buffer)
                                    if (read == -1) break
                                    output.write(buffer, 0, read)
                                    bytesCopied += read
                                    val currentTime = System.currentTimeMillis()
                                    if (currentTime - startTime >= 1000) {
                                        val elapsedTimeInSeconds = (currentTime - startTime) / 1000.0
                                        val speedMbPerSec =
                                            (bytesCopied.toDouble() / (1024 * 1024)) / elapsedTimeInSeconds
                                        withContext(Dispatchers.Main) {
                                            onProgressUpdate(index + 1, speedMbPerSec)
                                        }
                                        bytesCopied = 0
                                        startTime = currentTime
                                    }
                                }
                            }
                        }
                        successFiles.add(destFile)
                    } ?: throw IOException("Empty response body")
                }
            } catch (e: Exception) {
                destFile.delete()
                println("下载失败 [$url]: ${e.message}")
            }
        }
        withContext(Dispatchers.Main) {
            onAllComplete(successFiles)
        }
    }
}
package icu.bluedream.gamesteup.core.utils

import java.io.File

object FileSystemUtil {
    private val localCacheStorage = "${System.getProperty("user.home")}/Library/Caches/GameSetup"

    fun getCacheRoot(): String {
        val file = File(localCacheStorage)
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath
    }

    fun getCommonFileDir(): String {
        val file = File("$localCacheStorage/FILES")
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath
    }

    fun getPrebuildDir(): String {
        val file = File("$localCacheStorage/PREBUILD")
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath
    }
}
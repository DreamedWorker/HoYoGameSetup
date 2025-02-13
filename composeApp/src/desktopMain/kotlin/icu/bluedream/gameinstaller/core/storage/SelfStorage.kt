package icu.bluedream.gameinstaller.core.storage

import java.io.File
import java.util.*

object SelfStorage {
    private val userHome = System.getProperty("user.home")

    @Throws(Exception::class)
    fun getCacheFolder(): String {
        if (!System.getProperty("os.name").lowercase(Locale.getDefault()).contains("mac")) {
            throw Exception("This app is only suitable on Mac!")
        }
        val path = "$userHome/Library/Caches/GameSetup"
        val file = File(path)
        if (!file.exists()) {
            file.mkdirs()
        }
        return path
    }

    fun getCachePackageFolder(): String {
        val path = "$userHome/Library/Caches/GameSetup/pkgs"
        val file = File(path)
        if (!file.exists()) {
            file.mkdirs()
        }
        return path
    }

    fun getCacheFileFolder(): String {
        val path = "$userHome/Library/Caches/GameSetup/files"
        val file = File(path)
        if (!file.exists()) {
            file.mkdirs()
        }
        return path
    }
}
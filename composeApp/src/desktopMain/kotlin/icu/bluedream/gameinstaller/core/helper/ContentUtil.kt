package icu.bluedream.gameinstaller.core.helper

import icu.bluedream.gameinstaller.data.Constants.LAUNCH_BAT
import icu.bluedream.gameinstaller.data.Constants.MAC_APPLICATION_PATH
import icu.bluedream.gameinstaller.data.Constants.WINE_DRIVE_C_PATH
import icu.bluedream.gameinstaller.data.Constants.WINE_MAC_ROOT_PATH
import icu.bluedream.gameinstaller.data.Constants.GS_CLOUD_GAME_PARAM
import icu.bluedream.gameinstaller.data.types.GameType
import java.io.File

object ContentUtil {
    fun reformatMd5File(inputPath: String, outputPath: String) {
        val regex = Regex("""MD5 \(\./(.*)\) = (.*)""") // 使用原始字符串避免转义

        File(outputPath).printWriter().use { writer ->
            File(inputPath).forEachLine { line ->
                val transformed = line.replace(regex) { match ->
                    "${match.groupValues[1]}=${match.groupValues[2]}"
                }
                writer.println(transformed)
            }
        }
    }

    fun getLeftPartsList(filePath: String): MutableList<String> {
        return File(filePath).useLines { lines ->
            lines.map { it.substringBefore('=').trim() }
                .filter { it.isNotEmpty() }
                .toMutableList()
        }
    }

    fun readMd5FileToMap(filePath: String): Map<String, String> {
        return try {
            File(filePath).useLines { lines ->
                lines
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .mapNotNull { line ->
                        val parts = line.split('=', limit = 2)
                            .map { it.trim() }
                        when {
                            parts.size != 2 -> {
                                null
                            }
                            parts[0].isEmpty() -> {
                                null
                            }
                            parts[1].isEmpty() -> {
                                null
                            }
                            else -> parts[0] to parts[1]
                        }
                    }
                    .toMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun modifyLaunchBat(gameType: GameType, installPath: String) {
        fun isGS(type: GameType) = type == GameType.GI_CN || type == GameType.GI_OS
        val launchBatPath = "$MAC_APPLICATION_PATH${GameType.getAppTranslationName(gameType)}/$WINE_DRIVE_C_PATH$LAUNCH_BAT"
        val launchString = "\"$WINE_MAC_ROOT_PATH$installPath${GameType.getExeAppName(gameType)}\"" + if (isGS(gameType)) " $GS_CLOUD_GAME_PARAM" else ""
        val launchBatFile = File(launchBatPath)
        launchBatFile.writeText(launchString)
    }
}
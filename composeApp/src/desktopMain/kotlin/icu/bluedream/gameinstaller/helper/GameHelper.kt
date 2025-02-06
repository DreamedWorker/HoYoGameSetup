package icu.bluedream.gameinstaller.helper

import icu.bluedream.gameinstaller.data.Constants.GS_EXE
import icu.bluedream.gameinstaller.data.Constants.ZZZ_EXE
import icu.bluedream.gameinstaller.data.GameType

object GameHelper {
    fun getGameFinderName(type: GameType): String = when(type) {
        GameType.GenshinCN -> "原神.app"
        GameType.GenshinOS -> "Genshin Impact.app"
        GameType.ZzzCN -> "绝区零.app"
        GameType.ZzzOS -> "Zenless Zone Zero.app"
    }

    fun getGameFileNameInner(type: GameType): String = when(type) {
        GameType.GenshinCN, GameType.GenshinOS -> "GS.zip"
        GameType.ZzzCN, GameType.ZzzOS -> "ZZZ.zip"
    }

    fun getExeFileName(type: GameType) = when(type) {
        GameType.GenshinCN, GameType.GenshinOS -> GS_EXE
        GameType.ZzzCN, GameType.ZzzOS -> ZZZ_EXE
    }

    fun isGS(type: GameType) = type == GameType.GenshinCN || type == GameType.GenshinOS
}
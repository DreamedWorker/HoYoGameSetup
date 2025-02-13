package icu.bluedream.gameinstaller.data.types

import gamesetup.composeapp.generated.resources.*
import gamesetup.composeapp.generated.resources.Res
import icu.bluedream.gameinstaller.data.Constants
import org.jetbrains.compose.resources.StringResource

enum class GameType {
    GI_CN, GI_OS, ZZZ_CN, ZZZ_OS;

    companion object {
        fun getGameName(type: GameType): StringResource =
            when (type) {
                GI_CN -> Res.string.g1
                GI_OS -> Res.string.g2
                ZZZ_CN -> Res.string.g3
                ZZZ_OS -> Res.string.g4
            }

        fun getGameAppName(type: GameType): String = when(type) {
            GI_CN, GI_OS -> "GS.app"
            ZZZ_CN, ZZZ_OS -> "ZZZ.app"
        }

        fun getExeAppName(type: GameType): String = when(type) {
            GI_CN, GI_OS -> Constants.GS_EXE
            ZZZ_CN, ZZZ_OS -> Constants.ZZZ_EXE
        }

        fun getAppTranslationName(type: GameType): String = when(type) {
            GI_CN -> "原神.app"
            GI_OS -> "Genshin Impact.app"
            ZZZ_CN -> "绝区零.app"
            ZZZ_OS -> "Zenless Zone Zero.app"
        }

        fun getMetaFileName(type: GameType): String = when (type) {
            GI_OS, GI_CN -> "md5sums_gs.txt"
            ZZZ_OS, ZZZ_CN -> "md5sums_zzz.txt"
        }

        fun getSplitFileName(type: GameType): String = when (type) {
            GI_OS, GI_CN -> "GS_split_"
            ZZZ_OS, ZZZ_CN -> "ZZZ_split_"
        }

        fun getPackInfoDownloadLink(type: GameType): String = when(type) {
            GI_CN -> Constants.GENSHIN_PKG_API_CN
            GI_OS -> Constants.GENSHIN_PKG_API_GL
            ZZZ_CN -> Constants.ZZZ_PKG_API_CN
            ZZZ_OS -> Constants.ZZZ_PKG_API_GL
        }

        fun getPackDownloadPrefix(type: GameType): String = when(type) {
            GI_CN -> "Yuan"
            GI_OS -> "Genshin"
            ZZZ_CN -> "jue"
            ZZZ_OS -> "Zen"
        }
    }
}
package icu.bluedream.gameinstaller.data

import gameinstaller.composeapp.generated.resources.Res
import gameinstaller.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource

enum class GameType {
    GenshinCN,
    GenshinOS,
    ZzzCN,
    ZzzOS;

    companion object {
        fun getLiteralName(type: GameType): StringResource {
            return when (type) {
                GenshinCN -> Res.string.gameType_gi_cn
                GenshinOS -> Res.string.gameType_gi_os
                ZzzCN -> Res.string.gameType_zzz_cn
                ZzzOS -> Res.string.gameType_zzz_os
            }
        }

        fun getGameType(literalName: String): GameType {
            return when (literalName) {
                "原神国服" -> GenshinCN
                "原神国际服" -> GenshinOS
                "绝区零国服" -> ZzzCN
                "绝区零国际服" -> ZzzOS
                else -> GenshinOS
            }
        }
    }
}
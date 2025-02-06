package icu.bluedream.gameinstaller.data

object Constants {
    // MARK: - 游戏下载地址
    const val GENSHIN_PKG_API_CN = "https://hyp-api.mihoyo.com/hyp/hyp-connect/api/getGamePackages?launcher_id=jGHBHlcOq1&game_ids[]=1Z8W5NHUQb"
    const val GENSHIN_PKG_API_GL = "https://sg-hyp-api.hoyoverse.com/hyp/hyp-connect/api/getGamePackages?launcher_id=VYTpXlbWo8&game_ids[]=gopR6Cufr3"
    const val ZZZ_PKG_API_CN = "https://hyp-api.mihoyo.com/hyp/hyp-connect/api/getGamePackages?launcher_id=jGHBHlcOq1&game_ids[]=x6znKlJ0xK"
    const val ZZZ_PKG_API_GL = "https://sg-hyp-api.hoyoverse.com/hyp/hyp-connect/api/getGamePackages?launcher_id=VYTpXlbWo8&game_ids[]=U5hbdsT9W7"

    // MARK: - 游戏标签
    const val GENSHIN_CN = 0
    const val GENSHIN_GL = 1
    const val GENSHIN_BILI = 2
    const val ZZZ_CN = 3
    const val ZZZ_GL = 4
    const val INVALIDE_GAME = -1

    // MARK: - 本地相对路径
    const val GS_EXE = "YuanShen.exe"
    const val ZZZ_EXE = "ZenlessZoneZero.exe"
    const val WINE_DRIVE_C_PATH = "Contents/SharedSupport/prefix/drive_c/"
    const val LAUNCH_BAT = "launchGame.bat"
    const val WINE_MAC_ROOT_PATH = "Z:"
    const val MAC_APPLICATION_PATH = "/Applications/"

    // MARK: - 启动参数
    const val GS_CLOUD_GAME_PARAM = "-platform_type CLOUD_THIRD_PARTY_PC"

    fun getGameURL(type: GameType) = when(type) {
        GameType.GenshinCN -> GENSHIN_PKG_API_CN
        GameType.GenshinOS -> GENSHIN_PKG_API_GL
        GameType.ZzzCN -> ZZZ_PKG_API_CN
        GameType.ZzzOS -> ZZZ_PKG_API_GL
    }
}
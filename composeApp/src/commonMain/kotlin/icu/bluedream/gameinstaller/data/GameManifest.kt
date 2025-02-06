package icu.bluedream.gameinstaller.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class GameManifest (
    val retcode: Long,
    val message: String,
    val data: Data
)

@Serializable
data class Data (
    @SerialName("game_packages")
    val gamePackages: List<GamePackage>
)

@Serializable
data class GamePackage (
    val game: Game,
    val main: Main,

    @SerialName("pre_download")
    val preDownload: Main
)

@Serializable
data class Game (
    val id: String,
    val biz: String
)

@Serializable
data class Main (
    val major: Major? = null,
    val patches: List<Major>
)

@Serializable
data class Major (
    val version: String,

    @SerialName("game_pkgs")
    val gamePkgs: List<Pkg>,

    @SerialName("audio_pkgs")
    val audioPkgs: List<Pkg>,

    @SerialName("res_list_url")
    val resListURL: String
)

@Serializable
data class Pkg (
    val language: String? = null,
    val url: String,
    val md5: String,
    val size: String,

    @SerialName("decompressed_size")
    val decompressedSize: String
)

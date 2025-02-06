import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.0.0"
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.icons.jetbrains)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.filekit)
        }
    }
}

compose.resources {
    generateResClass = always
}

compose.desktop {
    application {
        mainClass = "icu.bluedream.gameinstaller.MainKt"

        buildTypes.release.proguard {
            version = "7.5.0"
            isEnabled = false
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "GameInstaller"
            packageVersion = "1.0.0"
            copyright = "Copyright © YuanShine 2025-present."
            description = "A GUI way to install games to your Mac quickly."

            macOS {
                iconFile.set(File("${project.projectDir}/src/AppIcon.icns"))
            }
        }
    }
}

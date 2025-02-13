import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.1.0"
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
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.voyager.navigator)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.filekit)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.icons.jetbrains)
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
        }
    }
}


compose.desktop {
    application {
        mainClass = "icu.bluedream.gameinstaller.MainKt"

        buildTypes.release.proguard {
            isEnabled = false
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "HoYoGameSetup"
            packageVersion = "1.0.0"
            copyright = "Copyright © YuanShine 2025-present."

            macOS {
                iconFile.set(file("src/AppIcon.icns"))
            }
        }
    }
}

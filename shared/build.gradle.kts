import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Code shared by the Android app and the iOS app.
//   src/commonMain  - Kotlin + Compose used by both platforms (models, theme, home screen; more over time)
//   src/androidMain - Android-only implementations, when common code needs one
//   src/iosMain     - iOS-only code, including the entry point the iOS app calls (MainViewController)
//   src/commonMain/composeResources - images, fonts and translated text used by shared screens
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // iPhone (device) and the iOS simulator on Apple Silicon Macs.
    // Each builds a "Shared" framework that the Xcode project in iosApp/ links against.
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.example.bhandara.shared.resources"
}

android {
    namespace = "com.example.bhandara.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 35
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

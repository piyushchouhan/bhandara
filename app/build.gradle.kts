import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

// Load API keys from local.properties (which is gitignored)
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

android {
    namespace = "com.example.bhandara"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.bhandara"
        minSdk = 35
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // API Configuration
        // Using computer's local IP for physical device testing
        // Phone must be on same WiFi network OR use USB with ADB reverse port forwarding
//        buildConfigField("String", "API_BASE_URL", "\"https://tobago-sphere-photo-magnetic.trycloudflare.com/\"")
//         buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.14:8080/\"")
//        buildConfigField("String", "API_BASE_URL", "\"https://9814-2402-e280-3e17-820-592c-1ff3-b22b-e05.ngrok-free.app/\"")
//        buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.4:8080/\"")
//        buildConfigField("String", "API_BASE_URL", "\"https://fa92-2402-e280-3e17-820-f853-99f8-7fb7-b3dd.ngrok-free.app/\"")
        buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.5:8080/\"")

        // Platform admins (comma-separated). Keep in sync with the backend's app.admin-emails.
        // Only used to skip owner-only prompts in the UI; the backend does the real check from the login token.
        buildConfigField("String", "ADMIN_EMAILS", "\"piyush9695@gmail.com\"")
        
        // Google Maps API Key from local.properties
        manifestPlaceholders["MAPS_API_KEY"] = localProperties.getProperty("MAPS_API_KEY", "")
    }

    signingConfigs {
        create("release") {
            // Use debug keystore for testing release build on local device
            storeFile = file(System.getProperty("user.home") + "/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    // Code shared with the iOS app: screens, view models, networking and models
    implementation(project(":shared"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Extended icons library
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    
    // Firebase - Import the BoM
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")
    
    // Google Play Services for Location
    implementation("com.google.android.gms:play-services-location:21.3.0")
    
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    
    
    // WorkManager for periodic location updates
    implementation("androidx.work:work-runtime-ktx:2.11.2")
    
    // Google Maps SDK for Android
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:4.3.3")
}
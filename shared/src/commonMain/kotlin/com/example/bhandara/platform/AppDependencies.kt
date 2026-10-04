package com.example.bhandara.platform

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.settings.AppSettings

/** The app-wide objects shared screens build their ViewModels from; each platform provides its own */
class AppDependencies(val apiClient: ApiClient, val settings: AppSettings)

val LocalAppDependencies = staticCompositionLocalOf<AppDependencies> {
    error("AppDependencies not provided: wrap the UI in CompositionLocalProvider(LocalAppDependencies provides ...)")
}

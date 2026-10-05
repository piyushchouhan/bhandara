package com.example.bhandara.platform

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.bhandara.auth.AuthService
import com.example.bhandara.data.network.ApiClient
import com.example.bhandara.data.settings.AppSettings
import com.example.bhandara.photos.PhotoUploader

/** The app-wide objects shared screens build their ViewModels from; each platform provides its own */
/**
 * @param appVersion    the version users see, e.g. "1.0.3"
 * @param photoUploader uploads the photos users add to reviews, feasts and shops
 */
class AppDependencies(
    val apiClient: ApiClient,
    val settings: AppSettings,
    val auth: AuthService,
    val appVersion: String,
    val photoUploader: PhotoUploader,
)

val LocalAppDependencies = staticCompositionLocalOf<AppDependencies> {
    error("AppDependencies not provided: wrap the UI in CompositionLocalProvider(LocalAppDependencies provides ...)")
}

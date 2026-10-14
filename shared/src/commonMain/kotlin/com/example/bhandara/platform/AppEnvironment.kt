package com.example.bhandara.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade

/**
 * What every shared screen relies on, set up once at the root of the app's UI on both platforms:
 * photos loaded from the internet (Coil) and the platform's [PlatformActions].
 */
@Composable
fun ProvideAppEnvironment(content: @Composable () -> Unit) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(true)
            .build()
    }
    CompositionLocalProvider(LocalPlatformActions provides rememberPlatformActions(), content = content)
}

package com.example.bhandara.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.serialization.json.Json

/**
 * Which screens are open, newest last, on both Android and iOS. Home is always at the bottom: going back from
 * it is left to the platform (on Android it closes the app).
 */
class AppNavigator(initialBackStack: List<Route> = listOf(Route.Home)) {

    var backStack: List<Route> by mutableStateOf(initialBackStack.ifEmpty { listOf(Route.Home) })
        private set

    /** The screen being shown */
    val current: Route get() = backStack.last()

    val canGoBack: Boolean get() = backStack.size > 1

    /** Opens [route] on top. Opening the screen already shown (e.g. a double tap) does nothing. */
    fun navigate(route: Route) {
        if (route != current) backStack = backStack + route
    }

    /** Shows [route] in place of the current screen, e.g. a feast's page after posting it, so back skips the form */
    fun replace(route: Route) {
        backStack = if (canGoBack) backStack.dropLast(1) + route else backStack + route
    }

    /** Closes the current screen; returns false when on the first screen, so the platform decides */
    fun back(): Boolean {
        if (!canGoBack) return false
        backStack = backStack.dropLast(1)
        return true
    }

    /** Closes everything above Home, e.g. "Find a shop to review" from deep in the profile */
    fun backToHome() {
        backStack = backStack.take(1)
    }

    /**
     * One key per open screen, bottom first. The same screen opened twice (e.g. two shops' pages) gets two keys,
     * and a screen keeps its key while screens open and close above it.
     */
    fun entryKeys(): List<String> = backStack.mapIndexed { index, route -> "$index:$route" }

    /** The back stack as text, so it can be restored after rotation or when Android recreates the screen */
    fun save(): ArrayList<String> = ArrayList(backStack.map { json.encodeToString(Route.serializer(), it) })

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /** Restores a saved back stack; screens that can't be read (e.g. from an older app version) are skipped */
        fun restore(saved: List<String>): AppNavigator = AppNavigator(
            saved.mapNotNull { runCatching { json.decodeFromString(Route.serializer(), it) }.getOrNull() }
        )

        val Saver: Saver<AppNavigator, ArrayList<String>> = Saver(save = { it.save() }, restore = { restore(it) })
    }
}

/**
 * The app's navigator, kept across rotation and when the system recreates the screen
 *
 * @param startOnFeasts a fresh start opens on the feasts map (Settings → Open app on), with Home under it
 */
@Composable
fun rememberAppNavigator(startOnFeasts: Boolean = false): AppNavigator = rememberSaveable(saver = AppNavigator.Saver) {
    AppNavigator(if (startOnFeasts) listOf(Route.Home, Route.Hungry) else listOf(Route.Home))
}

package com.example.bhandara.navigation

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.abs

/**
 * One ViewModelStore per open screen. A screen's view models live while it's on the back stack (so going back to
 * the map finds it as it was) and are cleared once it's closed (so opening it again starts fresh).
 * Itself a ViewModel of the whole app, so it survives Android recreating the activity.
 */
class NavEntryStores : ViewModel() {

    private val stores = mutableMapOf<String, ViewModelStore>()

    fun storeFor(key: String): ViewModelStore = stores.getOrPut(key) { ViewModelStore() }

    /** Clears the stores of screens that are no longer open */
    fun keepOnly(keys: Collection<String>) {
        (stores.keys - keys.toSet()).forEach { stores.remove(it)?.clear() }
    }

    override fun onCleared() {
        stores.values.forEach { it.clear() }
        stores.clear()
    }
}

/**
 * Shows the current screen with its own view models and saved UI state (e.g. the selected tab), kept while it's
 * on the back stack and dropped when it's closed.
 */
@Composable
fun NavEntries(navigator: AppNavigator, content: @Composable (Route) -> Unit) {
    val stores = viewModel { NavEntryStores() }
    val savedStates = rememberSaveableStateHolder()
    val keys = navigator.entryKeys()
    val currentKey = keys.last()
    val seenKeys = remember { mutableSetOf<String>() }

    LaunchedEffect(keys) {
        stores.keepOnly(keys)
        (seenKeys - keys.toSet()).forEach(savedStates::removeState)
        seenKeys.retainAll(keys.toSet())
    }
    seenKeys += currentKey

    val owner = remember(currentKey) {
        object : ViewModelStoreOwner {
            override val viewModelStore = stores.storeFor(currentKey)
        }
    }
    savedStates.SaveableStateProvider(currentKey) {
        CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
            content(navigator.current)
        }
    }
}

/**
 * Whether a drag is iOS's "swipe from the left edge to go back": it starts at the edge and moves mostly
 * rightwards far enough
 */
fun isBackSwipe(startX: Float, dx: Float, dy: Float, edgeWidth: Float, distance: Float): Boolean =
    startX <= edgeWidth && dx >= distance && dx > abs(dy) * 2

/** iPhone users go back by swiping from the screen's left edge; does that when [enabled] */
fun Modifier.swipeFromEdgeToGoBack(enabled: Boolean, onBack: () -> Unit): Modifier =
    if (!enabled) this else pointerInput(onBack) {
        val edge = 24.dp.toPx()
        val distance = 80.dp.toPx()
        awaitEachGesture {
            // Watch before the screen's own handlers, without taking the touch unless it is a back swipe
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            if (down.position.x > edge) return@awaitEachGesture
            while (true) {
                val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) break
                val dx = change.position.x - down.position.x
                val dy = change.position.y - down.position.y
                if (isBackSwipe(down.position.x, dx, dy, edge, distance)) {
                    change.consume()
                    onBack()
                    break
                }
            }
        }
    }

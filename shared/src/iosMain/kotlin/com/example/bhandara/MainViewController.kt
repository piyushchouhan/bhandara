package com.example.bhandara

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import com.example.bhandara.ui.screens.HomeScreen
import com.example.bhandara.ui.theme.BhandaraTheme

/**
 * Entry point for the iOS app: the SwiftUI code in iosApp/ shows this view controller full screen.
 *
 * For now it shows the shared home screen; its buttons will navigate once the other screens are shared.
 */
fun MainViewController() = ComposeUIViewController {
    BhandaraTheme {
        var homeTabIndex by remember { mutableIntStateOf(0) }
        HomeScreen(
            selectedTabIndex = homeTabIndex,
            onTabSelected = { homeTabIndex = it }
        )
    }
}

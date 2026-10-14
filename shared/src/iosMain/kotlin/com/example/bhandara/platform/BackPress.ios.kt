package com.example.bhandara.platform

import androidx.compose.runtime.Composable

@Composable
actual fun BackPress(enabled: Boolean, onBack: () -> Unit) {
    // No system back on iOS: screens close with their own buttons
}

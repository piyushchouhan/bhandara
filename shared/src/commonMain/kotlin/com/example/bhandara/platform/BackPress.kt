package com.example.bhandara.platform

import androidx.compose.runtime.Composable

/**
 * While [enabled], the system back (Android's back button or gesture) calls [onBack] instead of leaving the screen,
 * e.g. to close a full-screen photo first. iOS has no system back, so there it does nothing.
 */
@Composable
expect fun BackPress(enabled: Boolean = true, onBack: () -> Unit)

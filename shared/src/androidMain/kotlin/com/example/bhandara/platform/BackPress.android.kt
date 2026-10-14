package com.example.bhandara.platform

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun BackPress(enabled: Boolean, onBack: () -> Unit) = BackHandler(enabled, onBack)

package com.example.bhandara.platform

import androidx.compose.runtime.Composable

/** What the phone currently allows the app; null where it can't be told */
data class PermissionStatuses(
    val location: Boolean,
    val camera: Boolean?,
    val notifications: Boolean?,
)

/** The phone's current permissions for the app, read again whenever the screen comes back (e.g. from Settings) */
@Composable
expect fun rememberPermissionStatuses(): PermissionStatuses

package com.example.bhandara.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
actual fun rememberPermissionStatuses(): PermissionStatuses {
    val context = LocalContext.current
    var statuses by remember { mutableStateOf(read(context)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { statuses = read(context) }
    return statuses
}

private fun read(context: Context): PermissionStatuses {
    fun granted(permission: String) = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    return PermissionStatuses(
        location = granted(Manifest.permission.ACCESS_FINE_LOCATION) || granted(Manifest.permission.ACCESS_COARSE_LOCATION),
        camera = granted(Manifest.permission.CAMERA),
        notifications = NotificationManagerCompat.from(context).areNotificationsEnabled(),
    )
}

package com.example.bhandara.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberPermissionStatuses(): PermissionStatuses {
    var statuses by remember { mutableStateOf(read(notifications = null)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        statuses = read(statuses.notifications)
        // Notification settings only come back asynchronously
        UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
            val allowed = settings?.authorizationStatus.let { it == UNAuthorizationStatusAuthorized || it == UNAuthorizationStatusProvisional }
            dispatch_async(dispatch_get_main_queue()) { statuses = statuses.copy(notifications = allowed) }
        }
    }
    return statuses
}

private fun read(notifications: Boolean?): PermissionStatuses {
    val location = CLLocationManager().authorizationStatus
    return PermissionStatuses(
        location = location == kCLAuthorizationStatusAuthorizedWhenInUse || location == kCLAuthorizationStatusAuthorizedAlways,
        camera = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) == AVAuthorizationStatusAuthorized,
        notifications = notifications,
    )
}

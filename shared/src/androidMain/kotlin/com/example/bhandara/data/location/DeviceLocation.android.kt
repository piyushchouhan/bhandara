package com.example.bhandara.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** The phone's location on Android, from Google Play services */
class AndroidDeviceLocation(context: Context) : DeviceLocation {

    private val appContext = context.applicationContext
    private val fused = LocationServices.getFusedLocationProviderClient(appContext)

    /** Precise or approximate: since Android 12 users may allow only an approximate location */
    override fun hasPermission(): Boolean = PERMISSIONS.any {
        ContextCompat.checkSelfPermission(appContext, it) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission") // checked by LocationService before calling
    override suspend fun current(): GeoLocation? {
        val cancellation = CancellationTokenSource()
        val location = try {
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellation.token).await()
        } finally {
            cancellation.cancel()
        }
        return location?.let { GeoLocation(it.latitude, it.longitude) }
    }

    @SuppressLint("MissingPermission")
    override suspend fun lastKnown(): TimedLocation? =
        fused.lastLocation.await()?.let { TimedLocation(GeoLocation(it.latitude, it.longitude), it.time) }

    companion object {
        val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    }
}

private suspend fun <T> Task<T>.await(): T? = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
    addOnFailureListener { if (continuation.isActive) continuation.resumeWithException(it) }
    addOnCanceledListener { if (continuation.isActive) continuation.resume(null) }
}

@Composable
actual fun rememberDeviceLocation(): DeviceLocation {
    val context = LocalContext.current
    return remember(context) { AndroidDeviceLocation(context) }
}

@Composable
actual fun rememberLocationPermissionRequest(onResult: (granted: Boolean) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        onResult(results.values.any { it })
    }
    return { launcher.launch(AndroidDeviceLocation.PERMISSIONS) }
}

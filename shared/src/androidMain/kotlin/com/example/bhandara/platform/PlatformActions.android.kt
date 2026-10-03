package com.example.bhandara.platform

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPlatformActions(): PlatformActions {
    val context = LocalContext.current
    return remember(context) { AndroidPlatformActions(context) }
}

private class AndroidPlatformActions(private val context: Context) : PlatformActions {

    override fun dial(phoneNumber: String) {
        val tel = ExternalLinks.phoneCall(phoneNumber) ?: return
        start(Intent(Intent.ACTION_DIAL, Uri.parse(tel)))
    }

    override fun openDirections(latitude: Double, longitude: Double) =
        openUrl(ExternalLinks.directions(latitude, longitude))

    override fun share(text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        start(Intent.createChooser(send, null))
    }

    override fun openUrl(url: String) = start(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    private fun start(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // e.g. no dialer on a tablet: nothing to open, not worth crashing over
            Log.w("PlatformActions", "No app can handle ${intent.action}", e)
        }
    }
}

package com.example.bhandara.platform

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

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

    override fun openDirections(latitude: Double, longitude: Double) = when (PlatformPreferences.directionsApp) {
        com.example.bhandara.data.settings.DirectionsApp.GOOGLE_MAPS -> openUrl(ExternalLinks.directions(latitude, longitude))
        // Every maps app on the phone is offered
        com.example.bhandara.data.settings.DirectionsApp.PHONE_MAPS ->
            start(Intent.createChooser(Intent(Intent.ACTION_VIEW, Uri.parse(ExternalLinks.geo(latitude, longitude))), null))
    }

    override fun share(text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        start(Intent.createChooser(send, null))
    }

    override fun openUrl(url: String) = start(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    override fun openAppSettings() = start(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
    )

    // Saved into Pictures/Local Feast through MediaStore, which needs no permission on Android 10+
    override suspend fun savePhoto(url: String): Boolean = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        var saved: Uri? = null
        try {
            val bytes = URL(url).openStream().use { it.readBytes() }
            val details = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "local-feast-${System.currentTimeMillis()}")
                put(MediaStore.Images.Media.MIME_TYPE, imageType(bytes))
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Local Feast")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            saved = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, details) ?: return@withContext false
            resolver.openOutputStream(saved)?.use { it.write(bytes) } ?: throw IOException("No output stream")
            resolver.update(saved, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            true
        } catch (e: Exception) {
            Log.w("PlatformActions", "Couldn't save photo $url", e)
            saved?.let { runCatching { resolver.delete(it, null, null) } }
            false
        }
    }

    // Written to the cache and handed over through the app's FileProvider (res/xml/file_paths.xml, "exports")
    override fun shareFile(fileName: String, mimeType: String, content: String) {
        try {
            val folder = java.io.File(context.cacheDir, "exports").apply { mkdirs() }
            val file = java.io.File(folder, fileName).apply { writeText(content) }
            val uri = androidx.core.content.FileProvider.getUriForFile(context, context.packageName + ".provider", file)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            start(Intent.createChooser(send, null).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
        } catch (e: Exception) {
            Log.w("PlatformActions", "Couldn't share $fileName", e)
        }
    }

    /** The photo's type from its first bytes; review photos are uploaded as WebP, older ones may be JPEG or PNG */
    private fun imageType(bytes: ByteArray): String = when {
        bytes.size > 12 && String(bytes, 0, 4) == "RIFF" && String(bytes, 8, 4) == "WEBP" -> "image/webp"
        bytes.size > 4 && bytes[0] == 0x89.toByte() && bytes[1] == 'P'.code.toByte() -> "image/png"
        else -> "image/jpeg"
    }

    private fun start(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // e.g. no dialer on a tablet: nothing to open, not worth crashing over
            Log.w("PlatformActions", "No app can handle ${intent.action}", e)
        }
    }
}

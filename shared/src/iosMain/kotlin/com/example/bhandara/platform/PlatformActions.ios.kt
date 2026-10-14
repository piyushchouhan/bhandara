package com.example.bhandara.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToFile
import platform.Photos.PHAccessLevelAddOnly
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
import platform.UIKit.UIImage
import kotlin.coroutines.resume
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIViewController

@Composable
actual fun rememberPlatformActions(): PlatformActions = remember { IosPlatformActions() }

private class IosPlatformActions : PlatformActions {

    override fun dial(phoneNumber: String) {
        val tel = ExternalLinks.phoneCall(phoneNumber) ?: return
        open(tel)
    }

    override fun openDirections(latitude: Double, longitude: Double) = when (PlatformPreferences.directionsApp) {
        com.example.bhandara.data.settings.DirectionsApp.GOOGLE_MAPS -> open(ExternalLinks.directions(latitude, longitude))
        com.example.bhandara.data.settings.DirectionsApp.PHONE_MAPS -> open(ExternalLinks.appleMapsDirections(latitude, longitude))
    }

    override fun share(text: String) {
        val sheet = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
        topViewController()?.presentViewController(sheet, animated = true, completion = null)
    }

    override fun openUrl(url: String) = open(url)

    override fun openAppSettings() = open(UIApplicationOpenSettingsURLString)

    // Adding to the library needs NSPhotoLibraryAddUsageDescription; iOS asks the user the first time
    override suspend fun savePhoto(url: String): Boolean {
        val image = withContext(Dispatchers.IO) {
            NSURL.URLWithString(url)?.let { NSData.dataWithContentsOfURL(it) }?.let { UIImage(data = it) }
        } ?: return false
        val status = suspendCancellableCoroutine { done ->
            PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelAddOnly) { done.resume(it) }
        }
        if (status != PHAuthorizationStatusAuthorized && status != PHAuthorizationStatusLimited) return false
        return suspendCancellableCoroutine { done ->
            PHPhotoLibrary.sharedPhotoLibrary().performChanges(
                { PHAssetChangeRequest.creationRequestForAssetFromImage(image) },
                completionHandler = { success, _ -> done.resume(success) },
            )
        }
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    override fun shareFile(fileName: String, mimeType: String, content: String) {
        val path = platform.Foundation.NSTemporaryDirectory() + fileName
        @Suppress("CAST_NEVER_SUCCEEDS")
        val text = content as platform.Foundation.NSString
        text.writeToFile(path, atomically = true, encoding = platform.Foundation.NSUTF8StringEncoding, error = null)
        val sheet = UIActivityViewController(activityItems = listOf(NSURL.fileURLWithPath(path)), applicationActivities = null)
        topViewController()?.presentViewController(sheet, animated = true, completion = null)
    }

    private fun open(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
    }

}

/** The screen currently shown, to present system screens (share sheet, photo picker) on top of it */
internal fun topViewController(): UIViewController? {
    var top = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (top?.presentedViewController != null) top = top.presentedViewController
    return top
}

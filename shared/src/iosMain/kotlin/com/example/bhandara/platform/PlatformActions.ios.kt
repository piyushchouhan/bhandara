package com.example.bhandara.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

@Composable
actual fun rememberPlatformActions(): PlatformActions = remember { IosPlatformActions() }

private class IosPlatformActions : PlatformActions {

    override fun dial(phoneNumber: String) {
        val tel = ExternalLinks.phoneCall(phoneNumber) ?: return
        open(tel)
    }

    override fun openDirections(latitude: Double, longitude: Double) =
        open(ExternalLinks.directions(latitude, longitude))

    override fun share(text: String) {
        val sheet = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
        topViewController()?.presentViewController(sheet, animated = true, completion = null)
    }

    override fun openUrl(url: String) = open(url)

    private fun open(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
    }

    /** The screen currently shown, to present the share sheet on top of it */
    private fun topViewController(): UIViewController? {
        var top = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (top?.presentedViewController != null) top = top.presentedViewController
        return top
    }
}

package com.example.bhandara.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

@Composable
actual fun rememberAppLanguage(): AppLanguage = remember { IosAppLanguage() }

/**
 * iOS chooses each app's language in the Settings app (shown there because Info.plist lists the app's
 * languages); the app reads it as the first preferred language
 */
private class IosAppLanguage : AppLanguage {

    override val current: String
        get() = appLanguageFor(NSLocale.preferredLanguages.firstOrNull() as? String)

    override val choosableInApp = false

    override fun choose(tag: String) {
        val settings = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(settings, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
}

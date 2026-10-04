package com.example.bhandara.platform

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.os.LocaleListCompat
import java.util.Locale

@Composable
actual fun rememberAppLanguage(): AppLanguage = remember { AndroidAppLanguage() }

/** Android 13+ keeps a language per app; AppCompat does the same on older versions */
private class AndroidAppLanguage : AppLanguage {

    override val current: String
        get() = appLanguageFor((AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()).toLanguageTag())

    override val choosableInApp = true

    override fun choose(tag: String) = AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
}

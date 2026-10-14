package com.example.bhandara.platform

import androidx.compose.runtime.Composable

/** The languages the app is translated into: tag and the name in that language */
val APP_LANGUAGES: List<Pair<String, String>> = listOf("en" to "English", "hi" to "हिंदी", "mr" to "मराठी")

/** The app language for a locale tag like "hi-IN"; English for anything the app isn't translated into */
fun appLanguageFor(localeTag: String?): String {
    val language = localeTag?.substringBefore('-')?.substringBefore('_')?.lowercase()
    return APP_LANGUAGES.firstOrNull { it.first == language }?.first ?: "en"
}

/** The app's display language, chosen in the side menu */
interface AppLanguage {
    /** "en", "hi" or "mr" */
    val current: String

    /** Android switches inside the app; on iOS the language is chosen in the Settings app */
    val choosableInApp: Boolean

    /** Switches to [tag]; on iOS opens the app's page in Settings instead */
    fun choose(tag: String)
}

@Composable
expect fun rememberAppLanguage(): AppLanguage

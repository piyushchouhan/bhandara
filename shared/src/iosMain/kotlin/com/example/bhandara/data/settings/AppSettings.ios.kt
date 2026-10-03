package com.example.bhandara.data.settings

import com.russhwolf.settings.NSUserDefaultsSettings

/** The app's settings, stored in NSUserDefaults */
fun createAppSettings(): AppSettings = AppSettings(NSUserDefaultsSettings.Factory())

package com.example.bhandara.data.settings

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings

/** The app's settings, stored in this app's SharedPreferences files (the same ones older versions used) */
fun AppSettings(context: Context): AppSettings =
    AppSettings(SharedPreferencesSettings.Factory(context.applicationContext))

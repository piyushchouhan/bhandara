package com.example.bhandara

import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook

/**
 * Sends Kotlin crashes to Crashlytics with their Kotlin stack trace (without this, iOS only sees an abort).
 * Called by the iOS app once Firebase is set up (iOSApp.swift).
 */
fun startCrashReporting() {
    enableCrashlytics()
    setCrashlyticsUnhandledExceptionHook()
}

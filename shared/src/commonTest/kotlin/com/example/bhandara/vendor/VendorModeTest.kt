package com.example.bhandara.vendor

import com.example.bhandara.data.settings.AppSettings
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Vendor Mode's switch, the same on Android and iOS: only with a cart added on this phone, and remembered */
class VendorModeTest {

    private class FakeTracker : VendorTracker {
        val started = mutableListOf<Pair<Long, String>>()
        var stops = 0
        override var isActive = false
        override fun start(shopId: Long, ownerUid: String) {
            started += shopId to ownerUid
            isActive = true
        }
        override fun stop() {
            stops++
            isActive = false
        }
    }

    private val stores = mutableMapOf<String?, Settings>()
    private fun settings() = AppSettings(object : Settings.Factory {
        override fun create(name: String?): Settings = stores.getOrPut(name) { MapSettings() }
    })

    private val tracker = FakeTracker()

    @Test
    fun withoutACartOnThisPhoneItCannotBeTurnedOn() {
        val vendorMode = VendorMode(settings(), tracker)

        vendorMode.set(true)

        assertFalse(vendorMode.hasCart)
        assertFalse(vendorMode.isOn)
        assertTrue(tracker.started.isEmpty())
    }

    @Test
    fun turningItOnSharesTheCartAndOffStops() {
        settings().rememberVendorCart(7, "vendor-1")
        val vendorMode = VendorMode(settings(), tracker)

        vendorMode.set(true)
        assertTrue(vendorMode.isOn)
        assertEquals(listOf(7L to "vendor-1"), tracker.started)

        vendorMode.set(false)
        assertFalse(vendorMode.isOn)
        assertEquals(1, tracker.stops)
    }

    @Test
    fun afterARestartSharingResumesIfItWasOn() {
        settings().rememberVendorCart(7, "vendor-1")
        VendorMode(settings(), tracker).set(true)
        tracker.isActive = false // the app was closed

        val reopened = VendorMode(settings(), tracker)
        assertTrue(reopened.isOn, "the switch is remembered")
        reopened.restore()
        reopened.restore()

        assertEquals(2, tracker.started.size, "started once more, not twice")
    }
}

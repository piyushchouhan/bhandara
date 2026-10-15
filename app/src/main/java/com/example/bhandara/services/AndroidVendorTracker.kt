package com.example.bhandara.services

import android.content.Context
import com.example.bhandara.vendor.VendorTracker

/** Android's Vendor Mode: a foreground service keeps the cart's location shared while the screen is off */
class AndroidVendorTracker(private val context: Context) : VendorTracker {
    override fun start(shopId: Long, ownerUid: String) = VendorTrackingService.start(context, shopId, ownerUid)
    override fun stop() = VendorTrackingService.stop(context)
    override val isActive: Boolean get() = VendorLocationManager.getInstance(context).isActive()
}

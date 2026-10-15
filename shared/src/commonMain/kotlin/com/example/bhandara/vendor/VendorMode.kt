package com.example.bhandara.vendor

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bhandara.data.settings.AppSettings
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.vendor_mode
import com.example.bhandara.shared.resources.vendor_mode_active
import com.example.bhandara.shared.resources.vendor_mode_no_shop
import com.example.bhandara.ui.drawer.SheetToggleRow
import com.example.bhandara.ui.profile.VendorModeCard
import com.example.bhandara.ui.theme.feastColors
import org.jetbrains.compose.resources.stringResource

/**
 * Keeps a cart's location shared in the background on this platform (Android: a foreground service; iOS:
 * background location), feeding [VendorSharing]
 */
interface VendorTracker {
    fun start(shopId: Long, ownerUid: String)
    fun stop()
    val isActive: Boolean
}

/**
 * Vendor Mode, the same on Android and iOS: a moving-cart owner shares the cart's live location while it's on. The
 * cart is the one added on this phone; the switch is remembered, and sharing restarts with the app if it was on.
 */
class VendorMode(private val settings: AppSettings, private val tracker: VendorTracker) {

    // Read each time: a cart can be added while the app is open
    private val shopId: Long? get() = settings.vendorShopId
    private val ownerUid: String? get() = settings.vendorOwnerUid

    /** The user added a moving cart on this phone */
    val hasCart: Boolean get() = (shopId ?: 0) > 0 && ownerUid != null

    var isOn by mutableStateOf(settings.isVendorModeOn)
        private set

    fun set(on: Boolean) {
        if (!hasCart) return
        isOn = on
        settings.isVendorModeOn = on
        if (on) tracker.start(shopId!!, ownerUid!!) else tracker.stop()
    }

    /** Starts sharing again if it was on (e.g. after the app was restarted) */
    fun restore() {
        if (isOn && hasCart && !tracker.isActive) tracker.start(shopId!!, ownerUid!!)
    }
}

/** This platform's Vendor Mode; null where there is none */
val LocalVendorMode = staticCompositionLocalOf<VendorMode?> { null }

/** Vendor Mode's row in the side menu: a saffron switch, with why it's unavailable or that it's on */
@Composable
fun VendorModeDrawerRow() {
    val vendorMode = LocalVendorMode.current ?: return
    LaunchedEffect(vendorMode) { vendorMode.restore() }
    SheetToggleRow(
        icon = Icons.Rounded.Explore,
        label = stringResource(Res.string.vendor_mode),
        checked = vendorMode.isOn,
        onCheckedChange = vendorMode::set,
        enabled = vendorMode.hasCart,
        subtitle = vendorSubtitle(vendorMode),
    )
}

/** Vendor Mode's card on the profile */
@Composable
fun VendorModeProfileCard() {
    val vendorMode = LocalVendorMode.current ?: return
    VendorModeCard(checked = vendorMode.isOn, onCheckedChange = vendorMode::set, enabled = vendorMode.hasCart, subtitle = vendorSubtitle(vendorMode))
}

/** Over the home screen while Vendor Mode is on */
@Composable
fun VendorModeBanner(modifier: Modifier = Modifier) {
    val vendorMode = LocalVendorMode.current ?: return
    if (!vendorMode.isOn || !vendorMode.hasCart) return
    val colors = MaterialTheme.feastColors
    Surface(color = colors.saffronSoft.copy(alpha = 0.95f), contentColor = colors.saffronText, modifier = modifier.fillMaxWidth()) {
        Text(
            stringResource(Res.string.vendor_mode_active), style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun vendorSubtitle(vendorMode: VendorMode): String? = when {
    !vendorMode.hasCart -> stringResource(Res.string.vendor_mode_no_shop)
    vendorMode.isOn -> stringResource(Res.string.vendor_mode_active)
    else -> null
}

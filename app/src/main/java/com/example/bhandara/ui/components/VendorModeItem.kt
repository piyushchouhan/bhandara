package com.example.bhandara.ui.components

import android.content.Context
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bhandara.R
import com.example.bhandara.data.settings.AppSettings
import com.example.bhandara.services.VendorLocationManager
import com.example.bhandara.services.VendorTrackingService
import com.example.bhandara.ui.drawer.SheetToggleRow
import com.example.bhandara.ui.profile.VendorModeCard

/**
 * Vendor mode (Android only for now): a moving-cart owner shares the cart's live location. Shown in the side
 * menu; [VendorModeBanner] reminds them while it's on.
 */
class VendorMode(private val context: Context) {

    private val settings = AppSettings(context)
    // Read each time: a cart can be added while the app is open
    private val shopId get() = settings.vendorShopId
    private val ownerUid get() = settings.vendorOwnerUid

    /** The user registered a moving cart on this phone */
    val hasCart: Boolean get() = shopId.let { it != null && it > 0 } && ownerUid != null

    var isOn by mutableStateOf(settings.isVendorModeOn)
        private set

    fun set(on: Boolean) {
        if (!hasCart) return
        isOn = on
        settings.isVendorModeOn = on
        if (on) VendorTrackingService.start(context, shopId!!, ownerUid!!) else VendorTrackingService.stop(context)
    }

    /** Restarts sharing if it was on (e.g. after the app was restarted) */
    fun restore() {
        if (isOn && hasCart && !VendorLocationManager.getInstance(context).isActive()) {
            VendorTrackingService.start(context, shopId!!, ownerUid!!)
        }
    }
}

/** Vendor mode's row in the side menu: a saffron on/off switch, with why it's unavailable or that it's on */
@Composable
fun VendorModeItem(vendorMode: VendorMode) {
    LaunchedEffect(Unit) { vendorMode.restore() }
    SheetToggleRow(
        icon = Icons.Rounded.Explore,
        label = stringResource(R.string.vendor_mode),
        checked = vendorMode.isOn,
        onCheckedChange = vendorMode::set,
        enabled = vendorMode.hasCart,
        subtitle = when {
            !vendorMode.hasCart -> stringResource(R.string.vendor_mode_no_shop)
            vendorMode.isOn -> stringResource(R.string.vendor_mode_active)
            else -> null
        },
    )
}

/** Vendor mode's card on the profile */
@Composable
fun VendorModeProfileCard(vendorMode: VendorMode) {
    VendorModeCard(
        checked = vendorMode.isOn,
        onCheckedChange = vendorMode::set,
        enabled = vendorMode.hasCart,
        subtitle = when {
            !vendorMode.hasCart -> stringResource(R.string.vendor_mode_no_shop)
            vendorMode.isOn -> stringResource(R.string.vendor_mode_active)
            else -> null
        },
    )
}

/** Over the home screen while vendor mode is on */
@Composable
fun VendorModeBanner(vendorMode: VendorMode, modifier: Modifier = Modifier) {
    if (!vendorMode.isOn || !vendorMode.hasCart) return
    Surface(color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.95f), modifier = modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.vendor_mode_active),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}

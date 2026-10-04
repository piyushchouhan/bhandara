package com.example.bhandara.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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

/**
 * Vendor mode (Android only for now): a moving-cart owner shares the cart's live location. Shown in the side
 * menu; [VendorModeBanner] reminds them while it's on.
 */
class VendorMode(private val context: Context) {

    private val settings = AppSettings(context)
    private val shopId = settings.vendorShopId
    private val ownerUid = settings.vendorOwnerUid

    /** The user registered a moving cart on this phone */
    val hasCart: Boolean = shopId != null && shopId > 0 && ownerUid != null

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

@Composable
fun VendorModeItem(vendorMode: VendorMode) {
    LaunchedEffect(Unit) { vendorMode.restore() }
    Column {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Explore,
                contentDescription = null,
                tint = if (vendorMode.isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.vendor_mode), style = MaterialTheme.typography.bodyLarge)
                if (!vendorMode.hasCart) {
                    Text(
                        stringResource(R.string.vendor_mode_no_shop),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Switch(
                checked = vendorMode.isOn,
                onCheckedChange = vendorMode::set,
                enabled = vendorMode.hasCart,
                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
            )
        }
        if (vendorMode.isOn && vendorMode.hasCart) {
            Text(
                stringResource(R.string.vendor_mode_active),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 64.dp, end = 24.dp, bottom = 8.dp),
            )
        }
    }
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

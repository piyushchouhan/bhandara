package com.example.bhandara.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bhandara.data.live.LiveCartFeed
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.live_connecting
import com.example.bhandara.shared.resources.live_speed
import com.example.bhandara.shared.resources.live_tracking
import com.example.bhandara.shared.resources.live_view_details
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.map.CameraTarget
import com.example.bhandara.ui.map.MapLook
import com.example.bhandara.ui.map.MapPin
import com.example.bhandara.ui.map.PlatformMap
import com.example.bhandara.ui.shopdetails.FeastSheet
import com.example.bhandara.ui.theme.feastColors
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * A moving cart, live: its spot on a small map follows the vendor as they move, with how fast they're going. Opens
 * when a live cart is tapped on the map; its connection closes with the sheet. (No design yet; drawn in the sheet
 * style of the redesign.)
 */
@Composable
internal fun LiveCartSheet(cart: LocalShopResponse, onDismiss: () -> Unit, onViewDetails: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val dependencies = LocalAppDependencies.current
    val feed = remember { LiveCartFeed.connectingTo(dependencies.apiClient.baseUrl) { dependencies.auth.idToken() } }
    var position by remember { mutableStateOf(GeoLocation(cart.latitude, cart.longitude)) }
    var camera by remember { mutableStateOf(CameraTarget(position, ZOOM)) }
    var speedMetersPerSecond by remember { mutableStateOf(0.0) }
    var connected by remember { mutableStateOf(false) }

    LaunchedEffect(cart.id) {
        val shopId = cart.id.toLongOrNull() ?: return@LaunchedEffect
        feed.positions(shopId, onConnected = { connected = it }).collect { update ->
            position = GeoLocation(update.lat, update.lng)
            camera = CameraTarget(position, ZOOM)
            speedMetersPerSecond = update.speed
        }
    }

    FeastSheet(onDismiss) { close ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(44.dp).background(colors.saffron, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.LocalShipping, contentDescription = null, tint = colors.onSaffron, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(cart.shopName, color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (connected) {
                        Box(Modifier.size(7.dp).background(colors.saffron, CircleShape))
                    } else {
                        CircularProgressIndicator(color = colors.saffronText, strokeWidth = 2.dp, modifier = Modifier.size(12.dp))
                    }
                    Text(
                        stringResource(if (connected) Res.string.live_tracking else Res.string.live_connecting),
                        color = colors.saffronText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        Box(Modifier.padding(top = 14.dp).fillMaxWidth().height(240.dp).clip(RoundedCornerShape(20.dp))) {
            PlatformMap(
                camera = camera,
                pins = listOf(MapPin(id = cart.id, position = position, label = cart.shopName, emoji = "🛺", isLiveCart = true, selected = true)),
                showMyLocation = true,
                darkMap = colors.isDark,
                onPinClick = {},
                look = MapLook.FEAST,
                modifier = Modifier.fillMaxWidth().height(240.dp),
            )
        }
        if (speedMetersPerSecond > 0.3) {
            Row(
                Modifier.padding(top = 12.dp).fillMaxWidth().background(colors.saffronSoft, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Rounded.Speed, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(20.dp))
                Text(stringResource(Res.string.live_speed, kmPerHour(speedMetersPerSecond)), color = colors.ink, fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium)
            }
        }
        FeastButton(stringResource(Res.string.live_view_details), onClick = { close(onViewDetails) }, modifier = Modifier.padding(top = 16.dp).fillMaxWidth())
    }
}

private const val ZOOM = 16f

/** "4.5" for 1.25 m/s */
internal fun kmPerHour(metersPerSecond: Double): String {
    val tenths = (metersPerSecond * 36).roundToInt()
    return "${tenths / 10}.${tenths % 10}"
}

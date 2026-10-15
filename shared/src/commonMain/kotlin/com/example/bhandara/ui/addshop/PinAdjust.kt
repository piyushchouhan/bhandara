package com.example.bhandara.ui.addshop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.SatelliteAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationResult
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.rememberDeviceLocation
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.map_view
import com.example.bhandara.shared.resources.move_map_pin
import com.example.bhandara.shared.resources.my_location
import com.example.bhandara.shared.resources.pin_adjust_hint
import com.example.bhandara.shared.resources.pin_adjust_title
import com.example.bhandara.shared.resources.satellite_view
import com.example.bhandara.shared.resources.use_this_spot
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.map.CameraTarget
import com.example.bhandara.ui.map.MapLook
import com.example.bhandara.ui.map.PlatformMap
import com.example.bhandara.ui.theme.feastColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The shop's pin on a full-screen map (1g / 2g, from "Adjust pin" on Details): the owner moves the map until the pin
 * sits on the shop, on the drawn map or on satellite photos, then keeps that spot.
 *
 * @param start the pin already put; null starts where the phone is
 */
@Composable
internal fun PinAdjustScreen(start: GeoLocation?, onPicked: (GeoLocation) -> Unit, onClose: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val deviceLocation = rememberDeviceLocation()
    var here by remember { mutableStateOf<GeoLocation?>(null) }
    var camera by remember { mutableStateOf(start?.let { CameraTarget(it, zoom = PIN_ZOOM) }) }
    // Where the pin's tip is now: the map's middle, each time it stops
    var spot by remember { mutableStateOf(start) }
    var satellite by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        here = (LocationService(deviceLocation).locate() as? LocationResult.Found)?.location
        if (camera == null) here?.let { camera = CameraTarget(it, zoom = PIN_ZOOM) }
    }
    var sheetHeight by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(Modifier.fillMaxSize().background(colors.mapLand)) {
        camera?.let { target ->
            PlatformMap(
                camera = target,
                pins = emptyList(),
                showMyLocation = false,
                darkMap = colors.isDark,
                onPinClick = {},
                look = MapLook.FEAST,
                // The map's middle is the pin's tip: under the status bar and above the sheet
                contentPadding = PaddingValues(top = statusBar, bottom = with(density) { sheetHeight.toDp() }),
                satellite = satellite,
                onCameraIdle = { spot = it },
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), onClose)
                    Box(Modifier.weight(1f))
                    MapKindToggle(satellite) { satellite = it }
                }
                // The pin's tip marks the middle of the map
                Column(Modifier.align(Alignment.Center).padding(bottom = 80.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.height(30.dp).background(colors.indigo, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(Res.string.move_map_pin), color = colors.onIndigo, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                    }
                    Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = colors.saffron, modifier = Modifier.padding(top = 6.dp).size(44.dp))
                }
                here?.let { phone ->
                    SquareIconButton(
                        Icons.Outlined.MyLocation, stringResource(Res.string.my_location), { camera = CameraTarget(phone, zoom = PIN_ZOOM) },
                        Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    )
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .onSizeChanged { sheetHeight = it.height.toFloat() }
                    .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.15f))
                    .background(colors.surface, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.align(Alignment.CenterHorizontally).size(width = 40.dp, height = 5.dp).background(colors.border, RoundedCornerShape(3.dp)))
                Text(stringResource(Res.string.pin_adjust_title), color = colors.ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text(stringResource(Res.string.pin_adjust_hint), color = colors.muted, fontSize = 14.sp, lineHeight = 19.sp, style = MaterialTheme.typography.bodyMedium)
                FeastButton(
                    stringResource(Res.string.use_this_spot),
                    onClick = { spot?.let(onPicked) },
                    enabled = spot != null,
                    icon = Icons.Rounded.Check,
                    iconTint = colors.saffron,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Map | Satellite, the picked one in indigo */
@Composable
private fun MapKindToggle(satellite: Boolean, onChange: (Boolean) -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier
            .shadow(4.dp, RoundedCornerShape(14.dp), ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.15f))
            .background(colors.surface, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        MapKindOption(Icons.Outlined.Map, Res.string.map_view, selected = !satellite) { onChange(false) }
        MapKindOption(Icons.Outlined.SatelliteAlt, Res.string.satellite_view, selected = satellite) { onChange(true) }
    }
}

@Composable
private fun MapKindOption(icon: ImageVector, label: StringResource, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) colors.indigo else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) colors.saffron else colors.accent, modifier = Modifier.size(18.dp))
        Text(
            stringResource(label), color = if (selected) colors.onIndigo else colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/** Close enough to see which building is the shop */
private const val PIN_ZOOM = 18f

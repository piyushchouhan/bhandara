package com.example.bhandara.ui.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.CompositionLocalProvider
import com.example.bhandara.ui.theme.LocalUnscaledDensity
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIView

/** A pin as the iOS map draws it */
class IosMapPin(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val icon: UIImage,
    val anchorX: Double,
    val anchorY: Double,
)

/**
 * A shop zone's shape as the iOS map draws it: the outline's corners, and colours as 0xAARRGGBB with the stroke
 * width in points
 */
class IosMapZone(
    val id: String,
    val latitudes: List<Double>,
    val longitudes: List<Double>,
    val fillArgb: Long,
    val strokeArgb: Long,
    val strokeWidth: Double,
)

/** What the map tells the shared screen */
interface IosMapListener {
    fun onPinClick(pinId: String)
    fun onZoneClick(zoneId: String)
    fun onZoomChanged(zoom: Float)
}

/**
 * Google Maps on iOS, implemented in Swift with the Google Maps SDK (IosGoogleMapBridge.swift) and provided
 * through [LocalIosMapBridge]. [updateMapView] is called whenever the pins, camera or style change.
 */
interface IosMapBridge {
    fun createMapView(listener: IosMapListener): UIView

    /**
     * Moves the camera only when [cameraId] is new; adds, moves or removes markers to match [pins] and shapes to
     * match [zones]. The paddings (in points) are the parts covered by the screen's bars and sheets.
     */
    fun updateMapView(
        view: UIView,
        pins: List<IosMapPin>,
        latitude: Double,
        longitude: Double,
        zoom: Float,
        cameraId: Long,
        showMyLocation: Boolean,
        styleJson: String,
        topPadding: Double,
        bottomPadding: Double,
        zones: List<IosMapZone>,
    )
}

val LocalIosMapBridge = staticCompositionLocalOf<IosMapBridge?> { null }

@OptIn(ExperimentalComposeUiApi::class) // UIKitInteropInteractionMode
@Composable
actual fun PlatformMap(
    camera: CameraTarget,
    pins: List<MapPin>,
    showMyLocation: Boolean,
    darkMap: Boolean,
    onPinClick: (MapPin) -> Unit,
    modifier: Modifier,
    onZoomChanged: (Float) -> Unit,
    look: MapLook,
    contentPadding: PaddingValues,
    zones: List<MapZone>,
    onZoneClick: (zoneId: String) -> Unit,
) {
    val bridge = LocalIosMapBridge.current ?: error("IosMapBridge not provided (see MainViewController)")
    val scale = LocalDensity.current.density.toDouble()

    val iosPins = pins.map { pin ->
        key(pin.id) {
            val image = rememberPinImage(pin, darkMap, look)
            val icon = remember(image) { image.bitmap.toUIImage(scale) }
            IosMapPin(pin.id, pin.position.latitude, pin.position.longitude, icon, image.anchorX.toDouble(), image.anchorY.toDouble())
        }
    }

    val zoneColors = remember(darkMap) { zoneColors(darkMap) }
    val iosZones = zones.map { zone ->
        IosMapZone(
            zone.id,
            zone.outline.map { it.latitude },
            zone.outline.map { it.longitude },
            (if (zone.faint) zoneColors.faintFill else zoneColors.fill).toArgb().toUInt().toLong(),
            (if (zone.faint) zoneColors.faintStroke else zoneColors.stroke).toArgb().toUInt().toLong(),
            ZONE_STROKE_WIDTH.value.toDouble(),
        )
    }

    val currentPins by rememberUpdatedState(pins)
    val currentOnPinClick by rememberUpdatedState(onPinClick)
    val currentOnZoomChanged by rememberUpdatedState(onZoomChanged)
    val currentOnZoneClick by rememberUpdatedState(onZoneClick)
    val listener = remember {
        object : IosMapListener {
            override fun onPinClick(pinId: String) {
                currentPins.firstOrNull { it.id == pinId }?.let(currentOnPinClick)
            }

            override fun onZoneClick(zoneId: String) = currentOnZoneClick(zoneId)

            override fun onZoomChanged(zoom: Float) = currentOnZoomChanged(zoom)
        }
    }

    // A redesigned screen is scaled to the design's width (FeastTheme); the map view is sized in iOS points, so it
    // gets the phone's own density, and the padding is turned into points
    val unscaled = LocalUnscaledDensity.current ?: LocalDensity.current
    val toPoints = (LocalDensity.current.density / unscaled.density).toDouble()
    CompositionLocalProvider(LocalDensity provides unscaled) {
        UIKitView(
            factory = { bridge.createMapView(listener) },
            modifier = modifier,
            update = { view ->
                bridge.updateMapView(
                    view, iosPins, camera.center.latitude, camera.center.longitude, camera.zoom, camera.id,
                    showMyLocation, MapStyles.forLook(look, darkMap),
                    contentPadding.calculateTopPadding().value * toPoints, contentPadding.calculateBottomPadding().value * toPoints,
                    iosZones,
                )
            },
            // Pans and pinches go straight to the map
            properties = UIKitInteropProperties(interactionMode = UIKitInteropInteractionMode.NonCooperative),
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun ImageBitmap.toUIImage(scale: Double): UIImage {
    val png = Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)!!.bytes
    val data = png.usePinned { NSData.create(bytes = it.addressOf(0), length = png.size.toULong()) }
    return UIImage.imageWithData(data, scale)!!
}

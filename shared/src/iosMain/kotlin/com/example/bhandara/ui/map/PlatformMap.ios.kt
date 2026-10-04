package com.example.bhandara.ui.map

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
import androidx.compose.ui.platform.LocalDensity
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

/** What the map tells the shared screen */
interface IosMapListener {
    fun onPinClick(pinId: String)
    fun onZoomChanged(zoom: Float)
}

/**
 * Google Maps on iOS, implemented in Swift with the Google Maps SDK (IosGoogleMapBridge.swift) and provided
 * through [LocalIosMapBridge]. [updateMapView] is called whenever the pins, camera or style change.
 */
interface IosMapBridge {
    fun createMapView(listener: IosMapListener): UIView

    /** Moves the camera only when [cameraId] is new; adds, moves or removes markers to match [pins] */
    fun updateMapView(
        view: UIView,
        pins: List<IosMapPin>,
        latitude: Double,
        longitude: Double,
        zoom: Float,
        cameraId: Long,
        showMyLocation: Boolean,
        styleJson: String,
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
) {
    val bridge = LocalIosMapBridge.current ?: error("IosMapBridge not provided (see MainViewController)")
    val scale = LocalDensity.current.density.toDouble()

    val iosPins = pins.map { pin ->
        key(pin.id) {
            val image = rememberPinImage(pin, darkMap)
            val icon = remember(image) { image.bitmap.toUIImage(scale) }
            IosMapPin(pin.id, pin.position.latitude, pin.position.longitude, icon, image.anchorX.toDouble(), image.anchorY.toDouble())
        }
    }

    val currentPins by rememberUpdatedState(pins)
    val currentOnPinClick by rememberUpdatedState(onPinClick)
    val currentOnZoomChanged by rememberUpdatedState(onZoomChanged)
    val listener = remember {
        object : IosMapListener {
            override fun onPinClick(pinId: String) {
                currentPins.firstOrNull { it.id == pinId }?.let(currentOnPinClick)
            }

            override fun onZoomChanged(zoom: Float) = currentOnZoomChanged(zoom)
        }
    }

    UIKitView(
        factory = { bridge.createMapView(listener) },
        modifier = modifier,
        update = { view ->
            bridge.updateMapView(
                view, iosPins, camera.center.latitude, camera.center.longitude, camera.zoom, camera.id,
                showMyLocation, if (darkMap) MapStyles.DARK else MapStyles.LIGHT,
            )
        },
        // Pans and pinches go straight to the map
        properties = UIKitInteropProperties(interactionMode = UIKitInteropInteractionMode.NonCooperative),
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun ImageBitmap.toUIImage(scale: Double): UIImage {
    val png = Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)!!.bytes
    val data = png.usePinned { NSData.create(bytes = it.addressOf(0), length = png.size.toULong()) }
    return UIImage.imageWithData(data, scale)!!
}

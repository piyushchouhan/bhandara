package com.example.bhandara.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.max

/** A pin's picture, and the point in it (0..1 of its width and height) that sits on the place's location */
class PinImage(val bitmap: ImageBitmap, val anchorX: Float, val anchorY: Float)

/**
 * The pin for [pin], drawn once in shared code so it looks the same on Android and iOS: the emoji, with the
 * place's name in a rounded label underneath (shortened with "…" when long), so places can be told apart without
 * tapping. Light map: a charcoal label with white text. Dark map: a white label with dark text. Live carts are
 * green in both.
 */
@Composable
fun rememberPinImage(pin: MapPin, darkMap: Boolean): PinImage {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(pin.emoji, pin.label, pin.isLiveCart, darkMap, density) {
        drawPin(measurer, density, pin.emoji, pin.label, pin.isLiveCart, darkMap)
    }
}

private fun drawPin(
    measurer: TextMeasurer,
    density: Density,
    emoji: String,
    label: String,
    isLiveCart: Boolean,
    darkMap: Boolean,
): PinImage = with(density) {
    val fill = when {
        isLiveCart -> Color(0xFF2E7D32)
        darkMap -> Color.White
        else -> Color(0xFF424242)
    }
    val textColor = if (darkMap && !isLiveCart) Color(0xFF212121) else Color.White

    val emojiSize = 28.dp.toPx()
    val gap = 1.dp.toPx()
    val padH = 6.dp.toPx()
    val padV = 2.dp.toPx()
    val shadow = 2.dp.toPx()
    val maxLabelWidth = 120.dp.toPx()

    val emojiText = measurer.measure(emoji, TextStyle(fontSize = (emojiSize * 0.85f).toSp()))
    val labelText = measurer.measure(
        label,
        TextStyle(fontSize = 11.dp.toSp(), fontWeight = FontWeight.Bold, color = textColor),
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        constraints = Constraints(maxWidth = maxLabelWidth.toInt()),
    )

    val labelWidth = labelText.size.width + padH * 2
    val labelHeight = labelText.size.height + padV * 2
    val width = ceil(max(emojiSize, labelWidth) + shadow * 2).toInt()
    val height = ceil(emojiSize + gap + labelHeight + shadow * 2).toInt()
    val centerX = width / 2f
    val emojiCenterY = shadow + emojiSize / 2f

    val bitmap = ImageBitmap(width, height)
    CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(bitmap), Size(width.toFloat(), height.toFloat())) {
        drawText(
            emojiText,
            topLeft = Offset(centerX - emojiText.size.width / 2f, emojiCenterY - emojiText.size.height / 2f),
        )
        // Name label: a rounded pill with a soft shadow, readable over streets and the map's own labels
        val top = shadow + emojiSize + gap
        val pillTopLeft = Offset(centerX - labelWidth / 2f, top)
        val radius = CornerRadius(labelHeight / 2f)
        drawRoundRect(
            Color.Black.copy(alpha = if (darkMap) 0.35f else 0.25f),
            topLeft = pillTopLeft + Offset(0f, 0.5.dp.toPx()),
            size = Size(labelWidth, labelHeight),
            cornerRadius = radius,
        )
        drawRoundRect(fill, topLeft = pillTopLeft, size = Size(labelWidth, labelHeight), cornerRadius = radius)
        if (!darkMap) {
            drawRoundRect(fill, pillTopLeft, Size(labelWidth, labelHeight), radius, style = Stroke(1.25.dp.toPx()))
        }
        drawText(labelText, topLeft = Offset(centerX - labelText.size.width / 2f, top + padV))
    }
    PinImage(bitmap, anchorX = 0.5f, anchorY = emojiCenterY / height)
}

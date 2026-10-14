package com.example.bhandara.ui.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.zone_shops_count
import com.example.bhandara.ui.theme.FeastColors
import org.jetbrains.compose.resources.stringResource
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sqrt

/** A pin's picture, and the point in it (0..1 of its width and height) that sits on the place's location */
class PinImage(val bitmap: ImageBitmap, val anchorX: Float, val anchorY: Float)

/**
 * The pin for [pin], drawn once in shared code so it looks the same on Android and iOS: the emoji, with the
 * place's name in a rounded label underneath (shortened with "…" when long), so places can be told apart without
 * tapping. Light map: a charcoal label with white text. Dark map: a white label with dark text. Live carts are
 * green in both.
 */
@Composable
fun rememberPinImage(pin: MapPin, darkMap: Boolean, look: MapLook = MapLook.CLASSIC): PinImage {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val cartIcon = rememberVectorPainter(Icons.Rounded.LocalShipping)
    val shopIcon = rememberVectorPainter(Icons.Rounded.Restaurant)
    val feastIcon = rememberVectorPainter(Icons.Rounded.VolunteerActivism)
    val zoneCountText = stringResource(Res.string.zone_shops_count, pin.zoneCount ?: 0)
    return remember(
        pin.emoji, pin.label, pin.isLiveCart, pin.selected, pin.dimmed, pin.feastPill, pin.feastLive, pin.zoneCount,
        zoneCountText, darkMap, look, density,
    ) {
        val pill = pin.feastPill
        when {
            pin.zoneCount != null -> drawZoneFlag(
                measurer, density, pin.emoji, pin.label, pin.zoneCount, zoneCountText, pin.dimmed, FeastColors.of(darkMap),
            )
            look == MapLook.CLASSIC -> drawPin(measurer, density, pin.emoji, pin.label, pin.isLiveCart, darkMap)
            pill != null -> drawFeastPill(measurer, density, feastIcon, pill, pin.feastLive, FeastColors.of(darkMap))
            else -> drawFeastPin(
                measurer, density, if (pin.isLiveCart) cartIcon else shopIcon, pin.label, pin.isLiveCart,
                pin.selected, pin.dimmed, FeastColors.of(darkMap),
            )
        }
    }
}

/**
 * The redesign's pin: a shop is an indigo drop pointing at its spot; a live cart is a saffron tile with a truck
 * and a soft saffron halo. The name sits underneath in a small label, as on the classic pins. The tapped shop
 * becomes a bigger saffron drop with a plate icon, and the other pins fade.
 */
private fun drawFeastPin(
    measurer: TextMeasurer,
    density: Density,
    icon: VectorPainter,
    label: String,
    isLiveCart: Boolean,
    selected: Boolean,
    dimmed: Boolean,
    colors: FeastColors,
): PinImage = with(density) {
    val ring = if (colors.isDark) colors.surface else Color.White
    val fade = if (dimmed) 0.55f else 1f
    val markSize = when {
        isLiveCart -> 36.dp.toPx()
        selected -> 48.dp.toPx()
        else -> 30.dp.toPx()
    }
    val halo = if (isLiveCart) 8.dp.toPx() else 0f
    // A drop is a square with three round corners, turned so the sharp one points down; its tip is this far below
    // the square's centre
    val dropTip = markSize / sqrt(2f)
    val markHeight = if (isLiveCart) markSize else markSize / 2f + dropTip
    val gap = 3.dp.toPx()
    val padH = 7.dp.toPx()
    val padV = 2.dp.toPx()
    val shadow = if (selected) 6.dp.toPx() else 2.dp.toPx()
    val maxLabelWidth = 120.dp.toPx()

    val labelText = measurer.measure(
        label,
        TextStyle(fontSize = 11.dp.toSp(), fontWeight = FontWeight.SemiBold, color = colors.ink.copy(alpha = fade)),
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        constraints = Constraints(maxWidth = maxLabelWidth.toInt()),
    )
    // A pin without a name (e.g. "the feast goes here") has no label
    val hasLabel = label.isNotBlank()
    val labelWidth = if (hasLabel) labelText.size.width + padH * 2 else 0f
    val labelHeight = if (hasLabel) labelText.size.height + padV * 2 else 0f
    val markWidth = markSize + halo * 2
    val width = ceil(max(markWidth, labelWidth) + shadow * 2).toInt()
    val top = shadow + halo
    val height = ceil(top + markHeight + gap + labelHeight + shadow * 2).toInt()
    val centerX = width / 2f
    val markCenterY = top + markSize / 2f

    val bitmap = ImageBitmap(width, height)
    CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(bitmap), Size(width.toFloat(), height.toFloat())) {
        val center = Offset(centerX, markCenterY)
        if (isLiveCart) {
            val corner = CornerRadius(12.dp.toPx())
            drawRoundRect(
                colors.saffron.copy(alpha = 0.25f),
                topLeft = center - Offset(markSize / 2f + halo, markSize / 2f + halo),
                size = Size(markSize + halo * 2, markSize + halo * 2),
                cornerRadius = CornerRadius(corner.x + halo),
            )
            drawRoundRect(ring, topLeft = center - Offset(markSize / 2f, markSize / 2f), size = Size(markSize, markSize), cornerRadius = corner)
            val inset = 3.dp.toPx()
            drawRoundRect(
                colors.saffron,
                topLeft = center - Offset(markSize / 2f - inset, markSize / 2f - inset),
                size = Size(markSize - inset * 2, markSize - inset * 2),
                cornerRadius = CornerRadius(corner.x - inset),
            )
            drawIcon(icon, center, 18.dp.toPx(), colors.onSaffron)
        } else if (selected) {
            // A soft shadow a little below, so the tapped pin stands up off the map
            val shadowCenter = center + Offset(0f, 3.dp.toPx())
            rotate(-45f, pivot = shadowCenter) { drawDrop(shadowCenter, markSize, Color.Black.copy(alpha = 0.2f)) }
            rotate(-45f, pivot = center) {
                drawDrop(center, markSize, ring)
                drawDrop(center, markSize - 6.dp.toPx(), colors.saffron)
            }
            drawIcon(icon, center, 22.dp.toPx(), colors.onSaffron)
        } else {
            rotate(-45f, pivot = center) {
                drawDrop(center, markSize, ring.copy(alpha = fade))
                drawDrop(center, markSize - 4.dp.toPx(), colors.indigo.copy(alpha = fade))
            }
        }

        // Name label under the mark
        if (!hasLabel) return@draw
        val labelTop = top + markHeight + gap
        val pillTopLeft = Offset(centerX - labelWidth / 2f, labelTop)
        val radius = CornerRadius(labelHeight / 2f)
        drawRoundRect(
            Color.Black.copy(alpha = (if (colors.isDark) 0.35f else 0.18f) * fade),
            topLeft = pillTopLeft + Offset(0f, 0.75.dp.toPx()),
            size = Size(labelWidth, labelHeight),
            cornerRadius = radius,
        )
        drawRoundRect(colors.surface.copy(alpha = fade), topLeft = pillTopLeft, size = Size(labelWidth, labelHeight), cornerRadius = radius)
        drawText(labelText, topLeft = Offset(centerX - labelText.size.width / 2f, labelTop + padV))
    }
    // A shop's pin stands on its tip; a cart's tile is centred on the cart
    val anchorY = if (isLiveCart) markCenterY else markCenterY + dropTip
    PinImage(bitmap, anchorX = 0.5f, anchorY = anchorY / height)
}

/**
 * A community feast in the redesign's look: a pill with an icon and when it starts ("Now", "6 PM"), centred on
 * the spot. On now it's saffron with a white ring; coming up it's white.
 */
private fun drawFeastPill(
    measurer: TextMeasurer,
    density: Density,
    icon: VectorPainter,
    text: String,
    live: Boolean,
    colors: FeastColors,
): PinImage = with(density) {
    val ring = if (colors.isDark) colors.surface else Color.White
    val height = if (live) 36.dp.toPx() else 34.dp.toPx()
    val circle = if (live) 26.dp.toPx() else 24.dp.toPx()
    val inset = (height - circle) / 2f
    val gap = 6.dp.toPx()
    val endPad = 12.dp.toPx()
    val shadow = 5.dp.toPx()
    val label = measurer.measure(
        text,
        TextStyle(fontSize = 13.dp.toSp(), fontWeight = FontWeight.SemiBold, color = if (live) colors.onSaffron else colors.ink),
        maxLines = 1,
    )
    val pillWidth = inset + circle + gap + label.size.width + endPad
    val width = ceil(pillWidth + shadow * 2).toInt()
    val fullHeight = ceil(height + shadow * 2).toInt()
    val bitmap = ImageBitmap(width, fullHeight)
    CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(bitmap), Size(width.toFloat(), fullHeight.toFloat())) {
        val topLeft = Offset(shadow, shadow)
        val radius = CornerRadius(height / 2f)
        drawRoundRect(Color.Black.copy(alpha = 0.2f), topLeft = topLeft + Offset(0f, 2.dp.toPx()), size = Size(pillWidth, height), cornerRadius = radius)
        if (live) {
            // The white ring around a live pill
            val r = 3.dp.toPx()
            drawRoundRect(ring, topLeft = topLeft, size = Size(pillWidth, height), cornerRadius = radius)
            drawRoundRect(colors.saffron, topLeft = topLeft + Offset(r, r), size = Size(pillWidth - r * 2, height - r * 2), cornerRadius = CornerRadius(height / 2f - r))
        } else {
            drawRoundRect(colors.surface, topLeft = topLeft, size = Size(pillWidth, height), cornerRadius = radius)
        }
        val circleCenter = topLeft + Offset(inset + circle / 2f, height / 2f)
        drawCircle(if (live) colors.indigo else colors.saffronSoft, radius = circle / 2f, center = circleCenter)
        drawIcon(icon, circleCenter, if (live) 15.dp.toPx() else 14.dp.toPx(), if (live) colors.saffron else colors.saffronText)
        drawText(label, topLeft = topLeft + Offset(inset + circle + gap, (height - label.size.height) / 2f))
    }
    PinImage(bitmap, anchorX = 0.5f, anchorY = 0.5f)
}

/**
 * A shop zone's marker: a small flag on a pole planted at the zone's middle, with the zone's emoji and how many
 * shops it holds ("🍢 6"), and its name underneath in the same small label as a shop's pin ("6 shops" while the
 * zone has no name).
 */
private fun drawZoneFlag(
    measurer: TextMeasurer,
    density: Density,
    emoji: String,
    name: String,
    count: Int,
    countText: String,
    dimmed: Boolean,
    colors: FeastColors,
): PinImage = with(density) {
    val fade = if (dimmed) 0.55f else 1f
    val ring = if (colors.isDark) colors.surface else Color.White
    val poleWidth = 2.5.dp.toPx()
    val poleHeight = 42.dp.toPx()
    val foot = 3.dp.toPx()
    val flagHeight = 22.dp.toPx()
    val flagPadH = 6.dp.toPx()
    val notch = 6.dp.toPx()
    val gap = 3.dp.toPx()
    val padH = 7.dp.toPx()
    val padV = 2.dp.toPx()
    val shadow = 2.dp.toPx()

    val flagText = measurer.measure(
        buildAnnotatedString {
            append(emoji)
            append(" ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.onIndigo.copy(alpha = fade))) {
                append(count.toString())
            }
        },
        TextStyle(fontSize = 12.dp.toSp()),
        maxLines = 1,
    )
    val labelText = measurer.measure(
        name.ifBlank { countText },
        TextStyle(fontSize = 11.dp.toSp(), fontWeight = FontWeight.SemiBold, color = colors.ink.copy(alpha = fade)),
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        constraints = Constraints(maxWidth = 120.dp.toPx().toInt()),
    )
    val flagWidth = flagPadH + flagText.size.width + flagPadH + notch
    val labelWidth = labelText.size.width + padH * 2
    val labelHeight = labelText.size.height + padV * 2

    // The pole stands on the zone's middle and the name is centred under it; the flag flies to the right
    val left = max(labelWidth / 2f, foot) + shadow
    val right = max(labelWidth / 2f, poleWidth / 2f + flagWidth) + shadow
    val width = ceil(left + right).toInt()
    val footY = shadow + poleHeight
    val height = ceil(footY + foot + gap + labelHeight + shadow).toInt()
    val poleX = left

    val bitmap = ImageBitmap(width, height)
    CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(bitmap), Size(width.toFloat(), height.toFloat())) {
        val top = shadow
        // Pole, with a dot where it meets the ground
        drawRect(colors.ink.copy(alpha = fade), topLeft = Offset(poleX - poleWidth / 2f, top), size = Size(poleWidth, poleHeight))
        drawCircle(ring.copy(alpha = fade), radius = foot + 1.dp.toPx(), center = Offset(poleX, footY))
        drawCircle(colors.ink.copy(alpha = fade), radius = foot, center = Offset(poleX, footY))

        // Flag: a rectangle with a notch cut into its flying end
        val x0 = poleX + poleWidth / 2f
        val x1 = x0 + flagWidth
        val flag = Path().apply {
            moveTo(x0, top)
            lineTo(x1, top)
            lineTo(x1 - notch, top + flagHeight / 2f)
            lineTo(x1, top + flagHeight)
            lineTo(x0, top + flagHeight)
            close()
        }
        translate(0f, 1.dp.toPx()) { drawPath(flag, Color.Black.copy(alpha = 0.18f * fade)) }
        drawPath(flag, colors.indigo.copy(alpha = fade))
        drawText(flagText, topLeft = Offset(x0 + flagPadH, top + (flagHeight - flagText.size.height) / 2f))

        // Name under the pole
        val labelTop = footY + foot + gap
        val pillTopLeft = Offset(poleX - labelWidth / 2f, labelTop)
        val radius = CornerRadius(labelHeight / 2f)
        drawRoundRect(
            Color.Black.copy(alpha = (if (colors.isDark) 0.35f else 0.18f) * fade),
            topLeft = pillTopLeft + Offset(0f, 0.75.dp.toPx()),
            size = Size(labelWidth, labelHeight),
            cornerRadius = radius,
        )
        drawRoundRect(colors.surface.copy(alpha = fade), topLeft = pillTopLeft, size = Size(labelWidth, labelHeight), cornerRadius = radius)
        drawText(labelText, topLeft = Offset(poleX - labelText.size.width / 2f, labelTop + padV))
    }
    PinImage(bitmap, anchorX = poleX / width, anchorY = footY / height)
}

/** A drop shape of [size] around [center], sharp corner at the bottom left (it's drawn turned by -45°) */
private fun DrawScope.drawDrop(center: Offset, size: Float, color: Color) {
    val r = CornerRadius(size / 2f)
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                left = center.x - size / 2f, top = center.y - size / 2f,
                right = center.x + size / 2f, bottom = center.y + size / 2f,
                topLeftCornerRadius = r, topRightCornerRadius = r,
                bottomRightCornerRadius = r, bottomLeftCornerRadius = CornerRadius.Zero,
            ),
        )
    }
    drawPath(path, color)
}

private fun DrawScope.drawIcon(icon: VectorPainter, center: Offset, size: Float, tint: Color) {
    translate(center.x - size / 2f, center.y - size / 2f) {
        with(icon) { draw(Size(size, size), colorFilter = ColorFilter.tint(tint)) }
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

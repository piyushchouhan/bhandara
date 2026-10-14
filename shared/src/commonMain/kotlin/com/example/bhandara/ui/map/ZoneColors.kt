package com.example.bhandara.ui.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bhandara.ui.theme.FeastColors

/** How a shop zone's shape is painted, the same on Android and iOS */
class ZoneColors(val fill: Color, val stroke: Color, val faintFill: Color, val faintStroke: Color)

/** A soft indigo patch with a clear edge; faint once the shops' own pins are shown */
fun zoneColors(darkMap: Boolean): ZoneColors {
    val indigo = FeastColors.of(darkMap).indigo
    return ZoneColors(
        fill = indigo.copy(alpha = 0.14f),
        stroke = indigo.copy(alpha = 0.7f),
        faintFill = indigo.copy(alpha = 0.05f),
        faintStroke = indigo.copy(alpha = 0.3f),
    )
}

val ZONE_STROKE_WIDTH = 2.dp

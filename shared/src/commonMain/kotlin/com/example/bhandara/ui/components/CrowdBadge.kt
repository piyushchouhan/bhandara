package com.example.bhandara.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.crowd_busy
import com.example.bhandara.shared.resources.crowd_moderate
import com.example.bhandara.shared.resources.crowd_quiet
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// "How busy is it now" for a shop, from the backend's crowdLevel: QUIET, MODERATE or BUSY.

/** "Quiet", "Moderately busy" or "Busy now"; null when the level is unknown (e.g. moving carts) */
fun crowdLabel(level: String?): StringResource? = when (level) {
    "QUIET" -> Res.string.crowd_quiet
    "MODERATE" -> Res.string.crowd_moderate
    "BUSY" -> Res.string.crowd_busy
    else -> null
}

fun crowdColor(level: String?): Color = when (level) {
    "BUSY" -> Color(0xFFC62828)
    "MODERATE" -> Color(0xFFEF6C00)
    else -> Color(0xFF2E7D32)
}

/** Small "● Busy now" badge; shows nothing when the level is unknown */
@Composable
fun CrowdBadge(level: String?, modifier: Modifier = Modifier) {
    val label = crowdLabel(level) ?: return
    val color = crowdColor(level)
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Text(stringResource(label), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = color)
    }
}

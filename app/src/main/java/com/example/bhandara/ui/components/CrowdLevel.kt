package com.example.bhandara.ui.components

import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bhandara.R

// "How busy is it now" for a shop, from the backend's crowdLevel: QUIET, MODERATE or BUSY.

@StringRes
fun crowdLabelRes(level: String?): Int? = when (level) {
    "QUIET" -> R.string.crowd_quiet
    "MODERATE" -> R.string.crowd_moderate
    "BUSY" -> R.string.crowd_busy
    else -> null
}

fun crowdColor(level: String?): Color = when (level) {
    "BUSY" -> Color(0xFFC62828)
    "MODERATE" -> Color(0xFFEF6C00)
    else -> Color(0xFF2E7D32)
}

/** Small "● Busy now" badge; shows nothing when the level is unknown (e.g. moving carts) */
@Composable
fun CrowdBadge(level: String?, modifier: Modifier = Modifier) {
    val label = crowdLabelRes(level) ?: return
    val color = crowdColor(level)
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

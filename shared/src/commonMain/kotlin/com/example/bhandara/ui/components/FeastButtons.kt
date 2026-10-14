package com.example.bhandara.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bhandara.ui.theme.feastColors

// Buttons of the redesign (use inside FeastTheme)

/** How a [FeastButton] looks: indigo for the main action, lavender for the other, red to destroy, saffron to create */
enum class FeastButtonStyle { PRIMARY, SOFT, DANGER, SAFFRON }

/**
 * A big rounded button. When not [enabled] it greys out (e.g. "Report" until a reason is picked); [dimmed] greys it
 * out but keeps it tappable (e.g. "Next" on an unfinished step, which then says what's missing).
 *
 * @param trailingIcon after the label, e.g. the arrow on "Next"
 * @param iconTint the leading icon's colour when it differs from the label's (e.g. a saffron tick on indigo)
 * @param content replaces the label, e.g. a spinner with "Checking your claim…"
 */
@Composable
fun FeastButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: FeastButtonStyle = FeastButtonStyle.PRIMARY,
    enabled: Boolean = true,
    dimmed: Boolean = false,
    icon: ImageVector? = null,
    height: Dp = 54.dp,
    trailingIcon: ImageVector? = null,
    iconTint: Color? = null,
    content: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.feastColors
    val (fill, text) = when {
        !enabled || dimmed -> colors.border to colors.disabledText
        style == FeastButtonStyle.PRIMARY -> colors.indigo to colors.onIndigo
        style == FeastButtonStyle.SOFT -> colors.lavender to colors.ink
        style == FeastButtonStyle.DANGER -> colors.danger to Color.White
        else -> colors.saffron to colors.onSaffron
    }
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier
            .height(height)
            .clip(shape)
            .background(fill, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        if (content != null) {
            content()
        } else {
            icon?.let { Icon(it, contentDescription = null, tint = iconTint.takeIf { enabled && !dimmed } ?: text, modifier = Modifier.size(20.dp)) }
            Text(
                label, color = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge,
            )
            trailingIcon?.let { Icon(it, contentDescription = null, tint = text, modifier = Modifier.size(22.dp)) }
        }
    }
}

/** A 44dp rounded white button with an icon, like the redesign's back button */
@Composable
fun SquareIconButton(icon: ImageVector, description: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier
            .size(44.dp)
            .shadow(2.dp, shape, ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.2f))
            .background(colors.surface, shape)
            .clip(shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = colors.ink.copy(alpha = if (enabled) 1f else 0.4f), modifier = Modifier.size(22.dp))
    }
}

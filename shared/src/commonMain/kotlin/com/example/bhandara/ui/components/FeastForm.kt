package com.example.bhandara.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bhandara.ui.theme.feastColors

// Form pieces of the redesign's multi-step forms (posting a feast, adding a shop); use inside FeastTheme

/** Bars and labels for the steps: done steps indigo with a tick, the current one saffron, the rest grey */
@Composable
fun FeastStepper(labels: List<String>, current: Int, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.feastColors
    // Five steps (a cart) get a tighter row, as in the design
    val tight = labels.size > 4
    Column(modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(if (tight) 5.dp else 6.dp)) {
            labels.indices.forEach { i ->
                val fill = when {
                    i < current -> colors.indigo
                    i == current -> colors.saffron
                    else -> colors.border
                }
                Box(Modifier.weight(1f).height(5.dp).background(fill, RoundedCornerShape(3.dp)))
            }
        }
        Row(Modifier.padding(top = 8.dp)) {
            labels.forEachIndexed { i, label ->
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (i < current) Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.muted, modifier = Modifier.size(if (tight) 13.dp else 14.dp))
                    Text(
                        label, color = if (i == current) colors.ink else colors.muted, fontSize = if (tight) 12.sp else 13.sp,
                        fontWeight = if (i == current) FontWeight.SemiBold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

/** A field's title above it, e.g. "Kind of shop" */
@Composable
fun FeastSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text, color = MaterialTheme.feastColors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.titleSmall, modifier = modifier.padding(bottom = 10.dp),
    )
}

/** An outlined field on white with an optional leading icon, outlined in the accent while typing; the hint shows only while it's empty */
@Composable
fun FeastFormField(
    value: String,
    hint: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    height: Dp = 54.dp,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    textSize: Int = 16,
    horizontalPadding: Dp = 16.dp,
    iconTint: Color? = null,
) {
    val colors = MaterialTheme.feastColors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val style = MaterialTheme.typography.bodyLarge.copy(fontSize = textSize.sp, lineHeight = (textSize + 6).sp, color = colors.ink)
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier
            .fillMaxWidth()
            .height(height)
            .background(colors.surface, shape)
            .border(if (focused) 2.dp else 1.5.dp, if (focused) colors.accent else colors.border, shape)
            .padding(horizontal = horizontalPadding, vertical = if (singleLine) 0.dp else 14.dp),
        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        icon?.let { Icon(it, contentDescription = null, tint = iconTint ?: colors.accent, modifier = Modifier.size(if (height >= 56.dp) 22.dp else 20.dp)) }
        Column(Modifier.weight(1f).then(if (singleLine) Modifier else Modifier.fillMaxSize())) {
            BasicTextField(
                value = value,
                onValueChange = onChange,
                enabled = enabled,
                singleLine = singleLine,
                textStyle = style,
                cursorBrush = SolidColor(colors.ink),
                interactionSource = interaction,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth().then(if (singleLine) Modifier else Modifier.fillMaxSize()),
                decorationBox = { field ->
                    Box(contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart) {
                        if (value.isEmpty()) Text(hint, style = style.copy(color = colors.placeholder), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        field()
                    }
                },
            )
        }
    }
}

/**
 * Items typed into one field, each turning into a saffron chip (a comma or Done adds it), e.g. dishes served
 *
 * @param outline the field's outline; saffron where the design shows it picked out
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeastChipsField(
    items: List<String>,
    input: String,
    hint: String,
    onInputChange: (String) -> Unit,
    onDone: () -> Unit,
    onRemove: (String) -> Unit,
    enabled: Boolean = true,
    outline: Color = MaterialTheme.feastColors.border,
) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(16.dp)
    FlowRow(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).background(colors.surface, shape).border(1.5.dp, outline, shape).padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items.forEach { item ->
            Row(
                Modifier.height(32.dp).background(colors.saffronSoft, RoundedCornerShape(10.dp)).padding(start = 12.dp, end = 4.dp).align(Alignment.CenterVertically),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(item, color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Box(Modifier.size(24.dp).clip(CircleShape).clickable(enabled = enabled) { onRemove(item) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Close, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(16.dp))
                }
            }
        }
        val style = MaterialTheme.typography.bodyLarge.copy(fontSize = if (items.isEmpty()) 16.sp else 15.sp, color = colors.ink)
        BasicTextField(
            value = input,
            onValueChange = onInputChange,
            enabled = enabled,
            singleLine = true,
            textStyle = style,
            cursorBrush = SolidColor(colors.ink),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            modifier = Modifier.widthIn(min = 160.dp).height(if (items.isEmpty()) 40.dp else 32.dp).padding(start = 6.dp).align(Alignment.CenterVertically),
            decorationBox = { field ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (input.isEmpty()) Text(hint, style = style.copy(color = colors.placeholder), maxLines = 1)
                    field()
                }
            },
        )
    }
}

/** The Indian veg / non-veg mark: a square outline with a dot */
@Composable
fun FoodMark(color: Color, modifier: Modifier = Modifier, dot: Color = color, dotSize: Dp = 4.dp) {
    Box(modifier.border(1.5.dp, color), contentAlignment = Alignment.Center) {
        Box(Modifier.size(dotSize).background(dot, CircleShape))
    }
}

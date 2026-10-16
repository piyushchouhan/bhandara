package com.example.bhandara.ui.shopdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.DoorFront
import androidx.compose.material.icons.rounded.GppBad
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.NoFood
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.WrongLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import com.example.bhandara.shared.resources.deactivate_instead
import com.example.bhandara.shared.resources.delete
import com.example.bhandara.shared.resources.delete_lose_menu
import com.example.bhandara.shared.resources.delete_lose_photos
import com.example.bhandara.shared.resources.delete_lose_reviews
import com.example.bhandara.shared.resources.delete_shop_text
import com.example.bhandara.shared.resources.delete_shop_title
import com.example.bhandara.shared.resources.type_name_confirm
import com.example.bhandara.data.models.api.ShopReportReason
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.cancel
import com.example.bhandara.shared.resources.report_prompt
import com.example.bhandara.shared.resources.report_reason_closed
import com.example.bhandara.shared.resources.report_reason_fssai
import com.example.bhandara.shared.resources.report_reason_inappropriate
import com.example.bhandara.shared.resources.report_reason_not_food_shop
import com.example.bhandara.shared.resources.report_reason_wrong_location
import com.example.bhandara.shared.resources.report_submit
import com.example.bhandara.shared.resources.report_title
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.ReportDetailsFields
import com.example.bhandara.photos.PickedPhoto
import androidx.compose.runtime.remember
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.theme.feastColors
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// The shop page's bottom sheets (design 2b / 2c): reporting, and the destructive confirmations

/** A bottom sheet in the redesign's look: white, round top corners, a small handle, the page dimmed behind */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeastSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.(close: (then: () -> Unit) -> Unit) -> Unit) {
    val colors = MaterialTheme.feastColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    // Slides the sheet away first, then does what was asked
    val close: (() -> Unit) -> Unit = { then ->
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            onDismiss()
            then()
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        containerColor = colors.surface,
        contentColor = colors.ink,
        scrimColor = colors.sheetScrim,
        dragHandle = {
            Box(Modifier.padding(top = 10.dp).size(width = 38.dp, height = 5.dp).background(colors.border, RoundedCornerShape(3.dp)))
        },
    ) {
        // Scrolls when a tall sheet (e.g. a report with a note) doesn't fit above the keyboard
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 16.dp)) {
            content(close)
        }
    }
}

/**
 * "Report shop": big reason rows to pick one, then an optional note and photos ("What's wrong?"); Report unlocks
 * once a reason is picked
 */
@Composable
internal fun ReportShopSheet(
    onReport: (ShopReportReason, note: String, photos: List<PickedPhoto>) -> Unit,
    onDismiss: () -> Unit,
    hasFssai: Boolean = false,
    initial: ShopReportReason? = null,
) {
    val colors = MaterialTheme.feastColors
    var reason by rememberSaveable { mutableStateOf(initial) }
    var note by rememberSaveable { mutableStateOf("") }
    var photos by remember { mutableStateOf(emptyList<PickedPhoto>()) }
    FeastSheet(onDismiss) { close ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(44.dp).background(colors.lavender, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Flag, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(stringResource(Res.string.report_title), color = colors.ink, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
                Text(stringResource(Res.string.report_prompt), color = colors.muted, fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Column(Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ShopReportReason.entries.filter { hasFssai || it != ShopReportReason.WRONG_FSSAI_LICENCE }.forEach { option ->
                ReasonRow(option.icon, stringResource(option.label), selected = option == reason) { reason = option }
            }
        }
        ReportDetailsFields(
            note = note,
            onNoteChange = { note = it },
            photos = photos,
            onAddPhotos = { photos = photos + it },
            onRemovePhoto = { photos = photos - it },
            showPhotos = true,
            modifier = Modifier.padding(top = 16.dp),
        )
        Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FeastButton(stringResource(Res.string.cancel), onClick = { close {} }, style = FeastButtonStyle.SOFT, modifier = Modifier.weight(1f))
            FeastButton(
                stringResource(Res.string.report_submit),
                onClick = { reason?.let { picked -> close { onReport(picked, note, photos) } } },
                enabled = reason != null,
                modifier = Modifier.weight(1.3f),
            )
        }
    }
}

@Composable
internal fun ReasonRow(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(if (selected) colors.lavenderSoft else colors.surface)
            .border(if (selected) 2.dp else 1.5.dp, if (selected) colors.selectedOutline else colors.border, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) colors.accent else colors.muted, modifier = Modifier.size(22.dp))
        Text(
            label, color = colors.ink, fontSize = 16.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f),
        )
        // The radio: a thick ring when picked, a thin one when not
        Box(Modifier.size(20.dp).border(if (selected) 6.dp else 2.dp, if (selected) colors.selectedOutline else colors.starOff, CircleShape))
    }
}

private val ShopReportReason.label: StringResource
    get() = when (this) {
        ShopReportReason.NOT_FOOD_SHOP -> Res.string.report_reason_not_food_shop
        ShopReportReason.WRONG_LOCATION -> Res.string.report_reason_wrong_location
        ShopReportReason.INAPPROPRIATE_CONTENT -> Res.string.report_reason_inappropriate
        ShopReportReason.PERMANENTLY_CLOSED -> Res.string.report_reason_closed
        ShopReportReason.WRONG_FSSAI_LICENCE -> Res.string.report_reason_fssai
    }

private val ShopReportReason.icon: ImageVector
    get() = when (this) {
        ShopReportReason.NOT_FOOD_SHOP -> Icons.Rounded.NoFood
        ShopReportReason.WRONG_LOCATION -> Icons.Rounded.WrongLocation
        ShopReportReason.INAPPROPRIATE_CONTENT -> Icons.Rounded.Block
        ShopReportReason.PERMANENTLY_CLOSED -> Icons.Rounded.DoorFront
        ShopReportReason.WRONG_FSSAI_LICENCE -> Icons.Rounded.GppBad
    }

/**
 * A calm confirmation for a destructive action (suggest deleting; for the owner, deactivating): the explanation
 * is the reassurance, red only on the action. Shows which shop it's about.
 */
@Composable
internal fun ConfirmShopSheet(
    icon: ImageVector,
    title: StringResource,
    text: StringResource,
    confirm: StringResource,
    shopName: String,
    photoUrl: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    FeastSheet(onDismiss) { close ->
        Box(Modifier.size(64.dp).background(colors.dangerSoft, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = colors.danger, modifier = Modifier.size(32.dp))
        }
        Text(
            stringResource(title), color = colors.ink, fontSize = 23.sp, fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            stringResource(text), color = colors.body, fontSize = 16.sp, lineHeight = 23.sp,
            style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 6.dp),
        )
        Row(
            Modifier.padding(top = 16.dp).fillMaxWidth().background(colors.lavenderSoft, RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(colors.lavender), contentAlignment = Alignment.Center) {
                if (photoUrl != null) {
                    AsyncImage(photoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Rounded.Storefront, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
                }
            }
            Text(shopName, color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
        }
        Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FeastButton(stringResource(Res.string.cancel), onClick = { close {} }, style = FeastButtonStyle.SOFT, modifier = Modifier.weight(1f))
            FeastButton(stringResource(confirm), onClick = { close(onConfirm) }, style = FeastButtonStyle.DANGER, modifier = Modifier.weight(1.3f))
        }
    }
}

/**
 * Deleting a shop for good (design 1c): what goes with it, Deactivate as the softer way out, and Delete only once
 * the shop's name is typed
 *
 * @param onDeactivate null when the shop is already deactivated
 */
@Composable
internal fun DeleteShopSheet(shopName: String, onDeactivate: (() -> Unit)?, onDelete: () -> Unit, onDismiss: () -> Unit) {
    val colors = MaterialTheme.feastColors
    var typed by rememberSaveable { mutableStateOf("") }
    val confirmed = typed.trim().equals(shopName.trim(), ignoreCase = true)
    FeastSheet(onDismiss) { close ->
        Box(Modifier.size(60.dp).background(colors.dangerSoft, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = colors.danger, modifier = Modifier.size(30.dp))
        }
        Text(
            stringResource(Res.string.delete_shop_title), color = colors.ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 14.dp),
        )
        Text(
            stringResource(Res.string.delete_shop_text, shopName), color = colors.body, fontSize = 15.sp, lineHeight = 22.sp,
            style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp),
        )
        Column(
            Modifier.padding(top = 14.dp).fillMaxWidth().background(colors.dangerSoft, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(Res.string.delete_lose_menu, Res.string.delete_lose_photos, Res.string.delete_lose_reviews).forEach {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = null, tint = colors.dangerText, modifier = Modifier.size(18.dp))
                    Text(stringResource(it), color = colors.dangerText, fontSize = 14.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Text(
            stringResource(Res.string.type_name_confirm), color = colors.muted, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
        )
        val style = TextStyle(color = colors.ink, fontSize = 15.sp, fontFamily = MaterialTheme.typography.bodyLarge.fontFamily)
        BasicTextField(
            value = typed,
            onValueChange = { typed = it },
            singleLine = true,
            textStyle = style,
            cursorBrush = SolidColor(colors.ink),
            modifier = Modifier.fillMaxWidth().height(50.dp).border(1.5.dp, if (confirmed) colors.danger else colors.border, RoundedCornerShape(14.dp)),
            decorationBox = { field ->
                Box(Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
                    if (typed.isEmpty()) Text(shopName, style = style.copy(color = colors.placeholder), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    field()
                }
            },
        )
        Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val shape = RoundedCornerShape(18.dp)
            Box(
                Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(shape)
                    .background(colors.saffronSoft, shape)
                    .then(if (onDeactivate != null) Modifier.selectable(selected = false, role = Role.Button, onClick = { close(onDeactivate) }) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(Res.string.deactivate_instead), color = colors.saffronText.copy(alpha = if (onDeactivate != null) 1f else 0.4f),
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, style = MaterialTheme.typography.labelLarge,
                )
            }
            Box(
                Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(shape)
                    .background(if (confirmed) colors.danger else colors.dangerDisabled, shape)
                    .selectable(selected = false, enabled = confirmed, role = Role.Button, onClick = { close(onDelete) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(Res.string.delete), color = Color.White,
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

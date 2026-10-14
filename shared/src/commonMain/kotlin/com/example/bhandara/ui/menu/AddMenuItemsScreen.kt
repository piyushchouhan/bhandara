package com.example.bhandara.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.data.models.api.MenuItemRequest
import com.example.bhandara.data.network.MenuApi
import com.example.bhandara.photos.rememberPhotoPicker
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.add_another_item
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.cancel
import com.example.bhandara.shared.resources.clear_menu
import com.example.bhandara.shared.resources.clear_menu_text
import com.example.bhandara.shared.resources.clear_menu_title
import com.example.bhandara.shared.resources.food_egg
import com.example.bhandara.shared.resources.item_name
import com.example.bhandara.shared.resources.item_price
import com.example.bhandara.shared.resources.menu_items_title
import com.example.bhandara.shared.resources.menu_non_veg
import com.example.bhandara.shared.resources.menu_veg
import com.example.bhandara.shared.resources.photo_camera
import com.example.bhandara.shared.resources.photo_gallery
import com.example.bhandara.shared.resources.remove_item
import com.example.bhandara.shared.resources.review_remove_photo
import com.example.bhandara.shared.resources.save_and_back
import com.example.bhandara.shared.resources.save_menu
import com.example.bhandara.shared.resources.scan_found
import com.example.bhandara.shared.resources.scan_intro
import com.example.bhandara.shared.resources.scan_read
import com.example.bhandara.shared.resources.scan_reading
import com.example.bhandara.shared.resources.tab_scan_menu
import com.example.bhandara.shared.resources.tab_type_items
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastFormField
import com.example.bhandara.ui.components.FoodMark
import com.example.bhandara.ui.components.ScreenViewModels
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val foodTypeLabels: Map<String, StringResource> = mapOf(
    "VEG" to Res.string.menu_veg,
    "NON_VEG" to Res.string.menu_non_veg,
    "EGG" to Res.string.food_egg,
)

/**
 * Adding items to a menu (design "Add Shop Flow" 1e / 1f), shared by Android and iOS: typed in, or read from photos of the menu by AI and then
 * checked. With a [shopId] the items are saved to that (the user's own) shop; without, it's the menu of a shop
 * being added, handed back through [onSaved] as a draft. Starts fresh every time it opens.
 *
 * @param existingNames items already on the menu, which can't be added again
 * @param onClearDraft  for a draft: clears the saved draft too (shows "Clear menu")
 */
@Composable
fun AddMenuItemsScreen(
    shopId: String?,
    existingNames: List<String>,
    onSaved: (List<MenuItemRequest>) -> Unit,
    onBack: () -> Unit,
    initialItems: List<MenuItemRequest> = emptyList(),
    onClearDraft: (() -> Unit)? = null,
) = ScreenViewModels {
    val dependencies = LocalAppDependencies.current
    val viewModel = viewModel { AddMenuItemsViewModel(shopId, existingNames, initialItems, MenuApi(dependencies.apiClient)) }
    FeastTheme { AddMenuItemsPage(viewModel, isDraft = shopId == null, onSaved = onSaved, onBack = onBack, onClearDraft = onClearDraft) }
}

@Composable
private fun AddMenuItemsPage(
    viewModel: AddMenuItemsViewModel,
    isDraft: Boolean,
    onSaved: (List<MenuItemRequest>) -> Unit,
    onBack: () -> Unit,
    onClearDraft: (() -> Unit)?,
) {
    val colors = MaterialTheme.feastColors
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var confirmingClear by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.savedItems) { state.savedItems?.let(onSaved) }
    // What AI read is shown in the list to check
    LaunchedEffect(state.scanFound) { if (state.scanFound != null) tab = 0 }

    Column(Modifier.fillMaxSize().background(colors.page).windowInsetsPadding(WindowInsets.statusBars).imePadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), onBack)
            Text(
                stringResource(Res.string.menu_items_title), color = colors.ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, maxLines = 1,
                overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f),
            )
            if (onClearDraft != null) {
                Text(
                    stringResource(Res.string.clear_menu), color = colors.saffronText.copy(alpha = if (state.busy) 0.4f else 1f), fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable(enabled = !state.busy) { confirmingClear = true }.padding(horizontal = 6.dp, vertical = 8.dp),
                )
            }
        }
        Row(Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp).fillMaxWidth().background(colors.lavender, RoundedCornerShape(16.dp)).padding(4.dp)) {
            TabButton(Icons.Outlined.EditNote, Res.string.tab_type_items, selected = tab == 0) { tab = 0 }
            TabButton(Icons.Outlined.DocumentScanner, Res.string.tab_scan_menu, selected = tab == 1) { tab = 1 }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (tab == 0) TypedItems(state, viewModel) else ScanMenu(state, viewModel)
        }
        Column(
            Modifier
                .fillMaxWidth()
                .shadow(12.dp, ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.12f))
                .background(colors.surface)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            state.error?.let { Text(it, color = colors.danger, fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium) }
            FeastButton(
                stringResource(if (isDraft) Res.string.save_and_back else Res.string.save_menu),
                onClick = viewModel::save,
                enabled = !state.busy,
                icon = Icons.Rounded.Check,
                iconTint = colors.saffron,
                height = 56.dp,
                modifier = Modifier.fillMaxWidth(),
                content = if (state.saving) {
                    { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.onIndigo) }
                } else null,
            )
        }
    }

    if (confirmingClear) {
        AlertDialog(
            onDismissRequest = { confirmingClear = false },
            title = { Text(stringResource(Res.string.clear_menu_title)) },
            text = { Text(stringResource(Res.string.clear_menu_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingClear = false
                        viewModel.clearAll()
                        onClearDraft?.invoke()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(Res.string.clear_menu)) }
            },
            dismissButton = { TextButton(onClick = { confirmingClear = false }) { Text(stringResource(Res.string.cancel)) } },
        )
    }
}

/** One half of the "Type items / Scan menu photo" switch: indigo with a saffron icon when picked */
@Composable
private fun RowScope.TabButton(icon: ImageVector, label: StringResource, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp)).background(if (selected) colors.indigo else Color.Transparent).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) colors.saffron else colors.accent, modifier = Modifier.size(20.dp))
        Text(
            stringResource(label), color = if (selected) colors.onIndigo else colors.accent, fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/** A dashed outline drawn around a box, e.g. "Add another item" */
private fun Modifier.dashedOutline(color: Color, radius: Float) = drawBehind {
    drawRoundRect(
        color, cornerRadius = CornerRadius(radius * density),
        style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))),
    )
}

@Composable
private fun TypedItems(state: AddMenuItemsState, viewModel: AddMenuItemsViewModel) {
    val colors = MaterialTheme.feastColors
    state.scanFound?.let { found ->
        Row(
            Modifier.fillMaxWidth().background(colors.saffronSoft, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(20.dp))
            Text(stringResource(Res.string.scan_found, found), color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
        }
    }
    state.rows.forEach { row -> ItemCard(row, enabled = !state.busy, viewModel) }
    Row(
        Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .dashedOutline(colors.starOff, 18f)
            .clickable(enabled = !state.busy, onClick = viewModel::addRow),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
        Text(stringResource(Res.string.add_another_item), color = colors.accent, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
    }
}

/** An item: name, price and a remove button, then Veg / Non-veg / Egg */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemCard(row: MenuRow, enabled: Boolean, viewModel: AddMenuItemsViewModel) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(22.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.08f))
            .background(colors.surface, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeastFormField(
                row.name, stringResource(Res.string.item_name), { viewModel.onNameChange(row.id, it) },
                height = 50.dp, enabled = enabled, horizontalPadding = 14.dp, modifier = Modifier.weight(1f),
            )
            FeastFormField(
                row.price, stringResource(Res.string.item_price), { viewModel.onPriceChange(row.id, it) },
                height = 50.dp, enabled = enabled, keyboardType = KeyboardType.Decimal, horizontalPadding = 12.dp, modifier = Modifier.width(96.dp),
            )
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(colors.dangerSoft).clickable(enabled = enabled) { viewModel.removeRow(row.id) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Close, stringResource(Res.string.remove_item), tint = colors.danger, modifier = Modifier.size(20.dp))
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PICKABLE_FOOD_TYPES.forEach { type ->
                FoodTypeChip(type, selected = row.foodType == type, enabled = enabled) { viewModel.onFoodTypeChange(row.id, type) }
            }
        }
    }
}

/** Veg in green, non-veg in red, egg in amber, each with its mark; filled in its colour when picked */
@Composable
private fun FoodTypeChip(type: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val (mark, dot, soft, text) = when (type) {
        "NON_VEG" -> listOf(colors.danger, colors.danger, colors.dangerSoft, colors.danger)
        "EGG" -> listOf(colors.saffronText, colors.saffron, colors.saffronSoft, colors.saffronText)
        else -> listOf(colors.success, colors.success, colors.successSoft, colors.success)
    }
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .height(40.dp)
            .clip(shape)
            .background(if (selected) soft else Color.Transparent)
            .border(1.5.dp, if (selected) mark else colors.border, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FoodMark(mark, Modifier.size(12.dp), dot = dot, dotSize = 5.dp)
        Text(
            stringResource(foodTypeLabels.getValue(type)), color = if (selected) text else colors.ink, fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, style = MaterialTheme.typography.labelLarge,
        )
    }
}

/** Photos of the menu for AI to read: a saffron drop area with Camera and Gallery, then "Read the menu" */
@Composable
private fun ScanMenu(state: AddMenuItemsState, viewModel: AddMenuItemsViewModel) {
    val colors = MaterialTheme.feastColors
    val picker = rememberPhotoPicker(viewModel::addMenuPhotos)
    Text(stringResource(Res.string.scan_intro), color = colors.body, fontSize = 16.sp, lineHeight = 23.sp, style = MaterialTheme.typography.bodyLarge)
    if (state.menuPhotos.isNotEmpty()) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            state.menuPhotos.forEach { photo ->
                Box(Modifier.size(92.dp).clip(RoundedCornerShape(18.dp)).background(colors.lavender)) {
                    AsyncImage(photo.preview, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(92.dp))
                    Box(
                        Modifier.align(Alignment.TopEnd).padding(5.dp).size(24.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.55f))
                            .clickable(enabled = !state.busy) { viewModel.removeMenuPhoto(photo) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Close, stringResource(Res.string.review_remove_photo), tint = Color.White, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
    if (state.canAddMenuPhotos) {
        val left = MAX_MENU_PHOTOS - state.menuPhotos.size
        Column(
            Modifier
                .fillMaxWidth()
                .height(if (state.menuPhotos.isEmpty()) 210.dp else 160.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.saffronSoft)
                .dashedOutline(colors.claimBorder, 24f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            Box(Modifier.size(64.dp).background(colors.saffron, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, tint = colors.onSaffron, modifier = Modifier.size(32.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SourceButton(Icons.Outlined.PhotoCamera, Res.string.photo_camera, enabled = !state.busy) { picker.takePhoto() }
                SourceButton(Icons.Outlined.PhotoLibrary, Res.string.photo_gallery, enabled = !state.busy) { picker.chooseFromGallery(left) }
            }
        }
    }
    FeastButton(
        stringResource(if (state.scanning) Res.string.scan_reading else Res.string.scan_read),
        onClick = viewModel::scan,
        enabled = state.menuPhotos.isNotEmpty() && !state.busy,
        icon = Icons.Outlined.AutoAwesome,
        iconTint = colors.saffron.takeIf { state.menuPhotos.isNotEmpty() && !state.busy },
        modifier = Modifier.fillMaxWidth(),
        content = if (state.scanning) {
            {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = colors.disabledText)
                Text(stringResource(Res.string.scan_reading), color = colors.disabledText, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
            }
        } else null,
    )
}

@Composable
private fun SourceButton(icon: ImageVector, label: StringResource, enabled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.height(40.dp).clip(RoundedCornerShape(12.dp)).background(colors.surface).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(18.dp))
        Text(stringResource(label), color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
    }
}

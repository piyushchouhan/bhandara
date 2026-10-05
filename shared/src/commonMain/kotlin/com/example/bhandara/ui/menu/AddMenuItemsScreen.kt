package com.example.bhandara.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.data.models.api.MenuItemRequest
import com.example.bhandara.data.network.MenuApi
import com.example.bhandara.photos.PhotoSourceSheet
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
import com.example.bhandara.shared.resources.remove_item
import com.example.bhandara.shared.resources.review_add_photo
import com.example.bhandara.shared.resources.review_remove_photo
import com.example.bhandara.shared.resources.save_and_back
import com.example.bhandara.shared.resources.save_menu
import com.example.bhandara.shared.resources.scan_found
import com.example.bhandara.shared.resources.scan_intro
import com.example.bhandara.shared.resources.scan_read
import com.example.bhandara.shared.resources.scan_reading
import com.example.bhandara.shared.resources.tab_scan_menu
import com.example.bhandara.shared.resources.tab_type_items
import com.example.bhandara.ui.components.ScreenViewModels
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val foodTypeLabels: Map<String, StringResource> = mapOf(
    "VEG" to Res.string.menu_veg,
    "NON_VEG" to Res.string.menu_non_veg,
    "EGG" to Res.string.food_egg,
)

/**
 * Adding items to a menu, shared by Android and iOS: typed in, or read from photos of the menu by AI and then
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
    AddMenuItemsPage(viewModel, isDraft = shopId == null, onSaved = onSaved, onBack = onBack, onClearDraft = onClearDraft)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMenuItemsPage(
    viewModel: AddMenuItemsViewModel,
    isDraft: Boolean,
    onSaved: (List<MenuItemRequest>) -> Unit,
    onBack: () -> Unit,
    onClearDraft: (() -> Unit)?,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var confirmingClear by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.savedItems) { state.savedItems?.let(onSaved) }
    // What AI read is shown in the list to check
    LaunchedEffect(state.scanFound) { if (state.scanFound != null) tab = 0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.menu_items_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back)) }
                },
                actions = {
                    if (onClearDraft != null) {
                        TextButton(
                            onClick = { confirmingClear = true },
                            enabled = !state.busy,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        ) { Text(stringResource(Res.string.clear_menu)) }
                    }
                },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, tonalElevation = 3.dp) {
                Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(onClick = viewModel::save, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                        if (state.saving) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(stringResource(if (isDraft) Res.string.save_and_back else Res.string.save_menu))
                        }
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(Res.string.tab_type_items)) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(Res.string.tab_scan_menu)) })
            }
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (tab == 0) TypedItems(state, viewModel) else ScanMenu(state, viewModel)
            }
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

@Composable
private fun TypedItems(state: AddMenuItemsState, viewModel: AddMenuItemsViewModel) {
    state.scanFound?.let { found ->
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.scan_found, found), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
    state.rows.forEach { row -> ItemCard(row, enabled = !state.busy, viewModel) }
    OutlinedButton(onClick = viewModel::addRow, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(Res.string.add_another_item))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemCard(row: MenuRow, enabled: Boolean, viewModel: AddMenuItemsViewModel) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = row.name,
                    onValueChange = { viewModel.onNameChange(row.id, it) },
                    label = { Text(stringResource(Res.string.item_name)) },
                    singleLine = true,
                    enabled = enabled,
                    modifier = Modifier.weight(1.6f),
                )
                OutlinedTextField(
                    value = row.price,
                    onValueChange = { viewModel.onPriceChange(row.id, it) },
                    label = { Text(stringResource(Res.string.item_price)) },
                    singleLine = true,
                    enabled = enabled,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { viewModel.removeRow(row.id) }, enabled = enabled) {
                    Icon(Icons.Default.Close, stringResource(Res.string.remove_item), tint = MaterialTheme.colorScheme.error)
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PICKABLE_FOOD_TYPES.forEach { type ->
                    FilterChip(
                        selected = row.foodType == type,
                        onClick = { viewModel.onFoodTypeChange(row.id, type) },
                        enabled = enabled,
                        label = { Text(stringResource(foodTypeLabels.getValue(type))) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ScanMenu(state: AddMenuItemsState, viewModel: AddMenuItemsViewModel) {
    var picking by remember { mutableStateOf(false) }
    Text(stringResource(Res.string.scan_intro), color = MaterialTheme.colorScheme.onSurfaceVariant)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        state.menuPhotos.take(3).forEach { photo ->
            Box {
                AsyncImage(
                    model = photo.preview,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp)),
                )
                IconButton(
                    onClick = { viewModel.removeMenuPhoto(photo) },
                    enabled = !state.busy,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape),
                ) {
                    Icon(Icons.Default.Close, stringResource(Res.string.review_remove_photo), tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        if (state.menuPhotos.size > 3) {
            Box(Modifier.size(96.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant), Alignment.Center) {
                Text("+${state.menuPhotos.size - 3}", style = MaterialTheme.typography.titleMedium)
            }
        }
        if (state.canAddMenuPhotos) {
            OutlinedButton(
                onClick = { picking = true },
                enabled = !state.busy,
                modifier = Modifier.size(96.dp),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(Icons.Default.AddPhotoAlternate, stringResource(Res.string.review_add_photo), modifier = Modifier.size(32.dp))
            }
        }
    }
    Button(onClick = viewModel::scan, enabled = state.menuPhotos.isNotEmpty() && !state.busy, modifier = Modifier.fillMaxWidth()) {
        if (state.scanning) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(Res.string.scan_reading))
        } else {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(Res.string.scan_read))
        }
    }
    if (picking) {
        PhotoSourceSheet(
            maxCount = MAX_MENU_PHOTOS - state.menuPhotos.size,
            onPicked = viewModel::addMenuPhotos,
            onDismiss = { picking = false },
        )
    }
}

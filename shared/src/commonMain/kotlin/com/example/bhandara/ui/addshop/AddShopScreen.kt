package com.example.bhandara.ui.addshop

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.rememberDeviceLocation
import com.example.bhandara.data.location.rememberLocationPermissionRequest
import com.example.bhandara.data.network.MenuApi
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.photos.PhotoSourceSheet
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.add_shop_button
import com.example.bhandara.shared.resources.add_shop_sign_in_reason
import com.example.bhandara.shared.resources.add_shop_title
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.cancel
import com.example.bhandara.shared.resources.clear_all
import com.example.bhandara.shared.resources.clear_all_title
import com.example.bhandara.shared.resources.detailed_menu_add
import com.example.bhandara.shared.resources.detailed_menu_count
import com.example.bhandara.shared.resources.detailed_menu_text
import com.example.bhandara.shared.resources.detailed_menu_title
import com.example.bhandara.shared.resources.edit
import com.example.bhandara.shared.resources.facility_home_delivery
import com.example.bhandara.shared.resources.facility_seating
import com.example.bhandara.shared.resources.facility_takeaway
import com.example.bhandara.shared.resources.facility_wifi
import com.example.bhandara.shared.resources.info_facilities
import com.example.bhandara.shared.resources.map_allow_location
import com.example.bhandara.shared.resources.menu_not_saved_text
import com.example.bhandara.shared.resources.menu_not_saved_title
import com.example.bhandara.shared.resources.next
import com.example.bhandara.shared.resources.ok
import com.example.bhandara.shared.resources.owner_confirm_message
import com.example.bhandara.shared.resources.owner_confirm_title
import com.example.bhandara.shared.resources.review_add_photo
import com.example.bhandara.shared.resources.review_remove_photo
import com.example.bhandara.shared.resources.review_uploading
import com.example.bhandara.shared.resources.section_description
import com.example.bhandara.shared.resources.shop_address
import com.example.bhandara.shared.resources.shop_cost_for_two
import com.example.bhandara.shared.resources.shop_cuisine
import com.example.bhandara.shared.resources.shop_dishes
import com.example.bhandara.shared.resources.shop_dishes_hint
import com.example.bhandara.shared.resources.shop_email
import com.example.bhandara.shared.resources.shop_kind_cart
import com.example.bhandara.shared.resources.shop_kind_cart_text
import com.example.bhandara.shared.resources.shop_kind_fixed
import com.example.bhandara.shared.resources.shop_kind_fixed_text
import com.example.bhandara.shared.resources.shop_kind_question
import com.example.bhandara.shared.resources.shop_landmark
import com.example.bhandara.shared.resources.shop_name
import com.example.bhandara.shared.resources.shop_phone
import com.example.bhandara.shared.resources.shop_photos_label
import com.example.bhandara.shared.resources.shop_place_info
import com.example.bhandara.shared.resources.shop_type
import com.example.bhandara.shared.resources.shop_type_bakery
import com.example.bhandara.shared.resources.shop_type_cafe
import com.example.bhandara.shared.resources.shop_type_fast_food
import com.example.bhandara.shared.resources.shop_type_food_truck
import com.example.bhandara.shared.resources.shop_type_restaurant
import com.example.bhandara.shared.resources.shop_type_street_food
import com.example.bhandara.shared.resources.shop_type_sweet_shop
import com.example.bhandara.shared.resources.step_basics
import com.example.bhandara.shared.resources.step_details
import com.example.bhandara.shared.resources.step_kind
import com.example.bhandara.shared.resources.step_menu
import com.example.bhandara.ui.menu.AddMenuItemsScreen
import com.example.bhandara.ui.profile.SignInGate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val ShopStep.label: StringResource
    get() = when (this) {
        ShopStep.KIND -> Res.string.step_kind
        ShopStep.BASICS -> Res.string.step_basics
        ShopStep.MENU -> Res.string.step_menu
        ShopStep.DETAILS -> Res.string.step_details
    }

private val shopTypeLabels: Map<String, StringResource> = mapOf(
    "Restaurant" to Res.string.shop_type_restaurant,
    "Food Truck" to Res.string.shop_type_food_truck,
    "Street Food" to Res.string.shop_type_street_food,
    "Cafe" to Res.string.shop_type_cafe,
    "Fast Food" to Res.string.shop_type_fast_food,
    "Bakery" to Res.string.shop_type_bakery,
    "Sweet Shop" to Res.string.shop_type_sweet_shop,
)

/**
 * Adding a shop, shared by Android and iOS. Only for users signed in with an account, who confirm they own the shop
 * (admins list shops for their owners and skip that). Once added, [onAdded] gets the new shop so the app can open it.
 */
@Composable
fun AddShopScreen(onBack: () -> Unit, onAdded: (shopId: String) -> Unit) {
    val dependencies = LocalAppDependencies.current
    SignInGate(reason = stringResource(Res.string.add_shop_sign_in_reason), onBack = onBack) {
        val account = dependencies.auth.account()
        val isAdmin = isAdminEmail(account?.email, dependencies.adminEmails)
        var confirmedOwner by rememberSaveable { mutableStateOf(isAdmin) }
        if (!confirmedOwner) {
            AlertDialog(
                onDismissRequest = onBack,
                title = { Text(stringResource(Res.string.owner_confirm_title)) },
                text = { Text(stringResource(Res.string.owner_confirm_message)) },
                confirmButton = { TextButton(onClick = { confirmedOwner = true }) { Text(stringResource(Res.string.ok)) } },
                dismissButton = { TextButton(onClick = onBack) { Text(stringResource(Res.string.cancel)) } },
            )
        }
        val deviceLocation = rememberDeviceLocation()
        val viewModel = viewModel {
            AddShopViewModel(
                ShopApi(dependencies.apiClient),
                MenuApi(dependencies.apiClient),
                dependencies.photoUploader::upload,
                LocationService(deviceLocation),
                dependencies.settings,
                { dependencies.auth.currentUid },
                account?.email,
            )
        }
        AddShopPage(viewModel, onBack, onAdded)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddShopPage(viewModel: AddShopViewModel, onBack: () -> Unit, onAdded: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmingClear by rememberSaveable { mutableStateOf(false) }
    var editingMenu by rememberSaveable { mutableStateOf(false) }
    val requestLocation = rememberLocationPermissionRequest { granted -> if (granted) viewModel.submit() }

    // Open the new shop; if its menu couldn't be saved, say so first
    val added = state.added
    LaunchedEffect(added) { if (added != null && added.menuSaved) onAdded(added.id) }
    if (added != null && !added.menuSaved) {
        AlertDialog(
            onDismissRequest = { onAdded(added.id) },
            title = { Text(stringResource(Res.string.menu_not_saved_title)) },
            text = { Text(stringResource(Res.string.menu_not_saved_text)) },
            confirmButton = { TextButton(onClick = { onAdded(added.id) }) { Text(stringResource(Res.string.ok)) } },
        )
    }

    if (editingMenu) {
        AddMenuItemsScreen(
            shopId = null,
            existingNames = emptyList(),
            initialItems = state.form.detailedMenu,
            onSaved = { items ->
                viewModel.setDetailedMenu(items)
                editingMenu = false
            },
            onBack = { editingMenu = false },
            onClearDraft = { viewModel.setDetailedMenu(emptyList()) },
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.add_shop_title)) },
                navigationIcon = {
                    IconButton(onClick = { if (!viewModel.back()) onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = { confirmingClear = true },
                        enabled = !state.submitting,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) { Text(stringResource(Res.string.clear_all)) }
                },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, tonalElevation = 3.dp) {
                Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.uploadProgress?.let { progress ->
                        LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth())
                        Text(stringResource(Res.string.review_uploading, "$progress%"), style = MaterialTheme.typography.bodySmall)
                    }
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    if (state.needsLocation) {
                        OutlinedButton(onClick = requestLocation, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(Res.string.map_allow_location))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (state.step != ShopStep.KIND) {
                            OutlinedButton(onClick = { viewModel.back() }, enabled = !state.submitting, modifier = Modifier.weight(1f)) {
                                Text(stringResource(Res.string.back))
                            }
                        }
                        if (state.isLastStep) {
                            Button(onClick = viewModel::submit, enabled = !state.submitting, modifier = Modifier.weight(1f)) {
                                if (state.submitting) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Text(stringResource(Res.string.add_shop_button))
                                }
                            }
                        } else {
                            val canGoOn = viewModel.canGoOn()
                            Button(
                                onClick = viewModel::next,
                                modifier = Modifier.weight(1f),
                                colors = if (canGoOn) ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            ) { Text(stringResource(Res.string.next)) }
                        }
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            StepIndicator(state.step)
            Spacer(Modifier.height(20.dp))
            when (state.step) {
                ShopStep.KIND -> KindStep(state.form.isMovingCart, viewModel::setMovingCart)
                ShopStep.BASICS -> BasicsStep(state, viewModel)
                ShopStep.MENU -> MenuStep(state.form, onEdit = { editingMenu = true })
                ShopStep.DETAILS -> DetailsStep(state.form, viewModel)
            }
        }
    }

    if (confirmingClear) {
        AlertDialog(
            onDismissRequest = { confirmingClear = false },
            title = { Text(stringResource(Res.string.clear_all_title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingClear = false
                        viewModel.clearAll()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(Res.string.clear_all)) }
            },
            dismissButton = { TextButton(onClick = { confirmingClear = false }) { Text(stringResource(Res.string.cancel)) } },
        )
    }
}

@Composable
private fun StepIndicator(step: ShopStep) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ShopStep.entries.forEach {
                Text(
                    stringResource(it.label),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (it == step) FontWeight.Bold else FontWeight.Normal,
                    color = if (it.ordinal <= step.ordinal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        LinearProgressIndicator(
            progress = { (step.ordinal + 1f) / ShopStep.entries.size },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)),
        )
    }
}

@Composable
private fun KindStep(isMovingCart: Boolean?, onChoose: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(Res.string.shop_kind_question), style = MaterialTheme.typography.titleMedium)
        KindCard(Icons.Default.Storefront, Res.string.shop_kind_fixed, Res.string.shop_kind_fixed_text, selected = isMovingCart == false) { onChoose(false) }
        KindCard(Icons.Default.LocalShipping, Res.string.shop_kind_cart, Res.string.shop_kind_cart_text, selected = isMovingCart == true) { onChoose(true) }
    }
}

@Composable
private fun KindCard(icon: ImageVector, title: StringResource, text: StringResource, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(stringResource(title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(stringResource(text), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BasicsStep(state: AddShopState, viewModel: AddShopViewModel) {
    val form = state.form
    var picking by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = form.shopName,
            onValueChange = viewModel::onShopNameChange,
            label = { Text(stringResource(Res.string.shop_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(stringResource(Res.string.shop_type), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SHOP_TYPES.forEach { type ->
                FilterChip(
                    selected = form.shopType == type,
                    onClick = { viewModel.onShopTypeChange(type) },
                    label = { Text(stringResource(shopTypeLabels.getValue(type))) },
                )
            }
        }
        Text(stringResource(Res.string.shop_dishes), style = MaterialTheme.typography.titleSmall)
        if (form.menuNames.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                form.menuNames.forEach { name ->
                    InputChip(
                        selected = false,
                        onClick = { viewModel.removeMenuName(name) },
                        label = { Text(name) },
                        trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(InputChipDefaults.IconSize)) },
                    )
                }
            }
        }
        OutlinedTextField(
            value = form.menuInput,
            onValueChange = viewModel::onMenuInputChange,
            placeholder = { Text(stringResource(Res.string.shop_dishes_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { viewModel.addMenuInput() }),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(stringResource(Res.string.shop_photos_label, MAX_SHOP_PHOTOS), style = MaterialTheme.typography.titleSmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(form.photos) { photo ->
                Box {
                    AsyncImage(
                        model = photo.preview,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp)),
                    )
                    IconButton(
                        onClick = { viewModel.removePhoto(photo) },
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape),
                    ) {
                        Icon(Icons.Default.Close, stringResource(Res.string.review_remove_photo), tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
            if (state.canAddPhotos) {
                item {
                    OutlinedButton(
                        onClick = { picking = true },
                        modifier = Modifier.size(96.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, stringResource(Res.string.review_add_photo), modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
    if (picking) {
        PhotoSourceSheet(
            maxCount = MAX_SHOP_PHOTOS - form.photos.size,
            onPicked = viewModel::addPhotos,
            onDismiss = { picking = false },
        )
    }
}

@Composable
private fun MenuStep(form: ShopForm, onEdit: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(Res.string.detailed_menu_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            if (form.detailedMenu.isEmpty()) {
                Text(stringResource(Res.string.detailed_menu_text), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = onEdit) { Text(stringResource(Res.string.detailed_menu_add)) }
            } else {
                Text(stringResource(Res.string.detailed_menu_count, form.detailedMenu.size), style = MaterialTheme.typography.bodyLarge)
                Text(form.detailedMenu.joinToString(", ") { it.name }, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
                OutlinedButton(onClick = onEdit) { Text(stringResource(Res.string.edit)) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsStep(form: ShopForm, viewModel: AddShopViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MyLocation, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(Res.string.shop_place_info), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Field(form.description, viewModel::onDescriptionChange, Res.string.section_description, minLines = 3)
        Field(form.phone, viewModel::onPhoneChange, Res.string.shop_phone, keyboard = KeyboardType.Phone)
        Field(form.email, viewModel::onEmailChange, Res.string.shop_email, keyboard = KeyboardType.Email)
        Field(form.address, viewModel::onAddressChange, Res.string.shop_address, minLines = 2)
        Field(form.landmark, viewModel::onLandmarkChange, Res.string.shop_landmark)
        Field(form.cuisine, viewModel::onCuisineChange, Res.string.shop_cuisine)
        Field(form.costForTwo, viewModel::onCostForTwoChange, Res.string.shop_cost_for_two, keyboard = KeyboardType.Number)
        Text(stringResource(Res.string.info_facilities), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(form.takeaway, { viewModel.setTakeaway(!form.takeaway) }, label = { Text(stringResource(Res.string.facility_takeaway)) })
            FilterChip(form.homeDelivery, { viewModel.setHomeDelivery(!form.homeDelivery) }, label = { Text(stringResource(Res.string.facility_home_delivery)) })
            FilterChip(form.hasSeating, { viewModel.setSeating(!form.hasSeating) }, label = { Text(stringResource(Res.string.facility_seating)) })
            FilterChip(form.wifi, { viewModel.setWifi(!form.wifi) }, label = { Text(stringResource(Res.string.facility_wifi)) })
        }
    }
}

@Composable
private fun Field(
    value: String,
    onChange: (String) -> Unit,
    label: StringResource,
    keyboard: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(label)) },
        singleLine = minLines == 1,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = Modifier.fillMaxWidth(),
    )
}

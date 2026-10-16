package com.example.bhandara.ui.addshop

import com.example.bhandara.data.network.FssaiApi
import com.example.bhandara.ui.fssai.FssaiForm
import com.example.bhandara.ui.fssai.FssaiScreen
import com.example.bhandara.ui.fssai.fssaiDate
import com.example.bhandara.ui.fssai.fssaiNumber
import com.example.bhandara.shared.resources.fssai_card_add
import com.example.bhandara.shared.resources.fssai_card_added
import com.example.bhandara.shared.resources.fssai_card_change
import com.example.bhandara.shared.resources.fssai_card_text
import com.example.bhandara.shared.resources.fssai_card_title
import com.example.bhandara.shared.resources.fssai_not_sent_text
import com.example.bhandara.shared.resources.fssai_not_sent_title
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.RamenDining
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.outlined.Storefront
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
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
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.rememberDeviceLocation
import com.example.bhandara.data.location.rememberLocationPermissionRequest
import com.example.bhandara.data.network.MenuApi
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.photos.PhotoSourceSheet
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.add_cart
import com.example.bhandara.shared.resources.add_cart_title
import com.example.bhandara.shared.resources.add_items_n
import com.example.bhandara.shared.resources.add_shop_button
import com.example.bhandara.shared.resources.items_skip
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
import com.example.bhandara.shared.resources.edit_menu
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
import com.example.bhandara.shared.resources.adjust_pin
import com.example.bhandara.shared.resources.pin_put_by_hand
import com.example.bhandara.shared.resources.pin_where_you_are
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
import com.example.bhandara.shared.resources.step_items
import com.example.bhandara.shared.resources.step_kind
import com.example.bhandara.shared.resources.step_menu
import com.example.bhandara.shared.resources.step_route
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.components.FeastChipsField
import com.example.bhandara.ui.components.FeastFormField
import com.example.bhandara.ui.components.FeastSectionLabel
import com.example.bhandara.ui.components.FeastStepper
import com.example.bhandara.ui.components.FoodMark
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.menu.AddMenuItemsScreen
import com.example.bhandara.ui.profile.SignInGate
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** The step's name; a cart's menu is its "Items" */
private fun ShopStep.label(isCart: Boolean): StringResource = when (this) {
    ShopStep.KIND -> Res.string.step_kind
    ShopStep.BASICS -> Res.string.step_basics
    ShopStep.ROUTE -> Res.string.step_route
    ShopStep.MENU -> if (isCart) Res.string.step_items else Res.string.step_menu
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

/** The order the kinds of shop are shown in (design 1b): the most common first */
private val SHOWN_SHOP_TYPES = listOf("Street Food", "Restaurant", "Food Truck", "Cafe", "Fast Food", "Bakery", "Sweet Shop")

/**
 * Adding a shop (design "Add Shop Flow" 1a–1d), shared by Android and iOS. Only for users signed in with an account,
 * who confirm they own the shop (admins list shops for their owners and skip that). Once added, [onAdded] gets the
 * new shop so the app can open it. A moving cart (3a–3g) also has its route, and once added explains Vendor Mode.
 *
 * @param onGoLive turns Vendor Mode on for the cart just added, then opens it; null where there's no Vendor Mode
 */
@Composable
fun AddShopScreen(onBack: () -> Unit, onAdded: (shopId: String) -> Unit, onGoLive: ((shopId: String) -> Unit)? = null) {
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
                FssaiApi(dependencies.apiClient),
            )
        }
        FeastTheme { AddShopPage(viewModel, onBack, onAdded, onGoLive) }
    }
}

@Composable
private fun AddShopPage(viewModel: AddShopViewModel, onBack: () -> Unit, onAdded: (String) -> Unit, onGoLive: ((String) -> Unit)?) {
    val colors = MaterialTheme.feastColors
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmingClear by rememberSaveable { mutableStateOf(false) }
    var editingMenu by rememberSaveable { mutableStateOf(false) }
    // The stop being added (a new one has no id yet) or changed, on the full-screen map
    var stopSheet by remember { mutableStateOf<CartStop?>(null) }
    var addingStop by rememberSaveable { mutableStateOf(false) }
    // The shop's pin on the full-screen map (1g)
    var adjustingPin by rememberSaveable { mutableStateOf(false) }
    // The FSSAI licence (5a), sent with the shop
    var addingFssai by rememberSaveable { mutableStateOf(false) }
    // A cart's Items step starts by picking what it sells (3m), then its items (3i)
    var pickingCategories by rememberSaveable { mutableStateOf(true) }
    val requestLocation = rememberLocationPermissionRequest { granted -> if (granted) viewModel.submit() }

    // Open the new shop; if its menu couldn't be saved, say so first
    val added = state.added
    val isCart = state.form.isCart
    // A cart first hears about Vendor Mode (3g)
    LaunchedEffect(added) { if (added != null && added.menuSaved && added.fssaiSent && !isCart) onAdded(added.id) }
    if (added != null && added.menuSaved && isCart) {
        GoLiveScreen(
            cartName = state.form.shopName.trim(),
            onLater = { onAdded(added.id) },
            onGoLive = onGoLive?.let { goLive -> { goLive(added.id) } },
        )
        return
    }
    if (added != null && (!added.menuSaved || !added.fssaiSent)) {
        AlertDialog(
            onDismissRequest = { onAdded(added.id) },
            title = { Text(stringResource(if (!added.menuSaved) Res.string.menu_not_saved_title else Res.string.fssai_not_sent_title)) },
            text = { Text(stringResource(if (!added.menuSaved) Res.string.menu_not_saved_text else Res.string.fssai_not_sent_text)) },
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

    if (addingStop || stopSheet != null) {
        val close = { addingStop = false; stopSheet = null }
        AddStopSheet(
            stop = stopSheet,
            nextId = (state.form.stops.maxOfOrNull { it.id } ?: 0L) + 1,
            onSave = { viewModel.saveStop(it); close() },
            onRemove = { viewModel.removeStop(it.id); close() },
            onNoFixedRoute = { viewModel.setNoFixedRoute(true); close() },
            onClose = close,
        )
        return
    }

    if (addingFssai) {
        FssaiScreen(
            shopId = null,
            initial = state.form.fssai,
            onBack = { addingFssai = false },
            onDone = { licence ->
                viewModel.setFssai(licence)
                addingFssai = false
            },
        )
        return
    }

    if (adjustingPin) {
        PinAdjustScreen(
            start = state.form.pin,
            onPicked = { viewModel.setPin(it); adjustingPin = false },
            onClose = { adjustingPin = false },
        )
        return
    }

    val inCartItems = isCart && state.step == ShopStep.MENU
    val goBack: () -> Boolean = {
        if (inCartItems && !pickingCategories) {
            pickingCategories = true
            true
        } else {
            viewModel.back()
        }
    }
    val goOn: () -> Unit = {
        when {
            state.step == ShopStep.ROUTE -> { pickingCategories = true; viewModel.next() }
            inCartItems && pickingCategories -> if (state.form.cartSells.isNotEmpty()) pickingCategories = false
            else -> viewModel.next()
        }
    }

    Column(Modifier.fillMaxSize().background(colors.page).windowInsetsPadding(WindowInsets.statusBars).imePadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), { if (!goBack()) onBack() }, enabled = !state.submitting)
            Text(
                stringResource(if (isCart) Res.string.add_cart_title else Res.string.add_shop_title), color = colors.ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, maxLines = 1,
                overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f),
            )
            // A cart's items are optional: "Skip" goes on to Details
            Text(
                stringResource(if (inCartItems) Res.string.items_skip else Res.string.clear_all), color = colors.saffronText.copy(alpha = if (state.submitting) 0.4f else 1f), fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable(enabled = !state.submitting) { if (inCartItems) viewModel.next() else confirmingClear = true }
                    .padding(horizontal = 6.dp, vertical = 8.dp),
            )
        }
        FeastStepper(state.steps.map { stringResource(it.label(isCart)) }, state.steps.indexOf(state.step), Modifier.padding(top = 2.dp))
        // Each step's spacing as in its design (1a–1d, 3c–3f)
        val (top, gap) = when (state.step) {
            ShopStep.BASICS -> if (isCart) 18 to 14 else 18 to 16
            ShopStep.ROUTE -> 16 to 12
            ShopStep.MENU -> if (isCart) 16 to 12 else 18 to 16
            ShopStep.DETAILS -> if (isCart) 16 to 14 else 18 to 12
            else -> 18 to 16
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = top.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(gap.dp),
        ) {
            when (state.step) {
                ShopStep.KIND -> KindStep(state.form.isMovingCart, viewModel::setMovingCart)
                ShopStep.BASICS -> if (isCart) CartBasicsStep(state, viewModel) else BasicsStep(state, viewModel)
                ShopStep.ROUTE -> RouteStep(state.form, viewModel, onAddStop = { addingStop = true }, onEditStop = { stopSheet = it })
                ShopStep.MENU -> when {
                    !isCart -> MenuStep(state.form, onEdit = { editingMenu = true })
                    pickingCategories -> CategoryPickStep(state.form, viewModel)
                    else -> CartItemsStep(state.form, viewModel, onEditRoute = { viewModel.goBackTo(ShopStep.ROUTE) })
                }
                ShopStep.DETAILS -> if (isCart) CartDetailsStep(state.form, viewModel) else DetailsStep(state.form, viewModel, onAdjustPin = { adjustingPin = true }, onAddFssai = { addingFssai = true })
            }
        }
        BottomBar(
            state = state,
            canGoOn = viewModel.canGoOn() && !(inCartItems && pickingCategories && state.form.cartSells.isEmpty()),
            nextLabel = if (inCartItems && pickingCategories) stringResource(Res.string.add_items_n, state.form.cartSells.size) else stringResource(Res.string.next),
            onBack = { goBack() },
            onNext = goOn,
            onAdd = viewModel::submit,
            onAllowLocation = requestLocation,
        )
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

/** Next (Add shop on the last step), Back after the first, and what's wrong or uploading above them */
@Composable
private fun BottomBar(
    state: AddShopState,
    canGoOn: Boolean,
    nextLabel: String,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onAdd: () -> Unit,
    onAllowLocation: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(12.dp, ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.12f))
            .background(colors.surface)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        state.uploadProgress?.let { Text(stringResource(Res.string.review_uploading, "$it%"), color = colors.muted, fontSize = 13.sp, style = MaterialTheme.typography.bodySmall) }
        state.error?.let { Text(it, color = colors.danger, fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium) }
        if (state.needsLocation) {
            FeastButton(stringResource(Res.string.map_allow_location), onClick = onAllowLocation, style = FeastButtonStyle.SOFT, icon = Icons.Outlined.MyLocation, modifier = Modifier.fillMaxWidth())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.step != ShopStep.KIND) {
                FeastButton(stringResource(Res.string.back), onClick = onBack, style = FeastButtonStyle.SOFT, enabled = !state.submitting, height = 56.dp, modifier = Modifier.weight(1f))
            }
            if (state.isLastStep) {
                FeastButton(
                    label = stringResource(if (state.form.isCart) Res.string.add_cart else Res.string.add_shop_button),
                    onClick = onAdd,
                    style = FeastButtonStyle.SAFFRON,
                    icon = Icons.Outlined.AddLocationAlt,
                    height = 56.dp,
                    modifier = Modifier.weight(1.4f),
                    content = if (state.submitting) {
                        { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.onSaffron) }
                    } else null,
                )
            } else {
                // Tapping Next on an unfinished step says what's missing, so it stays tappable
                FeastButton(
                    nextLabel, onClick = onNext, dimmed = !canGoOn, height = 56.dp,
                    trailingIcon = if (canGoOn) Icons.AutoMirrored.Rounded.ArrowForward else null,
                    modifier = Modifier.weight(1.4f),
                )
            }
        }
    }
}

/** Step 1 (design 1a): a shop in one place, or a moving cart */
@Composable
private fun KindStep(isMovingCart: Boolean?, onChoose: (Boolean) -> Unit) {
    Text(
        stringResource(Res.string.shop_kind_question), color = MaterialTheme.feastColors.ink, fontSize = 22.sp, lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 8.dp),
    )
    Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        KindCard(Icons.Outlined.Storefront, Res.string.shop_kind_fixed, Res.string.shop_kind_fixed_text, selected = isMovingCart == false, trail = false) { onChoose(false) }
        KindCard(Icons.Rounded.LocalShipping, Res.string.shop_kind_cart, Res.string.shop_kind_cart_text, selected = isMovingCart == true, trail = true) { onChoose(true) }
    }
}

/** A big card to pick: indigo with a saffron tick when picked; the cart's card has a dotted trail across it */
@Composable
private fun KindCard(icon: ImageVector, title: StringResource, text: StringResource, selected: Boolean, trail: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(22.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) colors.indigo else colors.surface)
            .then(if (selected) Modifier else Modifier.border(1.5.dp, colors.border, shape))
            .clickable(onClick = onClick),
    ) {
        if (trail) {
            Canvas(Modifier.matchParentSize()) {
                val s = 1.dp.toPx()
                val right = size.width
                fun p(x: Float, y: Float) = Offset(right - (320f - x) * s, y * s)
                val path = Path().apply {
                    p(120f, 70f).let { moveTo(it.x, it.y) }
                    val (a, b, c) = Triple(p(170f, 60f), p(200f, 20f), p(250f, 30f))
                    cubicTo(a.x, a.y, b.x, b.y, c.x, c.y)
                    val (d, e, f) = Triple(p(300f, 40f), p(300f, 20f), p(330f, 4f))
                    cubicTo(d.x, d.y, e.x, e.y, f.x, f.y)
                }
                drawPath(
                    path, colors.saffron,
                    style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 8.dp.toPx()))),
                )
            }
        }
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                val (tile, tint) = when {
                    selected && trail -> colors.saffron to colors.onSaffron
                    selected -> colors.tileOnSelected to colors.saffron
                    trail -> colors.saffronSoft to colors.saffronText
                    else -> colors.lavender to colors.accent
                }
                Box(Modifier.size(52.dp).background(tile, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
                }
                Box(Modifier.weight(1f))
                if (selected) {
                    Box(Modifier.size(26.dp).background(colors.saffron, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.onSaffron, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Column {
                Text(
                    stringResource(title), color = if (selected) colors.onIndigo else colors.ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(text), color = if (selected) colors.headerSubtext else colors.muted, fontSize = 15.sp, lineHeight = 20.sp,
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

/** Step 2 (design 1b): name, kind of shop, dishes as chips, photos */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BasicsStep(state: AddShopState, viewModel: AddShopViewModel) {
    val colors = MaterialTheme.feastColors
    val form = state.form
    var picking by remember { mutableStateOf(false) }

    FeastFormField(form.shopName, stringResource(Res.string.shop_name), viewModel::onShopNameChange, icon = Icons.Outlined.Storefront, height = 56.dp)

    Column {
        FeastSectionLabel(stringResource(Res.string.shop_type))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SHOWN_SHOP_TYPES.filter { it in SHOP_TYPES }.forEach { type ->
                val selected = form.shopType == type
                val shape = RoundedCornerShape(12.dp)
                Row(
                    Modifier
                        .height(40.dp)
                        .clip(shape)
                        .background(if (selected) colors.indigo else colors.surface)
                        .then(if (selected) Modifier else Modifier.border(1.5.dp, colors.border, shape))
                        .clickable { viewModel.onShopTypeChange(type) }
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (selected) Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.saffron, modifier = Modifier.size(16.dp))
                    Text(
                        stringResource(shopTypeLabels.getValue(type)), color = if (selected) colors.onIndigo else colors.ink, fontSize = 15.sp,
                        fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }

    Column {
        FeastSectionLabel(stringResource(Res.string.shop_dishes))
        FeastChipsField(
            items = form.menuNames,
            input = form.menuInput,
            hint = stringResource(Res.string.shop_dishes_hint),
            onInputChange = viewModel::onMenuInputChange,
            onDone = viewModel::addMenuInput,
            onRemove = viewModel::removeMenuName,
            enabled = !state.submitting,
        )
    }

    Column {
        FeastSectionLabel(stringResource(Res.string.shop_photos_label, MAX_SHOP_PHOTOS))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            form.photos.forEach { photo ->
                Box(Modifier.size(92.dp).clip(RoundedCornerShape(18.dp)).background(colors.lavender)) {
                    AsyncImage(photo.preview, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(92.dp))
                    Box(
                        Modifier.align(Alignment.TopEnd).padding(5.dp).size(24.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.55f))
                            .clickable(enabled = !state.submitting) { viewModel.removePhoto(photo) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Close, stringResource(Res.string.review_remove_photo), tint = Color.White, modifier = Modifier.size(15.dp))
                    }
                }
            }
            if (state.canAddPhotos) {
                Column(
                    Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.lavenderSoft)
                        .drawBehind {
                            drawRoundRect(
                                colors.starOff, cornerRadius = CornerRadius(18.dp.toPx()),
                                style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))),
                            )
                        }
                        .clickable(enabled = !state.submitting) { picking = true },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                ) {
                    Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, tint = colors.accent, modifier = Modifier.size(28.dp))
                    Text("${form.photos.size} / $MAX_SHOP_PHOTOS", color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }

    if (picking) {
        PhotoSourceSheet(maxCount = MAX_SHOP_PHOTOS - form.photos.size, onPicked = viewModel::addPhotos, onDismiss = { picking = false })
    }
}

/** Step 3 (design 1c): the optional menu with prices, behind a drawn menu card and camera */
@Composable
private fun MenuStep(form: ShopForm, onEdit: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(24.dp)
    Column(
        Modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
            .shadow(6.dp, shape, ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.08f))
            .clip(shape)
            .background(colors.surface),
    ) {
        MenuDrawing()
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(22.dp))
                Text(stringResource(Res.string.detailed_menu_title), color = colors.ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            }
            if (form.detailedMenu.isEmpty()) {
                Text(
                    stringResource(Res.string.detailed_menu_text), color = colors.muted, fontSize = 15.sp, lineHeight = 22.sp,
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp),
                )
                FeastButton(
                    stringResource(Res.string.detailed_menu_add), onClick = onEdit, style = FeastButtonStyle.SAFFRON, icon = Icons.Rounded.Add,
                    height = 52.dp, modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                )
            } else {
                Text(
                    stringResource(Res.string.detailed_menu_count, form.detailedMenu.size), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    form.detailedMenu.joinToString(", ") { it.name }, color = colors.muted, fontSize = 14.sp, lineHeight = 20.sp, maxLines = 3,
                    overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp),
                )
                FeastButton(
                    stringResource(Res.string.edit_menu), onClick = onEdit, style = FeastButtonStyle.SOFT, icon = Icons.Outlined.Edit,
                    height = 52.dp, modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                )
            }
        }
    }
}

/** A tilted paper menu (veg / non-veg dots, prices) and a camera tile on saffron */
@Composable
private fun MenuDrawing() {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.fillMaxWidth().background(colors.saffronSoft).padding(horizontal = 20.dp, vertical = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier
                .rotate(-4f)
                .width(118.dp)
                .shadow(4.dp, RoundedCornerShape(14.dp), spotColor = Color(0xFFB45309).copy(alpha = 0.2f))
                .background(colors.surface, RoundedCornerShape(14.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(colors.success to "₹30", colors.success to "₹40", colors.danger to "₹90").forEach { (dot, price) ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FoodMark(dot, Modifier.size(10.dp))
                    Box(Modifier.weight(1f).height(6.dp).background(colors.border, RoundedCornerShape(3.dp)))
                    Text(price, color = colors.ink, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Box(Modifier.rotate(5f).size(84.dp).background(colors.indigo, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = colors.saffron, modifier = Modifier.size(34.dp))
        }
    }
}

/** Step 4 (design 1d): where the shop goes on the map ("Adjust pin" opens 1g), then the optional details */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsStep(form: ShopForm, viewModel: AddShopViewModel, onAdjustPin: () -> Unit, onAddFssai: () -> Unit) {
    val colors = MaterialTheme.feastColors
    PlaceCard(pinnedByHand = form.pin != null, onAdjustPin = onAdjustPin)
    FeastFormField(form.description, stringResource(Res.string.section_description), viewModel::onDescriptionChange, height = 76.dp, singleLine = false)
    FeastFormField(form.phone, stringResource(Res.string.shop_phone), viewModel::onPhoneChange, icon = Icons.Outlined.Call, height = 52.dp, keyboardType = KeyboardType.Phone)
    FeastFormField(form.email, stringResource(Res.string.shop_email), viewModel::onEmailChange, icon = Icons.Outlined.Mail, height = 52.dp, keyboardType = KeyboardType.Email)
    FeastFormField(form.address, stringResource(Res.string.shop_address), viewModel::onAddressChange, icon = Icons.Outlined.Place, height = 52.dp)
    FeastFormField(form.landmark, stringResource(Res.string.shop_landmark), viewModel::onLandmarkChange, icon = Icons.Outlined.NearMe, height = 52.dp)
    FeastFormField(form.cuisine, stringResource(Res.string.shop_cuisine), viewModel::onCuisineChange, icon = Icons.Outlined.RamenDining, height = 52.dp)
    FeastFormField(
        form.costForTwo, stringResource(Res.string.shop_cost_for_two), viewModel::onCostForTwoChange, icon = Icons.Outlined.CurrencyRupee,
        height = 52.dp, keyboardType = KeyboardType.Number,
    )
    Column {
        FeastSectionLabel(stringResource(Res.string.info_facilities))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FacilityChip(Res.string.facility_takeaway, form.takeaway) { viewModel.setTakeaway(!form.takeaway) }
            FacilityChip(Res.string.facility_home_delivery, form.homeDelivery) { viewModel.setHomeDelivery(!form.homeDelivery) }
            FacilityChip(Res.string.facility_seating, form.hasSeating) { viewModel.setSeating(!form.hasSeating) }
            FacilityChip(Res.string.facility_wifi, form.wifi) { viewModel.setWifi(!form.wifi) }
        }
    }
    FssaiCard(form.fssai, onAddFssai)
}

/** The optional FSSAI licence, last on Details (food shops only; carts skip it): "Add certificate" opens 5a */
@Composable
private fun FssaiCard(licence: FssaiForm?, onAdd: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(20.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(colors.surface).border(1.5.dp, colors.border, shape).padding(start = 14.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(44.dp).background(colors.successSoft, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = colors.success, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(stringResource(Res.string.fssai_card_title), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            val validTill = licence?.validTill
            Text(
                if (licence != null && validTill != null) stringResource(Res.string.fssai_card_added, fssaiNumber(licence.number), fssaiDate(validTill))
                else stringResource(Res.string.fssai_card_text),
                color = colors.muted, fontSize = 13.sp, lineHeight = 17.sp, style = MaterialTheme.typography.bodySmall,
            )
        }
        Row(
            Modifier.height(40.dp).clip(RoundedCornerShape(12.dp)).background(colors.lavender).clickable(onClick = onAdd).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (licence == null) Icon(Icons.Outlined.AddAPhoto, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
            Text(
                stringResource(if (licence == null) Res.string.fssai_card_add else Res.string.fssai_card_change), color = colors.ink, fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

/** An on/off chip, indigo with a saffron tick when on (as the kinds of shop) */
@Composable
private fun FacilityChip(label: StringResource, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .height(40.dp)
            .clip(shape)
            .background(if (selected) colors.indigo else colors.surface)
            .then(if (selected) Modifier else Modifier.border(1.5.dp, colors.border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (selected) Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.saffron, modifier = Modifier.size(16.dp))
        Text(stringResource(label), color = if (selected) colors.onIndigo else colors.ink, fontSize = 15.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelLarge)
    }
}

/** A drawn bit of map with a saffron spot, over where the pin is and "Adjust pin" */
@Composable
private fun PlaceCard(pinnedByHand: Boolean, onAdjustPin: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.indigo)) {
        Box(Modifier.fillMaxWidth().height(76.dp).clipToBounds().background(colors.mapLand)) {
            Canvas(Modifier.matchParentSize()) {
                val sx = size.width / 320f
                val sy = size.height / 76f
                fun p(x: Float, y: Float) = Offset(x * sx, y * sy)
                val road = Stroke(9.dp.toPx())
                drawLine(colors.mapRoad, p(-10f, 30f), p(330f, 14f), road.width)
                drawLine(colors.mapRoad, p(140f, -10f), p(170f, 90f), road.width)
                val curve = Path().apply {
                    p(-10f, 64f).let { moveTo(it.x, it.y) }
                    val (a, b, c) = Triple(p(100f, 56f), p(220f, 76f), p(330f, 54f))
                    cubicTo(a.x, a.y, b.x, b.y, c.x, c.y)
                }
                drawPath(curve, colors.mapRoad, style = road)
            }
            // The spot, with a soft saffron ring, where the roads cross
            Box(Modifier.align(Alignment.TopStart).offset(x = 0.dp).fillMaxWidth().height(76.dp)) {
                Box(Modifier.align(Alignment.Center).offset(x = (-3).dp, y = (-5).dp).size(46.dp).background(colors.saffron.copy(alpha = 0.25f), CircleShape))
                Box(
                    Modifier.align(Alignment.Center).offset(x = (-3).dp, y = (-5).dp).size(22.dp).background(Color.White, CircleShape).padding(4.dp)
                        .background(colors.saffron, CircleShape),
                )
            }
        }
        Row(Modifier.padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(if (pinnedByHand) Icons.Outlined.Place else Icons.Outlined.MyLocation, contentDescription = null, tint = colors.saffron, modifier = Modifier.size(22.dp))
            Text(
                stringResource(if (pinnedByHand) Res.string.pin_put_by_hand else Res.string.pin_where_you_are), color = colors.onIndigo, fontSize = 14.sp, lineHeight = 19.sp,
                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f),
            )
            Row(
                Modifier.height(44.dp).clip(RoundedCornerShape(12.dp)).background(colors.saffron).clickable(onClick = onAdjustPin).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Outlined.OpenInFull, contentDescription = null, tint = colors.onSaffron, modifier = Modifier.size(18.dp))
                Text(stringResource(Res.string.adjust_pin), color = colors.onSaffron, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

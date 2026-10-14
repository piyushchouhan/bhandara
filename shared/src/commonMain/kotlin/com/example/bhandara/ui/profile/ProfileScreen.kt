package com.example.bhandara.ui.profile

import com.example.bhandara.shared.resources.notif_save_failed
import com.example.bhandara.data.network.UserApi
import com.example.bhandara.ui.settings.SettingsViewModel
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonRemove
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.data.network.ClaimApi
import com.example.bhandara.data.network.ShopApi
import com.example.bhandara.platform.APP_LANGUAGES
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.rememberAppLanguage
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.add_a_shop
import com.example.bhandara.shared.resources.app_version_line
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.badge_shop_owner
import com.example.bhandara.shared.resources.coming_soon
import com.example.bhandara.shared.resources.delete_account
import com.example.bhandara.shared.resources.feasts_posted
import com.example.bhandara.shared.resources.language
import com.example.bhandara.shared.resources.manage_details
import com.example.bhandara.shared.resources.manage_menu
import com.example.bhandara.shared.resources.manage_shop
import com.example.bhandara.shared.resources.manage_timings
import com.example.bhandara.shared.resources.my_activity
import com.example.bhandara.shared.resources.my_photos
import com.example.bhandara.shared.resources.my_reports
import com.example.bhandara.shared.resources.my_reviews
import com.example.bhandara.shared.resources.my_shops
import com.example.bhandara.shared.resources.no_shops_yet
import com.example.bhandara.shared.resources.pref_feasts_near
import com.example.bhandara.shared.resources.pref_veg_only
import com.example.bhandara.shared.resources.preferences
import com.example.bhandara.shared.resources.privacy_data
import com.example.bhandara.shared.resources.profile_change_photo
import com.example.bhandara.shared.resources.profile_connecting
import com.example.bhandara.shared.resources.profile_dialog_guest_mode
import com.example.bhandara.shared.resources.profile_dialog_guest_session
import com.example.bhandara.shared.resources.profile_dialog_guest_user
import com.example.bhandara.shared.resources.profile_dialog_linked_google
import com.example.bhandara.shared.resources.profile_dialog_sign_in
import com.example.bhandara.shared.resources.profile_dialog_sign_out
import com.example.bhandara.shared.resources.profile_dialog_title
import com.example.bhandara.shared.resources.profile_edit
import com.example.bhandara.shared.resources.profile_sign_in_later
import com.example.bhandara.shared.resources.profile_signing_out
import com.example.bhandara.shared.resources.reactivate_shop
import com.example.bhandara.shared.resources.saved_places
import com.example.bhandara.shared.resources.shop_claimed
import com.example.bhandara.shared.resources.shop_deactivated_banner
import com.example.bhandara.shared.resources.shop_hidden
import com.example.bhandara.shared.resources.shop_live
import com.example.bhandara.shared.resources.shop_not_shown
import com.example.bhandara.shared.resources.stat_feasts
import com.example.bhandara.shared.resources.stat_photos
import com.example.bhandara.shared.resources.stat_reviews
import com.example.bhandara.shared.resources.stat_verified
import com.example.bhandara.shared.resources.undo
import com.example.bhandara.shared.resources.vendor_mode_desc
import com.example.bhandara.shared.resources.vendor_mode_title
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * Signing in with an account (Google), as each platform does it: Android in the app module, iOS in Swift.
 */
interface AccountSignIn {

    /** Returns what opens the platform's sign-in (e.g. Google's account picker); [onResult] gets how it ended */
    @Composable
    fun rememberSignIn(onResult: (SignInOutcome) -> Unit): () -> Unit

    /** Signs out of the account (Firebase and the provider); throws if it couldn't */
    suspend fun signOut()
}

/**
 * The profile (design 1a / 1d), shared by Android and iOS: who is signed in, the shops they own, their activity,
 * preferences and account. Parts the backend doesn't have yet are shown and say "Coming soon".
 *
 * @param onManageShop / [onOpenShop] a shop of theirs, to manage or to see as others do
 * @param onMyReviews / [onMyPhotos] / [onMyFeasts] / [onSavedPlaces] My activity → My reviews / My photos / Feasts I posted /
 *   Saved places; [onMyReports] → My reports
 * @param onPrivacy Account → Privacy and data (or straight to deleting the account)
 * @param vendorMode Android's vendor mode card ([VendorModeCard]); null where there is none
 */
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onManageShop: (shopId: String) -> Unit = {},
    onOpenShop: (shopId: String) -> Unit = {},
    onAddShop: () -> Unit = {},
    onMyReviews: () -> Unit = {},
    onMyPhotos: () -> Unit = {},
    onMyFeasts: () -> Unit = {},
    onSavedPlaces: () -> Unit = {},
    onMyReports: () -> Unit = {},
    onPrivacy: (startAtDelete: Boolean) -> Unit = {},
    vendorMode: (@Composable () -> Unit)? = null,
    accountSignIn: AccountSignIn? = LocalAccountSignIn.current,
) {
    val dependencies = LocalAppDependencies.current
    val viewModel = viewModel {
        ProfileViewModel(
            dependencies.auth,
            accountSignIn?.let { it::signOut },
            ClaimApi(dependencies.apiClient),
            ShopApi(dependencies.apiClient),
        )
    }
    // Coming back (e.g. after deactivating a shop in Manage shop) shows the shops as they are now
    var shownBefore by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (shownBefore) viewModel.loadMyShops() else shownBefore = true
    }
    FeastTheme {
        ProfilePage(viewModel, accountSignIn, onBack, onManageShop, onOpenShop, onAddShop, onMyReviews, onMyPhotos, onMyFeasts, onSavedPlaces, onMyReports, onPrivacy, vendorMode)
    }
}

@Composable
private fun ProfilePage(
    viewModel: ProfileViewModel,
    accountSignIn: AccountSignIn?,
    onBack: () -> Unit,
    onManageShop: (String) -> Unit,
    onOpenShop: (String) -> Unit,
    onAddShop: () -> Unit,
    onMyReviews: () -> Unit,
    onMyPhotos: () -> Unit,
    onMyFeasts: () -> Unit,
    onSavedPlaces: () -> Unit,
    onMyReports: () -> Unit,
    onPrivacy: (startAtDelete: Boolean) -> Unit,
    vendorMode: (@Composable () -> Unit)?,
) {
    val colors = MaterialTheme.feastColors
    val dependencies = LocalAppDependencies.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val startSignIn = accountSignIn?.rememberSignIn(viewModel::onSignInResult)
    val comingSoon: () -> Unit = {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(getString(Res.string.coming_soon))
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            viewModel.onMessageShown()
            snackbar.showSnackbar(it)
        }
    }
    // The preferences here are the same as in Settings, "Feasts near me" also kept by the backend
    val preferences = dependencies.settings.preferences
    val settingsViewModel = viewModel { SettingsViewModel(preferences, UserApi(dependencies.apiClient)) }
    val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(settingsState.notificationsFailed) {
        if (settingsState.notificationsFailed) {
            settingsViewModel.onMessageShown()
            snackbar.showSnackbar(getString(Res.string.notif_save_failed))
        }
    }
    LaunchedEffect(state.justDeactivated) {
        if (state.justDeactivated != null) {
            delay(8_000)
            viewModel.onBannerShown()
        }
    }

    Box(Modifier.fillMaxSize().background(colors.page)) {
        val scroll = rememberScrollState()
        Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
            HeaderWithStats(
                header = { ProfileHeader(state, onBack, onEdit = comingSoon, onChangePhoto = comingSoon) },
                stats = { StatsCard() },
            )
            Column(
                Modifier.padding(start = 14.dp, end = 14.dp, top = 20.dp, bottom = 40.dp).windowInsetsPadding(WindowInsets.navigationBars),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                state.error?.let {
                    Text(
                        it, color = colors.dangerText, fontSize = 14.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().background(colors.dangerSoft, RoundedCornerShape(16.dp)).padding(12.dp),
                    )
                }
                state.justDeactivated?.let { DeactivatedBanner(onUndo = comingSoon) }
                if (!state.isGuest || state.myShops.isNotEmpty()) {
                    MyShops(state.myShops, onAddShop, onManageShop, onOpenShop, onTimings = comingSoon, onReactivate = comingSoon)
                }
                vendorMode?.invoke()
                MyActivity(dependencies.settings.savedPlaceCount, onMyReviews = onMyReviews, onMyPhotos = onMyPhotos, onMyFeasts = onMyFeasts, onSavedPlaces = onSavedPlaces, onMyReports = onMyReports)
                Preferences(
                    feastsNear = preferences.notifyNearbyFeasts,
                    onFeastsNear = { settingsViewModel.setNotifyNearbyFeasts(!preferences.notifyNearbyFeasts) },
                    vegOnly = preferences.vegOnly,
                    onVegOnly = { preferences.updateVegOnly(!preferences.vegOnly) },
                )
                Account(
                    state = state,
                    signInAvailable = startSignIn != null,
                    onSignIn = {
                        viewModel.onSignInStarted()
                        startSignIn?.invoke()
                    },
                    onSignOut = viewModel::signOut,
                    onComingSoon = comingSoon,
                    onPrivacy = onPrivacy,
                )
                Text(
                    stringResource(Res.string.app_version_line, dependencies.appVersion),
                    color = colors.disabledText, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        // Once the header has scrolled away, the page's colour keeps the status bar readable
        val headerPx = with(LocalDensity.current) { 300.dp.toPx() }
        val headerGone by remember { derivedStateOf { scroll.value > headerPx } }
        Box(
            Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars)
                .background(if (headerGone) colors.page.copy(alpha = 0.96f) else Color.Transparent),
        )
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars))
    }
}

/** The indigo header, with the stats card overlapping its bottom by 44dp */
@Composable
private fun HeaderWithStats(header: @Composable () -> Unit, stats: @Composable () -> Unit) {
    Layout(content = { header(); stats() }) { measurables, constraints ->
        val top = measurables[0].measure(constraints)
        val card = measurables[1].measure(constraints.copy(minWidth = 0, maxWidth = constraints.maxWidth - 28.dp.roundToPx()))
        val cardTop = top.height - 44.dp.roundToPx()
        layout(constraints.maxWidth, cardTop + card.height) {
            top.place(0, 0)
            card.place(14.dp.roundToPx(), cardTop)
        }
    }
}

@Composable
private fun ProfileHeader(state: ProfileState, onBack: () -> Unit, onEdit: () -> Unit, onChangePhoto: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val account = state.account?.takeIf { !state.isGuest }
    Box(Modifier.fillMaxWidth().background(colors.indigo)) {
        // The saffron dotted trail across the header
        Canvas(Modifier.matchParentSize()) {
            val s = size.width / 360f
            fun p(x: Float, y: Float) = Offset(x * s, y * s)
            val path = Path().apply {
                p(-10f, 230f).let { moveTo(it.x, it.y) }
                val (a, b, c) = Triple(p(80f, 200f), p(150f, 130f), p(230f, 120f))
                cubicTo(a.x, a.y, b.x, b.y, c.x, c.y)
                val (d, e, f) = Triple(p(310f, 110f), p(330f, 60f), p(380f, 30f))
                cubicTo(d.x, d.y, e.x, e.y, f.x, f.y)
            }
            drawPath(
                path, colors.saffron.copy(alpha = 0.5f),
                style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 9.dp.toPx()))),
            )
        }
        Column(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(bottom = 64.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HeaderButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), onBack)
                Text(
                    stringResource(Res.string.profile_dialog_title), color = colors.onIndigo, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f),
                )
                HeaderButton(Icons.Rounded.Edit, stringResource(Res.string.profile_edit), onEdit)
            }
            Column(
                Modifier.fillMaxWidth().padding(top = 14.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Avatar(account?.photoUrl, initial = account?.let { (it.name ?: it.email)?.trim()?.firstOrNull()?.uppercaseChar() }, onChangePhoto)
                Text(
                    if (state.isGuest) stringResource(Res.string.profile_dialog_guest_user) else state.accountTitle.orEmpty(),
                    color = colors.onIndigo, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                )
                val subtitle = if (state.isGuest) stringResource(Res.string.profile_dialog_guest_session) else state.accountSubtitle
                subtitle?.let {
                    Text(it, color = colors.headerSubtext, fontSize = 14.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (state.isGuest) {
                        HeaderBadge(stringResource(Res.string.profile_dialog_guest_mode)) {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = colors.onIndigo, modifier = Modifier.size(14.dp))
                        }
                    } else {
                        HeaderBadge(stringResource(Res.string.profile_dialog_linked_google)) {
                            Box(Modifier.size(14.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                                Text("G", color = Color(0xFF4F46E5), fontSize = 9.sp, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    if (state.isShopOwner) {
                        Row(
                            Modifier.height(26.dp).background(colors.saffron, CircleShape).padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(Icons.Rounded.Storefront, contentDescription = null, tint = colors.onSaffron, modifier = Modifier.size(14.dp))
                            Text(
                                stringResource(Res.string.badge_shop_owner), color = colors.onSaffron, fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier.size(44.dp).clip(shape).background(Color.White.copy(alpha = 0.12f), shape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = MaterialTheme.feastColors.onIndigo, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun HeaderBadge(label: String, leading: @Composable () -> Unit) {
    Row(
        Modifier.height(26.dp).background(Color.White.copy(alpha = 0.12f), CircleShape).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        leading()
        Text(label, color = MaterialTheme.feastColors.onIndigo, fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
    }
}

/** The picture (or first letter) in a saffron ring, with a camera badge to change it */
@Composable
private fun Avatar(photoUrl: String?, initial: Char?, onChangePhoto: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Box {
        Box(
            Modifier.size(92.dp).clip(CircleShape).background(colors.avatarFill).border(4.dp, colors.saffron, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            when {
                photoUrl != null -> AsyncImage(
                    photoUrl, contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().padding(4.dp).clip(CircleShape),
                )
                initial != null -> Text(initial.toString(), color = colors.ink, fontSize = 36.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.headlineLarge)
                else -> Icon(Icons.Rounded.Person, contentDescription = null, tint = colors.ink, modifier = Modifier.size(44.dp))
            }
        }
        Box(
            Modifier.align(Alignment.BottomEnd).offset(2.dp, 2.dp).size(30.dp).clip(CircleShape).background(colors.surface).clickable(onClick = onChangePhoto),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.PhotoCamera, contentDescription = stringResource(Res.string.profile_change_photo), tint = colors.ink, modifier = Modifier.size(16.dp))
        }
    }
}

/** Reviews, photos, feasts and verifications: "–" until the backend counts them */
@Composable
private fun StatsCard() {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(22.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .shadow(10.dp, shape, ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.12f))
            .background(colors.surface, shape)
            .padding(horizontal = 6.dp, vertical = 14.dp),
    ) {
        listOf(Res.string.stat_reviews, Res.string.stat_photos, Res.string.stat_feasts, Res.string.stat_verified).forEachIndexed { i, label ->
            if (i > 0) Box(Modifier.width(1.dp).height(40.dp).background(colors.lavender))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("–", color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
                Text(stringResource(label), color = colors.muted, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/** "Shop deactivated" on indigo (design 1d), with Undo */
@Composable
private fun DeactivatedBanner(onUndo: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.fillMaxWidth().background(colors.indigo, RoundedCornerShape(16.dp)).padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(20.dp))
        Text(
            stringResource(Res.string.shop_deactivated_banner), color = colors.onIndigo, fontSize = 14.sp, fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(vertical = 8.dp),
        )
        Text(
            stringResource(Res.string.undo), color = colors.saffron, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onUndo).padding(horizontal = 8.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text, color = MaterialTheme.feastColors.ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.titleMedium, modifier = modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun MyShops(
    shops: List<LocalShopResponse>,
    onAddShop: () -> Unit,
    onManageShop: (String) -> Unit,
    onOpenShop: (String) -> Unit,
    onTimings: () -> Unit,
    onReactivate: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(stringResource(Res.string.my_shops), Modifier.weight(1f))
            Row(
                Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onAddShop).padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
                Text(stringResource(Res.string.add_a_shop), color = colors.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
            }
        }
        if (shops.isEmpty()) {
            Text(
                stringResource(Res.string.no_shops_yet), color = colors.muted, fontSize = 14.sp,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(20.dp)).padding(16.dp),
            )
        }
        shops.forEach { shop ->
            if (shop.isActive == false) {
                HiddenShopCard(shop, onReactivate)
            } else {
                ShopCard(
                    shop,
                    onDetails = { onOpenShop(shop.id) },
                    onMenu = { onOpenShop(shop.id) },
                    onTimings = onTimings,
                    onManage = { onManageShop(shop.id) },
                )
            }
        }
    }
}

/** The shop's photo, name and a status line; shared by the live and hidden cards */
@Composable
private fun ShopSummary(shop: LocalShopResponse, hidden: Boolean, status: @Composable RowScope.() -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(colors.lavender).alpha(if (hidden) 0.6f else 1f),
            contentAlignment = Alignment.Center,
        ) {
            val photo = shop.imageUrls?.firstOrNull()
            if (photo != null) {
                AsyncImage(
                    photo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(),
                    colorFilter = if (hidden) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                )
            } else {
                Icon(Icons.Rounded.Storefront, contentDescription = null, tint = if (hidden) colors.muted else colors.accent, modifier = Modifier.size(28.dp))
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                shop.shopName, color = if (hidden) colors.muted else colors.ink, fontSize = 16.sp, lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium,
            )
            Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), content = status)
        }
    }
}

@Composable
private fun ShopCard(shop: LocalShopResponse, onDetails: () -> Unit, onMenu: () -> Unit, onTimings: () -> Unit, onManage: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(colors.surface)) {
        ShopSummary(shop, hidden = false) {
            Row(
                Modifier.height(22.dp).background(colors.successSoft, RoundedCornerShape(7.dp)).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(Modifier.size(6.dp).background(colors.success, CircleShape))
                Text(stringResource(Res.string.shop_live), color = colors.success, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Icon(Icons.Rounded.Verified, contentDescription = null, tint = colors.accent, modifier = Modifier.size(14.dp))
                Text(stringResource(Res.string.shop_claimed), color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender))
        Row(Modifier.fillMaxWidth().height(58.dp)) {
            ShopAction(Icons.Rounded.EditNote, Res.string.manage_details, onDetails)
            Box(Modifier.width(1.dp).height(58.dp).background(colors.lavender))
            ShopAction(Icons.Rounded.RestaurantMenu, Res.string.manage_menu, onMenu)
            Box(Modifier.width(1.dp).height(58.dp).background(colors.lavender))
            ShopAction(Icons.Rounded.Schedule, Res.string.manage_timings, onTimings)
        }
        Row(
            Modifier.fillMaxWidth().height(50.dp).background(colors.lavenderSoft).clickable(onClick = onManage),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        ) {
            Text(stringResource(Res.string.manage_shop), color = colors.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun RowScope.ShopAction(icon: ImageVector, label: StringResource, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Column(
        Modifier.weight(1f).fillMaxSize().clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
        Text(stringResource(label), color = colors.ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
    }
}

/** A deactivated shop (design 1d): greyed out, "Hidden", and one way to turn it back on */
@Composable
private fun HiddenShopCard(shop: LocalShopResponse, onReactivate: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(colors.surface)) {
        ShopSummary(shop, hidden = true) {
            Row(
                Modifier.height(22.dp).background(colors.fadedPage, RoundedCornerShape(7.dp)).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Rounded.VisibilityOff, contentDescription = null, tint = colors.muted, modifier = Modifier.size(13.dp))
                Text(stringResource(Res.string.shop_hidden), color = colors.muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
            }
            Text(
                stringResource(Res.string.shop_not_shown), color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium,
            )
        }
        FeastButton(
            stringResource(Res.string.reactivate_shop), onClick = onReactivate, style = FeastButtonStyle.SAFFRON, icon = Icons.Rounded.Visibility,
            height = 50.dp, modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        )
    }
}

/**
 * Android's vendor mode on the profile: a saffron card with the cart, and an on/off switch
 *
 * @param subtitle under the title; the default explains what it does
 */
@Composable
fun VendorModeCard(checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true, subtitle: String? = null) {
    val colors = MaterialTheme.feastColors
    val thumb = if (colors.isDark) colors.surface else Color.White
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.saffronSoft)
            .then(if (enabled) Modifier.clickable { onCheckedChange(!checked) } else Modifier)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(44.dp).background(colors.saffron, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.LocalShipping, contentDescription = null, tint = colors.onSaffron, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(stringResource(Res.string.vendor_mode_title), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Text(subtitle ?: stringResource(Res.string.vendor_mode_desc), color = colors.saffronBody, fontSize = 12.sp, style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = colors.saffron,
                checkedThumbColor = thumb,
                checkedBorderColor = Color.Transparent,
                uncheckedTrackColor = colors.saffronTrack,
                uncheckedThumbColor = thumb,
                uncheckedBorderColor = Color.Transparent,
                disabledUncheckedTrackColor = colors.saffronTrack.copy(alpha = 0.5f),
                disabledUncheckedThumbColor = thumb,
                disabledUncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

/** A white card of rows with thin lines between them */
@Composable
private fun RowsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.feastColors.surface), content = content)
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    label: String,
    onClick: (() -> Unit)?,
    divider: Boolean = true,
    iconTint: Color = MaterialTheme.feastColors.accent,
    labelColor: Color = MaterialTheme.feastColors.ink,
    labelWeight: FontWeight = FontWeight.Medium,
    subtitle: String? = null,
    chevron: Boolean = false,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val colors = MaterialTheme.feastColors
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f)) {
                Text(label, color = labelColor, fontSize = 15.sp, fontWeight = labelWeight, style = MaterialTheme.typography.bodyLarge)
                subtitle?.let { Text(it, color = colors.muted, fontSize = 12.sp, style = MaterialTheme.typography.bodySmall) }
            }
            trailing()
            if (chevron) Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = colors.muted, modifier = Modifier.size(20.dp))
        }
        if (divider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender))
    }
}

@Composable
private fun RowCount(text: String) {
    Text(text, color = MaterialTheme.feastColors.muted, fontSize = 13.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
}

/** What the user did: each row opens its page; saved places are counted (they're on this phone) */
@Composable
private fun MyActivity(savedCount: Int, onMyReviews: () -> Unit, onMyPhotos: () -> Unit, onMyFeasts: () -> Unit, onSavedPlaces: () -> Unit, onMyReports: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(stringResource(Res.string.my_activity))
        RowsCard {
            ProfileRow(Icons.Rounded.RateReview, stringResource(Res.string.my_reviews), onMyReviews, chevron = true)
            ProfileRow(Icons.Rounded.PhotoLibrary, stringResource(Res.string.my_photos), onMyPhotos, chevron = true)
            ProfileRow(Icons.Rounded.VolunteerActivism, stringResource(Res.string.feasts_posted), onMyFeasts, iconTint = colors.saffronText, chevron = true)
            ProfileRow(Icons.Rounded.Bookmark, stringResource(Res.string.saved_places), onSavedPlaces, chevron = true) {
                if (savedCount > 0) RowCount(savedCount.toString())
            }
            ProfileRow(Icons.Rounded.Flag, stringResource(Res.string.my_reports), onMyReports, divider = false, chevron = true)
        }
    }
}

@Composable
private fun Preferences(feastsNear: Boolean, onFeastsNear: () -> Unit, vegOnly: Boolean, onVegOnly: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(stringResource(Res.string.preferences))
        RowsCard {
            LanguageRow()
            ProfileRow(Icons.Rounded.Notifications, stringResource(Res.string.pref_feasts_near), onFeastsNear) {
                PreferenceSwitch(checked = feastsNear, onFeastsNear)
            }
            ProfileRow(Icons.Rounded.Eco, stringResource(Res.string.pref_veg_only), onVegOnly, divider = false) {
                PreferenceSwitch(checked = vegOnly, onVegOnly)
            }
        }
    }
}

/** An indigo on/off switch */
@Composable
private fun PreferenceSwitch(checked: Boolean, onToggle: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val thumb = if (colors.isDark) colors.surface else Color.White
    Switch(
        checked = checked,
        onCheckedChange = { onToggle() },
        colors = SwitchDefaults.colors(
            checkedTrackColor = colors.indigo,
            checkedThumbColor = thumb,
            checkedBorderColor = Color.Transparent,
            uncheckedTrackColor = colors.border,
            uncheckedThumbColor = thumb,
            uncheckedBorderColor = Color.Transparent,
        ),
    )
}

/** The app's language: on Android a menu of languages, on iOS it opens Settings (as in the side menu) */
@Composable
private fun LanguageRow() {
    val colors = MaterialTheme.feastColors
    val language = rememberAppLanguage()
    var choosing by remember { mutableStateOf(false) }
    val currentName = APP_LANGUAGES.first { it.first == language.current }.second
    ProfileRow(
        Icons.Rounded.Language,
        stringResource(Res.string.language),
        onClick = { if (language.choosableInApp) choosing = true else language.choose(language.current) },
    ) {
        Box {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(currentName, color = colors.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Icon(
                    if (language.choosableInApp) Icons.Rounded.ExpandMore else Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(if (language.choosableInApp) 18.dp else 14.dp),
                )
            }
            DropdownMenu(expanded = choosing, onDismissRequest = { choosing = false }) {
                APP_LANGUAGES.forEach { (tag, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            choosing = false
                            language.choose(tag)
                        },
                    )
                }
            }
        }
    }
}

/** Privacy, signing in or out, and deleting the account */
@Composable
private fun Account(
    state: ProfileState,
    signInAvailable: Boolean,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onComingSoon: () -> Unit,
    onPrivacy: (startAtDelete: Boolean) -> Unit,
) {
    val colors = MaterialTheme.feastColors
    RowsCard {
        ProfileRow(Icons.Rounded.Lock, stringResource(Res.string.privacy_data), { onPrivacy(false) }, chevron = true)
        if (state.isGuest) {
            ProfileRow(
                Icons.AutoMirrored.Rounded.Login,
                stringResource(if (state.busy) Res.string.profile_connecting else Res.string.profile_dialog_sign_in),
                onClick = if (signInAvailable && !state.busy) onSignIn else null,
                divider = false,
                labelColor = if (signInAvailable) colors.accent else colors.muted,
                labelWeight = FontWeight.SemiBold,
                subtitle = if (signInAvailable) null else stringResource(Res.string.profile_sign_in_later),
            ) { if (state.busy) BusySpinner(colors.accent) }
        } else {
            ProfileRow(
                Icons.AutoMirrored.Rounded.Logout,
                stringResource(if (state.busy) Res.string.profile_signing_out else Res.string.profile_dialog_sign_out),
                onClick = if (!state.busy) onSignOut else null,
                iconTint = colors.danger,
                labelColor = colors.danger,
                labelWeight = FontWeight.SemiBold,
            ) { if (state.busy) BusySpinner(colors.danger) }
            ProfileRow(
                Icons.Rounded.PersonRemove, stringResource(Res.string.delete_account), { onPrivacy(true) }, divider = false,
                iconTint = colors.muted, labelColor = colors.muted,
            )
        }
    }
}

@Composable
private fun BusySpinner(color: Color, size: Dp = 18.dp) {
    CircularProgressIndicator(Modifier.size(size), color = color, strokeWidth = 2.dp)
}

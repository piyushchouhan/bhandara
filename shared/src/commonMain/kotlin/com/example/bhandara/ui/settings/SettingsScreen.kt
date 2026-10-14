package com.example.bhandara.ui.settings

import com.example.bhandara.shared.resources.notif_saved_feasts_note
import com.example.bhandara.shared.resources.notif_saved_feasts
import androidx.compose.material.icons.outlined.NoMeals
import com.example.bhandara.shared.resources.notifications_on_count
import com.example.bhandara.shared.resources.quiet_hours_note
import com.example.bhandara.shared.resources.quiet_hours
import com.example.bhandara.shared.resources.notif_claim_status_note
import com.example.bhandara.shared.resources.notif_claim_status
import com.example.bhandara.shared.resources.notif_shop_reviews
import com.example.bhandara.shared.resources.notif_my_shop
import com.example.bhandara.shared.resources.notif_report_updates_note
import com.example.bhandara.shared.resources.notif_report_updates
import com.example.bhandara.shared.resources.notif_contributions
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Reviews
import androidx.compose.material.icons.outlined.Flag
import com.example.bhandara.data.settings.NotificationKind
import com.example.bhandara.shared.resources.privacy_policy
import com.example.bhandara.platform.ExternalLinks
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.Egg
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PersonRemove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import com.example.bhandara.data.network.UserApi
import com.example.bhandara.data.settings.AppPreferences
import com.example.bhandara.data.settings.DirectionsApp
import com.example.bhandara.data.settings.StartTab
import com.example.bhandara.data.settings.TextSize
import com.example.bhandara.data.settings.ThemeMode
import com.example.bhandara.platform.APP_LANGUAGES
import com.example.bhandara.platform.BackPress
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.LocalPlatformActions
import com.example.bhandara.platform.rememberAppLanguage
import com.example.bhandara.platform.rememberPermissionStatuses
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.allow_notifications
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.cache_cleared
import com.example.bhandara.shared.resources.clear_cache
import com.example.bhandara.shared.resources.directions_apple
import com.example.bhandara.shared.resources.directions_choose
import com.example.bhandara.shared.resources.directions_google
import com.example.bhandara.shared.resources.directions_open_in
import com.example.bhandara.shared.resources.feasts_near_me
import com.example.bhandara.shared.resources.feasts_near_me_note
import com.example.bhandara.shared.resources.language
import com.example.bhandara.shared.resources.language_ios_note
import com.example.bhandara.shared.resources.language_note
import com.example.bhandara.shared.resources.location_while_using
import com.example.bhandara.shared.resources.notif_finding_food
import com.example.bhandara.shared.resources.notif_save_failed
import com.example.bhandara.shared.resources.notifications_allowed
import com.example.bhandara.shared.resources.notifications_blocked
import com.example.bhandara.shared.resources.open_app_on
import com.example.bhandara.shared.resources.preview_shop_name
import com.example.bhandara.shared.resources.preview_shop_note
import com.example.bhandara.shared.resources.privacy_data
import com.example.bhandara.shared.resources.privacy_delete
import com.example.bhandara.shared.resources.privacy_location_access
import com.example.bhandara.shared.resources.privacy_notifications
import com.example.bhandara.shared.resources.radius_km
import com.example.bhandara.shared.resources.radius_m
import com.example.bhandara.shared.resources.search_radius
import com.example.bhandara.shared.resources.settings
import com.example.bhandara.shared.resources.settings_alerts
import com.example.bhandara.shared.resources.settings_data
import com.example.bhandara.shared.resources.settings_food
import com.example.bhandara.shared.resources.settings_footer
import com.example.bhandara.shared.resources.settings_general
import com.example.bhandara.shared.resources.settings_map
import com.example.bhandara.shared.resources.settings_no_match
import com.example.bhandara.shared.resources.settings_off
import com.example.bhandara.shared.resources.settings_search
import com.example.bhandara.shared.resources.settings_section_text
import com.example.bhandara.shared.resources.settings_section_theme
import com.example.bhandara.shared.resources.settings_theme
import com.example.bhandara.shared.resources.show_egg_items
import com.example.bhandara.shared.resources.show_egg_note
import com.example.bhandara.shared.resources.start_feasts
import com.example.bhandara.shared.resources.start_shops
import com.example.bhandara.shared.resources.text_default
import com.example.bhandara.shared.resources.text_large
import com.example.bhandara.shared.resources.text_largest
import com.example.bhandara.shared.resources.text_size
import com.example.bhandara.shared.resources.text_small
import com.example.bhandara.shared.resources.theme_and_text
import com.example.bhandara.shared.resources.theme_dark
import com.example.bhandara.shared.resources.theme_light
import com.example.bhandara.shared.resources.theme_system
import com.example.bhandara.shared.resources.veg_only
import com.example.bhandara.shared.resources.veg_only_note
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

// Settings (design "Settings": 1a side menu entry, 1b main, 1c language, 1d notifications, 1e theme & text; 1f
// location access is Privacy and data's page). Only what the app really does is offered: no "feast starting soon",
// "saved cart nearby" or "likes on my reviews" notifications, vendor auto-stop, battery or data saver, or "name on
// reviews".

/**
 * Settings, from the side menu
 *
 * @param appleMaps the phone's own maps app is Apple Maps (iPhone); on Android it's a choice each time
 * @param onOpenLocation location access, on Privacy and data
 * @param onOpenPrivacy Privacy and data
 * @param onDeleteAccount Privacy and data, on deleting the account
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenLocation: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onDeleteAccount: () -> Unit,
    appleMaps: Boolean,
) {
    val dependencies = LocalAppDependencies.current
    val preferences = dependencies.settings.preferences
    val viewModel = viewModel { SettingsViewModel(preferences, UserApi(dependencies.apiClient)) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.notificationsFailed) {
        if (state.notificationsFailed) {
            viewModel.onMessageShown()
            snackbar.showSnackbar(getString(Res.string.notif_save_failed))
        }
    }
    BackPress(enabled = state.page != SettingsPage.MAIN) { viewModel.back() }
    val goBack: () -> Unit = { if (!viewModel.back()) onBack() }

    FeastTheme {
        val colors = MaterialTheme.feastColors
        Box(Modifier.fillMaxSize().background(colors.page)) {
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).imePadding()) {
                val title = when (state.page) {
                    SettingsPage.MAIN -> Res.string.settings
                    SettingsPage.LANGUAGE -> Res.string.language
                    SettingsPage.NOTIFICATIONS -> Res.string.privacy_notifications
                    SettingsPage.APPEARANCE -> Res.string.theme_and_text
                }
                Row(
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), goBack)
                    Text(
                        stringResource(title), color = colors.ink, fontSize = if (state.page == SettingsPage.MAIN) 22.sp else 20.sp,
                        fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f),
                    )
                }
                val pageModifier = Modifier.weight(1f)
                when (state.page) {
                    SettingsPage.MAIN -> MainPage(
                        state, preferences, viewModel, appleMaps, dependencies.appVersion,
                        onOpenLocation, onOpenPrivacy, onDeleteAccount,
                        onCacheCleared = { snackbar.showSnackbar(it) },
                        modifier = pageModifier,
                    )
                    SettingsPage.LANGUAGE -> LanguagePage(pageModifier)
                    SettingsPage.NOTIFICATIONS -> NotificationsPage(preferences, state.saving, viewModel::setNotification, pageModifier)
                    SettingsPage.APPEARANCE -> AppearancePage(preferences, pageModifier)
                }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = 24.dp))
        }
    }
}

// ==================== 1b: the main page ====================

/** A row of the main page, with the words "Search settings" looks in */
private class Item(val words: String, val content: @Composable (divider: Boolean) -> Unit)

@Composable
private fun MainPage(
    state: SettingsState,
    preferences: AppPreferences,
    viewModel: SettingsViewModel,
    appleMaps: Boolean,
    appVersion: String,
    onOpenLocation: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onDeleteAccount: () -> Unit,
    onCacheCleared: suspend (String) -> Unit,
    modifier: Modifier,
) {
    val colors = MaterialTheme.feastColors
    val language = rememberAppLanguage()
    val permissions = rememberPermissionStatuses()
    val imageLoader = SingletonImageLoader.get(LocalPlatformContext.current)
    val actions = LocalPlatformActions.current
    val apiBaseUrl = LocalAppDependencies.current.apiClient.baseUrl
    val scope = rememberCoroutineScope()
    var cacheBytes by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(imageLoader) { cacheBytes = withContext(Dispatchers.Default) { imageLoader.diskCache?.size } }

    val languageName = APP_LANGUAGES.first { it.first == language.current }.second
    val themeLabel = stringResource(themeName(preferences.theme))
    val textLabel = stringResource(textSizeName(preferences.textSize))
    val startLabel = stringResource(if (preferences.startTab == StartTab.FEASTS) Res.string.start_feasts else Res.string.start_shops)
    val directionsLabel = stringResource(directionsName(preferences.directionsApp, appleMaps))
    // How many kinds of notification are on (quiet hours isn't one); none when the phone blocks them
    val notificationsOn = if (permissions.notifications == false) 0 else NOTIFICATION_KINDS.count(preferences::notification)
    val cleared = stringResource(Res.string.cache_cleared)

    val general = listOf(
        Item(stringResource(Res.string.language) + " " + APP_LANGUAGES.joinToString(" ") { it.second }) { divider ->
            ValueRow(Icons.Outlined.Language, stringResource(Res.string.language), languageName, divider) { viewModel.open(SettingsPage.LANGUAGE) }
        },
        Item(stringResource(Res.string.settings_theme)) { divider ->
            ValueRow(Icons.Outlined.Contrast, stringResource(Res.string.settings_theme), themeLabel, divider) { viewModel.open(SettingsPage.APPEARANCE) }
        },
        Item(stringResource(Res.string.text_size)) { divider ->
            ValueRow(Icons.Outlined.FormatSize, stringResource(Res.string.text_size), textLabel, divider) { viewModel.open(SettingsPage.APPEARANCE) }
        },
    )
    val map = listOf(
        Item(stringResource(Res.string.search_radius)) { divider -> RadiusRow(preferences.searchRadiusMeters, preferences::updateSearchRadius, divider) },
        Item(stringResource(Res.string.open_app_on)) { divider ->
            ChoiceRow(
                Icons.Outlined.Home, stringResource(Res.string.open_app_on), startLabel, divider,
                options = listOf(StartTab.SHOPS to stringResource(Res.string.start_shops), StartTab.FEASTS to stringResource(Res.string.start_feasts)),
                onPick = preferences::updateStartTab,
            )
        },
        Item(stringResource(Res.string.directions_open_in)) { divider ->
            ChoiceRow(
                Icons.Rounded.Directions, stringResource(Res.string.directions_open_in), directionsLabel, divider,
                options = DirectionsApp.entries.map { it to stringResource(directionsName(it, appleMaps)) },
                onPick = preferences::updateDirectionsApp,
            )
        },
    )
    val food = listOf(
        Item(stringResource(Res.string.veg_only) + " " + stringResource(Res.string.veg_only_note)) { divider ->
            SwitchRow(
                icon = { VegMark() }, tile = colors.successSoft, label = stringResource(Res.string.veg_only), note = stringResource(Res.string.veg_only_note),
                checked = preferences.vegOnly, onChange = preferences::updateVegOnly, divider = divider,
            )
        },
        Item(stringResource(Res.string.show_egg_items) + " " + stringResource(Res.string.show_egg_note)) { divider ->
            SwitchRow(
                icon = { TileIcon(Icons.Outlined.Egg, colors.saffronText) }, tile = colors.saffronSoft, label = stringResource(Res.string.show_egg_items),
                note = stringResource(Res.string.show_egg_note), checked = preferences.showEggItems, onChange = preferences::updateShowEggItems, divider = divider,
            )
        },
    )
    val alerts = listOf(
        Item(stringResource(Res.string.privacy_notifications)) { divider ->
            ValueRow(
                Icons.Outlined.Notifications, stringResource(Res.string.privacy_notifications),
                if (notificationsOn > 0) stringResource(Res.string.notifications_on_count, notificationsOn) else stringResource(Res.string.settings_off), divider,
            ) { viewModel.open(SettingsPage.NOTIFICATIONS) }
        },
        Item(stringResource(Res.string.privacy_location_access)) { divider ->
            ValueRow(
                Icons.Rounded.MyLocation, stringResource(Res.string.privacy_location_access),
                stringResource(if (permissions.location) Res.string.location_while_using else Res.string.settings_off), divider,
                valueColor = if (permissions.location) colors.success else null, onClick = onOpenLocation,
            )
        },
    )
    val data = listOf(
        Item(stringResource(Res.string.clear_cache)) { divider ->
            ValueRow(Icons.Outlined.CleaningServices, stringResource(Res.string.clear_cache), cacheBytes?.let(::megabytes), divider, chevron = false) {
                scope.launch {
                    withContext(Dispatchers.Default) { imageLoader.diskCache?.clear() }
                    imageLoader.memoryCache?.clear()
                    cacheBytes = 0
                    onCacheCleared(cleared)
                }
            }
        },
        Item(stringResource(Res.string.privacy_data)) { divider ->
            ValueRow(Icons.Outlined.Shield, stringResource(Res.string.privacy_data), null, divider, onClick = onOpenPrivacy)
        },
        Item(stringResource(Res.string.privacy_policy)) { divider ->
            ValueRow(Icons.Outlined.Policy, stringResource(Res.string.privacy_policy), null, divider, chevron = false, trailingIcon = Icons.AutoMirrored.Rounded.OpenInNew) {
                actions.openUrl(ExternalLinks.privacyPolicy(apiBaseUrl))
            }
        },
    )
    val delete = listOf(
        Item(stringResource(Res.string.privacy_delete)) { divider ->
            ValueRow(
                Icons.Rounded.PersonRemove, stringResource(Res.string.privacy_delete), null, divider,
                tile = colors.dangerSoft, tint = colors.danger, labelColor = colors.danger, onClick = onDeleteAccount,
            )
        },
    )

    val query = state.query.trim()
    fun List<Item>.matching() = if (query.isEmpty()) this else filter { it.words.contains(query, ignoreCase = true) }
    val sections = listOf(
        Res.string.settings_general to general.matching(),
        Res.string.settings_map to map.matching(),
        Res.string.settings_food to food.matching(),
        Res.string.settings_alerts to alerts.matching(),
        Res.string.settings_data to data.matching(),
        null to delete.matching(),
    ).filter { it.second.isNotEmpty() }

    Column(
        modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 40.dp)
            .windowInsetsPadding(WindowInsets.navigationBars),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SearchField(state.query, viewModel::onQueryChange)
        sections.forEach { (title, items) ->
            Column {
                title?.let { SectionTitle(stringResource(it)) }
                Card { items.forEachIndexed { index, item -> item.content(index < items.lastIndex) } }
            }
        }
        if (sections.isEmpty()) {
            Text(
                stringResource(Res.string.settings_no_match, query), color = colors.muted, fontSize = 14.sp,
                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            )
        }
        if (query.isEmpty()) {
            Text(
                stringResource(Res.string.settings_footer, appVersion), color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.labelMedium, modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

/** The kinds of notification counted in "N on" */
private val NOTIFICATION_KINDS = NotificationKind.entries - NotificationKind.QUIET_HOURS

/** "24 MB"; under 10 MB with one decimal when there is one, e.g. "0.4 MB" */
internal fun megabytes(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    if (mb >= 10) return "${mb.roundToInt()} MB"
    val tenths = (mb * 10).roundToInt()
    return if (tenths % 10 == 0) "${tenths / 10} MB" else "${tenths / 10}.${tenths % 10} MB"
}

private fun themeName(theme: ThemeMode): StringResource = when (theme) {
    ThemeMode.SYSTEM -> Res.string.theme_system
    ThemeMode.LIGHT -> Res.string.theme_light
    ThemeMode.DARK -> Res.string.theme_dark
}

private fun textSizeName(size: TextSize): StringResource = when (size) {
    TextSize.SMALL -> Res.string.text_small
    TextSize.DEFAULT -> Res.string.text_default
    TextSize.LARGE -> Res.string.text_large
    TextSize.LARGEST -> Res.string.text_largest
}

private fun directionsName(app: DirectionsApp, appleMaps: Boolean): StringResource = when (app) {
    DirectionsApp.GOOGLE_MAPS -> Res.string.directions_google
    DirectionsApp.PHONE_MAPS -> if (appleMaps) Res.string.directions_apple else Res.string.directions_choose
}

@Composable
private fun SearchField(text: String, onChange: (String) -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.fillMaxWidth().height(48.dp).background(colors.surface, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = colors.placeholder, modifier = Modifier.size(20.dp))
        Box(Modifier.weight(1f)) {
            if (text.isEmpty()) Text(stringResource(Res.string.settings_search), color = colors.placeholder, fontSize = 15.sp, style = MaterialTheme.typography.bodyLarge)
            BasicTextField(
                text, onChange, singleLine = true, cursorBrush = SolidColor(colors.accent),
                textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = colors.ink, fontSize = 15.sp)),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (text.isNotEmpty()) {
            Icon(Icons.Rounded.Close, contentDescription = null, tint = colors.muted, modifier = Modifier.size(20.dp).clip(CircleShape).clickable { onChange("") })
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text, color = MaterialTheme.feastColors.muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp,
        style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 6.dp, end = 6.dp, bottom = 8.dp),
    )
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.feastColors.surface)) { content() }
}

/** The small rounded square behind a row's icon */
@Composable
private fun Tile(fill: Color, content: @Composable () -> Unit) {
    Box(Modifier.size(34.dp).background(fill, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun TileIcon(icon: ImageVector, tint: Color) {
    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
}

/** The green veg mark: a dot in a square */
@Composable
private fun VegMark() {
    val green = MaterialTheme.feastColors.success
    Box(Modifier.size(13.dp).border(1.5.dp, green), contentAlignment = Alignment.Center) {
        Box(Modifier.size(5.dp).background(green, CircleShape))
    }
}

@Composable
private fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.feastColors.lavender))
}

/** A row that opens something, with its current value on the right */
@Composable
private fun ValueRow(
    icon: ImageVector,
    label: String,
    value: String?,
    divider: Boolean,
    tile: Color? = null,
    tint: Color? = null,
    labelColor: Color? = null,
    valueColor: Color? = null,
    chevron: Boolean = true,
    trailingIcon: ImageVector? = null,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    Column {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Tile(tile ?: colors.lavender) { TileIcon(icon, tint ?: colors.accent) }
            Text(label, color = labelColor ?: colors.ink, fontSize = 15.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            value?.let { Text(it, color = valueColor ?: colors.muted, fontSize = 14.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium) }
            if (chevron) Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = colors.placeholder, modifier = Modifier.size(20.dp))
            trailingIcon?.let { Icon(it, contentDescription = null, tint = colors.placeholder, modifier = Modifier.size(18.dp)) }
        }
        if (divider) Divider()
    }
}

/** A row whose value is picked from a short menu that opens under it */
@Composable
private fun <T> ChoiceRow(
    icon: ImageVector,
    label: String,
    value: String,
    divider: Boolean,
    options: List<Pair<T, String>>,
    onPick: (T) -> Unit,
) {
    val colors = MaterialTheme.feastColors
    var open by remember { mutableStateOf(false) }
    Box {
        // The map group's icons are saffron
        ValueRow(icon, label, value, divider, tile = colors.saffronSoft, tint = colors.saffronText) { open = true }
        Box(Modifier.align(Alignment.BottomEnd)) {
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                options.forEach { (option, name) ->
                    DropdownMenuItem(text = { Text(name) }, onClick = { open = false; onPick(option) })
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(
    icon: @Composable () -> Unit,
    tile: Color,
    label: String,
    note: String?,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    divider: Boolean,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.feastColors
    Column {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(enabled = enabled) { onChange(!checked) }.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Tile(tile, icon)
            Column(Modifier.weight(1f)) {
                Text(label, color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                note?.let { Text(it, color = colors.muted, fontSize = 12.sp, lineHeight = 16.sp, style = MaterialTheme.typography.bodySmall) }
            }
            FeastSwitch(checked, onChange, enabled)
        }
        if (divider) Divider()
    }
}

/** An indigo on/off switch, as in the design */
@Composable
private fun FeastSwitch(checked: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true, onIndigo: Boolean = false) {
    val colors = MaterialTheme.feastColors
    val thumb = if (colors.isDark) colors.surface else Color.White
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedTrackColor = if (onIndigo) colors.saffron else colors.indigo,
            checkedThumbColor = if (onIndigo) colors.indigo else thumb,
            checkedBorderColor = Color.Transparent,
            uncheckedTrackColor = colors.border,
            uncheckedThumbColor = thumb,
            uncheckedBorderColor = Color.Transparent,
            disabledCheckedTrackColor = (if (onIndigo) colors.saffron else colors.indigo).copy(alpha = 0.5f),
            disabledCheckedThumbColor = thumb,
        ),
    )
}

/** Search radius: four chips, the picked one dark */
@Composable
private fun RadiusRow(meters: Int, onPick: (Int) -> Unit, divider: Boolean) {
    val colors = MaterialTheme.feastColors
    Column {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Tile(colors.saffronSoft) { TileIcon(Icons.Outlined.Radar, colors.saffronText) }
                Text(stringResource(Res.string.search_radius), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AppPreferences.RADII.forEach { radius ->
                    val picked = radius == meters
                    Box(
                        Modifier.weight(1f).height(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(if (picked) colors.indigo else colors.lavenderSoft)
                            .clickable { onPick(radius) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (radius < 1_000) stringResource(Res.string.radius_m, radius) else stringResource(Res.string.radius_km, radius / 1_000),
                            color = if (picked) colors.onIndigo else colors.ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
        if (divider) Divider()
    }
}

// ==================== 1c: language ====================

/** A language's tile letters, its name in English, and a sample line in it, so anyone can find their own */
private val LANGUAGE_SAMPLES = mapOf(
    "en" to Triple("En", "", "Find food near you"),
    "hi" to Triple("हि", "Hindi", "पास में खाना खोजें"),
    "mr" to Triple("म", "Marathi", "जवळ जेवण शोधा"),
)

@Composable
private fun LanguagePage(modifier: Modifier) {
    val colors = MaterialTheme.feastColors
    val language = rememberAppLanguage()
    Column(
        modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        APP_LANGUAGES.forEachIndexed { index, (tag, name) ->
            val (letters, english, sample) = LANGUAGE_SAMPLES[tag] ?: Triple(tag, "", "")
            val picked = tag == language.current
            val (tileFill, tileText) = when {
                picked -> colors.indigo to colors.onIndigo
                index % 2 == 1 -> colors.saffronSoft to colors.saffronText
                else -> colors.successSoft to colors.success
            }
            val shape = RoundedCornerShape(20.dp)
            Row(
                Modifier.fillMaxWidth().clip(shape).background(colors.surface)
                    .then(if (picked) Modifier.border(2.dp, colors.selectedOutline, shape) else Modifier)
                    .clickable { if (!picked || !language.choosableInApp) language.choose(tag) }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(Modifier.size(48.dp).background(tileFill, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                    Text(letters, color = tileText, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                }
                Column(Modifier.weight(1f)) {
                    Text(name, color = colors.ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (english.isEmpty()) sample else "$english · $sample", color = colors.muted, fontSize = 13.sp,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (picked) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = colors.selectedOutline, modifier = Modifier.size(24.dp))
                } else {
                    Box(Modifier.size(22.dp).border(2.dp, colors.border, CircleShape))
                }
            }
        }
        Note(stringResource(Res.string.language_note))
        if (!language.choosableInApp) Note(stringResource(Res.string.language_ios_note))
    }
}

@Composable
private fun Note(text: String) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.fillMaxWidth().padding(top = 6.dp).background(colors.lavenderSoft, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Rounded.Translate, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
        Text(text, color = colors.body, fontSize = 13.sp, lineHeight = 18.sp, style = MaterialTheme.typography.bodySmall)
    }
}

// ==================== 1d: notifications ====================

/**
 * Whether the phone lets the app notify (only the phone's settings can change that), the notifications the app
 * sends, grouped by who you are (looking for food, a contributor, a shop owner), and quiet hours
 */
@Composable
private fun NotificationsPage(
    preferences: AppPreferences,
    saving: Set<NotificationKind>,
    onChange: (NotificationKind, Boolean) -> Unit,
    modifier: Modifier,
) {
    val colors = MaterialTheme.feastColors
    // Not yet known counts as allowed, so the page doesn't flash "off"
    val allowed = rememberPermissionStatuses().notifications != false
    val actions = LocalPlatformActions.current
    Column(
        modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.indigo).clickable(onClick = actions::openAppSettings).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = colors.saffron, modifier = Modifier.size(24.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(Res.string.allow_notifications), color = colors.onIndigo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(if (allowed) Res.string.notifications_allowed else Res.string.notifications_blocked),
                    color = colors.headerSubtext, fontSize = 12.sp, lineHeight = 16.sp, style = MaterialTheme.typography.bodySmall,
                )
            }
            // Shows the phone's setting; tapping opens the phone's settings to change it
            FeastSwitch(allowed, onChange = { actions.openAppSettings() }, onIndigo = true)
        }
        @Composable
        fun Toggle(kind: NotificationKind, icon: ImageVector, saffron: Boolean, label: StringResource, note: StringResource?, divider: Boolean) {
            SwitchRow(
                icon = { TileIcon(icon, if (saffron) colors.saffronText else colors.accent) },
                tile = if (saffron) colors.saffronSoft else colors.lavender,
                label = stringResource(label), note = note?.let { stringResource(it) },
                checked = preferences.notification(kind), onChange = { onChange(kind, it) }, divider = divider, enabled = kind !in saving,
            )
        }
        Column {
            SectionTitle(stringResource(Res.string.notif_finding_food))
            Card {
                Toggle(NotificationKind.NEARBY_FEASTS, Icons.Outlined.VolunteerActivism, true, Res.string.feasts_near_me, Res.string.feasts_near_me_note, divider = true)
                Toggle(NotificationKind.SAVED_FEASTS, Icons.Outlined.NoMeals, true, Res.string.notif_saved_feasts, Res.string.notif_saved_feasts_note, divider = false)
            }
        }
        Column {
            SectionTitle(stringResource(Res.string.notif_contributions))
            Card { Toggle(NotificationKind.REPORT_UPDATES, Icons.Outlined.Flag, false, Res.string.notif_report_updates, Res.string.notif_report_updates_note, divider = false) }
        }
        Column {
            SectionTitle(stringResource(Res.string.notif_my_shop))
            Card {
                Toggle(NotificationKind.SHOP_REVIEWS, Icons.Outlined.Reviews, false, Res.string.notif_shop_reviews, null, divider = true)
                Toggle(NotificationKind.CLAIM_STATUS, Icons.Outlined.Verified, false, Res.string.notif_claim_status, Res.string.notif_claim_status_note, divider = false)
            }
        }
        Card { Toggle(NotificationKind.QUIET_HOURS, Icons.Outlined.Bedtime, false, Res.string.quiet_hours, Res.string.quiet_hours_note, divider = false) }
    }
}

// ==================== 1e: theme & text ====================

@Composable
private fun AppearancePage(preferences: AppPreferences, modifier: Modifier) {
    val colors = MaterialTheme.feastColors
    Column(
        modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // The preview follows both choices as they're made
        Row(
            Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(20.dp)).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(52.dp).background(colors.saffronSoft, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Storefront, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(26.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(stringResource(Res.string.preview_shop_name), color = colors.ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                Text(stringResource(Res.string.preview_shop_note), color = colors.muted, fontSize = 13.sp, style = MaterialTheme.typography.bodySmall)
            }
        }
        Column {
            SectionTitle(stringResource(Res.string.settings_section_theme))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM).forEach { mode ->
                    ThemeCard(mode, picked = preferences.theme == mode, onClick = { preferences.updateTheme(mode) }, modifier = Modifier.weight(1f))
                }
            }
        }
        Column {
            SectionTitle(stringResource(Res.string.settings_section_text))
            TextSizeCard(preferences.textSize, preferences::updateTextSize)
        }
    }
}

/** A little picture of the app in each theme */
@Composable
private fun ThemeCard(mode: ThemeMode, picked: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(18.dp)
    // The pictures keep their own colours whatever the current theme is
    val lightPage = Color(0xFFFAF7F1)
    val darkPage = Color(0xFF15132E)
    Column(
        modifier.clip(shape).background(colors.surface).then(if (picked) Modifier.border(2.dp, colors.selectedOutline, shape) else Modifier)
            .clickable(onClick = onClick).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val pictureShape = RoundedCornerShape(12.dp)
        val picture = Modifier.fillMaxWidth().height(80.dp).clip(pictureShape)
        when (mode) {
            ThemeMode.SYSTEM -> Row(picture.border(1.dp, colors.lavender, pictureShape)) {
                Box(Modifier.weight(1f).fillMaxHeight().background(lightPage))
                Box(Modifier.weight(1f).fillMaxHeight().background(darkPage))
            }
            else -> {
                val dark = mode == ThemeMode.DARK
                Column(
                    picture.background(if (dark) darkPage else lightPage)
                        .then(if (dark) Modifier else Modifier.border(1.dp, colors.lavender, pictureShape)).padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Box(Modifier.fillMaxWidth(0.6f).height(8.dp).background(if (dark) Color.White else Color(0xFF1E1B4B), RoundedCornerShape(4.dp)))
                    Box(Modifier.fillMaxWidth(0.8f).height(6.dp).background(if (dark) Color(0xFF2E2A6B) else Color(0xFFE3E1F0), RoundedCornerShape(3.dp)))
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.fillMaxWidth().height(16.dp).background(Color(0xFFF59E0B), RoundedCornerShape(6.dp)))
                }
            }
        }
        Text(
            stringResource(themeName(mode)), color = colors.ink, fontSize = 13.sp,
            fontWeight = if (picked) FontWeight.SemiBold else FontWeight.Medium, style = MaterialTheme.typography.labelLarge,
        )
    }
}

/** Four text sizes on a slider, small A to large A */
@Composable
private fun TextSizeCard(size: TextSize, onPick: (TextSize) -> Unit) {
    val colors = MaterialTheme.feastColors
    val sizes = TextSize.entries
    Column(Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(20.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("A", color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
            Slider(
                value = size.ordinal.toFloat(),
                onValueChange = { onPick(sizes[it.roundToInt().coerceIn(0, sizes.lastIndex)]) },
                valueRange = 0f..sizes.lastIndex.toFloat(),
                steps = sizes.size - 2,
                colors = SliderDefaults.colors(
                    thumbColor = colors.saffron,
                    activeTrackColor = colors.indigo,
                    inactiveTrackColor = colors.border,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                ),
                modifier = Modifier.weight(1f),
            )
            Text("A", color = colors.ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            sizes.forEach { option ->
                val picked = option == size
                Text(
                    stringResource(textSizeName(option)), color = if (picked) colors.ink else colors.muted, fontSize = 12.sp,
                    fontWeight = if (picked) FontWeight.SemiBold else FontWeight.Medium, style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.width(64.dp).clickable { onPick(option) },
                    textAlign = when (option) {
                        sizes.first() -> TextAlign.Start
                        sizes.last() -> TextAlign.End
                        else -> TextAlign.Center
                    },
                )
            }
        }
    }
}

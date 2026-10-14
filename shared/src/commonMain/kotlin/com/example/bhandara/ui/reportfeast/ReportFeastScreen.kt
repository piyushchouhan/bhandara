package com.example.bhandara.ui.reportfeast

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HomeWork
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.data.location.GeoLocation
import com.example.bhandara.data.location.LocationResult
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.rememberDeviceLocation
import com.example.bhandara.data.location.rememberLocationPermissionRequest
import com.example.bhandara.data.network.FeastApi
import com.example.bhandara.photos.PhotoSourceSheet
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.cancel
import com.example.bhandara.shared.resources.choose
import com.example.bhandara.shared.resources.clear_all
import com.example.bhandara.shared.resources.clear_all_text
import com.example.bhandara.shared.resources.clear_all_title
import com.example.bhandara.shared.resources.date_pick
import com.example.bhandara.shared.resources.date_today
import com.example.bhandara.shared.resources.date_tomorrow
import com.example.bhandara.shared.resources.feast_address
import com.example.bhandara.shared.resources.feast_date
import com.example.bhandara.shared.resources.feast_ends
import com.example.bhandara.shared.resources.feast_ends_next_day
import com.example.bhandara.shared.resources.feast_food_type
import com.example.bhandara.shared.resources.feast_landmark
import com.example.bhandara.shared.resources.feast_menu_hint
import com.example.bhandara.shared.resources.feast_menu_label
import com.example.bhandara.shared.resources.feast_organizer
import com.example.bhandara.shared.resources.feast_people
import com.example.bhandara.shared.resources.feast_phone
import com.example.bhandara.shared.resources.feast_photos_label
import com.example.bhandara.shared.resources.feast_place_info
import com.example.bhandara.shared.resources.feast_starts
import com.example.bhandara.shared.resources.food_type_jain
import com.example.bhandara.shared.resources.food_type_mixed
import com.example.bhandara.shared.resources.food_type_non_veg
import com.example.bhandara.shared.resources.food_type_veg
import com.example.bhandara.shared.resources.map_allow_location
import com.example.bhandara.shared.resources.next
import com.example.bhandara.shared.resources.post_feast
import com.example.bhandara.shared.resources.report_feast_title
import com.example.bhandara.shared.resources.edit_feast_save
import com.example.bhandara.shared.resources.edit_feast_title
import com.example.bhandara.shared.resources.review_remove_photo
import com.example.bhandara.shared.resources.review_uploading
import com.example.bhandara.shared.resources.section_description
import com.example.bhandara.shared.resources.step_food
import com.example.bhandara.shared.resources.step_when
import com.example.bhandara.shared.resources.step_where
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastChipsField
import com.example.bhandara.ui.components.FeastFormField
import com.example.bhandara.ui.components.FeastSectionLabel
import com.example.bhandara.ui.components.FeastStepper
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.feasts.formatFeastDay
import com.example.bhandara.ui.feasts.formatFeastTime
import com.example.bhandara.ui.feasts.localNow
import com.example.bhandara.ui.map.CameraTarget
import com.example.bhandara.ui.map.MapLook
import com.example.bhandara.ui.map.MapPin
import com.example.bhandara.ui.map.PlatformMap
import com.example.bhandara.ui.theme.FeastColors
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val FeastStep.label: StringResource
    get() = when (this) {
        FeastStep.FOOD -> Res.string.step_food
        FeastStep.WHEN -> Res.string.step_when
        FeastStep.WHERE -> Res.string.step_where
    }

/** The food types to pick from; sent as these English words so the feast page and moderators read them the same */
private val FOOD_TYPES = listOf(
    "Veg" to Res.string.food_type_veg,
    "Jain" to Res.string.food_type_jain,
    "Non-veg" to Res.string.food_type_non_veg,
    "Veg & non-veg" to Res.string.food_type_mixed,
)

/** The message the form gives when a photo is missing; the photo tile shows it in place of the bottom bar */
private const val PHOTO_MISSING = "Add at least one photo of the food or the place"

/**
 * Posting a community feast, shared by Android and iOS (the redesign's 1e–1i): a three-step form in saffron
 * (food & photos, when & contact, place), with one-tap date and time sheets and a map showing where the feast will
 * go. Once posted, [onPosted] gets the new feast so the app can open it.
 */
@Composable
fun ReportFeastScreen(onBack: () -> Unit, onPosted: (feastId: String) -> Unit, start: FeastFormStart = FeastFormStart.New) {
    val dependencies = LocalAppDependencies.current
    val deviceLocation = rememberDeviceLocation()
    val viewModel = viewModel {
        ReportFeastViewModel(
            FeastApi(dependencies.apiClient),
            dependencies.photoUploader::upload,
            LocationService(deviceLocation),
            { dependencies.auth.currentUid },
            start = start,
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmingClear by rememberSaveable { mutableStateOf(false) }
    val requestLocation = rememberLocationPermissionRequest { granted -> if (granted) viewModel.submit() }

    LaunchedEffect(state.postedFeastId) { state.postedFeastId?.let(onPosted) }

    FeastTheme {
        val colors = MaterialTheme.feastColors
        Column(Modifier.fillMaxSize().background(colors.page).windowInsetsPadding(WindowInsets.statusBars).imePadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), { if (!viewModel.back()) onBack() }, enabled = !state.submitting)
                Text(stringResource(if (state.isEditing) Res.string.edit_feast_title else Res.string.report_feast_title), color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                // Editing or reposting starts from a feast; there's nothing to clear
                if (state.start == FeastFormStart.New) {
                    Text(
                        stringResource(Res.string.clear_all), color = colors.saffronText.copy(alpha = if (state.submitting) 0.4f else 1f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable(enabled = !state.submitting) { confirmingClear = true }.padding(horizontal = 6.dp, vertical = 8.dp),
                    )
                }
            }
            FeastStepper(FeastStep.entries.map { stringResource(it.label) }, state.step.ordinal)
            if (state.loading) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.saffron)
                }
                return@Column
            }
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (state.step) {
                    FeastStep.FOOD -> FoodStep(state, viewModel)
                    FeastStep.WHEN -> WhenStep(state.form, viewModel)
                    FeastStep.WHERE -> WhereStep(state.form, viewModel, deviceLocation)
                }
            }
            BottomBar(
                state = state,
                canGoOn = viewModel.canGoOn(),
                onBack = { viewModel.back() },
                onNext = viewModel::next,
                onPost = viewModel::submit,
                onAllowLocation = requestLocation,
            )
        }

        if (confirmingClear) {
            AlertDialog(
                onDismissRequest = { confirmingClear = false },
                title = { Text(stringResource(Res.string.clear_all_title)) },
                text = { Text(stringResource(Res.string.clear_all_text)) },
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
}

/** Next (or Post feast on the last step), Back after the first, and what's wrong or uploading above them */
@Composable
private fun BottomBar(
    state: ReportFeastState,
    canGoOn: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onPost: () -> Unit,
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
        // A missing photo is shown on the photo tile instead
        state.error?.takeIf { !(it == PHOTO_MISSING && state.step == FeastStep.FOOD) }?.let {
            Text(it, color = colors.danger, fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium)
        }
        if (state.needsLocation) {
            FeastButton(stringResource(Res.string.map_allow_location), onClick = onAllowLocation, style = FeastButtonStyle.SOFT, icon = Icons.Rounded.MyLocation, modifier = Modifier.fillMaxWidth())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.step != FeastStep.FOOD) {
                FeastButton(stringResource(Res.string.back), onClick = onBack, style = FeastButtonStyle.SOFT, enabled = !state.submitting, height = 56.dp, modifier = Modifier.weight(1f))
            }
            if (state.isLastStep) {
                FeastButton(
                    label = stringResource(if (state.isEditing) Res.string.edit_feast_save else Res.string.post_feast),
                    onClick = onPost,
                    style = FeastButtonStyle.SAFFRON,
                    icon = Icons.Rounded.VolunteerActivism,
                    height = 56.dp,
                    modifier = Modifier.weight(1.4f),
                    content = if (state.submitting) {
                        { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.onSaffron) }
                    } else null,
                )
            } else {
                // Tapping Next on an unfinished step says what's missing, so it stays tappable
                FeastButton(stringResource(Res.string.next), onClick = onNext, dimmed = !canGoOn, height = 56.dp, modifier = Modifier.weight(1.4f))
            }
        }
    }
}

/** Step 1 (design 1e): photos, what's served as chips, food type and description */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FoodStep(state: ReportFeastState, viewModel: ReportFeastViewModel) {
    val colors = MaterialTheme.feastColors
    val form = state.form
    var picking by remember { mutableStateOf(false) }
    val photoMissing = state.error == PHOTO_MISSING && form.photoCount == 0

    Column {
        FeastSectionLabel(stringResource(Res.string.feast_photos_label, MAX_FEAST_PHOTOS))
        Row(Modifier.horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Photos the feast already has (editing or reposting), then the ones picked now
            form.photoUrls.forEach { url ->
                Box(Modifier.size(92.dp).clip(RoundedCornerShape(18.dp)).background(colors.lavender)) {
                    AsyncImage(url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(92.dp))
                    Box(
                        Modifier.align(Alignment.TopEnd).padding(5.dp).size(24.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.55f)).clickable(enabled = !state.submitting) { viewModel.removePhotoUrl(url) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Close, stringResource(Res.string.review_remove_photo), tint = Color.White, modifier = Modifier.size(15.dp))
                    }
                }
            }
            form.photos.forEach { photo ->
                Box(Modifier.size(92.dp).clip(RoundedCornerShape(18.dp)).background(colors.lavender)) {
                    AsyncImage(photo.preview, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(92.dp))
                    Box(
                        Modifier.align(Alignment.TopEnd).padding(5.dp).size(24.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.55f)).clickable(enabled = !state.submitting) { viewModel.removePhoto(photo) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Close, stringResource(Res.string.review_remove_photo), tint = Color.White, modifier = Modifier.size(15.dp))
                    }
                }
            }
            if (state.canAddPhotos) {
                val line = if (photoMissing) colors.danger else colors.starOff
                Column(
                    Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (photoMissing) colors.dangerSoft else colors.lavender)
                        .drawBehind {
                            drawRoundRect(line, cornerRadius = CornerRadius(18.dp.toPx()), style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))))
                        }
                        .clickable(enabled = !state.submitting) { picking = true },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                ) {
                    val tint = if (photoMissing) colors.danger else colors.accent
                    Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
                    Text("${form.photoCount} / $MAX_FEAST_PHOTOS", color = tint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                }
            }
            if (photoMissing) {
                Row(Modifier.widthIn(max = 200.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.Error, contentDescription = null, tint = colors.danger, modifier = Modifier.size(18.dp))
                    Text(PHOTO_MISSING, color = colors.danger, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    Column {
        FeastSectionLabel(stringResource(Res.string.feast_menu_label))
        FeastChipsField(
            items = form.menuItems,
            input = form.menuInput,
            hint = stringResource(Res.string.feast_menu_hint),
            onInputChange = viewModel::onMenuInputChange,
            onDone = viewModel::addMenuInput,
            onRemove = viewModel::removeMenuItem,
            enabled = !state.submitting,
            outline = colors.saffron,
        )
    }

    FoodTypeField(form.foodType, onChange = viewModel::onFoodTypeChange)

    Column {
        FeastFormField(form.description, stringResource(Res.string.section_description), viewModel::onDescriptionChange, height = 92.dp, singleLine = false)
        Text(
            "${form.description.length} / $MAX_FEAST_DESCRIPTION", color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
        )
    }

    if (picking) {
        PhotoSourceSheet(maxCount = MAX_FEAST_PHOTOS - form.photoCount, onPicked = viewModel::addPhotos, onDismiss = { picking = false })
    }
}

/** "Food type" as a menu of common types; a type typed in before (older posts) still shows */
@Composable
private fun FoodTypeField(value: String, onChange: (String) -> Unit) {
    val colors = MaterialTheme.feastColors
    var open by remember { mutableStateOf(false) }
    val shown = FOOD_TYPES.firstOrNull { it.first.equals(value, ignoreCase = true) }?.let { stringResource(it.second) } ?: value
    Box {
        val shape = RoundedCornerShape(16.dp)
        Row(
            Modifier.fillMaxWidth().height(54.dp).clip(shape).background(colors.surface).border(1.5.dp, colors.border, shape).clickable { open = true }.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                shown.ifEmpty { stringResource(Res.string.feast_food_type) }, color = if (shown.isEmpty()) colors.placeholder else colors.ink,
                fontSize = 16.sp, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f),
            )
            Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = colors.placeholder, modifier = Modifier.size(22.dp))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, shape = RoundedCornerShape(16.dp), containerColor = colors.surface) {
            FOOD_TYPES.forEach { (sent, label) ->
                DropdownMenuItem(text = { Text(stringResource(label), color = colors.ink) }, onClick = { open = false; onChange(sent) })
            }
        }
    }
}

private enum class Picking { DATE, START, END }

/** Step 2 (design 1f): the date with Today / Tomorrow, start and end side by side, organizer and phone */
@Composable
private fun WhenStep(form: FeastForm, viewModel: ReportFeastViewModel) {
    val colors = MaterialTheme.feastColors
    var picking by rememberSaveable { mutableStateOf<Picking?>(null) }
    val today = remember { localNow().date }
    val tomorrow = today.plus(DatePeriod(days = 1))

    Column(Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(20.dp)).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(44.dp).background(colors.saffronSoft, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(22.dp))
            }
            PickedValue(Res.string.feast_date, form.date?.let(::formatFeastDay))
        }
        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            QuickPick(stringResource(Res.string.date_today), selected = form.date == today) { viewModel.onDateChange(today) }
            QuickPick(stringResource(Res.string.date_tomorrow), selected = form.date == tomorrow) { viewModel.onDateChange(tomorrow) }
            QuickPick(stringResource(Res.string.date_pick), selected = form.date != null && form.date != today && form.date != tomorrow, icon = Icons.Rounded.Event) { picking = Picking.DATE }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TimeCard(Res.string.feast_starts, form.startTime) { picking = Picking.START }
            TimeCard(Res.string.feast_ends, form.endTime) { picking = Picking.END }
        }
        val start = form.startTime
        val end = form.endTime
        if (start != null && end != null && end < start) {
            Text(stringResource(Res.string.feast_ends_next_day), color = colors.saffronText, fontSize = 13.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp))
        }
    }
    FeastFormField(form.organizerName, stringResource(Res.string.feast_organizer), viewModel::onOrganizerNameChange, icon = Icons.Rounded.Groups)
    FeastFormField(form.contactPhone, stringResource(Res.string.feast_phone), viewModel::onContactPhoneChange, icon = Icons.Rounded.Call, keyboardType = KeyboardType.Phone)

    when (picking) {
        Picking.DATE -> FeastDateSheet(form.date, onPicked = viewModel::onDateChange, onDismiss = { picking = null })
        Picking.START -> FeastTimeSheet(form.startTime ?: LocalTime(12, 0), onPicked = viewModel::onStartTimeChange, onDismiss = { picking = null })
        Picking.END -> FeastTimeSheet(
            form.endTime ?: form.startTime?.let { LocalTime((it.hour + 3) % 24, it.minute) } ?: LocalTime(15, 0),
            onPicked = viewModel::onEndTimeChange,
            onDismiss = { picking = null },
        )
        null -> {}
    }
}

/** A small label over the picked value, or "Choose" in the accent before one is picked */
@Composable
private fun PickedValue(label: StringResource, value: String?) {
    val colors = MaterialTheme.feastColors
    Column {
        Text(stringResource(label), color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
        Text(value ?: stringResource(Res.string.choose), color = if (value != null) colors.ink else colors.accent, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun RowScope.QuickPick(label: String, selected: Boolean, icon: ImageVector? = null, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.weight(1f).height(38.dp).clip(RoundedCornerShape(12.dp)).background(if (selected) colors.indigo else colors.lavender).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
    ) {
        icon?.let { Icon(it, contentDescription = null, tint = if (selected) colors.onIndigo else colors.ink, modifier = Modifier.size(16.dp)) }
        Text(label, color = if (selected) colors.onIndigo else colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun RowScope.TimeCard(label: StringResource, time: LocalTime?, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(colors.surface).clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Rounded.Schedule, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(22.dp))
        PickedValue(label, time?.let(::formatFeastTime))
    }
}

/** Step 3 (design 1i): the map showing where the feast will go, then the optional address, landmark and people */
@Composable
private fun WhereStep(form: FeastForm, viewModel: ReportFeastViewModel, deviceLocation: com.example.bhandara.data.location.DeviceLocation) {
    val colors = MaterialTheme.feastColors
    var here by remember { mutableStateOf<GeoLocation?>(null) }
    LaunchedEffect(Unit) {
        here = (LocationService(deviceLocation).locate() as? LocationResult.Found)?.location
    }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.indigo)) {
        Box(Modifier.fillMaxWidth().height(110.dp).background(colors.mapLand)) {
            val spot = here
            if (spot != null) {
                PlatformMap(
                    camera = remember(spot) { CameraTarget(spot, zoom = 16f) },
                    pins = listOf(MapPin(id = "here", position = spot, label = "", emoji = "", selected = true)),
                    showMyLocation = false,
                    darkMap = colors.isDark,
                    onPinClick = {},
                    look = MapLook.FEAST,
                    // The pin stands on the spot, so centre a little lower to keep the whole pin in view
                    contentPadding = PaddingValues(top = 44.dp),
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                DrawnSpot(colors)
            }
        }
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Rounded.MyLocation, contentDescription = null, tint = colors.saffron, modifier = Modifier.size(22.dp))
            Text(stringResource(Res.string.feast_place_info), color = colors.onIndigo, fontSize = 14.sp, lineHeight = 19.sp, style = MaterialTheme.typography.bodyMedium)
        }
    }
    FeastFormField(form.address, stringResource(Res.string.feast_address), viewModel::onAddressChange, icon = Icons.Rounded.HomeWork, height = 76.dp, singleLine = false)
    FeastFormField(form.landmark, stringResource(Res.string.feast_landmark), viewModel::onLandmarkChange, icon = Icons.Rounded.NearMe)
    FeastFormField(form.capacity, stringResource(Res.string.feast_people), viewModel::onCapacityChange, icon = Icons.Rounded.Groups, keyboardType = KeyboardType.Number)
}

/** Before the location is known: a few streets and a saffron drop in the middle */
@Composable
private fun DrawnSpot(colors: FeastColors) {
    Canvas(Modifier.fillMaxSize()) {
        val sx = size.width / 320f
        val sy = size.height / 110f
        val road = 10.dp.toPx()
        drawLine(colors.mapRoad, Offset(-10 * sx, 40 * sy), Offset(330 * sx, 20 * sy), strokeWidth = road)
        drawLine(colors.mapRoad, Offset(140 * sx, -10 * sy), Offset(170 * sx, 120 * sy), strokeWidth = road)
        val center = Offset(size.width / 2f, size.height * 0.4f)
        drawCircle(colors.saffron.copy(alpha = 0.22f), radius = 28.dp.toPx(), center = center)
        val pin = 28.dp.toPx()
        rotate(-45f, pivot = center) {
            drawRoundRect(Color.White, topLeft = center - Offset(pin / 2, pin / 2), size = androidx.compose.ui.geometry.Size(pin, pin), cornerRadius = CornerRadius(pin / 2))
            val inner = pin - 6.dp.toPx()
            drawRoundRect(colors.saffron, topLeft = center - Offset(inner / 2, inner / 2), size = androidx.compose.ui.geometry.Size(inner, inner), cornerRadius = CornerRadius(inner / 2))
        }
    }
}

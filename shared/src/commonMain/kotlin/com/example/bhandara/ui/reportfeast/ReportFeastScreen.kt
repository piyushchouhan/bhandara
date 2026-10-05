package com.example.bhandara.ui.reportfeast

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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import com.example.bhandara.shared.resources.feast_address
import com.example.bhandara.shared.resources.feast_date
import com.example.bhandara.shared.resources.feast_description_hint
import com.example.bhandara.shared.resources.feast_ends
import com.example.bhandara.shared.resources.feast_ends_next_day
import com.example.bhandara.shared.resources.feast_food_type
import com.example.bhandara.shared.resources.feast_food_type_hint
import com.example.bhandara.shared.resources.feast_landmark
import com.example.bhandara.shared.resources.feast_menu_hint
import com.example.bhandara.shared.resources.feast_menu_label
import com.example.bhandara.shared.resources.feast_organizer
import com.example.bhandara.shared.resources.feast_people
import com.example.bhandara.shared.resources.feast_phone
import com.example.bhandara.shared.resources.feast_photos_label
import com.example.bhandara.shared.resources.feast_place_info
import com.example.bhandara.shared.resources.feast_starts
import com.example.bhandara.shared.resources.map_allow_location
import com.example.bhandara.shared.resources.next
import com.example.bhandara.shared.resources.ok
import com.example.bhandara.shared.resources.post_feast
import com.example.bhandara.shared.resources.report_feast_title
import com.example.bhandara.shared.resources.review_add_photo
import com.example.bhandara.shared.resources.review_remove_photo
import com.example.bhandara.shared.resources.review_uploading
import com.example.bhandara.shared.resources.section_description
import com.example.bhandara.shared.resources.step_food
import com.example.bhandara.shared.resources.step_when
import com.example.bhandara.shared.resources.step_where
import com.example.bhandara.ui.feasts.formatFeastDay
import com.example.bhandara.ui.feasts.formatFeastTime
import com.example.bhandara.ui.feasts.localNow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val FeastStep.label: StringResource
    get() = when (this) {
        FeastStep.FOOD -> Res.string.step_food
        FeastStep.WHEN -> Res.string.step_when
        FeastStep.WHERE -> Res.string.step_where
    }

/**
 * Posting a community feast, shared by Android and iOS: what's served (with photos), when, and where. Once posted,
 * [onPosted] gets the new feast so the app can open it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFeastScreen(onBack: () -> Unit, onPosted: (feastId: String) -> Unit) {
    val dependencies = LocalAppDependencies.current
    val deviceLocation = rememberDeviceLocation()
    val viewModel = viewModel {
        ReportFeastViewModel(
            FeastApi(dependencies.apiClient),
            dependencies.photoUploader::upload,
            LocationService(deviceLocation),
            { dependencies.auth.currentUid },
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmingClear by rememberSaveable { mutableStateOf(false) }
    val requestLocation = rememberLocationPermissionRequest { granted -> if (granted) viewModel.submit() }

    LaunchedEffect(state.postedFeastId) { state.postedFeastId?.let(onPosted) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.report_feast_title)) },
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
            BottomActions(
                state = state,
                canGoOn = viewModel.canGoOn(),
                onBack = { viewModel.back() },
                onNext = viewModel::next,
                onPost = viewModel::submit,
                onAllowLocation = requestLocation,
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            StepIndicator(state.step)
            Spacer(Modifier.height(20.dp))
            when (state.step) {
                FeastStep.FOOD -> FoodStep(state, viewModel)
                FeastStep.WHEN -> WhenStep(state.form, viewModel)
                FeastStep.WHERE -> WhereStep(state.form, viewModel)
            }
        }
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

@Composable
private fun StepIndicator(step: FeastStep) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            FeastStep.entries.forEach {
                Text(
                    stringResource(it.label),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (it == step) FontWeight.Bold else FontWeight.Normal,
                    color = if (it.ordinal <= step.ordinal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        LinearProgressIndicator(
            progress = { (step.ordinal + 1f) / FeastStep.entries.size },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)),
        )
    }
}

@Composable
private fun BottomActions(
    state: ReportFeastState,
    canGoOn: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onPost: () -> Unit,
    onAllowLocation: () -> Unit,
) {
    Surface(shadowElevation = 8.dp, tonalElevation = 3.dp) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.uploadProgress?.let { progress ->
                LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth())
                Text(stringResource(Res.string.review_uploading, "$progress%"), style = MaterialTheme.typography.bodySmall)
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            if (state.needsLocation) {
                OutlinedButton(onClick = onAllowLocation, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.map_allow_location))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.step != FeastStep.FOOD) {
                    OutlinedButton(onClick = onBack, enabled = !state.submitting, modifier = Modifier.weight(1f)) {
                        Text(stringResource(Res.string.back))
                    }
                }
                if (state.isLastStep) {
                    Button(onClick = onPost, enabled = !state.submitting, modifier = Modifier.weight(1f)) {
                        if (state.submitting) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(stringResource(Res.string.post_feast))
                        }
                    }
                } else {
                    // Tapping Next on an unfinished step says what's missing, so it stays tappable
                    Button(
                        onClick = onNext,
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FoodStep(state: ReportFeastState, viewModel: ReportFeastViewModel) {
    val form = state.form
    var picking by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(Res.string.feast_photos_label, MAX_FEAST_PHOTOS), style = MaterialTheme.typography.titleSmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
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
                        enabled = !state.submitting,
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                    ) {
                        Icon(Icons.Default.Close, stringResource(Res.string.review_remove_photo), tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
            if (state.canAddPhotos) {
                item {
                    OutlinedButton(
                        onClick = { picking = true },
                        enabled = !state.submitting,
                        modifier = Modifier.size(96.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, stringResource(Res.string.review_add_photo), modifier = Modifier.size(32.dp))
                    }
                }
            }
        }

        Text(stringResource(Res.string.feast_menu_label), style = MaterialTheme.typography.titleSmall)
        if (form.menuItems.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                form.menuItems.forEach { item ->
                    InputChip(
                        selected = false,
                        onClick = { viewModel.removeMenuItem(item) },
                        label = { Text(item) },
                        trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(InputChipDefaults.IconSize)) },
                    )
                }
            }
        }
        OutlinedTextField(
            value = form.menuInput,
            onValueChange = viewModel::onMenuInputChange,
            placeholder = { Text(stringResource(Res.string.feast_menu_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { viewModel.addMenuInput() }),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.foodType,
            onValueChange = viewModel::onFoodTypeChange,
            label = { Text(stringResource(Res.string.feast_food_type)) },
            placeholder = { Text(stringResource(Res.string.feast_food_type_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text(stringResource(Res.string.section_description)) },
            placeholder = { Text(stringResource(Res.string.feast_description_hint)) },
            supportingText = { Text("${form.description.length} / $MAX_FEAST_DESCRIPTION") },
            minLines = 3,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    if (picking) {
        PhotoSourceSheet(
            maxCount = MAX_FEAST_PHOTOS - form.photos.size,
            onPicked = viewModel::addPhotos,
            onDismiss = { picking = false },
        )
    }
}

private enum class Picking { DATE, START, END }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WhenStep(form: FeastForm, viewModel: ReportFeastViewModel) {
    var picking by rememberSaveable { mutableStateOf<Picking?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PickerField(Icons.Default.CalendarToday, Res.string.feast_date, form.date?.let(::formatFeastDay)) { picking = Picking.DATE }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PickerField(Icons.Default.Schedule, Res.string.feast_starts, form.startTime?.let(::formatFeastTime), Modifier.weight(1f)) { picking = Picking.START }
            PickerField(Icons.Default.Schedule, Res.string.feast_ends, form.endTime?.let(::formatFeastTime), Modifier.weight(1f)) { picking = Picking.END }
        }
        val start = form.startTime
        val end = form.endTime
        if (start != null && end != null && end < start) {
            Text(stringResource(Res.string.feast_ends_next_day), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        OutlinedTextField(
            value = form.organizerName,
            onValueChange = viewModel::onOrganizerNameChange,
            label = { Text(stringResource(Res.string.feast_organizer)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.contactPhone,
            onValueChange = viewModel::onContactPhoneChange,
            label = { Text(stringResource(Res.string.feast_phone)) },
            placeholder = { Text("+91 98765 43210") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
        )
    }

    when (picking) {
        Picking.DATE -> FeastDatePicker(form.date, onPicked = { viewModel.onDateChange(it) }, onDismiss = { picking = null })
        Picking.START -> FeastTimePicker(form.startTime ?: LocalTime(12, 0), onPicked = viewModel::onStartTimeChange, onDismiss = { picking = null })
        Picking.END -> FeastTimePicker(form.endTime ?: form.startTime?.let { LocalTime((it.hour + 3) % 24, it.minute) } ?: LocalTime(15, 0), onPicked = viewModel::onEndTimeChange, onDismiss = { picking = null })
        null -> {}
    }
}

/** A field that opens a picker: shows the choice, or "Choose" */
@Composable
private fun PickerField(
    icon: ImageVector,
    label: StringResource,
    value: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    value ?: stringResource(Res.string.choose),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (value != null) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (value != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/** Today or later; the picker works in UTC midnights */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeastDatePicker(current: LocalDate?, onPicked: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val today = remember { localNow().date }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = (current ?: today).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                Instant.fromEpochMilliseconds(utcTimeMillis).toLocalDateTime(TimeZone.UTC).date >= today

            override fun isSelectableYear(year: Int) = year >= today.year
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let { onPicked(Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date) }
                onDismiss()
            }) { Text(stringResource(Res.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) } },
    ) {
        DatePicker(state = pickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeastTimePicker(initial: LocalTime, onPicked: (LocalTime) -> Unit, onDismiss: () -> Unit) {
    val pickerState = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            TextButton(onClick = {
                onPicked(LocalTime(pickerState.hour, pickerState.minute))
                onDismiss()
            }) { Text(stringResource(Res.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) } },
    )
}

@Composable
private fun WhereStep(form: FeastForm, viewModel: ReportFeastViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MyLocation, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(Res.string.feast_place_info), color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.bodyMedium)
            }
        }
        OutlinedTextField(
            value = form.address,
            onValueChange = viewModel::onAddressChange,
            label = { Text(stringResource(Res.string.feast_address)) },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.landmark,
            onValueChange = viewModel::onLandmarkChange,
            label = { Text(stringResource(Res.string.feast_landmark)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = form.capacity,
            onValueChange = viewModel::onCapacityChange,
            label = { Text(stringResource(Res.string.feast_people)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

package com.example.bhandara.ui.feasts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bhandara.data.location.LocationService
import com.example.bhandara.data.location.formatDistance
import com.example.bhandara.data.location.rememberDeviceLocation
import com.example.bhandara.data.models.api.FeastResponse
import com.example.bhandara.data.network.FeastApi
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.LocalPlatformActions
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.action_call
import com.example.bhandara.shared.resources.action_directions
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.cancel
import com.example.bhandara.shared.resources.feast_capacity
import com.example.bhandara.shared.resources.feast_capacity_value
import com.example.bhandara.shared.resources.feast_details_title
import com.example.bhandara.shared.resources.feast_food_type
import com.example.bhandara.shared.resources.feast_photo
import com.example.bhandara.shared.resources.feast_report
import com.example.bhandara.shared.resources.feast_report_text
import com.example.bhandara.shared.resources.feast_report_title
import com.example.bhandara.shared.resources.feast_taken_down
import com.example.bhandara.shared.resources.feast_unnamed
import com.example.bhandara.shared.resources.feast_verified
import com.example.bhandara.shared.resources.menu_title
import com.example.bhandara.shared.resources.report_submit
import com.example.bhandara.shared.resources.reported
import com.example.bhandara.shared.resources.section_description
import com.example.bhandara.shared.resources.section_more_info
import com.example.bhandara.shared.resources.shop_distance_away
import com.example.bhandara.shared.resources.shop_location_missing
import com.example.bhandara.shared.resources.shop_near_landmark
import com.example.bhandara.shared.resources.try_again
import com.example.bhandara.ui.shopdetails.FullScreenPhotos
import com.example.bhandara.ui.shopdetails.InfoRow
import com.example.bhandara.ui.shopdetails.SectionCard
import com.example.bhandara.ui.shopdetails.landmarkName
import com.example.bhandara.ui.shopdetails.ShopPhotos
import org.jetbrains.compose.resources.stringResource

/**
 * One community feast's page, shared by Android and iOS: photos, organizer, when (and whether it's on now), where,
 * the menu, directions and call, and reporting a fake feast.
 */
@Composable
fun FeastDetailsScreen(feastId: String, onBack: () -> Unit) {
    val dependencies = LocalAppDependencies.current
    val deviceLocation = rememberDeviceLocation()
    val viewModel = viewModel {
        FeastDetailsViewModel(
            feastId,
            FeastApi(dependencies.apiClient),
            dependencies.settings,
            { dependencies.auth.currentUid },
            LocationService(deviceLocation),
        )
    }
    FeastDetailsPage(viewModel, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeastDetailsPage(viewModel: FeastDetailsViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = LocalPlatformActions.current
    val snackbar = remember { SnackbarHostState() }
    var confirmingReport by rememberSaveable { mutableStateOf(false) }
    var fullScreenPhoto by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            viewModel.onMessageShown()
            snackbar.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.feast_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back)) }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        val feast = state.feast
        val error = state.error
        when {
            state.loading && feast == null -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }

            feast == null -> Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(error.orEmpty(), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = viewModel::retry) { Text(stringResource(Res.string.try_again)) }
            }

            else -> Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                ShopPhotos(feast.imageUrls, onPhotoClick = { fullScreenPhoto = it }, photoLabel = Res.string.feast_photo, placeholder = Icons.Default.Restaurant)
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FeastHeader(feast, state)
                    WhenAndWhere(feast, state.distanceMeters)
                    FeastMenu(feast.menuItems)
                    feast.description?.takeIf { it.isNotBlank() }?.let {
                        SectionCard(Res.string.section_description) { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    MoreAboutFeast(feast)

                    if (!state.isOrganizer && state.isActive) {
                        TextButton(
                            onClick = { confirmingReport = true },
                            enabled = state.canReport && !state.reporting,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        ) {
                            Icon(Icons.Outlined.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(if (state.hasReported) Res.string.reported else Res.string.feast_report))
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { actions.openDirections(feast.latitude, feast.longitude) },
                            enabled = state.isActive && state.timing != FeastTiming.Ended,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(Res.string.action_directions))
                        }
                        if (state.canCall) {
                            OutlinedButton(onClick = { feast.contactPhone?.let(actions::dial) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(Res.string.action_call))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }

    val photos = state.feast?.imageUrls.orEmpty()
    fullScreenPhoto?.takeIf { it in photos.indices }?.let { index ->
        FullScreenPhotos(photos, initialIndex = index, onDismiss = { fullScreenPhoto = null }, photoLabel = Res.string.feast_photo)
    }

    if (confirmingReport) {
        AlertDialog(
            onDismissRequest = { confirmingReport = false },
            title = { Text(stringResource(Res.string.feast_report_title)) },
            text = { Text(stringResource(Res.string.feast_report_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingReport = false
                        viewModel.report()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(Res.string.report_submit)) }
            },
            dismissButton = { TextButton(onClick = { confirmingReport = false }) { Text(stringResource(Res.string.cancel)) } },
        )
    }
}

/** Organizer, verified badge, and whether it's on now, coming up, over or taken down */
@Composable
private fun FeastHeader(feast: FeastResponse, state: FeastDetailsState) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            feast.organizerName?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.feast_unnamed),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        if (feast.isVerified) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(Res.string.feast_verified), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
        if (!state.isActive) {
            AssistChip(
                onClick = {},
                label = { Text(stringResource(Res.string.feast_taken_down)) },
                leadingIcon = { Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp)) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    labelColor = MaterialTheme.colorScheme.onErrorContainer,
                    leadingIconContentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
                border = null,
            )
        } else {
            val (label, color) = state.timing.label()
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

/** Date, hours, address, landmark and distance */
@Composable
private fun WhenAndWhere(feast: FeastResponse, distanceMeters: Double?) {
    val onCard = MaterialTheme.colorScheme.onPrimaryContainer
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = onCard)
                Text(formatFeastDate(feast), style = MaterialTheme.typography.bodyLarge, color = onCard)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = onCard)
                Text(formatFeastHours(feast), style = MaterialTheme.typography.bodyLarge, color = onCard)
            }
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = onCard)
                Column {
                    Text(
                        feast.address?.trim()?.takeIf { it.isNotEmpty() } ?: stringResource(Res.string.shop_location_missing),
                        style = MaterialTheme.typography.bodyLarge,
                        color = onCard,
                    )
                    landmarkName(feast.landmark)?.let {
                        Text(stringResource(Res.string.shop_near_landmark, it), style = MaterialTheme.typography.bodyMedium, color = onCard.copy(alpha = 0.8f))
                    }
                    distanceMeters?.let {
                        Text(stringResource(Res.string.shop_distance_away, formatDistance(it)), style = MaterialTheme.typography.bodySmall, color = onCard.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeastMenu(items: List<String>) {
    if (items.isEmpty()) return
    SectionCard(Res.string.menu_title) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item ->
                SuggestionChip(
                    onClick = {},
                    label = { Text(item) },
                    icon = { Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp)) },
                )
            }
        }
    }
}

/** Food type and how many people are expected; nothing when neither is known */
@Composable
private fun MoreAboutFeast(feast: FeastResponse) {
    val foodType = feast.foodType?.takeIf { it.isNotBlank() }
    val capacity = feast.estimatedCapacity.takeIf { it > 0 }
    if (foodType == null && capacity == null) return
    SectionCard(Res.string.section_more_info) {
        foodType?.let { InfoRow(Icons.Default.LocalDining, Res.string.feast_food_type, it) }
        capacity?.let { InfoRow(Icons.Default.People, Res.string.feast_capacity, stringResource(Res.string.feast_capacity_value, it)) }
    }
}

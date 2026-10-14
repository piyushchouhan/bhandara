package com.example.bhandara.ui.feasts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.NoMeals
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.data.models.api.FeastResponse
import com.example.bhandara.data.network.FeastApi
import com.example.bhandara.platform.ExternalLinks
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.LocalPlatformActions
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.community_feast
import com.example.bhandara.shared.resources.feasts_posted
import com.example.bhandara.shared.resources.my_feasts_change_time
import com.example.bhandara.shared.resources.my_feasts_contact_support
import com.example.bhandara.shared.resources.my_feasts_dishes
import com.example.bhandara.shared.resources.my_feasts_edit
import com.example.bhandara.shared.resources.my_feasts_empty_text
import com.example.bhandara.shared.resources.my_feasts_empty_title
import com.example.bhandara.shared.resources.my_feasts_end_confirm
import com.example.bhandara.shared.resources.my_feasts_end_early
import com.example.bhandara.shared.resources.my_feasts_end_text
import com.example.bhandara.shared.resources.my_feasts_end_title
import com.example.bhandara.shared.resources.my_feasts_ended
import com.example.bhandara.shared.resources.my_feasts_ended_done
import com.example.bhandara.shared.resources.my_feasts_ended_early
import com.example.bhandara.shared.resources.my_feasts_hidden
import com.example.bhandara.shared.resources.my_feasts_keep_live
import com.example.bhandara.shared.resources.my_feasts_live_on_map
import com.example.bhandara.shared.resources.my_feasts_more
import com.example.bhandara.shared.resources.my_feasts_none_past
import com.example.bhandara.shared.resources.my_feasts_none_taken_down
import com.example.bhandara.shared.resources.my_feasts_none_upcoming
import com.example.bhandara.shared.resources.my_feasts_past
import com.example.bhandara.shared.resources.my_feasts_past_heading
import com.example.bhandara.shared.resources.my_feasts_photos
import com.example.bhandara.shared.resources.my_feasts_post
import com.example.bhandara.shared.resources.my_feasts_reason
import com.example.bhandara.shared.resources.my_feasts_reason_reports
import com.example.bhandara.shared.resources.my_feasts_repost
import com.example.bhandara.shared.resources.my_feasts_serves
import com.example.bhandara.shared.resources.my_feasts_share
import com.example.bhandara.shared.resources.my_feasts_take_down
import com.example.bhandara.shared.resources.my_feasts_take_down_confirm
import com.example.bhandara.shared.resources.my_feasts_take_down_text
import com.example.bhandara.shared.resources.my_feasts_take_down_title
import com.example.bhandara.shared.resources.my_feasts_taken_down
import com.example.bhandara.shared.resources.my_feasts_taken_down_done
import com.example.bhandara.shared.resources.my_feasts_upcoming
import com.example.bhandara.shared.resources.share_shop_text
import com.example.bhandara.shared.resources.try_again
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.shopdetails.FeastSheet
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

// Feasts I posted (design "Feasts I Posted": 1a list, 1b More sheet, 1c end early, 1d taken down, 1e empty; dark
// 2a–2e). Views, directions, saves and "Fix and resubmit" have no backend yet, so they're left out.

/**
 * The user's own feasts, from Profile → My activity
 *
 * @param onEdit change a feast that hasn't ended (the post form, filled in); also "Change date or time"
 * @param onRepost post a past feast again (the post form with its details and no date)
 */
@Composable
fun MyFeastsScreen(
    onBack: () -> Unit,
    onOpenFeast: (feastId: String) -> Unit,
    onEdit: (feastId: String) -> Unit,
    onRepost: (feastId: String) -> Unit,
    onPostFeast: () -> Unit,
    onContactSupport: () -> Unit,
) {
    val dependencies = LocalAppDependencies.current
    val viewModel = viewModel { MyFeastsViewModel(FeastApi(dependencies.apiClient)) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = LocalPlatformActions.current
    val snackbar = remember { SnackbarHostState() }
    var moreFor by remember { mutableStateOf<FeastResponse?>(null) }
    var endingEarly by remember { mutableStateOf<FeastResponse?>(null) }
    var takingDown by remember { mutableStateOf<FeastResponse?>(null) }

    LaunchedEffect(state.message, state.done) {
        val text = state.message ?: when (state.done) {
            MyFeastsDone.ENDED -> getString(Res.string.my_feasts_ended_done)
            MyFeastsDone.TAKEN_DOWN -> getString(Res.string.my_feasts_taken_down_done)
            null -> null
        }
        if (text != null) {
            viewModel.onMessageShown()
            snackbar.showSnackbar(text)
        }
    }
    // Back from editing or posting: show the feasts as they are now
    var shownBefore by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (shownBefore) viewModel.load() else shownBefore = true
    }

    FeastTheme {
        val colors = MaterialTheme.feastColors
        Box(Modifier.fillMaxSize().background(colors.page)) {
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), onBack)
                    Text(
                        stringResource(Res.string.feasts_posted), color = colors.ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f),
                    )
                }
                val error = state.error
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when {
                        state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.saffron)
                        error != null -> Column(
                            Modifier.align(Alignment.Center).padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(error, color = colors.muted, fontSize = 15.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                            FeastButton(stringResource(Res.string.try_again), onClick = viewModel::load, style = FeastButtonStyle.SOFT, modifier = Modifier.padding(horizontal = 24.dp))
                        }
                        state.feasts.isEmpty() -> NoFeastsYet(onPostFeast, Modifier.align(Alignment.Center))
                        else -> FeastList(
                            state = state,
                            onTab = viewModel::onTab,
                            onOpen = onOpenFeast,
                            onEdit = onEdit,
                            onMore = { moreFor = it },
                            onRepost = onRepost,
                            onContactSupport = onContactSupport,
                        )
                    }
                }
            }
            // "Post a feast", floating at the bottom while there's a list
            if (state.feasts.isNotEmpty()) {
                FeastButton(
                    stringResource(Res.string.my_feasts_post), onClick = onPostFeast, style = FeastButtonStyle.SAFFRON, icon = Icons.Rounded.Add,
                    height = 56.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(start = 14.dp, end = 14.dp, bottom = 24.dp)
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(18.dp), ambientColor = colors.saffron, spotColor = colors.saffron.copy(alpha = 0.35f)),
                )
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = 92.dp))
        }

        moreFor?.let { feast ->
            val shareText = stringResource(Res.string.share_shop_text, feastName(feast), ExternalLinks.directions(feast.latitude, feast.longitude))
            MoreSheet(
                feast = feast,
                onShare = { actions.share(shareText) },
                onChangeTime = { onEdit(feast.id) },
                onEndEarly = { endingEarly = feast },
                onTakeDown = { takingDown = feast },
                onDismiss = { moreFor = null },
            )
        }
        endingEarly?.let { feast ->
            ConfirmDialog(
                icon = Icons.Rounded.NoMeals,
                danger = false,
                title = stringResource(Res.string.my_feasts_end_title),
                text = stringResource(Res.string.my_feasts_end_text, feastName(feast)),
                confirm = stringResource(Res.string.my_feasts_end_confirm),
                dismiss = stringResource(Res.string.my_feasts_keep_live),
                onConfirm = {
                    endingEarly = null
                    viewModel.endEarly(feast.id)
                },
                onDismiss = { endingEarly = null },
            )
        }
        takingDown?.let { feast ->
            ConfirmDialog(
                icon = Icons.Outlined.Delete,
                danger = true,
                title = stringResource(Res.string.my_feasts_take_down_title),
                text = stringResource(Res.string.my_feasts_take_down_text, feastName(feast)),
                confirm = stringResource(Res.string.my_feasts_take_down_confirm),
                dismiss = stringResource(Res.string.my_feasts_keep_live),
                onConfirm = {
                    takingDown = null
                    viewModel.takeDown(feast.id)
                },
                onDismiss = { takingDown = null },
            )
        }
    }
}

/** The feast's name: its organizer, or "Community Feast" */
@Composable
private fun feastName(feast: FeastResponse): String =
    feast.organizerName?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.community_feast)

private fun feastDate(feast: FeastResponse): LocalDate? = runCatching { LocalDate.parse(feast.feastDate) }.getOrNull()

private val WEEKDAYS = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
private val MONTHS = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

/** Tabs, then the tab's feasts: upcoming as tickets (with past ones listed under them), past, or taken down */
@Composable
private fun FeastList(
    state: MyFeastsState,
    onTab: (MyFeastsTab) -> Unit,
    onOpen: (String) -> Unit,
    onEdit: (String) -> Unit,
    onMore: (FeastResponse) -> Unit,
    onRepost: (String) -> Unit,
    onContactSupport: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    val tab = state.activeTab
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "tabs") {
            LazyRow(Modifier.padding(bottom = 2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item { TabChip(stringResource(Res.string.my_feasts_upcoming, state.upcoming.size), tab == MyFeastsTab.UPCOMING) { onTab(MyFeastsTab.UPCOMING) } }
                item { TabChip(stringResource(Res.string.my_feasts_past, state.past.size), tab == MyFeastsTab.PAST) { onTab(MyFeastsTab.PAST) } }
                item { TabChip(stringResource(Res.string.my_feasts_taken_down, state.takenDown.size), tab == MyFeastsTab.TAKEN_DOWN) { onTab(MyFeastsTab.TAKEN_DOWN) } }
            }
        }
        when (tab) {
            MyFeastsTab.UPCOMING -> {
                if (state.upcoming.isEmpty()) item { NothingHere(stringResource(Res.string.my_feasts_none_upcoming)) }
                items(state.upcoming, key = { it.id }) { feast ->
                    TicketCard(feast, busy = state.busyId == feast.id, onOpen = { onOpen(feast.id) }, onEdit = { onEdit(feast.id) }, onMore = { onMore(feast) })
                }
                if (state.past.isNotEmpty()) {
                    item(key = "past-heading") {
                        Text(
                            stringResource(Res.string.my_feasts_past_heading), color = colors.muted, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 4.dp, top = 8.dp),
                        )
                    }
                    items(state.past, key = { "past-" + it.id }) { feast -> PastRow(feast, onOpen = { onOpen(feast.id) }, onRepost = { onRepost(feast.id) }) }
                }
            }
            MyFeastsTab.PAST -> {
                if (state.past.isEmpty()) item { NothingHere(stringResource(Res.string.my_feasts_none_past)) }
                items(state.past, key = { it.id }) { feast -> PastRow(feast, onOpen = { onOpen(feast.id) }, onRepost = { onRepost(feast.id) }) }
            }
            MyFeastsTab.TAKEN_DOWN -> {
                if (state.takenDown.isEmpty()) item { NothingHere(stringResource(Res.string.my_feasts_none_taken_down)) }
                items(state.takenDown, key = { it.id }) { feast -> TakenDownCard(feast, onContactSupport) }
            }
        }
    }
}

/** A tab: saffron when picked, as in the feast screens */
@Composable
private fun TabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(50)
    Box(
        Modifier
            .height(36.dp)
            .clip(shape)
            .background(if (selected) colors.saffron else colors.surface, shape)
            .then(if (selected) Modifier else Modifier.border(1.5.dp, colors.border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (selected) colors.onSaffron else colors.ink, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun NothingHere(text: String) {
    Text(
        text, color = MaterialTheme.feastColors.muted, fontSize = 14.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 24.dp),
    )
}

/** An upcoming feast (1a): its photo with "Live on map", the date on a saffron stub, when and where, a few facts, Edit and More */
@Composable
private fun TicketCard(feast: FeastResponse, busy: Boolean, onOpen: () -> Unit, onEdit: () -> Unit, onMore: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val date = feastDate(feast)
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(colors.surface).clickable(onClick = onOpen)) {
        Box(Modifier.fillMaxWidth().height(130.dp)) {
            Box(Modifier.fillMaxSize().stripes(colors.saffronSoft, colors.surface, 8.dp), contentAlignment = Alignment.Center) {
                val photo = feast.imageUrls.firstOrNull()
                if (photo != null) {
                    AsyncImage(photo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Rounded.Image, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(32.dp))
                }
            }
            Row(
                Modifier.align(Alignment.TopStart).padding(12.dp).height(28.dp).background(colors.successSoft, CircleShape).padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(Modifier.size(6.dp).background(colors.success, CircleShape))
                Text(stringResource(Res.string.my_feasts_live_on_map), color = colors.success, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
            }
        }
        Box {
            Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 86.dp, top = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(feastName(feast), color = colors.ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
                Text(
                    listOfNotNull(formatFeastHours(feast), feast.address?.takeIf { it.isNotBlank() }).joinToString(" · "),
                    color = colors.saffronText, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            // The saffron date stub, overlapping the photo
            if (date != null) {
                Column(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-12).dp, y = (-26).dp)
                        .size(width = 64.dp, height = 68.dp)
                        .shadow(8.dp, RoundedCornerShape(18.dp), ambientColor = colors.saffron, spotColor = colors.saffron.copy(alpha = 0.35f))
                        .background(colors.saffron, RoundedCornerShape(18.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(WEEKDAYS[date.dayOfWeek.ordinal], color = colors.onSaffron, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp, style = MaterialTheme.typography.labelSmall)
                    Text("${date.dayOfMonth}", color = colors.onSaffron, fontSize = 26.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
        // Dashed tear line, like a ticket
        Box(
            Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp).fillMaxWidth().height(2.dp).drawBehind {
                drawLine(colors.saffronTrack, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())))
            },
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 12.dp)) {
            Fact(feast.estimatedCapacity.takeIf { it > 0 }?.toString() ?: "–", stringResource(Res.string.my_feasts_serves), Modifier.weight(1f))
            Divider()
            Fact(feast.menuItems.size.toString(), stringResource(Res.string.my_feasts_dishes), Modifier.weight(1f))
            Divider()
            Fact(feast.imageUrls.size.toString(), stringResource(Res.string.my_feasts_photos), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender)) {}
        Row(Modifier.fillMaxWidth().height(50.dp)) {
            CardAction(Icons.Outlined.Edit, stringResource(Res.string.my_feasts_edit), enabled = !busy, onClick = onEdit, modifier = Modifier.weight(1f))
            Box(Modifier.width(1.dp).fillMaxHeight().background(colors.lavender))
            CardAction(Icons.Rounded.MoreHoriz, stringResource(Res.string.my_feasts_more), enabled = !busy, onClick = onMore, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun Fact(value: String, label: String, modifier: Modifier) {
    val colors = MaterialTheme.feastColors
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = colors.ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
        Text(label, color = colors.muted, fontSize = 11.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun Divider() {
    Box(Modifier.width(1.dp).height(40.dp).background(MaterialTheme.feastColors.lavender))
}

@Composable
private fun CardAction(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.feastColors
    Row(
        modifier.fillMaxSize().clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        Icon(icon, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(20.dp))
        Text(label, color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
    }
}

/** The month and day on a lavender tile, for past and taken-down feasts */
@Composable
private fun DateTile(feast: FeastResponse, weekday: Boolean = false) {
    val colors = MaterialTheme.feastColors
    val date = feastDate(feast)
    Column(
        Modifier.size(width = 52.dp, height = 56.dp).background(colors.lavender, RoundedCornerShape(14.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (date != null) {
            Text(if (weekday) WEEKDAYS[date.dayOfWeek.ordinal] else MONTHS[date.monthNumber - 1], color = colors.muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
            Text("${date.dayOfMonth}", color = colors.ink, fontSize = 20.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }
    }
}

/** A past feast: date, name, "Ended" (or "Ended early"), and Repost */
@Composable
private fun PastRow(feast: FeastResponse, onOpen: () -> Unit, onRepost: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.surface).clickable(onClick = onOpen).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DateTile(feast)
        Column(Modifier.weight(1f)) {
            Text(feastName(feast), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(if (feast.endedEarlyAt != null) Res.string.my_feasts_ended_early else Res.string.my_feasts_ended),
                color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall,
            )
        }
        Row(
            Modifier.height(32.dp).clip(RoundedCornerShape(10.dp)).background(colors.saffronSoft).clickable(onClick = onRepost).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Icons.Rounded.Replay, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(16.dp))
            Text(stringResource(Res.string.my_feasts_repost), color = colors.saffronText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** A feast hidden after reports (1d): why, and a way to reach support */
@Composable
private fun TakenDownCard(feast: FeastResponse, onContactSupport: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Column(
        Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(24.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DateTile(feast, weekday = true)
            Column(Modifier.weight(1f)) {
                Text(feastName(feast), color = colors.ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                feast.address?.takeIf { it.isNotBlank() }?.let {
                    Text(it, color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Row(
            Modifier.height(26.dp).background(colors.dangerSoft, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Icons.Outlined.VisibilityOff, contentDescription = null, tint = colors.danger, modifier = Modifier.size(15.dp))
            Text(stringResource(Res.string.my_feasts_hidden), color = colors.danger, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
        }
        Column(Modifier.fillMaxWidth().background(colors.page, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(Res.string.my_feasts_reason), color = colors.ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
            Text(stringResource(Res.string.my_feasts_reason_reports), color = colors.muted, fontSize = 13.sp, lineHeight = 19.sp, style = MaterialTheme.typography.bodySmall)
        }
        FeastButton(stringResource(Res.string.my_feasts_contact_support), onClick = onContactSupport, style = FeastButtonStyle.SOFT, height = 50.dp, modifier = Modifier.fillMaxWidth())
    }
}

/** 1b: the feast on top, then share, change date or time, end early, take down */
@Composable
private fun MoreSheet(
    feast: FeastResponse,
    onShare: () -> Unit,
    onChangeTime: () -> Unit,
    onEndEarly: () -> Unit,
    onTakeDown: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    FeastSheet(onDismiss) { close ->
        Row(
            Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val date = feastDate(feast)
            Column(
                Modifier.size(width = 44.dp, height = 48.dp).background(colors.saffron, RoundedCornerShape(12.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (date != null) {
                    Text(WEEKDAYS[date.dayOfWeek.ordinal], color = colors.onSaffron, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
                    Text("${date.dayOfMonth}", color = colors.onSaffron, fontSize = 18.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
            Column {
                Text(feastName(feast), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                Text(
                    listOfNotNull(formatFeastHours(feast), feast.address?.takeIf { it.isNotBlank() }).joinToString(" · "),
                    color = colors.saffronText, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender))
        Column(Modifier.padding(top = 6.dp)) {
            SheetRow(Icons.Outlined.IosShare, stringResource(Res.string.my_feasts_share), colors.saffronText, colors.ink) { close(onShare) }
            SheetRow(Icons.Outlined.Schedule, stringResource(Res.string.my_feasts_change_time), colors.saffronText, colors.ink) { close(onChangeTime) }
            SheetRow(Icons.Rounded.NoMeals, stringResource(Res.string.my_feasts_end_early), colors.saffronText, colors.ink) { close(onEndEarly) }
            SheetRow(Icons.Outlined.Delete, stringResource(Res.string.my_feasts_take_down), colors.danger, colors.danger) { close(onTakeDown) }
        }
    }
}

@Composable
private fun SheetRow(icon: ImageVector, label: String, iconTint: Color, textColor: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        Text(label, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
    }
}

/** 1c (and taking down): a centred card, the action in saffron (or red to take down) and a way back */
@Composable
private fun ConfirmDialog(
    icon: ImageVector,
    danger: Boolean,
    title: String,
    text: String,
    confirm: String,
    dismiss: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(28.dp)).padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(Modifier.size(56.dp).background(if (danger) colors.dangerSoft else colors.saffronSoft, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = if (danger) colors.danger else colors.saffronText, modifier = Modifier.size(28.dp))
            }
            Text(title, color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
            Text(text, color = colors.muted, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
            Column(Modifier.fillMaxWidth().padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeastButton(confirm, onClick = onConfirm, style = if (danger) FeastButtonStyle.DANGER else FeastButtonStyle.SAFFRON, height = 52.dp, modifier = Modifier.fillMaxWidth())
                FeastButton(dismiss, onClick = onDismiss, style = FeastButtonStyle.SOFT, height = 52.dp, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

/** 1e: nothing posted yet */
@Composable
private fun NoFeastsYet(onPostFeast: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.feastColors
    Column(
        modifier.padding(start = 36.dp, end = 36.dp, bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(112.dp).background(colors.saffronSoft, RoundedCornerShape(36.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Celebration, contentDescription = null, tint = colors.saffron, modifier = Modifier.size(52.dp))
        }
        Text(stringResource(Res.string.my_feasts_empty_title), color = colors.ink, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
        Text(stringResource(Res.string.my_feasts_empty_text), color = colors.muted, fontSize = 14.sp, lineHeight = 21.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        FeastButton(
            stringResource(Res.string.my_feasts_post), onClick = onPostFeast, style = FeastButtonStyle.SAFFRON, icon = Icons.Rounded.Add,
            height = 52.dp, modifier = Modifier.padding(top = 6.dp).padding(horizontal = 10.dp),
        )
    }
}

package com.example.bhandara.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.NoFood
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.WrongLocation
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bhandara.data.models.api.MyReport
import com.example.bhandara.data.network.ReportApi
import com.example.bhandara.platform.BackPress
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.my_reports
import com.example.bhandara.shared.resources.my_reports_checking
import com.example.bhandara.shared.resources.my_reports_checking_note
import com.example.bhandara.shared.resources.my_reports_decision
import com.example.bhandara.shared.resources.my_reports_decision_note
import com.example.bhandara.shared.resources.my_reports_empty_text
import com.example.bhandara.shared.resources.my_reports_empty_title
import com.example.bhandara.shared.resources.my_reports_example_closed
import com.example.bhandara.shared.resources.my_reports_example_fake
import com.example.bhandara.shared.resources.my_reports_example_location
import com.example.bhandara.shared.resources.my_reports_feast_ended
import com.example.bhandara.shared.resources.my_reports_filter_all
import com.example.bhandara.shared.resources.my_reports_filter_closed
import com.example.bhandara.shared.resources.my_reports_filter_in_review
import com.example.bhandara.shared.resources.my_reports_fixed
import com.example.bhandara.shared.resources.my_reports_fixed_count
import com.example.bhandara.shared.resources.my_reports_fixed_feast
import com.example.bhandara.shared.resources.my_reports_fixed_review
import com.example.bhandara.shared.resources.my_reports_fixed_shop
import com.example.bhandara.shared.resources.my_reports_fixed_text_feast
import com.example.bhandara.shared.resources.my_reports_fixed_text_review
import com.example.bhandara.shared.resources.my_reports_fixed_text_shop
import com.example.bhandara.shared.resources.my_reports_fixed_title
import com.example.bhandara.shared.resources.my_reports_hidden
import com.example.bhandara.shared.resources.my_reports_in_review
import com.example.bhandara.shared.resources.my_reports_in_review_note
import com.example.bhandara.shared.resources.my_reports_keep
import com.example.bhandara.shared.resources.my_reports_kept
import com.example.bhandara.shared.resources.my_reports_kind_feast
import com.example.bhandara.shared.resources.my_reports_kind_shop
import com.example.bhandara.shared.resources.my_reports_no_action
import com.example.bhandara.shared.resources.my_reports_no_action_feast_text
import com.example.bhandara.shared.resources.my_reports_no_action_text
import com.example.bhandara.shared.resources.my_reports_no_action_title
import com.example.bhandara.shared.resources.my_reports_none_here
import com.example.bhandara.shared.resources.my_reports_on_map
import com.example.bhandara.shared.resources.my_reports_progress
import com.example.bhandara.shared.resources.my_reports_reason
import com.example.bhandara.shared.resources.my_reports_report
import com.example.bhandara.shared.resources.my_reports_reported_by
import com.example.bhandara.shared.resources.my_reports_reported_feast
import com.example.bhandara.shared.resources.my_reports_reported_review
import com.example.bhandara.shared.resources.my_reports_reported_shop
import com.example.bhandara.shared.resources.my_reports_resolved
import com.example.bhandara.shared.resources.my_reports_review_on
import com.example.bhandara.shared.resources.my_reports_sent
import com.example.bhandara.shared.resources.my_reports_sent_on
import com.example.bhandara.shared.resources.my_reports_suggested_removing
import com.example.bhandara.shared.resources.my_reports_thanks
import com.example.bhandara.shared.resources.my_reports_this_place
import com.example.bhandara.shared.resources.my_reports_withdraw
import com.example.bhandara.shared.resources.my_reports_withdraw_confirm
import com.example.bhandara.shared.resources.my_reports_withdraw_text
import com.example.bhandara.shared.resources.my_reports_withdraw_title
import com.example.bhandara.shared.resources.my_reports_what_changed
import com.example.bhandara.shared.resources.my_reports_withdrawn
import com.example.bhandara.shared.resources.report_reason_closed
import com.example.bhandara.shared.resources.report_reason_fssai
import com.example.bhandara.shared.resources.report_reason_inappropriate
import com.example.bhandara.shared.resources.report_reason_not_food_shop
import com.example.bhandara.shared.resources.report_reason_wrong_location
import com.example.bhandara.shared.resources.try_again
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.feasts.formatFeastTime
import com.example.bhandara.ui.reviews.formatReviewDate
import com.example.bhandara.ui.reviews.reviewAge
import com.example.bhandara.ui.reviews.reviewAgeText
import com.example.bhandara.ui.shopdetails.FeastSheet
import com.example.bhandara.ui.shopdetails.FullScreenPhotos
import com.example.bhandara.photos.PickedPhoto
import com.example.bhandara.ui.components.ReportDetailsFields
import com.example.bhandara.ui.components.ReportPhotoTile
import androidx.compose.material.icons.outlined.AddComment
import com.example.bhandara.shared.resources.review_photo
import com.example.bhandara.shared.resources.my_reports_add_info
import com.example.bhandara.shared.resources.my_reports_add_info_prompt
import com.example.bhandara.shared.resources.my_reports_add_info_send
import com.example.bhandara.shared.resources.my_reports_info_added
import com.example.bhandara.shared.resources.my_reports_what_you_said
import com.example.bhandara.shared.resources.feast_report_reason_wrong_time
import com.example.bhandara.shared.resources.feast_report_reason_not_happening
import com.example.bhandara.shared.resources.feast_report_reason_money
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

// My reports (design "My Reports": 1a list, 1b in review, 1c fixed, 1d withdraw, 1e empty; dark 2a–2e). "What you
// said" shows the note and photos sent with the report and any added later with "Add more info".

/**
 * The reports the user sent, from Profile → My activity
 *
 * @param onOpenShop / [onOpenFeast] the shop (or the shop a review is on) or feast a report is about
 */
@Composable
fun MyReportsScreen(onBack: () -> Unit, onOpenShop: (shopId: String) -> Unit, onOpenFeast: (feastId: String) -> Unit) {
    val dependencies = LocalAppDependencies.current
    val viewModel = viewModel { MyReportsViewModel(ReportApi(dependencies.apiClient)) { photos -> dependencies.photoUploader.upload(photos) } }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    var confirmWithdraw by remember { mutableStateOf<MyReport?>(null) }
    var addingInfoTo by remember { mutableStateOf<MyReport?>(null) }

    LaunchedEffect(state.message, state.withdrawn, state.infoAdded) {
        if (state.infoAdded) addingInfoTo = null
        val text = state.message ?: when {
            state.withdrawn -> getString(Res.string.my_reports_withdrawn)
            state.infoAdded -> getString(Res.string.my_reports_info_added)
            else -> null
        }
        if (text != null) {
            viewModel.onMessageShown()
            snackbar.showSnackbar(text)
        }
    }
    var shownBefore by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (shownBefore) viewModel.load() else shownBefore = true
    }
    // Back on a report's page goes back to the list
    BackPress(enabled = state.open != null, onBack = viewModel::close)

    val openTarget: (MyReport) -> Unit = { report ->
        report.feastId?.let(onOpenFeast) ?: report.shopId?.let(onOpenShop)
    }

    FeastTheme {
        val colors = MaterialTheme.feastColors
        Box(Modifier.fillMaxSize().background(colors.page)) {
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
                val open = state.open
                Row(
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), { if (open != null) viewModel.close() else onBack() })
                    Text(
                        stringResource(if (open != null) Res.string.my_reports_report else Res.string.my_reports), color = colors.ink,
                        fontSize = 19.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f),
                    )
                }
                val error = state.error
                when {
                    open != null -> ReportDetail(
                        open, today,
                        onOpenTarget = { openTarget(open) },
                        onWithdraw = { confirmWithdraw = open },
                        onAddInfo = { addingInfoTo = open },
                        withdrawing = state.withdrawing,
                        modifier = Modifier.weight(1f),
                    )
                    state.loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.indigo)
                    }
                    error != null -> Column(
                        Modifier.weight(1f).fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    ) {
                        Text(error, color = colors.muted, fontSize = 15.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                        FeastButton(stringResource(Res.string.try_again), onClick = viewModel::load, style = FeastButtonStyle.SOFT, modifier = Modifier.padding(horizontal = 24.dp))
                    }
                    state.reports.isEmpty() -> NoReportsYet(Modifier.weight(1f))
                    else -> LazyColumn(
                        Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item(key = "thanks") { Thanks(state.fixedCount, state.reports.size) }
                        item(key = "filters") { Filters(state, viewModel::onFilter) }
                        val shown = state.shown()
                        if (shown.isEmpty()) {
                            item {
                                Text(
                                    stringResource(Res.string.my_reports_none_here), color = colors.muted, fontSize = 14.sp, textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                )
                            }
                        }
                        items(shown, key = { it.key }) { report -> ReportCard(report, today) { viewModel.open(report) } }
                    }
                }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = 16.dp))
        }
        addingInfoTo?.let { report ->
            AddInfoSheet(
                sending = state.addingInfo,
                onSend = { note, photos -> viewModel.addInfo(report, note, photos) },
                onDismiss = { if (!state.addingInfo) addingInfoTo = null },
            )
        }
        confirmWithdraw?.let { report ->
            WithdrawSheet(
                report,
                onWithdraw = { viewModel.withdraw(report) },
                onDismiss = { confirmWithdraw = null },
            )
        }
    }
}

// ==================== What a report is about ====================

/** "Wrong location", "Reported review", ... */
@Composable
private fun reportTitle(report: MyReport): String = stringResource(
    when (report.kind) {
        "REVIEW" -> Res.string.my_reports_reported_review
        "FEAST" -> reasonText(report.reason) ?: Res.string.my_reports_reported_feast
        else -> reasonText(report.reason) ?: Res.string.my_reports_reported_shop
    },
)

private fun reasonText(reason: String?): StringResource? = when (reason) {
    "WRONG_LOCATION" -> Res.string.report_reason_wrong_location
    "PERMANENTLY_CLOSED" -> Res.string.report_reason_closed
    "WRONG_FSSAI_LICENCE" -> Res.string.report_reason_fssai
    "NOT_FOOD_SHOP" -> Res.string.report_reason_not_food_shop
    "INAPPROPRIATE_CONTENT" -> Res.string.report_reason_inappropriate
    "SUGGEST_DELETE" -> Res.string.my_reports_suggested_removing
    "WRONG_TIME" -> Res.string.feast_report_reason_wrong_time
    "NOT_HAPPENING" -> Res.string.feast_report_reason_not_happening
    "ASKING_FOR_MONEY" -> Res.string.feast_report_reason_money
    "INAPPROPRIATE" -> Res.string.report_reason_inappropriate
    else -> null
}

private fun reportIcon(report: MyReport): ImageVector = when (report.kind) {
    "REVIEW" -> Icons.Outlined.RateReview
    "FEAST" -> Icons.Rounded.Celebration
    else -> when (report.reason) {
        "WRONG_LOCATION" -> Icons.Outlined.WrongLocation
        "NOT_FOOD_SHOP" -> Icons.Outlined.NoFood
        "INAPPROPRIATE_CONTENT" -> Icons.Outlined.Report
        "SUGGEST_DELETE" -> Icons.Outlined.Delete
        else -> Icons.Outlined.Storefront
    }
}

/** "Sai Amruttulya · Shop", "Review on Chitale Sweets", "Ganesh Mandal · Feast" */
@Composable
private fun targetText(report: MyReport): String {
    val name = report.targetName?.takeIf { it.isNotBlank() }
    return when (report.kind) {
        "REVIEW" -> name?.let { stringResource(Res.string.my_reports_review_on, it) } ?: stringResource(Res.string.my_reports_reported_review)
        "FEAST" -> listOfNotNull(name, stringResource(Res.string.my_reports_kind_feast)).joinToString(" · ")
        else -> listOfNotNull(name, stringResource(Res.string.my_reports_kind_shop)).joinToString(" · ")
    }
}

private fun dateTime(timestamp: String?): LocalDateTime? = timestamp?.let { runCatching { LocalDateTime.parse(it.substringBefore('.')) }.getOrNull() }

/** "Today, 9:12 AM" / "3 days ago, 6:40 PM" / "3 Oct 2026, 6:40 PM" */
@Composable
private fun sentText(timestamp: String?, today: LocalDate): String? {
    val day = reviewAgeText(reviewAge(timestamp, today)) ?: return null
    val time = dateTime(timestamp)?.time?.let(::formatFeastTime)
    return listOfNotNull(day, time).joinToString(", ")
}

// ==================== The list (1a) ====================

@Composable
private fun Thanks(fixed: Int, total: Int) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.fillMaxWidth().background(colors.indigo, RoundedCornerShape(22.dp)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(48.dp).background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Shield, contentDescription = null, tint = colors.saffron, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(stringResource(Res.string.my_reports_thanks), color = colors.onIndigo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Text(stringResource(Res.string.my_reports_fixed_count, fixed, total), color = colors.headerSubtext, fontSize = 12.sp, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun Filters(state: MyReportsState, onFilter: (ReportsFilter) -> Unit) {
    LazyRow(Modifier.padding(top = 6.dp, bottom = 2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        item { Chip(stringResource(Res.string.my_reports_filter_all, state.reports.size), state.filter == ReportsFilter.ALL) { onFilter(ReportsFilter.ALL) } }
        item { Chip(stringResource(Res.string.my_reports_filter_in_review, state.inReviewCount), state.filter == ReportsFilter.IN_REVIEW) { onFilter(ReportsFilter.IN_REVIEW) } }
        item { Chip(stringResource(Res.string.my_reports_filter_closed, state.closedCount), state.filter == ReportsFilter.CLOSED) { onFilter(ReportsFilter.CLOSED) } }
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(50)
    Box(
        Modifier
            .height(36.dp)
            .clip(shape)
            .background(if (selected) colors.indigo else colors.surface, shape)
            .then(if (selected) Modifier else Modifier.border(1.5.dp, colors.border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (selected) colors.onIndigo else colors.ink, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, style = MaterialTheme.typography.labelLarge)
    }
}

/** A report: what and about what, when, its status and a short note */
@Composable
private fun ReportCard(report: MyReport, today: LocalDate, onOpen: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(colors.surface).clickable(onClick = onOpen).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val pending = report.isInReview
            Box(Modifier.size(44.dp).background(if (pending) colors.saffronSoft else colors.lavender, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(reportIcon(report), contentDescription = null, tint = if (pending) colors.saffronText else colors.accent, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(reportTitle(report), color = colors.ink, fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                Text(
                    listOfNotNull(targetText(report), reviewAgeText(reviewAge(report.createdAt, today))).joinToString(" · "),
                    color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = colors.muted, modifier = Modifier.size(20.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadge(report)
            Text(statusNote(report), color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun StatusBadge(report: MyReport, height: Int = 24) {
    val colors = MaterialTheme.feastColors
    val (icon, label, fill, ink) = when (report.status) {
        MyReportsState.STATUS_FIXED -> Quad(Icons.Rounded.CheckCircle, Res.string.my_reports_fixed, colors.successSoft, colors.success)
        MyReportsState.STATUS_NO_ACTION -> Quad(Icons.Rounded.DoNotDisturbOn, Res.string.my_reports_no_action, colors.lavender, colors.muted)
        else -> Quad(Icons.Rounded.Schedule, Res.string.my_reports_in_review, colors.saffronSoft, colors.saffronText)
    }
    Row(
        Modifier.height(height.dp).background(fill, RoundedCornerShape(8.dp)).padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = ink, modifier = Modifier.size(14.dp))
        Text(stringResource(label), color = ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
    }
}

private data class Quad(val icon: ImageVector, val label: StringResource, val fill: Color, val ink: Color)

@Composable
private fun statusNote(report: MyReport): String = stringResource(
    when (report.status) {
        MyReportsState.STATUS_FIXED -> when (report.kind) {
            "REVIEW" -> Res.string.my_reports_fixed_review
            "FEAST" -> Res.string.my_reports_fixed_feast
            else -> Res.string.my_reports_fixed_shop
        }
        MyReportsState.STATUS_NO_ACTION -> if (report.kind == "FEAST") Res.string.my_reports_feast_ended else Res.string.my_reports_kept
        else -> Res.string.my_reports_in_review_note
    },
)

// ==================== A report's page (1b in review, 1c decided) ====================

@Composable
private fun ReportDetail(
    report: MyReport,
    today: LocalDate,
    onOpenTarget: () -> Unit,
    onWithdraw: () -> Unit,
    onAddInfo: () -> Unit,
    withdrawing: Boolean,
    modifier: Modifier,
) {
    val colors = MaterialTheme.feastColors
    Column(modifier.fillMaxWidth()) {
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 14.dp, end = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (report.isInReview) {
                Card {
                    StatusBadge(report, height = 26)
                    Text(reportTitle(report), color = colors.ink, fontSize = 22.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.headlineSmall)
                    TargetRow(report, onOpenTarget)
                }
                Card {
                    Text(stringResource(Res.string.my_reports_progress), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                    Column {
                        Step(StepState.DONE, stringResource(Res.string.my_reports_sent), sentText(report.createdAt, today), last = false)
                        Step(StepState.NOW, stringResource(Res.string.my_reports_checking), stringResource(Res.string.my_reports_checking_note), last = false)
                        Step(StepState.LATER, stringResource(Res.string.my_reports_decision), stringResource(Res.string.my_reports_decision_note), last = true)
                    }
                }
                WhatYouSaid(report)
            } else {
                val fixed = report.status == MyReportsState.STATUS_FIXED
                val name = report.targetName?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.my_reports_this_place)
                Column(
                    Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(22.dp)).padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(Modifier.size(64.dp).background(if (fixed) colors.successSoft else colors.lavender, RoundedCornerShape(22.dp)), contentAlignment = Alignment.Center) {
                        Icon(if (fixed) Icons.Rounded.CheckCircle else Icons.Rounded.DoNotDisturbOn, contentDescription = null, tint = if (fixed) colors.success else colors.muted, modifier = Modifier.size(32.dp))
                    }
                    Text(
                        stringResource(if (fixed) Res.string.my_reports_fixed_title else Res.string.my_reports_no_action_title),
                        color = colors.ink, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        when {
                            fixed && report.kind == "REVIEW" -> stringResource(Res.string.my_reports_fixed_text_review, name)
                            fixed && report.kind == "FEAST" -> stringResource(Res.string.my_reports_fixed_text_feast, name)
                            fixed -> stringResource(Res.string.my_reports_fixed_text_shop, name)
                            report.kind == "FEAST" -> stringResource(Res.string.my_reports_no_action_feast_text)
                            else -> stringResource(Res.string.my_reports_no_action_text)
                        },
                        color = colors.muted, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (fixed) {
                    Card {
                        Text(stringResource(Res.string.my_reports_what_changed), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Pill(stringResource(Res.string.my_reports_on_map), colors.successSoft, colors.success, Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = colors.muted, modifier = Modifier.size(20.dp))
                            Pill(stringResource(Res.string.my_reports_hidden), colors.dangerSoft, colors.danger, Modifier.weight(1f))
                        }
                        Text(
                            listOfNotNull(
                                stringResource(Res.string.my_reports_reported_by, report.reporterCount.toInt()).takeIf { report.reporterCount > 1 },
                                formatReviewDate(report.resolvedAt)?.let { stringResource(Res.string.my_reports_resolved, it) },
                            ).joinToString(" · "),
                            color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Card {
                    TargetRow(report, onOpenTarget)
                    InfoRow(Icons.Outlined.Description, stringResource(Res.string.my_reports_reason, reportTitle(report)))
                    sentText(report.createdAt, today)?.let { InfoRow(Icons.Outlined.CalendarToday, stringResource(Res.string.my_reports_sent_on, it)) }
                }
                WhatYouSaid(report)
            }
        }
        if (report.isInReview) {
            // "Add more info" leads; "Withdraw" beside it, as in the design
            Row(
                Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FeastButton(
                    stringResource(Res.string.my_reports_add_info), onClick = onAddInfo, icon = Icons.Outlined.AddComment,
                    iconTint = colors.saffron, height = 52.dp, modifier = Modifier.weight(1f),
                )
                FeastButton(
                    stringResource(Res.string.my_reports_withdraw), onClick = onWithdraw, style = FeastButtonStyle.SOFT,
                    enabled = !withdrawing, height = 52.dp,
                )
            }
        }
    }
}

/** "What you said": the note and photos sent with the report, then anything added later; nothing when there's none */
@Composable
private fun WhatYouSaid(report: MyReport) {
    if (report.notes.isEmpty()) return
    val colors = MaterialTheme.feastColors
    var openPhotos by remember { mutableStateOf<Pair<List<String>, Int>?>(null) }
    Card {
        Text(stringResource(Res.string.my_reports_what_you_said), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
        report.notes.forEachIndexed { index, note ->
            if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender))
            note.text?.takeIf { it.isNotBlank() }?.let {
                Text(it, color = colors.ink, fontSize = 14.sp, lineHeight = 21.sp, style = MaterialTheme.typography.bodyMedium)
            }
            if (note.photoUrls.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    note.photoUrls.forEachIndexed { photo, url -> ReportPhotoTile(url, onClick = { openPhotos = note.photoUrls to photo }) }
                }
            }
        }
    }
    openPhotos?.let { (urls, index) ->
        FullScreenPhotos(urls, initialIndex = index, onDismiss = { openPhotos = null }, photoLabel = Res.string.review_photo)
    }
}

/** "Add more info": a note, photos or both; Send unlocks once there's something */
@Composable
private fun AddInfoSheet(sending: Boolean, onSend: (note: String, photos: List<PickedPhoto>) -> Unit, onDismiss: () -> Unit) {
    val colors = MaterialTheme.feastColors
    var note by rememberSaveable { mutableStateOf("") }
    var photos by remember { mutableStateOf(emptyList<PickedPhoto>()) }
    FeastSheet(onDismiss) { _ ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(44.dp).background(colors.lavender, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.AddComment, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(stringResource(Res.string.my_reports_add_info), color = colors.ink, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
                Text(stringResource(Res.string.my_reports_add_info_prompt), color = colors.muted, fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium)
            }
        }
        ReportDetailsFields(
            note = note,
            onNoteChange = { note = it },
            photos = photos,
            onAddPhotos = { photos = photos + it },
            onRemovePhoto = { photos = photos - it },
            showPhotos = true,
            title = null,
            enabled = !sending,
            modifier = Modifier.padding(top = 16.dp),
        )
        FeastButton(
            stringResource(Res.string.my_reports_add_info_send),
            onClick = { onSend(note, photos) },
            enabled = !sending && (note.isNotBlank() || photos.isNotEmpty()),
            height = 52.dp,
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
            content = if (sending) { { CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = colors.muted) } } else null,
        )
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(MaterialTheme.feastColors.surface, RoundedCornerShape(22.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) { content() }
}

/** The shop or feast the report is about; tap to open it */
@Composable
private fun TargetRow(report: MyReport, onOpen: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val canOpen = report.shopId != null || report.feastId != null
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.page).clickable(enabled = canOpen, onClick = onOpen).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(40.dp).background(colors.lavender, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(if (report.kind == "FEAST") Icons.Rounded.Celebration else Icons.Outlined.Storefront, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(targetText(report), color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
            report.targetArea?.takeIf { it.isNotBlank() }?.let {
                Text(it, color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (canOpen) Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = colors.muted, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    val colors = MaterialTheme.feastColors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
        Text(text, color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Pill(text: String, fill: Color, ink: Color, modifier: Modifier) {
    Box(modifier.height(40.dp).background(fill, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        Text(text, color = ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
    }
}

private enum class StepState { DONE, NOW, LATER }

/** One step of the progress tracker: done (indigo tick), now (saffron) or later (empty ring), joined by a line */
@Composable
private fun Step(step: StepState, title: String, note: String?, last: Boolean) {
    val colors = MaterialTheme.feastColors
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (step) {
                StepState.DONE -> Box(Modifier.size(24.dp).background(colors.indigo, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.onIndigo, modifier = Modifier.size(16.dp))
                }
                StepState.NOW -> Box(Modifier.size(34.dp).background(colors.saffronSoft, CircleShape).padding(5.dp).background(colors.saffron, CircleShape))
                StepState.LATER -> Box(Modifier.size(24.dp).border(2.dp, colors.border, CircleShape))
            }
            if (!last) Box(Modifier.width(2.dp).height(30.dp).background(if (step == StepState.DONE) colors.indigo else colors.border))
        }
        Column(Modifier.padding(top = if (step == StepState.NOW) 6.dp else 2.dp)) {
            Text(title, color = if (step == StepState.LATER) colors.muted else colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            note?.let { Text(it, color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

// ==================== Withdraw (1d) and empty (1e) ====================

@Composable
private fun WithdrawSheet(report: MyReport, onWithdraw: () -> Unit, onDismiss: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val name = report.targetName?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.my_reports_this_place)
    FeastSheet(onDismiss) { close ->
        Box(Modifier.size(56.dp).background(colors.lavender, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = null, tint = colors.accent, modifier = Modifier.size(28.dp))
        }
        Text(stringResource(Res.string.my_reports_withdraw_title), color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 14.dp))
        Text(stringResource(Res.string.my_reports_withdraw_text, name), color = colors.muted, fontSize = 14.sp, lineHeight = 20.sp, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
        Column(Modifier.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FeastButton(stringResource(Res.string.my_reports_withdraw_confirm), onClick = { close(onWithdraw) }, height = 52.dp, modifier = Modifier.fillMaxWidth())
            FeastButton(stringResource(Res.string.my_reports_keep), onClick = { close {} }, style = FeastButtonStyle.SOFT, height = 52.dp, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun NoReportsYet(modifier: Modifier) {
    val colors = MaterialTheme.feastColors
    Column(
        modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
    ) {
        Box(Modifier.size(112.dp).background(colors.lavender, RoundedCornerShape(36.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Flag, contentDescription = null, tint = colors.accent, modifier = Modifier.size(52.dp))
        }
        Text(stringResource(Res.string.my_reports_empty_title), color = colors.ink, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
        Text(stringResource(Res.string.my_reports_empty_text), color = colors.muted, fontSize = 14.sp, lineHeight = 21.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        Column(Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(18.dp)).padding(horizontal = 14.dp, vertical = 6.dp)) {
            ExampleRow(Icons.Outlined.WrongLocation, stringResource(Res.string.my_reports_example_location), divider = true)
            ExampleRow(Icons.Outlined.Storefront, stringResource(Res.string.my_reports_example_closed), divider = true)
            ExampleRow(Icons.Outlined.Report, stringResource(Res.string.my_reports_example_fake), divider = false)
        }
    }
}

@Composable
private fun ExampleRow(icon: ImageVector, text: String, divider: Boolean) {
    val colors = MaterialTheme.feastColors
    Column {
        Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
            Text(text, color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
        }
        if (divider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender))
    }
}

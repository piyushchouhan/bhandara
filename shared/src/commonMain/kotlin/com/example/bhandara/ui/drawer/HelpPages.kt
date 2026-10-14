package com.example.bhandara.ui.drawer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.data.network.SupportApi
import com.example.bhandara.photos.PhotoSourceSheet
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.LocalPlatformActions
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.about_bhandaras
import com.example.bhandara.shared.resources.about_by_upalio
import com.example.bhandara.shared.resources.about_crafted_with
import com.example.bhandara.shared.resources.about_dialog_description
import com.example.bhandara.shared.resources.about_dialog_title
import com.example.bhandara.shared.resources.about_moving_carts
import com.example.bhandara.shared.resources.about_street_food
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.copy
import com.example.bhandara.shared.resources.how_find_text
import com.example.bhandara.shared.resources.how_find_title
import com.example.bhandara.shared.resources.how_post_text
import com.example.bhandara.shared.resources.how_post_title
import com.example.bhandara.shared.resources.how_to_use
import com.example.bhandara.shared.resources.how_to_use_close
import com.example.bhandara.shared.resources.how_to_use_title
import com.example.bhandara.shared.resources.how_track_text
import com.example.bhandara.shared.resources.how_track_title
import com.example.bhandara.shared.resources.link_copied
import com.example.bhandara.shared.resources.logo_mask
import com.example.bhandara.shared.resources.report_add_screenshot
import com.example.bhandara.shared.resources.report_issue
import com.example.bhandara.shared.resources.report_issue_close
import com.example.bhandara.shared.resources.report_issue_desc
import com.example.bhandara.shared.resources.report_issue_hint
import com.example.bhandara.shared.resources.report_issue_submit
import com.example.bhandara.shared.resources.report_issue_success
import com.example.bhandara.shared.resources.report_issue_title
import com.example.bhandara.shared.resources.report_screenshot_hint
import com.example.bhandara.shared.resources.report_topic_feasts
import com.example.bhandara.shared.resources.report_topic_login
import com.example.bhandara.shared.resources.report_topic_map
import com.example.bhandara.shared.resources.report_topic_other
import com.example.bhandara.shared.resources.report_topic_shop
import com.example.bhandara.shared.resources.review_remove_photo
import com.example.bhandara.shared.resources.share_app_close
import com.example.bhandara.shared.resources.share_app_desc
import com.example.bhandara.shared.resources.share_app_link
import com.example.bhandara.shared.resources.share_app_share
import com.example.bhandara.shared.resources.share_app_title
import com.example.bhandara.shared.resources.share_message
import com.example.bhandara.shared.resources.share_more
import com.example.bhandara.shared.resources.share_whatsapp
import com.example.bhandara.shared.resources.suggest_feature
import com.example.bhandara.shared.resources.suggest_feature_close
import com.example.bhandara.shared.resources.suggest_feature_desc
import com.example.bhandara.shared.resources.suggest_feature_hint
import com.example.bhandara.shared.resources.suggest_feature_submit
import com.example.bhandara.shared.resources.suggest_feature_success
import com.example.bhandara.shared.resources.suggest_feature_title
import com.example.bhandara.shared.resources.support_dialog_description
import com.example.bhandara.shared.resources.support_dialog_title
import com.example.bhandara.shared.resources.support_email_button
import com.example.bhandara.shared.resources.terms_dialog_title
import com.example.bhandara.shared.resources.terms_intro
import com.example.bhandara.shared.resources.terms_moderation
import com.example.bhandara.shared.resources.terms_point1
import com.example.bhandara.shared.resources.terms_point2
import com.example.bhandara.shared.resources.terms_point3
import com.example.bhandara.shared.resources.version_label
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// The side menu's destinations (design "Help and Info"): pages for anything with typing or reading, and a bottom
// sheet only for sharing the app. Shared by Android and iOS.

/** Where users write to for help */
const val SUPPORT_EMAIL = "support@upalio.com"

/**
 * A help page: back and the title at the top, content that scrolls, and an optional bar at the bottom (on white
 * for forms, so the button stays above the keyboard)
 */
@Composable
private fun HelpPage(
    title: StringResource,
    onBack: () -> Unit,
    bottomBar: (@Composable () -> Unit)? = null,
    whiteBottomBar: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    FeastTheme {
        val colors = MaterialTheme.feastColors
        Column(Modifier.fillMaxSize().background(colors.page).windowInsetsPadding(WindowInsets.statusBars).imePadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), onBack)
                Text(stringResource(title), color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleLarge)
            }
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content,
            )
            bottomBar?.let { bar ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .then(if (whiteBottomBar) Modifier.shadow(12.dp, ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.12f)).background(colors.surface) else Modifier)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 16.dp),
                ) { bar() }
            }
        }
    }
}

@Composable
fun ReportIssueScreen(onBack: () -> Unit) = FeedbackPage(FeedbackKind.BUG, onBack)

@Composable
fun SuggestFeatureScreen(onBack: () -> Unit) = FeedbackPage(FeedbackKind.FEATURE, onBack)

/**
 * "Report an Issue" (design 1a: quick topics, details, an optional screenshot) or "Suggest a Feature" (1b, the same
 * shell in warm saffron). After sending it says thanks.
 */
@Composable
private fun FeedbackPage(kind: FeedbackKind, onBack: () -> Unit) {
    val dependencies = LocalAppDependencies.current
    val viewModel = viewModel { FeedbackViewModel(kind, SupportApi(dependencies.apiClient), dependencies.photoUploader::upload) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bug = kind == FeedbackKind.BUG
    var picking by remember { mutableStateOf(false) }

    HelpPage(
        title = if (bug) Res.string.report_issue_title else Res.string.suggest_feature_title,
        onBack = onBack,
        bottomBar = {
            if (state.sent) {
                FeastButton(
                    stringResource(if (bug) Res.string.report_issue_close else Res.string.suggest_feature_close),
                    onClick = onBack, height = 56.dp, modifier = Modifier.fillMaxWidth(),
                )
            } else {
                FeastButton(
                    label = stringResource(if (bug) Res.string.report_issue_submit else Res.string.suggest_feature_submit),
                    onClick = viewModel::send,
                    enabled = state.canSend || state.sending,
                    height = 56.dp,
                    modifier = Modifier.fillMaxWidth(),
                    content = if (state.sending) {
                        { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.feastColors.onIndigo) }
                    } else null,
                )
            }
        },
    ) {
        val colors = MaterialTheme.feastColors
        if (state.sent) {
            Sent(stringResource(if (bug) Res.string.report_issue_success else Res.string.suggest_feature_success))
            return@HelpPage
        }
        if (bug) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconTile(Icons.Rounded.BugReport, colors.lavender, colors.accent, size = 52.dp, iconSize = 26.dp, corner = 16.dp)
                Text(stringResource(Res.string.report_issue_desc), color = colors.body, fontSize = 15.sp, lineHeight = 21.sp, style = MaterialTheme.typography.bodyLarge)
            }
            TopicChips(state.topic, enabled = !state.sending, onClick = viewModel::onTopicClick)
            FeedbackField(state.text, stringResource(Res.string.report_issue_hint), viewModel::onTextChange, enabled = !state.sending, height = 150.dp, accent = colors.accent, alwaysOutlined = false, showCount = false)
            Screenshot(state.screenshot?.preview, enabled = !state.sending, onAdd = { picking = true }, onRemove = { viewModel.onScreenshot(null) })
        } else {
            Column(Modifier.fillMaxWidth().background(colors.saffronSoft, RoundedCornerShape(22.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                IconTile(Icons.Rounded.Lightbulb, colors.saffron, colors.onSaffron, size = 52.dp, iconSize = 26.dp, corner = 16.dp)
                Text(stringResource(Res.string.suggest_feature_desc), color = colors.ink, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
            }
            FeedbackField(state.text, stringResource(Res.string.suggest_feature_hint), viewModel::onTextChange, enabled = !state.sending, height = 170.dp, accent = colors.saffron, alwaysOutlined = true, showCount = true)
        }
        state.error?.let { Text(it, color = colors.danger, fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium) }
    }

    if (picking) {
        PhotoSourceSheet(maxCount = 1, onPicked = { viewModel.onScreenshot(it.firstOrNull()) }, onDismiss = { picking = false })
    }
}

/** Thanks after sending */
@Composable
private fun ColumnScope.Sent(text: String) {
    val colors = MaterialTheme.feastColors
    Column(
        Modifier.fillMaxWidth().padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(Modifier.size(110.dp).background(colors.successSoft, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = colors.success, modifier = Modifier.size(52.dp))
        }
        Text(text, color = colors.ink, fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun IconTile(icon: ImageVector, fill: Color, tint: Color, size: Dp, iconSize: Dp, corner: Dp = 14.dp, shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(corner)) {
    Box(Modifier.size(size).background(fill, shape), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

private val FeedbackTopic.label: StringResource
    get() = when (this) {
        FeedbackTopic.MAP -> Res.string.report_topic_map
        FeedbackTopic.SHOP -> Res.string.report_topic_shop
        FeedbackTopic.FEASTS -> Res.string.report_topic_feasts
        FeedbackTopic.LOGIN -> Res.string.report_topic_login
        FeedbackTopic.OTHER -> Res.string.report_topic_other
    }

/** Quick topics for a problem; the picked one is indigo */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopicChips(selected: FeedbackTopic?, enabled: Boolean, onClick: (FeedbackTopic) -> Unit) {
    val colors = MaterialTheme.feastColors
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FeedbackTopic.entries.forEach { topic ->
            val on = topic == selected
            val shape = RoundedCornerShape(12.dp)
            Box(
                Modifier
                    .height(36.dp)
                    .clip(shape)
                    .background(if (on) colors.indigo else colors.surface)
                    .then(if (on) Modifier else Modifier.border(1.5.dp, colors.border, shape))
                    .clickable(enabled = enabled) { onClick(topic) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(topic.label), color = if (on) colors.onIndigo else colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** The details box; outlined in [accent] while typing (or always, for ideas), with an optional count */
@Composable
private fun FeedbackField(
    text: String,
    hint: String,
    onChange: (String) -> Unit,
    enabled: Boolean,
    height: Dp,
    accent: Color,
    alwaysOutlined: Boolean,
    showCount: Boolean,
) {
    val colors = MaterialTheme.feastColors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 22.sp, color = colors.ink)
    val shape = RoundedCornerShape(18.dp)
    val border = when {
        focused -> 2.dp to accent
        alwaysOutlined -> 1.5.dp to accent
        else -> 1.5.dp to colors.border
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
            .background(colors.surface, shape)
            .border(border.first, border.second, shape),
    ) {
        BasicTextField(
            value = text,
            onValueChange = onChange,
            enabled = enabled,
            textStyle = style,
            cursorBrush = SolidColor(colors.ink),
            interactionSource = interaction,
            modifier = Modifier.matchParentSize().padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = if (showCount) 30.dp else 14.dp),
            decorationBox = { field ->
                Box {
                    if (text.isEmpty()) Text(hint, style = style.copy(color = colors.placeholder))
                    field()
                }
            },
        )
        if (showCount) {
            Text(
                "${text.length} / $MAX_FEEDBACK_LENGTH", color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 12.dp),
            )
        }
    }
}

/** "Add a screenshot": a dashed tile, or the picked screenshot with a remove button */
@Composable
private fun Screenshot(preview: Any?, enabled: Boolean, onAdd: () -> Unit, onRemove: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .height(if (preview != null) 76.dp else 60.dp)
            .clip(shape)
            .background(colors.lavenderSoft)
            .drawBehind {
                drawRoundRect(
                    colors.starOff, cornerRadius = CornerRadius(18.dp.toPx()),
                    style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))),
                )
            }
            .clickable(enabled = enabled && preview == null, onClick = onAdd)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (preview != null) {
            AsyncImage(preview, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)))
        } else {
            Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = colors.accent, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(stringResource(Res.string.report_add_screenshot), color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Text(stringResource(Res.string.report_screenshot_hint), color = colors.muted, fontSize = 12.sp, style = MaterialTheme.typography.bodySmall)
        }
        if (preview != null && enabled) {
            Box(Modifier.size(36.dp).clip(CircleShape).clickable(onClick = onRemove), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Close, stringResource(Res.string.review_remove_photo), tint = colors.muted, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/** "How to Use" (design 1c): the three tips as illustrated cards, and "Got it!" */
@Composable
fun HowToUseScreen(onBack: () -> Unit) = HelpPage(
    title = Res.string.how_to_use_title,
    onBack = onBack,
    whiteBottomBar = false,
    bottomBar = { FeastButton(stringResource(Res.string.how_to_use_close), onClick = onBack, height = 56.dp, modifier = Modifier.fillMaxWidth()) },
) {
    val colors = MaterialTheme.feastColors
    TipCard(Icons.Rounded.Search, colors.tileOnIndigo, colors.saffron, Res.string.how_find_title, Res.string.how_find_text, colors.indigo, colors.onIndigo, colors.headerSubtext)
    TipCard(Icons.Rounded.LocalShipping, colors.saffron, colors.onSaffron, Res.string.how_track_title, Res.string.how_track_text, colors.saffronSoft, colors.ink, colors.saffronBody, trail = true)
    TipCard(Icons.Rounded.Campaign, colors.successSoft, colors.success, Res.string.how_post_title, Res.string.how_post_text, colors.surface, colors.ink, colors.muted)
}

@Composable
private fun TipCard(
    icon: ImageVector,
    tile: Color,
    tint: Color,
    title: StringResource,
    text: StringResource,
    fill: Color,
    titleColor: Color,
    textColor: Color,
    trail: Boolean = false,
) {
    val colors = MaterialTheme.feastColors
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(fill)) {
        if (trail) {
            // The cart's dotted trail, bottom right
            Canvas(Modifier.align(Alignment.BottomEnd).padding(bottom = 4.dp).size(width = 90.dp, height = 26.dp)) {
                val path = Path().apply {
                    moveTo(0f, size.height * 22 / 26)
                    cubicTo(size.width * 30 / 90, size.height * 18 / 26, size.width * 50 / 90, size.height * 4 / 26, size.width * 92 / 90, size.height * 6 / 26)
                }
                drawPath(path, colors.saffron, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 8.dp.toPx()))))
            }
        }
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            IconTile(icon, tile, tint, size = 48.dp, iconSize = 24.dp)
            Column {
                Text(stringResource(title), color = titleColor, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text(stringResource(text), color = textColor, fontSize = 14.sp, lineHeight = 20.sp, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

/** A dotted saffron trail across an indigo header, drawn for a [w] × [h] box and stretched */
@Composable
private fun BoxScope.Trail(w: Float, h: Float, points: List<Float>, alpha: Float) {
    val colors = MaterialTheme.feastColors
    Canvas(Modifier.matchParentSize()) {
        val sx = size.width / w
        val sy = size.height / h
        val path = Path().apply {
            moveTo(points[0] * sx, points[1] * sy)
            cubicTo(points[2] * sx, points[3] * sy, points[4] * sx, points[5] * sy, points[6] * sx, points[7] * sy)
            cubicTo(points[8] * sx, points[9] * sy, points[10] * sx, points[11] * sy, points[12] * sx, points[13] * sy)
        }
        drawPath(path, colors.saffron.copy(alpha = alpha), style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 9.dp.toPx()))))
    }
}

/** The logo in indigo on a saffron tile */
@Composable
private fun LogoTile(size: Dp, logo: Dp, corner: Dp) {
    val colors = MaterialTheme.feastColors
    Box(Modifier.size(size).background(colors.saffron, RoundedCornerShape(corner)), contentAlignment = Alignment.Center) {
        Image(painterResource(Res.drawable.logo_mask), contentDescription = null, colorFilter = ColorFilter.tint(colors.onSaffron), modifier = Modifier.width(logo).height(logo * 0.9f))
    }
}

/** "About" (design 1e): the logo on indigo with the cart trail, what the app does, and who made it */
@Composable
fun AboutScreen(onBack: () -> Unit) = FeastTheme {
    val colors = MaterialTheme.feastColors
    val version = LocalAppDependencies.current.appVersion
    Column(Modifier.fillMaxSize().background(colors.page)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().background(colors.indigo)) {
                Trail(360f, 300f, listOf(-10f, 260f, 80f, 220f, 140f, 150f, 220f, 140f, 300f, 130f, 320f, 80f, 380f, 40f), alpha = 0.6f)
                Column(Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(bottom = 30.dp)) {
                    Box(
                        Modifier.padding(horizontal = 16.dp, vertical = 6.dp).size(44.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.12f)).clickable(onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Column(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        LogoTile(96.dp, 58.dp, 30.dp)
                        Text(stringResource(Res.string.about_dialog_title), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.headlineSmall)
                        if (version.isNotEmpty()) {
                            Box(Modifier.height(26.dp).background(Color.White.copy(alpha = 0.12f), CircleShape).padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
                                Text(stringResource(Res.string.version_label, version), color = colors.headerSubtext, fontSize = 13.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(stringResource(Res.string.about_dialog_description), color = colors.body, fontSize = 16.sp, lineHeight = 24.sp, style = MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WhatTile(Icons.Rounded.Storefront, colors.surface, colors.accent, Res.string.about_street_food)
                    WhatTile(Icons.Rounded.LocalShipping, colors.saffronSoft, colors.saffronText, Res.string.about_moving_carts)
                    WhatTile(Icons.Rounded.VolunteerActivism, colors.successSoft, colors.success, Res.string.about_bhandaras)
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(Res.string.about_crafted_with), color = colors.muted, fontSize = 14.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelLarge)
            Icon(Icons.Rounded.Favorite, contentDescription = null, tint = colors.heart, modifier = Modifier.size(18.dp))
            Text(stringResource(Res.string.about_by_upalio), color = colors.muted, fontSize = 14.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun RowScope.WhatTile(icon: ImageVector, fill: Color, tint: Color, label: StringResource) {
    val colors = MaterialTheme.feastColors
    Column(Modifier.weight(1f).background(fill, RoundedCornerShape(18.dp)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Text(stringResource(label), color = colors.ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * "Support & Feedback" (design 1f): the email as a card with a button, and shortcuts to the two forms and the tips
 * so people pick the right door
 */
@Composable
fun SupportScreen(onBack: () -> Unit, onReportIssue: () -> Unit, onSuggestFeature: () -> Unit, onHowToUse: () -> Unit) =
    HelpPage(Res.string.support_dialog_title, onBack) {
        val colors = MaterialTheme.feastColors
        val actions = LocalPlatformActions.current
        Column(Modifier.fillMaxWidth().background(colors.indigo, RoundedCornerShape(24.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            IconTile(Icons.Rounded.Mail, colors.saffron, colors.onSaffron, size = 56.dp, iconSize = 28.dp, corner = 18.dp)
            Text(stringResource(Res.string.support_dialog_description), color = colors.bodyOnIndigo, fontSize = 15.sp, lineHeight = 22.sp, style = MaterialTheme.typography.bodyLarge)
            FeastButton(
                stringResource(Res.string.support_email_button), onClick = { actions.openUrl("mailto:$SUPPORT_EMAIL") },
                style = FeastButtonStyle.SAFFRON, icon = Icons.Rounded.Mail, height = 52.dp, modifier = Modifier.fillMaxWidth(),
            )
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.surface)) {
            SupportRow(Icons.Rounded.BugReport, colors.accent, Res.string.report_issue, onReportIssue)
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender))
            SupportRow(Icons.Rounded.Lightbulb, colors.saffronText, Res.string.suggest_feature, onSuggestFeature)
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender))
            SupportRow(Icons.AutoMirrored.Rounded.Help, colors.success, Res.string.how_to_use, onHowToUse)
        }
    }

@Composable
private fun SupportRow(icon: ImageVector, tint: Color, label: StringResource, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.fillMaxWidth().height(60.dp).clickable(onClick = onClick).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Text(stringResource(label), color = colors.ink, fontSize = 16.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = colors.muted, modifier = Modifier.size(22.dp))
    }
}

/** "Terms & Conditions" (design 1g): numbered points as a readable card, the moderation note set apart */
@Composable
fun TermsScreen(onBack: () -> Unit) = HelpPage(Res.string.terms_dialog_title, onBack) {
    val colors = MaterialTheme.feastColors
    Text(stringResource(Res.string.terms_intro), color = colors.ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
    Column(Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 4.dp)) {
        listOf(Res.string.terms_point1, Res.string.terms_point2, Res.string.terms_point3).forEachIndexed { i, point ->
            if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender))
            Row(Modifier.padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(30.dp).background(colors.lavender, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Text("${i + 1}", color = colors.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                }
                Text(stringResource(point), color = colors.ink, fontSize = 15.sp, lineHeight = 22.sp, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
    Row(Modifier.fillMaxWidth().background(colors.saffronSoft, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Rounded.Shield, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(22.dp))
        Text(stringResource(Res.string.terms_moderation), color = colors.saffronBody, fontSize = 15.sp, lineHeight = 22.sp, style = MaterialTheme.typography.bodyLarge)
    }
}

/** Percent-encodes text for a link's query, e.g. a WhatsApp message */
private fun urlEncode(text: String): String = buildString {
    text.encodeToByteArray().forEach { byte ->
        val c = byte.toInt().toChar()
        if (c.isLetterOrDigit() && c.code < 128 || c in "-_.~") append(c) else append("%" + (byte.toInt() and 0xFF).toString(16).uppercase().padStart(2, '0'))
    }
}

/**
 * "Share Local Feast" (design 1d), the one destination that stays a sheet: quick targets for WhatsApp and
 * messages, the link to copy, and the phone's own share sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ShareAppSheet(onDismiss: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val actions = LocalPlatformActions.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val description = stringResource(Res.string.share_app_desc)
    val link = stringResource(Res.string.share_app_link)
    val message = "$description\n$link"
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val close: (() -> Unit) -> Unit = { then -> scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss(); then() } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        containerColor = colors.surface,
        contentColor = colors.ink,
        scrimColor = colors.sheetScrim,
        dragHandle = { Box(Modifier.padding(top = 10.dp).size(width = 38.dp, height = 5.dp).background(colors.border, RoundedCornerShape(3.dp))) },
    ) {
        Box {
            Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(colors.indigo)) {
                    Canvas(Modifier.align(Alignment.BottomEnd).padding(bottom = 2.dp).size(width = 120.dp, height = 18.dp)) {
                        val path = Path().apply {
                            moveTo(0f, size.height * 14 / 18)
                            cubicTo(size.width / 3, size.height * 12 / 18, size.width * 2 / 3, size.height * 4 / 18, size.width * 122 / 120, size.height * 4 / 18)
                        }
                        drawPath(path, colors.saffron.copy(alpha = 0.7f), style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 8.dp.toPx()))))
                    }
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        LogoTile(52.dp, 32.dp, 16.dp)
                        Column {
                            Text(stringResource(Res.string.share_app_title), color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                            Text(description, color = colors.headerSubtext, fontSize = 13.sp, lineHeight = 18.sp, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ShareTarget(Icons.Rounded.Chat, colors.successSoft, colors.success, Res.string.share_whatsapp) {
                        close { actions.openUrl("https://wa.me/?text=${urlEncode(message)}") }
                    }
                    ShareTarget(Icons.Rounded.Sms, colors.lavender, colors.accent, Res.string.share_message) {
                        close { actions.openUrl("sms:?&body=${urlEncode(message)}") }
                    }
                    ShareTarget(Icons.Rounded.MoreHoriz, colors.lavenderSoft, colors.ink, Res.string.share_more) {
                        close { actions.share(message) }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().height(54.dp).background(colors.lavenderSoft, RoundedCornerShape(16.dp)).padding(start = 14.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Rounded.Link, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
                    Text(link, color = colors.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Row(
                        Modifier.height(42.dp).clip(RoundedCornerShape(12.dp)).background(colors.surface).clickable {
                            clipboard.setText(AnnotatedString(link))
                            scope.launch { snackbar.showSnackbar(getString(Res.string.link_copied)) }
                        }.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = colors.ink, modifier = Modifier.size(18.dp))
                        Text(stringResource(Res.string.copy), color = colors.ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeastButton(stringResource(Res.string.share_app_close), onClick = { close {} }, style = FeastButtonStyle.SOFT, modifier = Modifier.weight(1f))
                    FeastButton(
                        stringResource(Res.string.share_app_share), onClick = { close { actions.share(message) } }, style = FeastButtonStyle.SAFFRON,
                        icon = Icons.Rounded.Share, modifier = Modifier.weight(1.3f),
                    )
                }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun ShareTarget(icon: ImageVector, fill: Color, tint: Color, label: StringResource, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Column(
        Modifier.width(72.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        IconTile(icon, fill, tint, size = 56.dp, iconSize = 26.dp, shape = CircleShape)
        Text(stringResource(label), color = colors.ink, fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
    }
}

package com.example.bhandara.ui.drawer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.about
import com.example.bhandara.shared.resources.app_name
import com.example.bhandara.shared.resources.how_to_use
import com.example.bhandara.shared.resources.logo_mask
import com.example.bhandara.shared.resources.profile
import com.example.bhandara.shared.resources.report_issue
import com.example.bhandara.shared.resources.settings
import com.example.bhandara.shared.resources.share_app
import com.example.bhandara.shared.resources.suggest_feature
import com.example.bhandara.shared.resources.support
import com.example.bhandara.shared.resources.terms
import com.example.bhandara.ui.theme.feastColors
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The home screen's side menu, shared by Android and iOS (the redesign's side sheet; use inside FeastTheme):
 * profile, settings (with the language), about, support, terms, feedback, help and sharing the app.
 *
 * @param extraItems platform-only rows at the top (Android's vendor mode); gets a way to close the menu
 * @param overlay    drawn over the content, e.g. Android's "vendor mode is on" banner
 * @param content    the screen under the menu; gets a way to open it (the screen has its own menu button)
 */
@Composable
fun AppDrawer(
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onSupportClick: () -> Unit,
    onTermsClick: () -> Unit,
    onReportIssueClick: () -> Unit,
    onSuggestFeatureClick: () -> Unit,
    onHowToUseClick: () -> Unit,
    extraItems: @Composable ColumnScope.(closeDrawer: () -> Unit) -> Unit = {},
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable (openDrawer: () -> Unit) -> Unit,
) {
    val colors = MaterialTheme.feastColors
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }
    var sharing by rememberSaveable { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // Opens only from the menu button, so dragging the map never pulls it out; once open, a swipe closes it
        gesturesEnabled = drawerState.isOpen,
        scrimColor = colors.scrim,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(306.dp),
                drawerShape = RoundedCornerShape(topEnd = 30.dp, bottomEnd = 30.dp),
                drawerContainerColor = colors.surface,
                drawerContentColor = colors.ink,
                // The header runs under the status bar; the footer keeps clear of the home indicator
                windowInsets = WindowInsets(0),
            ) {
                Column(Modifier.fillMaxHeight()) {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        DrawerHeader()
                        Column(
                            Modifier.padding(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            extraItems(closeDrawer)
                            ProfileRow(onClick = { closeDrawer(); onProfileClick() })
                            SheetRow(
                                Icons.Rounded.Settings, stringResource(Res.string.settings),
                                border = colors.lavender,
                                trailing = { Icon(Icons.Rounded.ChevronRight, null, tint = colors.muted, modifier = Modifier.size(20.dp)) },
                                onClick = { closeDrawer(); onSettingsClick() },
                            )
                        }
                        Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            LinkRow(Icons.Rounded.Info, Res.string.about) { closeDrawer(); onAboutClick() }
                            LinkRow(Icons.Rounded.Mail, Res.string.support) { closeDrawer(); onSupportClick() }
                            LinkRow(Icons.Rounded.Description, Res.string.terms) { closeDrawer(); onTermsClick() }
                            LinkRow(Icons.Rounded.BugReport, Res.string.report_issue) { closeDrawer(); onReportIssueClick() }
                            LinkRow(Icons.Rounded.Lightbulb, Res.string.suggest_feature) { closeDrawer(); onSuggestFeatureClick() }
                            LinkRow(Icons.AutoMirrored.Rounded.Help, Res.string.how_to_use) { closeDrawer(); onHowToUseClick() }
                            LinkRow(Icons.Rounded.Share, Res.string.share_app) { sharing = true }
                        }
                    }
                    DrawerFooter()
                }
            }
        },
    ) {
        Box {
            content { scope.launch { drawerState.open() } }
            overlay()
        }
    }

    if (sharing) ShareAppSheet(onDismiss = { sharing = false })
}

/** Indigo band with the logo on a saffron tile, the app's name and version, and a dotted saffron trail */
@Composable
private fun DrawerHeader() {
    val colors = MaterialTheme.feastColors
    val version = LocalAppDependencies.current.appVersion
    Box(Modifier.fillMaxWidth().background(colors.indigo)) {
        Canvas(Modifier.matchParentSize()) {
            // The design's trail, drawn for a 306 × 140 header and stretched to this one
            val sx = size.width / 306f
            val sy = size.height / 140f
            fun p(x: Float, y: Float) = Offset(x * sx, y * sy)
            val trail = Path().apply {
                p(150f, 140f).let { moveTo(it.x, it.y) }
                val (a, b, c) = listOf(p(190f, 100f), p(220f, 80f), p(250f, 60f))
                cubicTo(a.x, a.y, b.x, b.y, c.x, c.y)
                val (d, e, f) = listOf(p(280f, 40f), p(290f, 30f), p(320f, 20f))
                cubicTo(d.x, d.y, e.x, e.y, f.x, f.y)
            }
            drawPath(
                trail,
                colors.saffron.copy(alpha = 0.7f),
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 8.dp.toPx())),
                ),
            )
        }
        Row(
            Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LogoTile(size = 52.dp, logoWidth = 32.dp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    stringResource(Res.string.app_name),
                    color = colors.onIndigo,
                    fontSize = 21.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge,
                )
                if (version.isNotEmpty()) {
                    Text(
                        "v$version",
                        color = colors.headerSubtext,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

/** The logo in indigo on a rounded saffron tile */
@Composable
internal fun LogoTile(size: Dp, logoWidth: Dp) {
    val colors = MaterialTheme.feastColors
    Box(Modifier.size(size).background(colors.saffron, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
        Image(
            painterResource(Res.drawable.logo_mask),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colors.onSaffron),
            modifier = Modifier.width(logoWidth).height(logoWidth * 0.9f),
        )
    }
}

/**
 * A row in the side sheet's top group: an icon, a label and something at the end, on a soft fill or with an
 * outline
 */
@Composable
fun SheetRow(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.feastColors.accent,
    background: Color = Color.Transparent,
    border: Color? = null,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(background, shape)
            .then(if (border != null) Modifier.border(1.5.dp, border, shape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = colors.ink, fontSize = 16.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let {
                Text(it, color = colors.muted, fontSize = 12.sp, style = MaterialTheme.typography.bodySmall)
            }
        }
        trailing()
    }
}

/**
 * An on/off row in saffron, e.g. Android's vendor mode
 *
 * @param subtitle under the label, e.g. why it can't be turned on
 */
@Composable
fun SheetToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    subtitle: String? = null,
) {
    val colors = MaterialTheme.feastColors
    val thumb = if (colors.isDark) colors.surface else Color.White
    SheetRow(
        icon, label,
        iconTint = colors.saffronText,
        background = colors.saffronSoft,
        subtitle = subtitle,
        onClick = if (enabled) ({ onCheckedChange(!checked) }) else null,
    ) {
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

/** The plain rows under the top group: about, support, terms, feedback, help, sharing */
@Composable
private fun LinkRow(icon: ImageVector, label: StringResource, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icon, contentDescription = null, tint = colors.muted, modifier = Modifier.size(22.dp))
        Text(stringResource(label), color = colors.ink, fontSize = 16.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun DrawerFooter() {
    val colors = MaterialTheme.feastColors
    Column {
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender))
        Row(
            Modifier.windowInsetsPadding(WindowInsets.navigationBars).padding(start = 26.dp, end = 26.dp, top = 14.dp, bottom = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.size(7.dp).background(colors.saffron, CircleShape))
            Text("Upalio", color = colors.muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** "Your profile", with the account's picture, or its first letter, when signed in with one */
@Composable
private fun ProfileRow(onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val account = LocalAppDependencies.current.auth.account()?.takeIf { !it.isAnonymous }
    val photo = account?.photoUrl
    val initial = (account?.name ?: account?.email)?.trim()?.firstOrNull()?.uppercaseChar()
    SheetRow(Icons.Rounded.AccountCircle, stringResource(Res.string.profile), border = colors.lavender, onClick = onClick) {
        when {
            photo != null -> AsyncImage(
                model = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(30.dp).clip(CircleShape),
            )
            initial != null -> Box(Modifier.size(30.dp).background(colors.indigo, CircleShape), contentAlignment = Alignment.Center) {
                Text(
                    initial.toString(),
                    color = colors.saffron,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

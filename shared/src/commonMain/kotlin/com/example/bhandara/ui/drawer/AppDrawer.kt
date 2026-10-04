package com.example.bhandara.ui.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.bhandara.platform.APP_LANGUAGES
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.rememberAppLanguage
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.about
import com.example.bhandara.shared.resources.app_name
import com.example.bhandara.shared.resources.how_to_use
import com.example.bhandara.shared.resources.language
import com.example.bhandara.shared.resources.language_in_settings
import com.example.bhandara.shared.resources.localfeast
import com.example.bhandara.shared.resources.menu
import com.example.bhandara.shared.resources.profile
import com.example.bhandara.shared.resources.report_issue
import com.example.bhandara.shared.resources.share_app
import com.example.bhandara.shared.resources.suggest_feature
import com.example.bhandara.shared.resources.support
import com.example.bhandara.shared.resources.terms
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private enum class DrawerDialog { REPORT_ISSUE, SUGGEST_FEATURE, HOW_TO_USE, SHARE_APP }

/**
 * The home screen's side menu, shared by Android and iOS: language, profile, about, support, terms, feedback,
 * help and sharing the app.
 *
 * @param extraItems platform-only items after the language (Android's vendor mode); gets a way to close the menu
 * @param overlay    drawn over the content, e.g. Android's "vendor mode is on" banner
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawer(
    onProfileClick: () -> Unit,
    onAboutClick: () -> Unit,
    onSupportClick: () -> Unit,
    onTermsClick: () -> Unit,
    extraItems: @Composable ColumnScope.(closeDrawer: () -> Unit) -> Unit = {},
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }
    var dialog by rememberSaveable { mutableStateOf<DrawerDialog?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(Modifier.width(300.dp)) {
                Column(Modifier.fillMaxHeight().verticalScroll(rememberScrollState())) {
                    DrawerHeader()
                    HorizontalDivider(Modifier.padding(bottom = 8.dp))

                    LanguageItem()
                    extraItems(closeDrawer)
                    ProfileItem(onClick = { closeDrawer(); onProfileClick() })
                    DrawerItem(Icons.Default.Info, Res.string.about) { closeDrawer(); onAboutClick() }
                    DrawerItem(Icons.Default.Mail, Res.string.support) { closeDrawer(); onSupportClick() }
                    DrawerItem(Icons.Default.Description, Res.string.terms) { closeDrawer(); onTermsClick() }
                    DrawerItem(Icons.Default.BugReport, Res.string.report_issue) { dialog = DrawerDialog.REPORT_ISSUE }
                    DrawerItem(Icons.Default.Lightbulb, Res.string.suggest_feature) { dialog = DrawerDialog.SUGGEST_FEATURE }
                    DrawerItem(Icons.AutoMirrored.Filled.HelpOutline, Res.string.how_to_use) { dialog = DrawerDialog.HOW_TO_USE }
                    DrawerItem(Icons.Default.Share, Res.string.share_app) { dialog = DrawerDialog.SHARE_APP }

                    Spacer(Modifier.weight(1f))
                    HorizontalDivider(Modifier.padding(top = 16.dp))
                    Text(
                        "Upalio",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    )
                }
            }
        },
    ) {
        Box {
            Column {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = stringResource(Res.string.menu))
                        }
                    },
                )
                content()
            }
            overlay()
        }
    }

    when (dialog) {
        DrawerDialog.REPORT_ISSUE -> FeedbackDialog(FeedbackKind.BUG, onDismiss = { dialog = null })
        DrawerDialog.SUGGEST_FEATURE -> FeedbackDialog(FeedbackKind.FEATURE, onDismiss = { dialog = null })
        DrawerDialog.HOW_TO_USE -> HowToUseDialog(onDismiss = { dialog = null })
        DrawerDialog.SHARE_APP -> ShareAppDialog(onDismiss = { dialog = null })
        null -> {}
    }
}

@Composable
private fun DrawerHeader() {
    val version = LocalAppDependencies.current.appVersion
    Row(Modifier.padding(horizontal = 24.dp, vertical = 24.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(Res.drawable.localfeast), contentDescription = null, modifier = Modifier.size(48.dp).clip(CircleShape))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (version.isNotEmpty()) {
                Text("v$version", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** One row of the menu: an icon and a label, with optional content at the end */
@Composable
internal fun DrawerItem(
    icon: ImageVector,
    label: StringResource,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = {},
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).heightIn(min = 56.dp).padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(label), style = MaterialTheme.typography.bodyLarge)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        trailing()
    }
}

/** On Android a menu of the app's languages; on iOS it opens Settings, where iOS keeps each app's language */
@Composable
private fun LanguageItem() {
    val language = rememberAppLanguage()
    var choosing by remember { mutableStateOf(false) }
    val currentName = APP_LANGUAGES.first { it.first == language.current }.second
    Box {
        DrawerItem(
            Icons.Default.Language,
            Res.string.language,
            subtitle = if (language.choosableInApp) null else stringResource(Res.string.language_in_settings),
            trailing = { Text(currentName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary) },
            onClick = { if (language.choosableInApp) choosing = true else language.choose(language.current) },
        )
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

/** "Your profile", with the account's picture when signed in with one */
@Composable
private fun ProfileItem(onClick: () -> Unit) {
    val account = LocalAppDependencies.current.auth.account()
    val photo = account?.photoUrl?.takeIf { !account.isAnonymous }
    DrawerItem(
        Icons.Default.AccountCircle,
        Res.string.profile,
        trailing = {
            if (photo != null) {
                AsyncImage(model = photo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(28.dp).clip(CircleShape))
            }
        },
        onClick = onClick,
    )
}

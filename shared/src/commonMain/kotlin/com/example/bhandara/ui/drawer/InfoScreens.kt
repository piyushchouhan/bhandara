package com.example.bhandara.ui.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.LocalPlatformActions
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.about_dialog_crafted
import com.example.bhandara.shared.resources.about_dialog_description
import com.example.bhandara.shared.resources.about_dialog_title
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.localfeast
import com.example.bhandara.shared.resources.support_dialog_description
import com.example.bhandara.shared.resources.support_dialog_title
import com.example.bhandara.shared.resources.support_email_button
import com.example.bhandara.shared.resources.terms_dialog_description
import com.example.bhandara.shared.resources.terms_dialog_title
import com.example.bhandara.shared.resources.version_label
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Where users write to for help */
const val SUPPORT_EMAIL = "support@upalio.com"

/** A simple page from the side menu: title bar with back, and centred content that scrolls */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoPage(title: StringResource, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back)) }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

@Composable
private fun RoundIcon(icon: ImageVector) {
    Box(Modifier.size(80.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) = InfoPage(Res.string.about_dialog_title, onBack) {
    val version = LocalAppDependencies.current.appVersion
    Image(painterResource(Res.drawable.localfeast), contentDescription = null, modifier = Modifier.size(96.dp).clip(CircleShape))
    Text(stringResource(Res.string.about_dialog_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    if (version.isNotEmpty()) {
        Text(stringResource(Res.string.version_label, version), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Text(stringResource(Res.string.about_dialog_description), textAlign = TextAlign.Center, lineHeight = 24.sp)
    Text(stringResource(Res.string.about_dialog_crafted), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
}

@Composable
fun SupportScreen(onBack: () -> Unit) = InfoPage(Res.string.support_dialog_title, onBack) {
    val actions = LocalPlatformActions.current
    RoundIcon(Icons.Default.Mail)
    Text(stringResource(Res.string.support_dialog_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(stringResource(Res.string.support_dialog_description), textAlign = TextAlign.Center, lineHeight = 24.sp)
    Button(onClick = { actions.openUrl("mailto:$SUPPORT_EMAIL") }) {
        Icon(Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(Res.string.support_email_button))
    }
}

@Composable
fun TermsScreen(onBack: () -> Unit) = InfoPage(Res.string.terms_dialog_title, onBack) {
    RoundIcon(Icons.Default.Description)
    Text(stringResource(Res.string.terms_dialog_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(stringResource(Res.string.terms_dialog_description), lineHeight = 24.sp)
}

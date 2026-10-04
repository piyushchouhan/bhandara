package com.example.bhandara.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bhandara.data.network.SupportApi
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.LocalPlatformActions
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.how_to_use_close
import com.example.bhandara.shared.resources.how_to_use_step1
import com.example.bhandara.shared.resources.how_to_use_step2
import com.example.bhandara.shared.resources.how_to_use_step3
import com.example.bhandara.shared.resources.how_to_use_title
import com.example.bhandara.shared.resources.report_issue_close
import com.example.bhandara.shared.resources.report_issue_desc
import com.example.bhandara.shared.resources.report_issue_hint
import com.example.bhandara.shared.resources.report_issue_submit
import com.example.bhandara.shared.resources.report_issue_success
import com.example.bhandara.shared.resources.report_issue_title
import com.example.bhandara.shared.resources.share_app_close
import com.example.bhandara.shared.resources.share_app_desc
import com.example.bhandara.shared.resources.share_app_link
import com.example.bhandara.shared.resources.share_app_share
import com.example.bhandara.shared.resources.share_app_title
import com.example.bhandara.shared.resources.suggest_feature_close
import com.example.bhandara.shared.resources.suggest_feature_desc
import com.example.bhandara.shared.resources.suggest_feature_hint
import com.example.bhandara.shared.resources.suggest_feature_submit
import com.example.bhandara.shared.resources.suggest_feature_success
import com.example.bhandara.shared.resources.suggest_feature_title
import com.example.bhandara.ui.components.ScreenViewModels
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** A round icon on top of the menu's dialogs */
@Composable
private fun DialogIcon(icon: ImageVector) {
    Box(Modifier.size(56.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
    }
}

private class FeedbackTexts(
    val icon: ImageVector,
    val title: StringResource,
    val description: StringResource,
    val hint: StringResource,
    val submit: StringResource,
    val success: StringResource,
    val close: StringResource,
)

private fun FeedbackKind.texts() = when (this) {
    FeedbackKind.BUG -> FeedbackTexts(
        Icons.Default.BugReport, Res.string.report_issue_title, Res.string.report_issue_desc, Res.string.report_issue_hint,
        Res.string.report_issue_submit, Res.string.report_issue_success, Res.string.report_issue_close,
    )
    FeedbackKind.FEATURE -> FeedbackTexts(
        Icons.Default.Lightbulb, Res.string.suggest_feature_title, Res.string.suggest_feature_desc, Res.string.suggest_feature_hint,
        Res.string.suggest_feature_submit, Res.string.suggest_feature_success, Res.string.suggest_feature_close,
    )
}

/** "Report an issue" or "Suggest a feature": a text box, sent to the team; starts empty every time it opens */
@Composable
internal fun FeedbackDialog(kind: FeedbackKind, onDismiss: () -> Unit) = ScreenViewModels {
    val dependencies = LocalAppDependencies.current
    val viewModel = viewModel { FeedbackViewModel(kind, SupportApi(dependencies.apiClient)) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val texts = kind.texts()

    AlertDialog(
        onDismissRequest = { if (!state.sending) onDismiss() },
        icon = { DialogIcon(if (state.sent) Icons.Default.CheckCircle else texts.icon) },
        title = { Text(stringResource(texts.title), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (state.sent) {
                    Text(stringResource(texts.success), textAlign = TextAlign.Center)
                } else {
                    Text(stringResource(texts.description), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = state.text,
                        onValueChange = viewModel::onTextChange,
                        enabled = !state.sending,
                        placeholder = { Text(stringResource(texts.hint)) },
                        isError = state.error != null,
                        supportingText = state.error?.let { error -> { Text(error) } },
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            if (state.sent) {
                TextButton(onClick = onDismiss) { Text(stringResource(texts.close)) }
            } else {
                Button(onClick = viewModel::send, enabled = state.canSend, shape = RoundedCornerShape(12.dp)) {
                    if (state.sending) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(stringResource(texts.submit))
                    }
                }
            }
        },
        dismissButton = if (state.sent) null else {
            { TextButton(onClick = onDismiss, enabled = !state.sending) { Text(stringResource(texts.close)) } }
        },
    )
}

@Composable
internal fun HowToUseDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { DialogIcon(Icons.AutoMirrored.Filled.HelpOutline) },
        title = { Text(stringResource(Res.string.how_to_use_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(Res.string.how_to_use_step1, Res.string.how_to_use_step2, Res.string.how_to_use_step3).forEach {
                    Text(stringResource(it), style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.how_to_use_close)) } },
    )
}

/** Invites friends: the phone's share sheet (which can also copy the link) */
@Composable
internal fun ShareAppDialog(onDismiss: () -> Unit) {
    val actions = LocalPlatformActions.current
    val description = stringResource(Res.string.share_app_desc)
    val link = stringResource(Res.string.share_app_link)
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { DialogIcon(Icons.Default.Share) },
        title = { Text(stringResource(Res.string.share_app_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(description, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(link, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    actions.share("$description\n$link")
                },
                shape = RoundedCornerShape(12.dp),
            ) { Text(stringResource(Res.string.share_app_share)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.share_app_close)) } },
    )
}

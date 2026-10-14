package com.example.bhandara.ui.feasts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WrongLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bhandara.data.models.api.FeastReportReason
import com.example.bhandara.photos.PickedPhoto
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.cancel
import com.example.bhandara.shared.resources.feast_report_prompt
import com.example.bhandara.shared.resources.feast_report_reason_money
import com.example.bhandara.shared.resources.feast_report_reason_not_happening
import com.example.bhandara.shared.resources.feast_report_reason_wrong_time
import com.example.bhandara.shared.resources.feast_report_title
import com.example.bhandara.shared.resources.report_reason_inappropriate
import com.example.bhandara.shared.resources.report_reason_wrong_location
import com.example.bhandara.shared.resources.report_submit
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.components.ReportDetailsFields
import com.example.bhandara.ui.shopdetails.FeastSheet
import com.example.bhandara.ui.shopdetails.ReasonRow
import com.example.bhandara.ui.theme.feastColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * "Report this feast": why (the most common reason tells its organizer what to fix if it's taken down), then an
 * optional note and photos. Report unlocks once a reason is picked. Same look as reporting a shop.
 */
@Composable
internal fun ReportFeastSheet(
    onReport: (FeastReportReason, note: String, photos: List<PickedPhoto>) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    var reason by rememberSaveable { mutableStateOf<FeastReportReason?>(null) }
    var note by rememberSaveable { mutableStateOf("") }
    var photos by remember { mutableStateOf(emptyList<PickedPhoto>()) }
    FeastSheet(onDismiss) { close ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(44.dp).background(colors.saffronSoft, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Flag, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(stringResource(Res.string.feast_report_title), color = colors.ink, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
                Text(stringResource(Res.string.feast_report_prompt), color = colors.muted, fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Column(Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FeastReportReason.entries.forEach { option ->
                ReasonRow(option.icon, stringResource(option.label), selected = option == reason) { reason = option }
            }
        }
        ReportDetailsFields(
            note = note,
            onNoteChange = { note = it },
            photos = photos,
            onAddPhotos = { photos = photos + it },
            onRemovePhoto = { photos = photos - it },
            showPhotos = true,
            modifier = Modifier.padding(top = 16.dp),
        )
        Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FeastButton(stringResource(Res.string.cancel), onClick = { close {} }, style = FeastButtonStyle.SOFT, modifier = Modifier.weight(1f))
            FeastButton(
                stringResource(Res.string.report_submit),
                onClick = { reason?.let { picked -> close { onReport(picked, note, photos) } } },
                enabled = reason != null,
                modifier = Modifier.weight(1.3f),
            )
        }
    }
}

internal val FeastReportReason.label: StringResource
    get() = when (this) {
        FeastReportReason.WRONG_LOCATION -> Res.string.report_reason_wrong_location
        FeastReportReason.WRONG_TIME -> Res.string.feast_report_reason_wrong_time
        FeastReportReason.NOT_HAPPENING -> Res.string.feast_report_reason_not_happening
        FeastReportReason.ASKING_FOR_MONEY -> Res.string.feast_report_reason_money
        FeastReportReason.INAPPROPRIATE -> Res.string.report_reason_inappropriate
    }

private val FeastReportReason.icon: ImageVector
    get() = when (this) {
        FeastReportReason.WRONG_LOCATION -> Icons.Rounded.WrongLocation
        FeastReportReason.WRONG_TIME -> Icons.Rounded.Schedule
        FeastReportReason.NOT_HAPPENING -> Icons.Rounded.EventBusy
        FeastReportReason.ASKING_FOR_MONEY -> Icons.Rounded.Payments
        FeastReportReason.INAPPROPRIATE -> Icons.Rounded.Block
    }

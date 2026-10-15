package com.example.bhandara.ui.shopdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.ReportGmailerrorred
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bhandara.data.models.api.FssaiBadge
import com.example.bhandara.data.models.api.FssaiKind
import com.example.bhandara.data.models.api.FssaiStatus
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.fssai_add_own
import com.example.bhandara.shared.resources.fssai_add_own_text
import com.example.bhandara.shared.resources.fssai_badge
import com.example.bhandara.shared.resources.fssai_check_on_site
import com.example.bhandara.shared.resources.fssai_checked_by
import com.example.bhandara.shared.resources.fssai_food_safety
import com.example.bhandara.shared.resources.fssai_licence_no
import com.example.bhandara.shared.resources.fssai_rejected
import com.example.bhandara.shared.resources.fssai_report
import com.example.bhandara.shared.resources.fssai_row_licence
import com.example.bhandara.shared.resources.fssai_row_registration
import com.example.bhandara.shared.resources.fssai_sheet_text
import com.example.bhandara.shared.resources.fssai_title
import com.example.bhandara.shared.resources.fssai_type
import com.example.bhandara.shared.resources.fssai_type_licence
import com.example.bhandara.shared.resources.fssai_type_registration
import com.example.bhandara.shared.resources.fssai_valid_till
import com.example.bhandara.shared.resources.fssai_waiting
import com.example.bhandara.ui.fssai.fssaiDate
import com.example.bhandara.ui.fssai.fssaiMonth
import com.example.bhandara.ui.theme.feastColors
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

/** The government's FSSAI site, where anyone can look a licence up */
const val FSSAI_LOOKUP_URL = "https://foscos.fssai.gov.in/"

/** "FSSAI licensed ›" under the shop's name (5a); opens the licence (5b) */
@Composable
internal fun FssaiBadgeChip(onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.height(30.dp).clip(RoundedCornerShape(10.dp)).background(colors.successSoft).clickable(onClick = onClick).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = colors.success, modifier = Modifier.size(17.dp))
        Text(stringResource(Res.string.fssai_badge), color = colors.success, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = colors.success, modifier = Modifier.size(16.dp))
    }
}

/** "Food safety · FSSAI licence · valid till Mar 2028", near Facilities (5a); opens the licence (5b) */
@Composable
internal fun FoodSafetyRow(badge: FssaiBadge, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val validTill = runCatching { fssaiMonth(LocalDate.parse(badge.validTill)) }.getOrDefault(badge.validTill)
    SafetyRow(
        icon = Icons.Rounded.VerifiedUser, tile = colors.successSoft, tint = colors.success,
        title = stringResource(Res.string.fssai_food_safety),
        text = stringResource(if (badge.kind == FssaiKind.REGISTRATION) Res.string.fssai_row_registration else Res.string.fssai_row_licence, validTill),
        onClick = onClick,
    )
}

/**
 * The owner's own licence when the shop shows no badge: add one, waiting for the check, or turned down (with why,
 * and sending it again)
 *
 * @param onAdd null while one is waiting for the check
 */
@Composable
internal fun OwnerFssaiRow(own: FssaiStatus?, onAdd: () -> Unit) {
    val colors = MaterialTheme.feastColors
    when (own?.status) {
        FssaiStatus.PENDING_REVIEW -> SafetyRow(
            icon = Icons.Outlined.HourglassTop, tile = colors.saffronSoft, tint = colors.saffronText,
            title = stringResource(Res.string.fssai_title), text = stringResource(Res.string.fssai_waiting), onClick = null,
        )
        FssaiStatus.REJECTED -> SafetyRow(
            icon = Icons.Outlined.ReportGmailerrorred, tile = colors.dangerSoft, tint = colors.danger,
            title = stringResource(Res.string.fssai_title), text = stringResource(Res.string.fssai_rejected, own.decisionReason.orEmpty()), onClick = onAdd,
        )
        else -> SafetyRow(
            icon = Icons.Outlined.VerifiedUser, tile = colors.successSoft, tint = colors.success,
            title = stringResource(Res.string.fssai_add_own), text = stringResource(Res.string.fssai_add_own_text), onClick = onAdd,
        )
    }
}

@Composable
private fun SafetyRow(icon: ImageVector, tile: Color, tint: Color, title: String, text: String, onClick: (() -> Unit)?) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(44.dp).background(tile, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Text(text, color = colors.muted, fontSize = 13.sp, lineHeight = 17.sp, style = MaterialTheme.typography.bodySmall)
        }
        if (onClick != null) Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = colors.muted, modifier = Modifier.size(20.dp))
    }
}

/**
 * The licence (5b): what was checked and when, the number partly hidden, a link to look it up on the FSSAI site,
 * and reporting a problem with it. The certificate is never shown.
 *
 * @param onReport null when the user can't report the shop (their own, or already reported)
 */
@Composable
internal fun FssaiBadgeSheet(badge: FssaiBadge, onCheckOnSite: () -> Unit, onReport: (() -> Unit)?, onDismiss: () -> Unit) {
    val colors = MaterialTheme.feastColors
    FeastSheet(onDismiss) { close ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(52.dp).background(colors.successSoft, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = colors.success, modifier = Modifier.size(28.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(stringResource(Res.string.fssai_badge), color = colors.ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text(stringResource(Res.string.fssai_sheet_text), color = colors.muted, fontSize = 13.sp, lineHeight = 18.sp, style = MaterialTheme.typography.bodySmall)
            }
        }
        Column(Modifier.padding(top = 14.dp)) {
            val rows = listOf(
                stringResource(Res.string.fssai_licence_no) to badge.maskedNumber,
                stringResource(Res.string.fssai_type) to stringResource(if (badge.kind == FssaiKind.REGISTRATION) Res.string.fssai_type_registration else Res.string.fssai_type_licence),
                stringResource(Res.string.fssai_valid_till) to fssaiDate(badge.validTill),
                stringResource(Res.string.fssai_checked_by) to fssaiDate(badge.checkedOn),
            )
            rows.forEachIndexed { index, (label, value) ->
                Row(Modifier.fillMaxWidth().heightIn(min = 46.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(label, color = colors.muted, fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(value, color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                }
                if (index < rows.lastIndex) HorizontalDivider(color = colors.lavender, thickness = 1.dp)
            }
        }
        Row(
            Modifier.padding(top = 14.dp).fillMaxWidth().height(52.dp).clip(RoundedCornerShape(16.dp)).background(colors.lavender).clickable(onClick = onCheckOnSite),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        ) {
            Icon(Icons.Outlined.OpenInNew, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
            Text(stringResource(Res.string.fssai_check_on_site), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
        }
        if (onReport != null) {
            Text(
                stringResource(Res.string.fssai_report), color = colors.saffronText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 14.dp).fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { close(onReport) }.padding(vertical = 4.dp),
            )
        }
    }
}

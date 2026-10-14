package com.example.bhandara.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.bhandara.data.location.formatDistance
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.action_call
import com.example.bhandara.shared.resources.action_directions
import com.example.bhandara.shared.resources.shop_closed
import com.example.bhandara.shared.resources.shop_inactive
import com.example.bhandara.shared.resources.shop_open_now
import com.example.bhandara.shared.resources.shop_verified
import com.example.bhandara.ui.components.crowdLabel
import com.example.bhandara.ui.shopdetails.DirectionsButton
import com.example.bhandara.ui.shopdetails.formatRating
import com.example.bhandara.ui.theme.feastColors
import org.jetbrains.compose.resources.stringResource

/**
 * The tapped shop over the map (the redesign's 1a): its photo, name, verified and rating, open and how busy, with
 * a big Directions button and a call button. Tapping the card opens the full page.
 */
@Composable
internal fun ShopPreviewCard(
    place: LocalShopResponse,
    onOpen: () -> Unit,
    onDirections: () -> Unit,
    onCall: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(28.dp)
    val active = place.isActive != false
    Column(
        modifier
            .fillMaxWidth()
            .shadow(20.dp, shape, ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.35f))
            .background(colors.surface, shape)
            .clip(shape)
            .clickable(onClick = onOpen),
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PreviewPhoto(place)
            Column(Modifier.weight(1f).padding(top = 2.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    place.shopName,
                    color = colors.ink, fontSize = 17.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium,
                )
                val rating = place.averageRating?.takeIf { (place.totalReviews ?: place.totalRatings ?: 0) > 0 }
                if (place.isVerified == true || rating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val meta = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.muted)
                        if (place.isVerified == true) {
                            Icon(Icons.Rounded.Verified, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
                            Text(stringResource(Res.string.shop_verified), style = meta, maxLines = 1)
                        }
                        if (place.isVerified == true && rating != null) Text("·", style = meta)
                        if (rating != null) {
                            Icon(Icons.Rounded.Star, contentDescription = null, tint = colors.saffron, modifier = Modifier.size(15.dp))
                            Text(formatRating(rating), style = meta)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    when {
                        !active -> StatusChip(stringResource(Res.string.shop_inactive), colors.dangerSoft, colors.danger)
                        place.isCurrentlyOpen == true -> StatusChip(stringResource(Res.string.shop_open_now), colors.successSoft, colors.success)
                        place.isCurrentlyOpen == false -> StatusChip(stringResource(Res.string.shop_closed), colors.dangerSoft, colors.danger)
                    }
                    crowdLabel(place.crowdLevel)?.takeIf { active && place.isCurrentlyOpen != false }?.let { crowd ->
                        Row(
                            Modifier.height(24.dp).background(colors.lavender, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            val dot = when (place.crowdLevel) {
                                "BUSY" -> colors.danger
                                "MODERATE" -> colors.saffron
                                else -> colors.success
                            }
                            Box(Modifier.size(6.dp).background(dot, CircleShape))
                            Text(stringResource(crowd), color = colors.ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
        Row(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val label = listOfNotNull(stringResource(Res.string.action_directions), place.distance?.let(::formatDistance)).joinToString(" · ")
            DirectionsButton(label, enabled = active, onClick = onDirections, modifier = Modifier.weight(1f), height = 52.dp)
            if (onCall != null) {
                Box(
                    Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(colors.lavender).clickable(onClick = onCall),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Call, stringResource(Res.string.action_call), tint = colors.accent, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

/** The first photo with the number of photos on it; a plain tile with an icon when there's none */
@Composable
private fun PreviewPhoto(place: LocalShopResponse) {
    val colors = MaterialTheme.feastColors
    val photos = place.imageUrls.orEmpty()
    Box(Modifier.size(96.dp).clip(RoundedCornerShape(18.dp)).background(colors.lavender), contentAlignment = Alignment.Center) {
        if (photos.isEmpty()) {
            Icon(
                if (place.isMovingCart == true) Icons.Rounded.LocalShipping else Icons.Rounded.Storefront,
                contentDescription = null, tint = colors.accent, modifier = Modifier.size(36.dp),
            )
        } else {
            AsyncImage(photos.first(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            if (photos.size > 1) {
                Row(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .height(20.dp)
                        .background(Color(0xBF1E1B4B), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Text("${photos.size}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, background: Color, content: Color) {
    Box(Modifier.height(24.dp).background(background, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
        Text(label, color = content, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, style = MaterialTheme.typography.labelMedium)
    }
}

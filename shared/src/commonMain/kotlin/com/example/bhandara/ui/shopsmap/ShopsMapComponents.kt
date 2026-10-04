package com.example.bhandara.ui.shopsmap

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bhandara.data.location.formatDistance
import com.example.bhandara.data.models.api.LocalShopResponse
import com.example.bhandara.ui.components.crowdLabel
import com.example.bhandara.shared.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** The floating search bar at the top of the map: back, the app's logo, the search field, clear */
@Composable
internal fun MapSearchBar(query: String, onQueryChange: (String) -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Row(Modifier.fillMaxSize().padding(start = 4.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back), tint = MaterialTheme.colorScheme.onSurface)
            }
            Image(
                painter = painterResource(Res.drawable.localfeast),
                contentDescription = null,
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                decorationBox = { field ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                stringResource(Res.string.search_local_feast),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        field()
                    }
                },
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, stringResource(Res.string.clear_search), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** The 🚚 button that switches the map between fixed shops and live moving carts */
@Composable
internal fun MovingVendorsToggle(isSelected: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            onClick = onToggle,
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = if (isSelected) BorderStroke(2.dp, Color(0xFF1A73E8)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("🚚", style = MaterialTheme.typography.titleLarge)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(Res.string.moving_vendors_label),
            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
        )
    }
}

/** The card for a tapped pin: what it is, whether it's open, how busy and how far; tapping it opens the place */
@Composable
internal fun PlacePreviewCard(place: LocalShopResponse, onOpen: () -> Unit, onClose: () -> Unit, modifier: Modifier = Modifier) {
    val isOpen = place.isCurrentlyOpen ?: true
    val statusColor = if (isOpen) Color(0xFF4CAF50) else Color(0xFFE57373)
    val details = listOfNotNull(
        crowdLabel(place.crowdLevel)?.takeIf { isOpen }?.let { stringResource(it) },
        place.distance?.let { formatDistance(it) },
    ).joinToString(" • ")

    Card(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Row(Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(Color(0xFFFFE3E8), CircleShape), contentAlignment = Alignment.Center) {
                Text(if (place.isMovingCart == true) "🚚" else "📍", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    place.shopName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF212121)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    place.cuisineType ?: stringResource(Res.string.shop_default_subtitle),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF757575)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).background(statusColor, CircleShape))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(if (isOpen) Res.string.shop_open else Res.string.shop_closed),
                        style = MaterialTheme.typography.labelSmall.copy(color = statusColor, fontWeight = FontWeight.SemiBold),
                    )
                    if (details.isNotEmpty()) {
                        Text(
                            " • $details",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF757575)),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, stringResource(Res.string.close), tint = Color(0xFF757575))
            }
        }
    }
}

/** "Is this place real?": asks a nearby user about an unverified shop or live cart */
@Composable
internal fun VerificationCard(place: LocalShopResponse, onAnswer: (String) -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.map_verify_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, stringResource(Res.string.close), modifier = Modifier.size(18.dp))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (place.isMovingCart == true) "🚚" else "📍", style = MaterialTheme.typography.headlineMedium)
                Column {
                    Text(place.shopName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text(
                        stringResource(if (place.isMovingCart == true) Res.string.map_verify_cart_question else Res.string.map_verify_shop_question),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("YES" to Res.string.vote_yes, "NO" to Res.string.vote_no, "NOT_SURE" to Res.string.vote_not_sure).forEach { (vote, label) ->
                    OutlinedButton(onClick = { onAnswer(vote) }, modifier = Modifier.weight(1f)) {
                        Text(stringResource(label), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

/** A short explanation over the map with one action, e.g. "Allow location" or "Try again" */
@Composable
internal fun MapNotice(text: String, action: String?, onAction: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(horizontal = 32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            if (action != null) Button(onClick = onAction) { Text(action) }
        }
    }
}

/** A small dark pill over the map, e.g. "Showing top-rated shops. Zoom in for more" */
@Composable
internal fun MapHint(text: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface)) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.inverseOnSurface,
        )
    }
}

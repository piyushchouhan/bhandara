package com.example.bhandara.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.BitmapDescriptorFactory

// Shared map pin and preview card, used by the shops map and the community feast map so both look the same.

/** A map pin image plus the point in it (0..1) that sits on the place's location */
class LabeledPin(
    val icon: com.google.android.gms.maps.model.BitmapDescriptor,
    val anchor: androidx.compose.ui.geometry.Offset
)

/**
 * Draws a pin as the emoji with the place's name in a small rounded label underneath, so every shop on the
 * map can be identified at a glance without tapping. Long names are shortened with "...".
 * Light map: a charcoal label with white text, which stands out clearly against the light map.
 *
 * Dark map: a plain white label with dark text (green with white text for carts), which stands out
 * strongly against the dark background on its own.
 *
 * Live carts are green in both themes so they stand out from fixed shops.
 */
fun labeledPin(emoji: String, name: String, isLiveCart: Boolean, darkMap: Boolean): LabeledPin {
    // Label colours per theme
    val fillColor: String
    val textColor: String
    val borderColor: String?
    if (darkMap) {
        fillColor = if (isLiveCart) "#2E7D32" else "#FFFFFF"
        textColor = if (isLiveCart) "#FFFFFF" else "#212121"
        borderColor = null
    } else {
        fillColor = if (isLiveCart) "#2E7D32" else "#424242"
        textColor = "#FFFFFF"
        borderColor = if (isLiveCart) "#2E7D32" else "#424242"
    }

    val density = android.content.res.Resources.getSystem().displayMetrics.density
    fun dp(value: Float) = value * density

    val emojiSize = dp(28f)
    val gap = dp(1f)
    val padH = dp(6f)
    val padV = dp(2f)
    val maxLabelWidth = dp(120f)
    val shadow = dp(2f)

    val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = emojiSize * 0.85f
        textAlign = Paint.Align.CENTER
    }
    val textPaint = android.text.TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dp(11f)
        typeface = Typeface.DEFAULT_BOLD
        color = android.graphics.Color.parseColor(textColor)
    }
    val label = android.text.TextUtils.ellipsize(name, textPaint, maxLabelWidth, android.text.TextUtils.TruncateAt.END)
        .toString()
    val textWidth = textPaint.measureText(label)
    val fontMetrics = textPaint.fontMetrics
    val textHeight = fontMetrics.descent - fontMetrics.ascent

    val labelWidth = textWidth + padH * 2
    val labelHeight = textHeight + padV * 2
    val width = maxOf(emojiSize, labelWidth) + shadow * 2
    val height = emojiSize + gap + labelHeight + shadow * 2

    val bitmap = Bitmap.createBitmap(width.toInt() + 1, height.toInt() + 1, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val centerX = width / 2f

    // Emoji
    val emojiCenterY = shadow + emojiSize / 2f
    canvas.drawText(emoji, centerX, emojiCenterY - (emojiPaint.ascent() + emojiPaint.descent()) / 2f, emojiPaint)

    // Name label: rounded pill with a soft shadow so it stays readable over streets and other labels
    val top = shadow + emojiSize + gap
    val rect = android.graphics.RectF(centerX - labelWidth / 2f, top, centerX + labelWidth / 2f, top + labelHeight)
    val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor(fillColor)
        setShadowLayer(shadow, 0f, dp(0.5f), android.graphics.Color.argb(if (darkMap) 90 else 60, 0, 0, 0))
    }
    canvas.drawRoundRect(rect, labelHeight / 2f, labelHeight / 2f, pillPaint)
    if (borderColor != null) {
        val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = dp(1.25f)
            color = android.graphics.Color.parseColor(borderColor)
        }
        canvas.drawRoundRect(rect, labelHeight / 2f, labelHeight / 2f, outlinePaint)
    }
    canvas.drawText(label, centerX - textWidth / 2f, top + padV - fontMetrics.ascent, textPaint)

    return LabeledPin(
        icon = BitmapDescriptorFactory.fromBitmap(bitmap),
        anchor = androidx.compose.ui.geometry.Offset(0.5f, emojiCenterY / bitmap.height)
    )
}

/**
 * The small card shown when a pin is tapped (rendered as the marker's info window; tapping it opens details):
 * emoji badge, title, subtitle, and a status line such as "Open • 0.4 km".
 */
@Composable
fun MapPreviewCard(
    emoji: String,
    title: String,
    subtitle: String,
    statusText: String,
    statusColor: Color,
    trailingText: String
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular light-pink background with food emoji
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = Color(0xFFFFE3E8), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF757575)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Tiny coloured status dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(color = statusColor, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF757575))
                    )
                    Text(
                        text = trailingText,
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF757575)),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/** Distance in meters as shown on map cards: "nearby" under 100 m, otherwise "1.2 km" */
fun formatMapDistance(distanceMeters: Double?): String {
    val distanceKm = (distanceMeters ?: 0.0) / 1000.0
    return if (distanceKm < 0.1) "nearby" else String.format("%.1f km", distanceKm)
}

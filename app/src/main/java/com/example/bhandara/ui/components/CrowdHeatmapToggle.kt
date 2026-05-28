package com.example.bhandara.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.bhandara.R
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

/**
 * A reusable, low-coupled map layer toggle component representing the Crowd Heatmap.
 * Features a diagonal linear gradient heatmap preview background and a thick blue border selection state
 * modeled after Google Maps layer selectors.
 */
@Composable
fun CrowdHeatmapToggle(
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heatmapBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFE53935), // 1. Red
                Color(0xFFFB8C00), // 2. Orange
                Color(0xFFFDD835), // 3. Yellow
                Color(0xFF4CAF50), // 4. Green
                Color(0xFF00ACC1), // 5. Cyan
                Color(0xFF1E88E5), // 6. Blue
                Color(0xFF3F51B5)  // 7. Indigo
            )
        )
    }

    val borderStroke = if (isSelected) {
        BorderStroke(2.dp, Color(0xFF1A73E8)) // Thick active blue border (Google Maps style)
    } else {
        BorderStroke(1.dp, Color(0xFFE0E0E0)) // Thin inactive grey border
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = borderStroke,
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            onClick = onToggle
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(heatmapBrush)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(id = R.string.crowd_heatmap_label),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            ),
            textAlign = TextAlign.Center
        )
    }
}

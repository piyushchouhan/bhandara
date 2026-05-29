package com.example.bhandara.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bhandara.R

@Composable
fun HowToUseDialog(
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    text = stringResource(R.string.how_to_use_close),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.how_to_use_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.how_to_use_step1),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = stringResource(R.string.how_to_use_step2),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = stringResource(R.string.how_to_use_step3),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

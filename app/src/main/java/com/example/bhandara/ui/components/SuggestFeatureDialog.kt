package com.example.bhandara.ui.components

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bhandara.R

@Composable
fun SuggestFeatureDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var details by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            if (submitted) {
                TextButton(onClick = onDismissRequest) {
                    Text(
                        text = stringResource(R.string.suggest_feature_close),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Button(
                    onClick = {
                        if (details.isNotBlank()) {
                            submitted = true
                            Toast.makeText(context, context.getString(R.string.suggest_feature_success), Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = details.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = stringResource(R.string.suggest_feature_submit))
                }
            }
        },
        dismissButton = {
            if (!submitted) {
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(R.string.no))
                }
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
                    imageVector = if (submitted) Icons.Default.CheckCircle else Icons.Default.Lightbulb,
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
                    text = stringResource(R.string.suggest_feature_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (submitted) {
                    Text(
                        text = stringResource(R.string.suggest_feature_success),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = stringResource(R.string.suggest_feature_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        placeholder = { Text(text = stringResource(R.string.suggest_feature_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

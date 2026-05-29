package com.example.bhandara.ui.components

import android.content.Context
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bhandara.R

@Composable
fun ShareAppDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val link = stringResource(R.string.share_app_link)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = {
                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Local Feast", link)
                    clipboardManager.setPrimaryClip(clip)
                    Toast.makeText(context, context.getString(R.string.share_app_copied), Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = stringResource(R.string.share_app_copy))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.share_app_close))
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
                    imageVector = Icons.Default.Share,
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
                    text = stringResource(R.string.share_app_title),
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
                Text(
                    text = stringResource(R.string.share_app_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = link,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

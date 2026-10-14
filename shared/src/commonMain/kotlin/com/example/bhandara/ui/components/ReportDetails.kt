package com.example.bhandara.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.bhandara.photos.PickedPhoto
import com.example.bhandara.photos.rememberPhotoPicker
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.report_details_add_photo
import com.example.bhandara.shared.resources.report_details_note_hint
import com.example.bhandara.shared.resources.report_details_title
import com.example.bhandara.shared.resources.review_remove_photo
import com.example.bhandara.ui.theme.feastColors
import org.jetbrains.compose.resources.stringResource

/** Longest note the backend keeps with a report */
const val MAX_REPORT_NOTE = 500

/** Most photos one report note can have (the backend's limit) */
const val MAX_REPORT_PHOTOS = 3

/**
 * "Tell us more (optional)": a note and up to [MAX_REPORT_PHOTOS] photos, as the report sheets and "Add more info"
 * ask for them. Photos are chosen from the gallery (the camera opens from there on most phones); they're uploaded
 * when the report is sent.
 *
 * @param showPhotos false where this phone can't upload photos
 */
@Composable
fun ReportDetailsFields(
    note: String,
    onNoteChange: (String) -> Unit,
    photos: List<PickedPhoto>,
    onAddPhotos: (List<PickedPhoto>) -> Unit,
    onRemovePhoto: (PickedPhoto) -> Unit,
    showPhotos: Boolean,
    modifier: Modifier = Modifier,
    title: String? = stringResource(Res.string.report_details_title),
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.feastColors
    val picker = rememberPhotoPicker { picked -> onAddPhotos(picked.take(MAX_REPORT_PHOTOS - photos.size)) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (title != null) {
            Text(title, color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
        }
        val interaction = remember { MutableInteractionSource() }
        val focused by interaction.collectIsFocusedAsState()
        val shape = RoundedCornerShape(16.dp)
        val style = TextStyle(color = colors.ink, fontSize = 15.sp, lineHeight = 21.sp)
        BasicTextField(
            value = note,
            onValueChange = { onNoteChange(it.take(MAX_REPORT_NOTE)) },
            enabled = enabled,
            textStyle = style,
            cursorBrush = SolidColor(colors.accent),
            interactionSource = interaction,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 84.dp)
                .background(colors.surface, shape)
                .border(if (focused) 2.dp else 1.5.dp, if (focused) colors.accent else colors.border, shape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            decorationBox = { field ->
                Box {
                    if (note.isEmpty()) Text(stringResource(Res.string.report_details_note_hint), style = style.copy(color = colors.placeholder))
                    field()
                }
            },
        )
        if (showPhotos) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                photos.forEach { photo -> ReportPhotoTile(photo.preview, onRemove = if (enabled) ({ onRemovePhoto(photo) }) else null) }
                if (photos.size < MAX_REPORT_PHOTOS) {
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.lavender)
                            .clickable(enabled = enabled) { picker.chooseFromGallery(MAX_REPORT_PHOTOS - photos.size) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
                        Text(
                            stringResource(Res.string.report_details_add_photo), color = colors.accent, fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

/** A square photo; with [onRemove], a small × to take it out */
@Composable
fun ReportPhotoTile(model: Any, onRemove: (() -> Unit)? = null, onClick: (() -> Unit)? = null) {
    val colors = MaterialTheme.feastColors
    Box(
        Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.lavender)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        AsyncImage(model, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        if (onRemove != null) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (colors.isDark) Color(0xBF07060E) else Color(0xBF1E1B4B))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Close, stringResource(Res.string.review_remove_photo), tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

/** Shown when a report's photos couldn't be uploaded; the report isn't sent */
const val PHOTOS_FAILED = "Couldn't upload the photos. Please try again."

/**
 * Uploads the photos sent with a report: their URLs (empty for none), or null if any couldn't be uploaded or this
 * phone can't upload, so the report isn't sent with photos silently missing.
 */
suspend fun uploadReportPhotos(upload: (suspend (List<PickedPhoto>) -> List<String>)?, photos: List<PickedPhoto>): List<String>? {
    if (photos.isEmpty()) return emptyList()
    val uploader = upload ?: return null
    return try {
        uploader(photos).takeIf { it.size == photos.size }
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
}

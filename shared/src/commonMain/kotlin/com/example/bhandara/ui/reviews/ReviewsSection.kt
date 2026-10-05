package com.example.bhandara.ui.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.data.models.api.ReviewResponse
import com.example.bhandara.data.network.ReviewApi
import com.example.bhandara.photos.PhotoSourceSheet
import com.example.bhandara.photos.PickedPhoto
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.cancel
import com.example.bhandara.shared.resources.review_add_photo
import com.example.bhandara.shared.resources.review_comment_hint
import com.example.bhandara.shared.resources.review_delete
import com.example.bhandara.shared.resources.review_delete_confirm
import com.example.bhandara.shared.resources.review_delete_text
import com.example.bhandara.shared.resources.review_delete_title
import com.example.bhandara.shared.resources.review_edit
import com.example.bhandara.shared.resources.review_edit_yours
import com.example.bhandara.shared.resources.review_photo
import com.example.bhandara.shared.resources.review_photos_label
import com.example.bhandara.shared.resources.review_post
import com.example.bhandara.shared.resources.review_remove_photo
import com.example.bhandara.shared.resources.review_report
import com.example.bhandara.shared.resources.review_save
import com.example.bhandara.shared.resources.review_stars
import com.example.bhandara.shared.resources.review_tap_stars
import com.example.bhandara.shared.resources.review_uploading
import com.example.bhandara.shared.resources.review_write
import com.example.bhandara.shared.resources.review_yours
import com.example.bhandara.shared.resources.reported
import com.example.bhandara.shared.resources.reviews_count
import com.example.bhandara.shared.resources.reviews_empty
import com.example.bhandara.shared.resources.try_again
import com.example.bhandara.ui.shopdetails.FullScreenPhotos
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private val StarColor = Color(0xFFFFC107)

/**
 * A shop's reviews: the list, writing or editing your own (one per shop), deleting it, and reporting others.
 *
 * @param showMessage shows a short message, e.g. in the page's snackbar
 */
@Composable
fun ReviewsSection(
    shopId: String,
    showMessage: suspend (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dependencies = LocalAppDependencies.current
    val viewModel = viewModel(key = "reviews-$shopId") {
        ReviewsViewModel(
            shopId,
            ReviewApi(dependencies.apiClient),
            dependencies.settings,
            { dependencies.auth.currentUid },
            dependencies.photoUploader::upload,
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var deleting by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            viewModel.onMessageShown()
            showMessage(it)
        }
    }

    Column(modifier.fillMaxWidth().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(Res.string.reviews_count, state.reviews.size), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                state.averageRating?.let { average ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = StarColor, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(oneDecimal(average), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
            if (state.canWrite) {
                Button(onClick = viewModel::startWriting) {
                    Text(stringResource(if (state.myReview != null) Res.string.review_edit_yours else Res.string.review_write))
                }
            }
        }

        val error = state.error
        when {
            state.loading && state.reviews.isEmpty() -> Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                CircularProgressIndicator()
            }

            error != null -> Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(error, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = viewModel::retry) { Text(stringResource(Res.string.try_again)) }
            }

            state.reviews.isEmpty() -> Text(
                stringResource(Res.string.reviews_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(32.dp),
            )

            // Your own review first, then everyone else's (newest first, as the backend sends them)
            else -> state.reviews.sortedByDescending { state.isMine(it) }.forEach { review ->
                ReviewCard(
                    review = review,
                    isMine = state.isMine(review),
                    canReport = state.canReport(review),
                    reported = review.id in state.reportedIds,
                    onEdit = viewModel::startWriting,
                    onDelete = { deleting = review.id },
                    onReport = { viewModel.report(review.id) },
                )
            }
        }
    }

    state.draft?.let { draft ->
        ReviewEditor(
            draft = draft,
            submitting = state.submitting,
            uploadProgress = state.uploadProgress,
            onRating = viewModel::setRating,
            onComment = viewModel::setComment,
            onRemoveKept = viewModel::removeKeptPhoto,
            onAddPhotos = viewModel::addPhotos,
            onRemoveNew = viewModel::removeNewPhoto,
            onSubmit = viewModel::submit,
            onCancel = viewModel::cancelDraft,
        )
    }

    deleting?.let { reviewId ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(Res.string.review_delete_title)) },
            text = { Text(stringResource(Res.string.review_delete_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleting = null
                        viewModel.delete(reviewId)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(Res.string.review_delete_confirm)) }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(Res.string.cancel)) } },
        )
    }
}

/** "4.3" */
private fun oneDecimal(value: Double): String {
    val tenths = (value * 10).roundToInt()
    return "${tenths / 10}.${tenths % 10}"
}

@Composable
private fun Stars(rating: Int, size: Int) {
    Row {
        for (i in 1..5) {
            Icon(
                if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (i <= rating) StarColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(size.dp),
            )
        }
    }
}

@Composable
private fun ReviewCard(
    review: ReviewResponse,
    isMine: Boolean,
    canReport: Boolean,
    reported: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit,
) {
    var openPhoto by remember { mutableStateOf<Int?>(null) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isMine) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Stars(review.rating, size = 16)
                Spacer(Modifier.width(8.dp))
                val date = formatReviewDate(review.createdAt)
                val label = listOfNotNull(if (isMine) stringResource(Res.string.review_yours) else null, date).joinToString(" • ")
                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                when {
                    isMine -> {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, stringResource(Res.string.review_edit), Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, stringResource(Res.string.review_delete), Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    reported -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Flag, contentDescription = null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(Res.string.reported), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                    canReport -> IconButton(onClick = onReport, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Flag, stringResource(Res.string.review_report), Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            review.comment?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            val urls = review.imageUrls.orEmpty()
            if (urls.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(urls) { index, url ->
                        AsyncImage(
                            model = url,
                            contentDescription = stringResource(Res.string.review_photo, index + 1),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).clickable { openPhoto = index },
                        )
                    }
                }
            }
        }
    }
    openPhoto?.let { index ->
        FullScreenPhotos(review.imageUrls.orEmpty(), initialIndex = index, onDismiss = { openPhoto = null }, photoLabel = Res.string.review_photo)
    }
}

/** Writing a new review or editing your own: stars, comment and photos */
@Composable
private fun ReviewEditor(
    draft: ReviewDraft,
    submitting: Boolean,
    uploadProgress: Int?,
    onRating: (Int) -> Unit,
    onComment: (String) -> Unit,
    onRemoveKept: (String) -> Unit,
    onAddPhotos: (List<PickedPhoto>) -> Unit,
    onRemoveNew: (PickedPhoto) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
) {
    var picking by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(if (draft.reviewId != null) Res.string.review_edit_yours else Res.string.review_write)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        for (i in 1..5) {
                            Icon(
                                if (i <= draft.rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = stringResource(Res.string.review_stars, i),
                                tint = if (i <= draft.rating) StarColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(44.dp)
                                    .selectable(selected = i == draft.rating, enabled = !submitting, role = Role.RadioButton) { onRating(i) }
                                    .padding(2.dp),
                            )
                        }
                    }
                    if (draft.rating == 0) {
                        Text(stringResource(Res.string.review_tap_stars), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                OutlinedTextField(
                    value = draft.comment,
                    onValueChange = onComment,
                    enabled = !submitting,
                    label = { Text(stringResource(Res.string.review_comment_hint)) },
                    supportingText = { Text("${draft.comment.length} / $MAX_REVIEW_COMMENT") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                )
                Text(stringResource(Res.string.review_photos_label, MAX_REVIEW_PHOTOS), style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(draft.keptImageUrls) { url -> PhotoThumb(url, enabled = !submitting) { onRemoveKept(url) } }
                    items(draft.newPhotos) { photo -> PhotoThumb(photo.preview, enabled = !submitting) { onRemoveNew(photo) } }
                    if (draft.canAddPhotos) {
                        item {
                            OutlinedButton(
                                onClick = { picking = true },
                                enabled = !submitting,
                                modifier = Modifier.size(64.dp),
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = stringResource(Res.string.review_add_photo))
                            }
                        }
                    }
                }
                uploadProgress?.let {
                    Text(stringResource(Res.string.review_uploading, "$it%"), style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = onSubmit, enabled = draft.canSubmit && !submitting) {
                if (submitting) {
                    CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(if (draft.reviewId != null) Res.string.review_save else Res.string.review_post))
                }
            }
        },
        dismissButton = { TextButton(onClick = onCancel, enabled = !submitting) { Text(stringResource(Res.string.cancel)) } },
    )
    if (picking) {
        PhotoSourceSheet(
            maxCount = MAX_REVIEW_PHOTOS - draft.photoCount,
            onPicked = onAddPhotos,
            onDismiss = { picking = false },
        )
    }
}

/** A small photo with a remove button */
@Composable
private fun PhotoThumb(model: Any, enabled: Boolean, onRemove: () -> Unit) {
    Box {
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
        )
        if (enabled) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).size(22.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape),
            ) {
                Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.review_remove_photo), tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

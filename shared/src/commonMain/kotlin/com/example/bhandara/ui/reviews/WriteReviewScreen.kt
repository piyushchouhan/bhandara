package com.example.bhandara.ui.reviews

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.data.network.ReviewApi
import com.example.bhandara.photos.PickedPhoto
import com.example.bhandara.photos.rememberPhotoPicker
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.close
import com.example.bhandara.shared.resources.photo_choose_from_gallery
import com.example.bhandara.shared.resources.photo_sheet_title
import com.example.bhandara.shared.resources.photo_take
import com.example.bhandara.shared.resources.review_add_photo
import com.example.bhandara.shared.resources.review_comment_hint
import com.example.bhandara.shared.resources.review_edit_yours
import com.example.bhandara.shared.resources.review_photos
import com.example.bhandara.shared.resources.review_photos_count
import com.example.bhandara.shared.resources.review_photos_optional
import com.example.bhandara.shared.resources.review_post_review
import com.example.bhandara.shared.resources.review_public
import com.example.bhandara.shared.resources.review_rating_1
import com.example.bhandara.shared.resources.review_rating_2
import com.example.bhandara.shared.resources.review_rating_3
import com.example.bhandara.shared.resources.review_rating_4
import com.example.bhandara.shared.resources.review_rating_5
import com.example.bhandara.shared.resources.review_remove_photo
import com.example.bhandara.shared.resources.review_save_changes
import com.example.bhandara.shared.resources.review_stars
import com.example.bhandara.shared.resources.review_tap_stars
import com.example.bhandara.shared.resources.review_uploading
import com.example.bhandara.shared.resources.review_write
import com.example.bhandara.shared.resources.try_again
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Writing a review, or editing your own, full screen (shared by Android and iOS): the shop it's for, five big
 * stars with a word for the rating, a comment, up to 5 photos, and "Post review", which unlocks once a star is
 * picked. Closes by itself once the review is posted.
 *
 * @param rating stars already picked for a new review (e.g. tapped on "No reviews yet"); 0 for none
 */
@Composable
fun WriteReviewScreen(shopId: String, shopName: String, photoUrl: String?, rating: Int, onClose: () -> Unit) {
    val dependencies = LocalAppDependencies.current
    val viewModel = viewModel {
        ReviewsViewModel(
            shopId,
            ReviewApi(dependencies.apiClient),
            dependencies.settings,
            { dependencies.auth.currentUid },
            dependencies.photoUploader::upload,
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    // The draft starts once the shop's reviews are in, so editing finds the user's own review
    var started by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.canWrite) {
        if (state.canWrite && !started) {
            started = true
            viewModel.startWriting(rating)
        }
    }
    LaunchedEffect(state.submitted) { if (state.submitted) onClose() }
    LaunchedEffect(state.message) {
        val message = state.message
        if (message != null && !state.submitted) {
            viewModel.onMessageShown()
            snackbar.showSnackbar(message)
        }
    }

    FeastTheme {
        val colors = MaterialTheme.feastColors
        val draft = state.draft
        var pickingPhotos by remember { mutableStateOf(false) }

        Box(Modifier.fillMaxSize().background(colors.page)) {
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).imePadding()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SquareIconButton(Icons.Rounded.Close, stringResource(Res.string.close), onClose, enabled = !state.submitting)
                    Text(
                        stringResource(if (draft?.reviewId != null) Res.string.review_edit_yours else Res.string.review_write),
                        color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge,
                    )
                }

                val error = state.error
                when {
                    draft != null -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        ShopCard(shopName, photoUrl)
                        StarPicker(draft.rating, enabled = !state.submitting, onRating = viewModel::setRating)
                        CommentBox(draft.comment, enabled = !state.submitting, onChange = viewModel::setComment)
                        if (state.photosSupported) {
                            PhotosRow(
                                draft = draft,
                                enabled = !state.submitting,
                                onAdd = { pickingPhotos = true },
                                onRemoveKept = viewModel::removeKeptPhoto,
                                onRemoveNew = viewModel::removeNewPhoto,
                            )
                        }
                    }

                    error != null -> Column(
                        Modifier.weight(1f).fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                    ) {
                        Text(error, color = colors.muted, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                        TextButton(onClick = viewModel::retry) { Text(stringResource(Res.string.try_again)) }
                    }

                    else -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.indigo)
                    }
                }

                if (draft != null) {
                    Box(Modifier.windowInsetsPadding(WindowInsets.navigationBars).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)) {
                        PostButton(
                            isEdit = draft.reviewId != null,
                            enabled = draft.canSubmit,
                            submitting = state.submitting,
                            uploadProgress = state.uploadProgress,
                            onClick = viewModel::submit,
                        )
                    }
                }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = 84.dp))
        }

        if (pickingPhotos && draft != null) {
            AddPhotosSheet(
                maxCount = MAX_REVIEW_PHOTOS - draft.photoCount,
                onPicked = viewModel::addPhotos,
                onDismiss = { pickingPhotos = false },
            )
        }
    }
}

/** The shop the review is for, and a reminder that reviews are public */
@Composable
private fun ShopCard(shopName: String, photoUrl: String?) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp).fillMaxWidth().background(colors.surface, RoundedCornerShape(20.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(colors.lavender), contentAlignment = Alignment.Center) {
            if (photoUrl != null) {
                AsyncImage(photoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.Rounded.Storefront, contentDescription = null, tint = colors.accent, modifier = Modifier.size(24.dp))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(shopName, color = colors.ink, fontSize = 16.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(Res.string.review_public), color = colors.muted, fontSize = 13.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private val RATING_WORDS: List<StringResource> = listOf(
    Res.string.review_rating_1, Res.string.review_rating_2, Res.string.review_rating_3, Res.string.review_rating_4, Res.string.review_rating_5,
)

/** Five big stars; under them the rating's word ("Good"), or how to rate before one is picked */
@Composable
private fun StarPicker(rating: Int, enabled: Boolean, onRating: (Int) -> Unit) {
    val colors = MaterialTheme.feastColors
    Column(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 1..5) {
                Icon(
                    Icons.Rounded.Star,
                    contentDescription = stringResource(Res.string.review_stars, i),
                    tint = if (i <= rating) colors.saffron else colors.starOff,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .selectable(selected = i == rating, enabled = enabled, role = Role.RadioButton) { onRating(i) },
                )
            }
        }
        if (rating in 1..5) {
            Text(stringResource(RATING_WORDS[rating - 1]), color = colors.saffronText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
        } else {
            Text(stringResource(Res.string.review_tap_stars), color = colors.muted, fontSize = 14.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** "Comment (optional)", outlined in the accent while typing, with a count up to the limit */
@Composable
private fun CommentBox(comment: String, enabled: Boolean, onChange: (String) -> Unit) {
    val colors = MaterialTheme.feastColors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 22.sp, color = colors.ink)
    val shape = RoundedCornerShape(16.dp)
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BasicTextField(
            value = comment,
            onValueChange = onChange,
            enabled = enabled,
            textStyle = style,
            cursorBrush = SolidColor(colors.ink),
            interactionSource = interaction,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(colors.surface, shape)
                .border(if (focused) 2.dp else 1.5.dp, if (focused) colors.accent else colors.border, shape)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            decorationBox = { field ->
                Box {
                    if (comment.isEmpty()) Text(stringResource(Res.string.review_comment_hint), style = style.copy(color = colors.placeholder))
                    field()
                }
            },
        )
        Text(
            "${comment.length} / $MAX_REVIEW_COMMENT", color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.End),
        )
    }
}

/** "Photos (2 of 5)": the photos with remove buttons, then a dashed tile to add more */
@Composable
private fun PhotosRow(
    draft: ReviewDraft,
    enabled: Boolean,
    onAdd: () -> Unit,
    onRemoveKept: (String) -> Unit,
    onRemoveNew: (PickedPhoto) -> Unit,
) {
    val colors = MaterialTheme.feastColors
    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(Res.string.review_photos), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Text(
                if (draft.photoCount == 0) stringResource(Res.string.review_photos_optional, MAX_REVIEW_PHOTOS)
                else stringResource(Res.string.review_photos_count, draft.photoCount, MAX_REVIEW_PHOTOS),
                color = colors.muted, fontSize = 15.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.titleSmall,
            )
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            draft.keptImageUrls.forEach { url -> PhotoTile(url, enabled) { onRemoveKept(url) } }
            draft.newPhotos.forEach { photo -> PhotoTile(photo.preview, enabled) { onRemoveNew(photo) } }
            if (draft.canAddPhotos) {
                Box(
                    Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.lavender)
                        .drawBehind {
                            drawRoundRect(
                                colors.starOff, cornerRadius = CornerRadius(18.dp.toPx()),
                                style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))),
                            )
                        }
                        .clickable(enabled = enabled, onClick = onAdd),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.AddPhotoAlternate, stringResource(Res.string.review_add_photo), tint = colors.accent, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
private fun PhotoTile(model: Any, enabled: Boolean, onRemove: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Box(Modifier.size(76.dp).clip(RoundedCornerShape(18.dp)).background(colors.lavender)) {
        AsyncImage(model, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        if (enabled) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (colors.isDark) Color(0xBF07060E) else Color(0xBF1E1B4B))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Close, stringResource(Res.string.review_remove_photo), tint = Color.White, modifier = Modifier.size(15.dp))
            }
        }
    }
}

/** "Post review" in saffron once there's a rating; while sending, how far the photos have uploaded */
@Composable
private fun PostButton(isEdit: Boolean, enabled: Boolean, submitting: Boolean, uploadProgress: Int?, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    FeastButton(
        label = stringResource(if (isEdit) Res.string.review_save_changes else Res.string.review_post_review),
        onClick = onClick,
        style = FeastButtonStyle.SAFFRON,
        enabled = enabled || submitting,
        // The send arrow only once it can be sent, as in the design
        icon = if (enabled) Icons.AutoMirrored.Rounded.Send else null,
        height = 56.dp,
        modifier = Modifier.fillMaxWidth(),
        content = if (submitting) {
            {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = colors.onSaffron)
                uploadProgress?.let {
                    Text(
                        stringResource(Res.string.review_uploading, "$it%"), color = colors.onSaffron, fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        } else null,
    )
}

/** "Add photos": take one with the camera or choose from the gallery (design 1c) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPhotosSheet(maxCount: Int, onPicked: (List<PickedPhoto>) -> Unit, onDismiss: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val picker = rememberPhotoPicker { photos ->
        onPicked(photos.take(maxCount))
        onDismiss()
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = colors.surface,
        contentColor = colors.ink,
        scrimColor = colors.sheetScrim,
        dragHandle = { Box(Modifier.padding(top = 10.dp).size(width = 40.dp, height = 4.dp).background(colors.border, RoundedCornerShape(2.dp))) },
    ) {
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                stringResource(Res.string.photo_sheet_title), color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
            )
            if (picker.canTakePhoto) SheetOption(Icons.Rounded.PhotoCamera, stringResource(Res.string.photo_take), picker::takePhoto)
            SheetOption(Icons.Rounded.Image, stringResource(Res.string.photo_choose_from_gallery)) { picker.chooseFromGallery(maxCount) }
        }
    }
}

@Composable
private fun SheetOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(44.dp).background(colors.lavender, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(24.dp))
        }
        Text(label, color = colors.ink, fontSize = 17.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
    }
}

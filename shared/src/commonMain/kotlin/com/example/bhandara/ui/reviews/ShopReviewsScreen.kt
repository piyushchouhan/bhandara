package com.example.bhandara.ui.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Reviews
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.data.models.api.ReviewResponse
import com.example.bhandara.data.network.ReviewApi
import com.example.bhandara.photos.PhotoSourceSheet
import com.example.bhandara.photos.PickedPhoto
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.cancel
import com.example.bhandara.shared.resources.review_add_photo
import com.example.bhandara.shared.resources.review_comment_hint
import com.example.bhandara.shared.resources.review_delete
import com.example.bhandara.shared.resources.review_delete_confirm
import com.example.bhandara.shared.resources.review_delete_text
import com.example.bhandara.shared.resources.review_delete_title
import com.example.bhandara.shared.resources.review_edit_yours
import com.example.bhandara.shared.resources.review_photo
import com.example.bhandara.shared.resources.review_remove_photo
import com.example.bhandara.shared.resources.review_report
import com.example.bhandara.shared.resources.review_stars
import com.example.bhandara.shared.resources.review_tap_stars
import com.example.bhandara.shared.resources.review_uploading
import com.example.bhandara.shared.resources.review_write
import com.example.bhandara.shared.resources.review_yours
import com.example.bhandara.shared.resources.reported
import com.example.bhandara.shared.resources.reviews_count
import com.example.bhandara.shared.resources.reviews_empty
import com.example.bhandara.shared.resources.tab_reviews
import com.example.bhandara.shared.resources.try_again
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.shopdetails.FullScreenPhotos
import com.example.bhandara.ui.shopdetails.formatRating
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import org.jetbrains.compose.resources.stringResource

/**
 * A shop's reviews, shared by Android and iOS: the rating summary with a star breakdown, everyone's reviews (your
 * own first, to edit or delete; others' to report), and "Write a review" at the thumb. With no reviews yet, five
 * big stars start the first one.
 *
 * @param onWriteReview opens the review screen (a new review, or the user's own to edit), with any stars already
 *                      tapped
 */
@Composable
fun ShopReviewsScreen(
    shopId: String,
    shopName: String,
    photoUrl: String?,
    onBack: () -> Unit,
    onWriteReview: (rating: Int) -> Unit,
) {
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
    var deleting by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            viewModel.onMessageShown()
            snackbar.showSnackbar(it)
        }
    }
    // Back from writing a review: show the list as it is now
    var shownBefore by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (shownBefore) viewModel.retry() else shownBefore = true
    }

    FeastTheme {
        val colors = MaterialTheme.feastColors
        Box(Modifier.fillMaxSize().background(colors.page)) {
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
                ReviewsHeader(shopName, photoUrl, state.countToShow, onBack)

                val error = state.error
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when {
                        state.loading && state.reviews.isEmpty() ->
                            CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.indigo)

                        error != null -> Column(
                            Modifier.align(Alignment.Center).padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(error, color = colors.muted, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                            TextButton(onClick = viewModel::retry) { Text(stringResource(Res.string.try_again)) }
                        }

                        state.reviews.isEmpty() -> NoReviewsYet(
                            canRate = state.canWrite,
                            onRate = onWriteReview,
                            modifier = Modifier.align(Alignment.Center),
                        )

                        else -> Column(
                            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            RatingSummary(state.averageRating ?: 0.0, state.reviews.size, state.starCounts)
                            // Your own review first, then everyone else's (newest first, as the backend sends them)
                            state.reviews.sortedByDescending { state.isMine(it) }.forEach { review ->
                                ReviewCard(
                                    review = review,
                                    isMine = state.isMine(review),
                                    canReport = state.canReport(review),
                                    reported = review.id in state.reportedIds,
                                    onDelete = { deleting = review.id },
                                    onReport = { viewModel.report(review.id) },
                                )
                            }
                        }
                    }
                }

                if (state.canWrite) {
                    Box(Modifier.windowInsetsPadding(WindowInsets.navigationBars).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)) {
                        WriteReviewButton(
                            label = stringResource(if (state.myReview != null) Res.string.review_edit_yours else Res.string.review_write),
                            onClick = { onWriteReview(0) },
                        )
                    }
                }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = 84.dp))
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
}

/** Back, the shop's photo and name, and "Reviews (n)" under it */
@Composable
private fun ReviewsHeader(shopName: String, photoUrl: String?, count: Int?, onBack: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), onBack)
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(colors.lavender), contentAlignment = Alignment.Center) {
            if (photoUrl != null) {
                AsyncImage(photoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.Rounded.Storefront, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                shopName,
                color = colors.ink, fontSize = 16.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium,
            )
            Text(
                count?.let { stringResource(Res.string.reviews_count, it) } ?: stringResource(Res.string.tab_reviews),
                color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/** The indigo card: the average, its stars, the count, and a bar for each number of stars */
@Composable
private fun RatingSummary(average: Double, count: Int, starCounts: List<Int>) {
    val colors = MaterialTheme.feastColors
    val most = starCounts.maxOrNull()?.takeIf { it > 0 } ?: 1
    Row(
        Modifier.fillMaxWidth().background(colors.indigo, RoundedCornerShape(22.dp)).padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(formatRating(average), color = colors.onIndigo, fontSize = 44.sp, lineHeight = 44.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(6.dp))
            Stars(average.toInt(), size = 16.dp, off = colors.starOffOnIndigo)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(Res.string.reviews_count, count), color = colors.headerSubtext, fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            for (stars in 5 downTo 1) {
                val share = starCounts[stars - 1].toFloat() / most
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("$stars", color = colors.headerSubtext, fontSize = 11.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelSmall)
                    Box(Modifier.weight(1f).height(5.dp).background(colors.barTrackOnIndigo, RoundedCornerShape(3.dp))) {
                        if (share > 0f) {
                            Box(Modifier.fillMaxHeight().fillMaxWidth(share).background(colors.saffron, RoundedCornerShape(3.dp)))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Stars(rating: Int, size: Dp, off: Color) {
    val colors = MaterialTheme.feastColors
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        for (i in 1..5) {
            Icon(Icons.Rounded.Star, contentDescription = null, tint = if (i <= rating) colors.saffron else off, modifier = Modifier.size(size))
        }
    }
}

@Composable
private fun ReviewCard(
    review: ReviewResponse,
    isMine: Boolean,
    canReport: Boolean,
    reported: Boolean,
    onDelete: () -> Unit,
    onReport: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    var openPhoto by remember { mutableStateOf<Int?>(null) }
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(20.dp))
            .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // The reviewer's first letter, as in the design; the backend doesn't send other people's names, so
            // their reviews show a person instead
            val account = LocalAppDependencies.current.auth.account()?.takeIf { !it.isAnonymous }
            val initial = if (isMine) (account?.name ?: account?.email)?.trim()?.firstOrNull()?.uppercaseChar() else null
            Box(Modifier.size(36.dp).background(colors.saffronSoft, CircleShape), contentAlignment = Alignment.Center) {
                if (initial != null) {
                    Text(initial.toString(), color = colors.saffronText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                } else {
                    Icon(Icons.Rounded.Person, contentDescription = null, tint = colors.saffronText, modifier = Modifier.size(20.dp))
                }
            }
            Column(Modifier.weight(1f)) {
                Stars(review.rating, size = 15.dp, off = colors.starOff)
                val date = formatReviewDate(review.createdAt)
                val label = listOfNotNull(if (isMine) stringResource(Res.string.review_yours) else null, date).joinToString(" · ")
                if (label.isNotEmpty()) {
                    Text(label, color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
                }
            }
            when {
                // One quiet icon at the end, like the design's flag; editing your own is the button at the bottom
                isMine -> IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.Delete, stringResource(Res.string.review_delete), Modifier.size(20.dp), tint = colors.muted)
                }
                reported -> Row(Modifier.padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.Flag, contentDescription = null, Modifier.size(16.dp), tint = colors.danger)
                    Text(stringResource(Res.string.reported), color = colors.danger, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
                }
                canReport -> IconButton(onClick = onReport, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.Flag, stringResource(Res.string.review_report), Modifier.size(20.dp), tint = colors.muted)
                }
            }
        }
        review.comment?.takeIf { it.isNotBlank() }?.let {
            Text(it, color = colors.ink, fontSize = 16.sp, lineHeight = 23.sp, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(end = 8.dp))
        }
        val urls = review.imageUrls.orEmpty()
        if (urls.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(urls) { index, url ->
                    AsyncImage(
                        model = url,
                        contentDescription = stringResource(Res.string.review_photo, index + 1),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(84.dp).clip(RoundedCornerShape(14.dp)).background(colors.lavender).clickable { openPhoto = index },
                    )
                }
            }
        }
    }
    openPhoto?.let { index ->
        FullScreenPhotos(review.imageUrls.orEmpty(), initialIndex = index, onDismiss = { openPhoto = null }, photoLabel = Res.string.review_photo)
    }
}

/** "No reviews yet": five big stars; tapping one starts the first review with that many */
@Composable
private fun NoReviewsYet(canRate: Boolean, onRate: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.feastColors
    Column(
        modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(Modifier.size(120.dp).background(colors.saffronSoft, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Reviews, contentDescription = null, tint = colors.saffron, modifier = Modifier.size(56.dp))
        }
        Text(
            stringResource(Res.string.reviews_empty),
            color = colors.ink, fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge,
        )
        if (canRate) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 1..5) {
                    Icon(
                        Icons.Rounded.Star,
                        contentDescription = stringResource(Res.string.review_stars, i),
                        tint = colors.border,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .selectable(selected = false, role = Role.Button) { onRate(i) },
                    )
                }
            }
            Text(stringResource(Res.string.review_tap_stars), color = colors.muted, fontSize = 14.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** The saffron "Write a review" button at the bottom */
@Composable
private fun WriteReviewButton(label: String, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth().height(56.dp).clip(shape).background(colors.saffron, shape).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        Icon(Icons.Rounded.Edit, contentDescription = null, tint = colors.onSaffron, modifier = Modifier.size(22.dp))
        Text(label, color = colors.onSaffron, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
    }
}

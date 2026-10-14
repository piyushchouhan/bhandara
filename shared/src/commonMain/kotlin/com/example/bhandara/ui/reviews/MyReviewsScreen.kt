package com.example.bhandara.ui.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.data.models.api.ReviewResponse
import com.example.bhandara.data.network.ReviewApi
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.LocalPlatformActions
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.date_days_ago
import com.example.bhandara.shared.resources.date_today
import com.example.bhandara.shared.resources.date_weeks_ago
import com.example.bhandara.shared.resources.date_yesterday
import com.example.bhandara.shared.resources.my_reviews
import com.example.bhandara.shared.resources.my_reviews_avg_given
import com.example.bhandara.shared.resources.my_reviews_delete_photos
import com.example.bhandara.shared.resources.my_reviews_delete_stars
import com.example.bhandara.shared.resources.my_reviews_delete_text
import com.example.bhandara.shared.resources.my_reviews_delete_title
import com.example.bhandara.shared.resources.my_reviews_deleted
import com.example.bhandara.shared.resources.my_reviews_empty_text
import com.example.bhandara.shared.resources.my_reviews_empty_title
import com.example.bhandara.shared.resources.my_reviews_filter_all
import com.example.bhandara.shared.resources.my_reviews_filter_published
import com.example.bhandara.shared.resources.my_reviews_filter_removed
import com.example.bhandara.shared.resources.my_reviews_find_shop
import com.example.bhandara.shared.resources.my_reviews_given
import com.example.bhandara.shared.resources.my_reviews_keep
import com.example.bhandara.shared.resources.my_reviews_more
import com.example.bhandara.shared.resources.my_reviews_more_photos
import com.example.bhandara.shared.resources.my_reviews_published
import com.example.bhandara.shared.resources.my_reviews_removed
import com.example.bhandara.shared.resources.my_reviews_removed_note
import com.example.bhandara.shared.resources.my_reviews_share
import com.example.bhandara.shared.resources.my_reviews_share_text
import com.example.bhandara.shared.resources.my_reviews_sort
import com.example.bhandara.shared.resources.my_reviews_sort_highest
import com.example.bhandara.shared.resources.my_reviews_sort_lowest
import com.example.bhandara.shared.resources.my_reviews_sort_newest
import com.example.bhandara.shared.resources.my_reviews_view_shop
import com.example.bhandara.shared.resources.review_delete
import com.example.bhandara.shared.resources.review_edit
import com.example.bhandara.shared.resources.review_photo
import com.example.bhandara.shared.resources.try_again
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.shopdetails.FeastSheet
import com.example.bhandara.ui.shopdetails.FullScreenPhotos
import com.example.bhandara.ui.shopdetails.formatRating
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

// My reviews (design "My Reviews": 1a list, 1b actions, 1c delete, 1d empty; dark 2a–2d)

/**
 * The user's own reviews, from Profile → My activity
 *
 * @param onEdit opens the review for editing (the write-review screen finds the user's review of the shop)
 * @param onFindShop "Find a shop to review": back to the map
 */
@Composable
fun MyReviewsScreen(
    onBack: () -> Unit,
    onOpenShop: (shopId: String) -> Unit,
    onEdit: (review: ReviewResponse) -> Unit,
    onFindShop: () -> Unit,
) {
    val dependencies = LocalAppDependencies.current
    val viewModel = viewModel { MyReviewsViewModel(ReviewApi(dependencies.apiClient)) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = LocalPlatformActions.current
    val snackbar = remember { SnackbarHostState() }
    var actionsFor by remember { mutableStateOf<ReviewResponse?>(null) }
    var confirmDelete by remember { mutableStateOf<ReviewResponse?>(null) }
    var sorting by rememberSaveable { mutableStateOf(false) }
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }

    LaunchedEffect(state.message, state.deleted) {
        val text = state.message ?: if (state.deleted) getString(Res.string.my_reviews_deleted) else null
        if (text != null) {
            viewModel.onMessageShown()
            snackbar.showSnackbar(text)
        }
    }
    // Back from editing a review: show it as it is now
    var shownBefore by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (shownBefore) viewModel.load() else shownBefore = true
    }

    FeastTheme {
        val colors = MaterialTheme.feastColors
        Box(Modifier.fillMaxSize().background(colors.page)) {
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
                // Header: back, title, and sort while there's something to sort
                Row(
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), onBack)
                    Text(
                        stringResource(Res.string.my_reviews),
                        color = colors.ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f),
                    )
                    if (state.reviews.size > 1) {
                        SquareIconButton(Icons.AutoMirrored.Rounded.Sort, stringResource(Res.string.my_reviews_sort), { sorting = true })
                    }
                }

                val error = state.error
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when {
                        state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = colors.indigo)
                        error != null -> Column(
                            Modifier.align(Alignment.Center).padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(error, color = colors.muted, fontSize = 15.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                            FeastButton(stringResource(Res.string.try_again), onClick = viewModel::load, style = FeastButtonStyle.SOFT, modifier = Modifier.padding(horizontal = 24.dp))
                        }
                        state.reviews.isEmpty() -> NoReviewsYet(onFindShop, Modifier.align(Alignment.Center))
                        else -> LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item(key = "summary") { Summary(state.reviews.size, state.averageGiven) }
                            item(key = "filters") { Filters(state, viewModel::onFilter) }
                            items(state.shown(), key = { it.id }) { review ->
                                MyReviewCard(review, today, onMore = { actionsFor = review })
                            }
                        }
                    }
                }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = 16.dp))
        }

        actionsFor?.let { review ->
            val text = shareText(review)
            ReviewActionsSheet(
                review = review,
                onEdit = { onEdit(review) },
                onViewShop = { onOpenShop(review.shopId) },
                onShare = { actions.share(text) },
                onDelete = { confirmDelete = review },
                onDismiss = { actionsFor = null },
            )
        }
        confirmDelete?.let { review ->
            DeleteReviewDialog(
                review = review,
                onDelete = {
                    confirmDelete = null
                    viewModel.delete(review.id)
                },
                onDismiss = { confirmDelete = null },
            )
        }
        if (sorting) {
            SortSheet(state.sort, onSort = viewModel::onSort, onDismiss = { sorting = false })
        }
    }
}

@Composable
private fun shareText(review: ReviewResponse): String =
    stringResource(Res.string.my_reviews_share_text, review.shopName.orEmpty(), review.rating, review.comment.orEmpty()).trimEnd()

/** The indigo card: how many reviews, and the average stars given */
@Composable
private fun Summary(count: Int, average: Double?) {
    val colors = MaterialTheme.feastColors
    Row(Modifier.fillMaxWidth().background(colors.indigo, RoundedCornerShape(22.dp)).padding(16.dp)) {
        SummaryCell(Modifier.weight(1f), label = stringResource(Res.string.my_reviews_given)) {
            Text(count.toString(), color = colors.onIndigo, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
        }
        Box(Modifier.width(1.dp).height(44.dp).background(Color.White.copy(alpha = 0.14f)).align(Alignment.CenterVertically))
        SummaryCell(Modifier.weight(1f), label = stringResource(Res.string.my_reviews_avg_given)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(formatRating(average ?: 0.0), color = colors.onIndigo, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
                Icon(Icons.Rounded.Star, contentDescription = null, tint = colors.saffron, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun SummaryCell(modifier: Modifier, label: String, value: @Composable () -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        value()
        Text(label, color = MaterialTheme.feastColors.headerSubtext, fontSize = 11.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelSmall)
    }
}

/** All / Published / Removed, with counts; "Removed" only when a moderator removed one */
@Composable
private fun Filters(state: MyReviewsState, onFilter: (MyReviewsFilter) -> Unit) {
    LazyRow(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        item { FilterChip(stringResource(Res.string.my_reviews_filter_all, state.reviews.size), state.activeFilter == MyReviewsFilter.ALL) { onFilter(MyReviewsFilter.ALL) } }
        item { FilterChip(stringResource(Res.string.my_reviews_filter_published, state.publishedCount), state.activeFilter == MyReviewsFilter.PUBLISHED) { onFilter(MyReviewsFilter.PUBLISHED) } }
        if (state.removedCount > 0) {
            item { FilterChip(stringResource(Res.string.my_reviews_filter_removed, state.removedCount), state.activeFilter == MyReviewsFilter.REMOVED) { onFilter(MyReviewsFilter.REMOVED) } }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(50)
    Box(
        Modifier
            .height(36.dp)
            .clip(shape)
            .background(if (selected) colors.indigo else colors.surface, shape)
            .then(if (selected) Modifier else Modifier.border(1.5.dp, colors.border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label, color = if (selected) colors.onIndigo else colors.ink, fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, style = MaterialTheme.typography.labelLarge,
        )
    }
}

/** One review: the shop, when, the stars and status, the text, its photos, and a note when it was removed */
@Composable
private fun MyReviewCard(review: ReviewResponse, today: LocalDate, onMore: () -> Unit) {
    val colors = MaterialTheme.feastColors
    var openPhoto by remember { mutableStateOf<Int?>(null) }
    Column(
        Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(22.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ShopThumb(review, size = 44.dp)
            Column(Modifier.weight(1f)) {
                Text(
                    review.shopName.orEmpty(), color = colors.ink, fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall,
                )
                val subtitle = listOfNotNull(review.shopArea, reviewAgeText(reviewAge(review.createdAt, today))).joinToString(" · ")
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onMore, modifier = Modifier.size(40.dp).offset(x = 6.dp)) {
                Icon(Icons.Rounded.MoreVert, stringResource(Res.string.my_reviews_more), tint = colors.muted, modifier = Modifier.size(22.dp))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stars(review.rating, 18.dp)
            StatusBadge(removed = review.isRemoved)
        }
        review.comment?.takeIf { it.isNotBlank() }?.let {
            Text(it, color = colors.ink, fontSize = 14.sp, lineHeight = 20.sp, style = MaterialTheme.typography.bodyMedium)
        }
        val photos = review.imageUrls.orEmpty()
        if (photos.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AsyncImage(
                    photos.first(), contentDescription = stringResource(Res.string.review_photo, 1), contentScale = ContentScale.Crop,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(colors.lavender).clickable { openPhoto = 0 },
                )
                if (photos.size > 1) {
                    Box(
                        Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(colors.lavender).clickable { openPhoto = 1 },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(stringResource(Res.string.my_reviews_more_photos, photos.size - 1), color = colors.muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        if (review.isRemoved) {
            Text(
                stringResource(Res.string.my_reviews_removed_note), color = colors.muted, fontSize = 12.sp, lineHeight = 17.sp,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().background(colors.lavenderSoft, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
    }
    openPhoto?.let { index ->
        FullScreenPhotos(review.imageUrls.orEmpty(), initialIndex = index, onDismiss = { openPhoto = null }, photoLabel = Res.string.review_photo)
    }
}

/** The shop's first photo, or an icon (a truck for a cart) on a soft tile */
@Composable
private fun ShopThumb(review: ReviewResponse, size: Dp) {
    val colors = MaterialTheme.feastColors
    val cart = review.shopIsMovingCart == true
    Box(
        Modifier.size(size).clip(RoundedCornerShape(12.dp)).background(if (cart) colors.saffronSoft else colors.lavender),
        contentAlignment = Alignment.Center,
    ) {
        val photo = review.shopImageUrl
        if (photo != null) {
            AsyncImage(photo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Icon(
                if (cart) Icons.Rounded.LocalShipping else Icons.Rounded.Storefront, contentDescription = null,
                tint = if (cart) colors.saffronText else colors.accent, modifier = Modifier.size(size / 2),
            )
        }
    }
}

@Composable
private fun Stars(rating: Int, size: Dp) {
    val colors = MaterialTheme.feastColors
    Row {
        for (i in 1..5) {
            Icon(Icons.Rounded.Star, contentDescription = null, tint = if (i <= rating) colors.saffron else colors.starOff, modifier = Modifier.size(size))
        }
    }
}

/** Green "Published", or "Removed" in red when a moderator took it down */
@Composable
private fun StatusBadge(removed: Boolean) {
    val colors = MaterialTheme.feastColors
    val (fill, text) = if (removed) colors.dangerSoft to colors.danger else colors.successSoft to colors.success
    Row(
        Modifier.height(22.dp).background(fill, RoundedCornerShape(7.dp)).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (removed) Icon(Icons.Rounded.Close, contentDescription = null, tint = text, modifier = Modifier.size(13.dp))
        Text(
            stringResource(if (removed) Res.string.my_reviews_removed else Res.string.my_reviews_published),
            color = text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall,
        )
    }
}

/** "Today", "2 days ago", "3 weeks ago" or the date */
@Composable
internal fun reviewAgeText(age: ReviewAge?): String? = when (age) {
    null -> null
    ReviewAge.Today -> stringResource(Res.string.date_today)
    ReviewAge.Yesterday -> stringResource(Res.string.date_yesterday)
    is ReviewAge.DaysAgo -> pluralStringResource(Res.plurals.date_days_ago, age.days, age.days)
    is ReviewAge.WeeksAgo -> pluralStringResource(Res.plurals.date_weeks_ago, age.weeks, age.weeks)
    is ReviewAge.On -> age.date
}

/** The ⋮ sheet (1b): the shop and the stars on top, then edit, view on shop page, share, delete */
@Composable
private fun ReviewActionsSheet(
    review: ReviewResponse,
    onEdit: () -> Unit,
    onViewShop: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    FeastSheet(onDismiss) { close ->
        Row(
            Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ShopThumb(review, size = 40.dp)
            Column {
                Text(review.shopName.orEmpty(), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                Stars(review.rating, 14.dp)
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.lavender))
        Column(Modifier.padding(top = 6.dp)) {
            // A removed review can't be changed or shared any more; it can still lead to the shop
            if (!review.isRemoved) {
                ActionRow(Icons.Outlined.Edit, stringResource(Res.string.review_edit), colors.accent, colors.ink) { close(onEdit) }
            }
            ActionRow(Icons.Outlined.Storefront, stringResource(Res.string.my_reviews_view_shop), colors.accent, colors.ink) { close(onViewShop) }
            if (!review.isRemoved) {
                ActionRow(Icons.Outlined.IosShare, stringResource(Res.string.my_reviews_share), colors.accent, colors.ink) { close(onShare) }
                ActionRow(Icons.Outlined.Delete, stringResource(Res.string.review_delete), colors.danger, colors.danger) { close(onDelete) }
            }
        }
    }
}

@Composable
private fun ActionRow(icon: ImageVector, label: String, iconTint: Color, textColor: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        Text(label, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
    }
}

/** 1c: what's lost with the review (its stars and photos), "Delete review" in red and "Keep it" */
@Composable
private fun DeleteReviewDialog(review: ReviewResponse, onDelete: () -> Unit, onDismiss: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(28.dp)).padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(Modifier.size(56.dp).background(colors.dangerSoft, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = colors.danger, modifier = Modifier.size(28.dp))
            }
            Text(stringResource(Res.string.my_reviews_delete_title), color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(Res.string.my_reviews_delete_text, review.shopName.orEmpty()),
                color = colors.muted, fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium,
            )
            Column(
                Modifier.fillMaxWidth().background(colors.page, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LostItem(pluralStringResource(Res.plurals.my_reviews_delete_stars, review.rating, review.rating))
                val photos = review.imageUrls.orEmpty().size
                if (photos > 0) LostItem(pluralStringResource(Res.plurals.my_reviews_delete_photos, photos, photos))
            }
            Column(Modifier.fillMaxWidth().padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FeastButton(stringResource(Res.string.review_delete), onClick = onDelete, style = FeastButtonStyle.DANGER, height = 52.dp, modifier = Modifier.fillMaxWidth())
                FeastButton(stringResource(Res.string.my_reviews_keep), onClick = onDismiss, style = FeastButtonStyle.SOFT, height = 52.dp, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun LostItem(text: String) {
    val colors = MaterialTheme.feastColors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Rounded.Close, contentDescription = null, tint = colors.muted, modifier = Modifier.size(18.dp))
        Text(text, color = colors.ink, fontSize = 13.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
    }
}

/** 1d: nothing reviewed yet; points back to the map */
@Composable
private fun NoReviewsYet(onFindShop: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.feastColors
    Column(
        modifier.padding(start = 36.dp, end = 36.dp, bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(112.dp).background(colors.lavender, RoundedCornerShape(36.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.RateReview, contentDescription = null, tint = colors.accent, modifier = Modifier.size(52.dp))
            Icon(Icons.Rounded.Star, contentDescription = null, tint = colors.saffron, modifier = Modifier.size(30.dp).align(Alignment.TopEnd).offset(x = 6.dp, y = (-6).dp))
        }
        Text(stringResource(Res.string.my_reviews_empty_title), color = colors.ink, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
        Text(stringResource(Res.string.my_reviews_empty_text), color = colors.muted, fontSize = 14.sp, lineHeight = 21.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        FeastButton(
            stringResource(Res.string.my_reviews_find_shop), onClick = onFindShop, icon = Icons.Rounded.Map, iconTint = colors.saffron,
            height = 52.dp, modifier = Modifier.padding(top = 6.dp).padding(horizontal = 10.dp),
        )
    }
}

/** Newest first, or by the stars given */
@Composable
private fun SortSheet(current: MyReviewsSort, onSort: (MyReviewsSort) -> Unit, onDismiss: () -> Unit) {
    val colors = MaterialTheme.feastColors
    FeastSheet(onDismiss) { close ->
        Text(
            stringResource(Res.string.my_reviews_sort), color = colors.ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 6.dp, bottom = 8.dp),
        )
        listOf(
            MyReviewsSort.NEWEST to Res.string.my_reviews_sort_newest,
            MyReviewsSort.HIGHEST to Res.string.my_reviews_sort_highest,
            MyReviewsSort.LOWEST to Res.string.my_reviews_sort_lowest,
        ).forEach { (sort, label) ->
            Row(
                Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).clickable { close { onSort(sort) } }.padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(label), color = colors.ink, fontSize = 15.sp,
                    fontWeight = if (sort == current) FontWeight.SemiBold else FontWeight.Medium,
                    style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f),
                )
                if (sort == current) Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
            }
        }
    }
}

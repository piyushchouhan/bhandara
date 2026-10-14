package com.example.bhandara.ui.reviews

import com.example.bhandara.shared.resources.photo_views
import com.example.bhandara.shared.resources.stat_views
import com.example.bhandara.ui.shopdetails.compactCount
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.data.models.api.ReviewResponse
import com.example.bhandara.data.network.ReviewApi
import com.example.bhandara.platform.BackPress
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.platform.LocalPlatformActions
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.cancel
import com.example.bhandara.shared.resources.my_photos
import com.example.bhandara.shared.resources.my_photos_close
import com.example.bhandara.shared.resources.my_photos_delete
import com.example.bhandara.shared.resources.my_photos_delete_confirm
import com.example.bhandara.shared.resources.my_photos_delete_n
import com.example.bhandara.shared.resources.my_photos_delete_text_many
import com.example.bhandara.shared.resources.my_photos_delete_text_shop
import com.example.bhandara.shared.resources.my_photos_delete_title
import com.example.bhandara.shared.resources.my_photos_deleted
import com.example.bhandara.shared.resources.my_photos_empty_text
import com.example.bhandara.shared.resources.my_photos_empty_title
import com.example.bhandara.shared.resources.my_photos_find_shop
import com.example.bhandara.shared.resources.my_photos_in_group
import com.example.bhandara.shared.resources.my_photos_label
import com.example.bhandara.shared.resources.my_photos_photo
import com.example.bhandara.shared.resources.my_photos_places
import com.example.bhandara.shared.resources.my_photos_position
import com.example.bhandara.shared.resources.my_photos_save
import com.example.bhandara.shared.resources.my_photos_save_failed
import com.example.bhandara.shared.resources.my_photos_saved
import com.example.bhandara.shared.resources.my_photos_select
import com.example.bhandara.shared.resources.my_photos_select_all
import com.example.bhandara.shared.resources.my_photos_selected
import com.example.bhandara.shared.resources.my_photos_share
import com.example.bhandara.shared.resources.my_photos_share_text
import com.example.bhandara.shared.resources.my_photos_stop_selecting
import com.example.bhandara.shared.resources.my_reviews_more
import com.example.bhandara.shared.resources.my_reviews_view_shop
import com.example.bhandara.shared.resources.review_edit
import com.example.bhandara.shared.resources.try_again
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.FeastButtonStyle
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.shopdetails.FeastSheet
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

// My photos (design "My Photos": 1a grid, 1b viewer, 1c select, 1d delete, 1e empty; dark 2a–2e). Only the photos
// in the user's reviews: there are no views or photo review yet, so those parts of the design are left out.

/**
 * The photos the user added to their reviews, from Profile → My activity
 *
 * @param onEditReview the viewer's ⋮ → Edit review (the write-review screen finds the user's review of the shop)
 * @param onFindShop "Find a shop": back to the map
 */
@Composable
fun MyPhotosScreen(
    onBack: () -> Unit,
    onOpenShop: (shopId: String) -> Unit,
    onEditReview: (review: ReviewResponse) -> Unit,
    onFindShop: () -> Unit,
) {
    val dependencies = LocalAppDependencies.current
    val viewModel = viewModel { MyPhotosViewModel(ReviewApi(dependencies.apiClient)) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = LocalPlatformActions.current
    val snackbar = remember { SnackbarHostState() }
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    // The photo open in the viewer, and the photos asked to be deleted (confirmed in a sheet)
    var viewing by remember { mutableStateOf<MyPhoto?>(null) }
    var confirmDelete by remember { mutableStateOf<List<MyPhoto>>(emptyList()) }

    LaunchedEffect(state.message, state.deletedCount) {
        val text = state.message
            ?: state.deletedCount.takeIf { it > 0 }?.let { getPluralString(Res.plurals.my_photos_deleted, it, it) }
        if (text != null) {
            viewModel.onMessageShown()
            snackbar.showSnackbar(text)
        }
    }
    var shownBefore by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (shownBefore) viewModel.load() else shownBefore = true
    }

    FeastTheme {
        val colors = MaterialTheme.feastColors
        Box(Modifier.fillMaxSize().background(colors.page)) {
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
                if (state.selecting) {
                    SelectingHeader(
                        count = state.selected.size,
                        onClose = viewModel::stopSelecting,
                        onSelectAll = viewModel::selectAll,
                    )
                } else {
                    Row(
                        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), onBack)
                        Text(
                            stringResource(Res.string.my_photos), color = colors.ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f),
                        )
                        if (state.photos.isNotEmpty()) {
                            SquareIconButton(Icons.Rounded.Checklist, stringResource(Res.string.my_photos_select), viewModel::startSelecting)
                        }
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
                        state.photos.isEmpty() -> NoPhotosYet(onFindShop, Modifier.align(Alignment.Center))
                        state.selecting -> SelectGrid(state, viewModel::toggle)
                        else -> LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                        ) {
                            item(key = "summary") { Summary(state.photos.size, state.placeCount, state.viewCount) }
                            items(state.groups, key = { it.review.id }) { group ->
                                Group(group, onOpenShop = { onOpenShop(group.review.shopId) }, onOpen = { viewing = it })
                            }
                        }
                    }
                }
            }

            if (state.selecting) {
                SelectionBar(
                    count = state.selected.size,
                    onShare = { actions.share(state.selectedPhotos.joinToString("\n") { it.url }) },
                    onDelete = { confirmDelete = state.selectedPhotos },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
            SnackbarHost(
                snackbar,
                Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = if (state.selecting) 96.dp else 16.dp),
            )
            // The viewer covers the whole screen (not a dialog, so it gets the same system bar spacing as the page)
            viewing?.let { photo ->
                // Swipe through the shop's photos, starting with the tapped one
                val group = state.groups.firstOrNull { it.review.id == photo.review.id }
                if (group == null) {
                    viewing = null
                } else {
                    PhotoViewer(
                        group = group,
                        startAt = group.photos.indexOfFirst { it.url == photo.url }.coerceAtLeast(0),
                        today = today,
                        onOpenShop = {
                            viewing = null
                            onOpenShop(group.review.shopId)
                        },
                        onEditReview = {
                            viewing = null
                            onEditReview(group.review)
                        },
                        onDelete = { confirmDelete = listOf(it) },
                        onClose = { viewing = null },
                    )
                }
            }
        }

        if (confirmDelete.isNotEmpty()) {
            DeletePhotosSheet(
                photos = confirmDelete,
                onDelete = {
                    val urls = confirmDelete.map { it.url }.toSet()
                    if (viewing?.url in urls) viewing = null
                    viewModel.delete(urls)
                },
                onDismiss = { confirmDelete = emptyList() },
            )
        }
    }
}

/** Select mode's header (1c): close, how many are picked, "Select all" */
@Composable
private fun SelectingHeader(count: Int, onClose: () -> Unit, onSelectAll: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SquareIconButton(Icons.Rounded.Close, stringResource(Res.string.my_photos_stop_selecting), onClose)
        Text(
            stringResource(Res.string.my_photos_selected, count), color = colors.ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f),
        )
        Text(
            stringResource(Res.string.my_photos_select_all), color = colors.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onSelectAll).padding(horizontal = 6.dp, vertical = 10.dp),
        )
    }
}

/** The indigo card: how many photos, in how many places, and how often others opened them */
@Composable
private fun Summary(photos: Int, places: Int, views: Long) {
    val colors = MaterialTheme.feastColors
    Row(Modifier.fillMaxWidth().background(colors.indigo, RoundedCornerShape(22.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        SummaryCell(photos.toString(), stringResource(Res.string.my_photos_label), Modifier.weight(1f))
        Box(Modifier.width(1.dp).height(44.dp).background(Color.White.copy(alpha = 0.14f)))
        SummaryCell(places.toString(), stringResource(Res.string.my_photos_places), Modifier.weight(1f))
        Box(Modifier.width(1.dp).height(44.dp).background(Color.White.copy(alpha = 0.14f)))
        SummaryCell(compactCount(views), stringResource(Res.string.stat_views), Modifier.weight(1f))
    }
}

@Composable
private fun SummaryCell(value: String, label: String, modifier: Modifier) {
    val colors = MaterialTheme.feastColors
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = colors.onIndigo, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
        Text(label, color = colors.headerSubtext, fontSize = 11.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelSmall)
    }
}

/** A shop's photos: its name and count (tap for the shop), then a big first photo beside two small ones, or a row */
@Composable
private fun Group(group: PhotoGroup, onOpenShop: () -> Unit, onOpen: (MyPhoto) -> Unit) {
    val colors = MaterialTheme.feastColors
    val review = group.review
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onOpenShop).padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(review.shopName.orEmpty(), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                val count = pluralStringResource(Res.plurals.my_photos_in_group, group.photos.size, group.photos.size)
                Text(listOfNotNull(review.shopArea, count).joinToString(" · "), color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, stringResource(Res.string.my_reviews_view_shop), tint = colors.muted, modifier = Modifier.size(20.dp))
        }
        val photos = group.photos
        val rest = if (photos.size >= 3) {
            Row(Modifier.fillMaxWidth().height(198.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Thumb(photos[0], Modifier.weight(2f).fillMaxHeight(), corner = 18.dp, onClick = onOpen)
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Thumb(photos[1], Modifier.fillMaxWidth().weight(1f), onClick = onOpen)
                    Thumb(photos[2], Modifier.fillMaxWidth().weight(1f), onClick = onOpen)
                }
            }
            photos.drop(3)
        } else {
            photos
        }
        rest.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { Thumb(it, Modifier.weight(1f).height(104.dp), onClick = onOpen) }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun Thumb(photo: MyPhoto, modifier: Modifier, corner: Dp = 14.dp, onClick: (MyPhoto) -> Unit, overlay: @Composable BoxScope.() -> Unit = {}) {
    Box(modifier.clip(RoundedCornerShape(corner)).background(MaterialTheme.feastColors.lavender).clickable { onClick(photo) }) {
        AsyncImage(photo.url, contentDescription = photo.review.shopName, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        overlay()
    }
}

/** Select mode (1c): every photo in one grid, each with a round check */
@Composable
private fun SelectGrid(state: MyPhotosState, onToggle: (String) -> Unit) {
    val colors = MaterialTheme.feastColors
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(state.photos.chunked(3)) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { photo ->
                    val picked = photo.url in state.selected
                    Thumb(
                        photo,
                        Modifier.weight(1f).height(108.dp).then(if (picked) Modifier.border(3.dp, colors.accent, RoundedCornerShape(14.dp)) else Modifier),
                        onClick = { onToggle(it.url) },
                    ) {
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(24.dp)
                                .then(
                                    if (picked) Modifier.background(colors.accent, CircleShape)
                                    else Modifier.background(colors.indigo.copy(alpha = 0.2f), CircleShape).border(2.dp, Color.White, CircleShape),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (picked) Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.onIndigo.takeIf { !colors.isDark } ?: colors.indigo, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** The floating bar in select mode: Share and "Delete N" */
@Composable
private fun SelectionBar(count: Int, onShare: () -> Unit, onDelete: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.feastColors
    Row(
        modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 14.dp, end = 14.dp, bottom = 24.dp)
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(22.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FeastButton(
            stringResource(Res.string.my_photos_share), onClick = onShare, style = FeastButtonStyle.SOFT, icon = Icons.Outlined.IosShare,
            enabled = count > 0, height = 52.dp, modifier = Modifier.weight(1f),
        )
        FeastButton(
            stringResource(Res.string.my_photos_delete_n, count), onClick = onDelete, style = FeastButtonStyle.DANGER, icon = Icons.Outlined.Delete,
            enabled = count > 0, height = 52.dp, modifier = Modifier.weight(1f),
        )
    }
}

/** The full-screen viewer (1b): swipe through a shop's photos; the shop, when it was added, and Share / Save / Delete */
@Composable
private fun PhotoViewer(
    group: PhotoGroup,
    startAt: Int,
    today: LocalDate,
    onOpenShop: () -> Unit,
    onEditReview: () -> Unit,
    onDelete: (MyPhoto) -> Unit,
    onClose: () -> Unit,
) {
    val colors = MaterialTheme.feastColors
    val actions = LocalPlatformActions.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val pager = rememberPagerState(initialPage = startAt) { group.photos.size }
    val review = group.review
    var saving by remember { mutableStateOf(false) }
    val shareText = stringResource(Res.string.my_photos_share_text, review.shopName.orEmpty(), group.photos.getOrNull(pager.currentPage)?.url.orEmpty())

    var menu by remember { mutableStateOf(false) }
    BackPress(onBack = onClose)
    // Taps on the dark backdrop must not reach the page underneath
    Box(Modifier.fillMaxSize().background(Color(0xFF07060E)).clickable(interactionSource = null, indication = null) {}) {
        HorizontalPager(pager, Modifier.fillMaxSize()) { page ->
            AsyncImage(
                group.photos[page].url,
                contentDescription = stringResource(Res.string.my_photos_photo, page + 1, review.shopName.orEmpty()),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            // Top: close and "1 of 3"
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                GlassButton(Icons.Rounded.Close, stringResource(Res.string.my_photos_close), onClose)
                Text(
                    stringResource(Res.string.my_photos_position, pager.currentPage + 1, group.photos.size),
                    color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f),
                )
                GlassButton(Icons.Rounded.MoreVert, stringResource(Res.string.my_reviews_more), { menu = true })
            }
            Spacer(Modifier.weight(1f))
            if (group.photos.size > 1) {
                Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
                    group.photos.indices.forEach { i ->
                        val current = i == pager.currentPage
                        Box(Modifier.size(width = if (current) 20.dp else 6.dp, height = 6.dp).background(if (current) colors.saffron else Color.White.copy(alpha = 0.4f), RoundedCornerShape(3.dp)))
                    }
                }
            }
            Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // The shop it belongs to; tap to open it
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.1f)).clickable(onClick = onOpenShop).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ShopThumbnail(review, 44.dp)
                    Column(Modifier.weight(1f)) {
                        Text(review.shopName.orEmpty(), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                        // "Added 2 days ago · 640 views", for the photo being shown
                        val views = group.photos.getOrNull(pager.currentPage)?.views ?: 0L
                        val line = listOfNotNull(
                            reviewAgeText(reviewAge(review.createdAt, today)),
                            pluralStringResource(Res.plurals.photo_views, views.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), compactCount(views)),
                        ).joinToString(" · ")
                        Text(line, color = Color(0xFFC7C4F0), fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, stringResource(Res.string.my_reviews_view_shop), tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ViewerAction(Icons.Outlined.IosShare, stringResource(Res.string.my_photos_share), Color.White, Color.White.copy(alpha = 0.1f), Modifier.weight(1f)) {
                        actions.share(shareText)
                    }
                    ViewerAction(Icons.Outlined.FileDownload, stringResource(Res.string.my_photos_save), Color.White, Color.White.copy(alpha = 0.1f), Modifier.weight(1f)) {
                        val url = group.photos.getOrNull(pager.currentPage)?.url ?: return@ViewerAction
                        if (saving) return@ViewerAction
                        saving = true
                        scope.launch {
                            val saved = actions.savePhoto(url)
                            saving = false
                            snackbar.showSnackbar(getString(if (saved) Res.string.my_photos_saved else Res.string.my_photos_save_failed))
                        }
                    }
                    ViewerAction(Icons.Outlined.Delete, stringResource(Res.string.my_photos_delete), Color(0xFFFCA5A5), Color(0xFFEF4444).copy(alpha = 0.18f), Modifier.weight(1f)) {
                        group.photos.getOrNull(pager.currentPage)?.let(onDelete)
                    }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.TopCenter).windowInsetsPadding(WindowInsets.safeDrawing).padding(top = 64.dp))
    }
    if (menu) {
        FeastSheet({ menu = false }) { close ->
            ViewerMenuRow(Icons.Outlined.Storefront, stringResource(Res.string.my_reviews_view_shop)) { close(onOpenShop) }
            ViewerMenuRow(Icons.Outlined.Edit, stringResource(Res.string.review_edit)) { close(onEditReview) }
        }
    }
}

@Composable
private fun ViewerMenuRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
        Text(label, color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun GlassButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.12f)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, description, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun ViewerAction(icon: ImageVector, label: String, tint: Color, fill: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.height(58.dp).clip(RoundedCornerShape(16.dp)).background(fill).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Text(label, color = tint, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ShopThumbnail(review: ReviewResponse, size: Dp) {
    val colors = MaterialTheme.feastColors
    val cart = review.shopIsMovingCart == true
    Box(Modifier.size(size).clip(RoundedCornerShape(12.dp)).background(if (cart) colors.saffronSoft else colors.lavender), contentAlignment = Alignment.Center) {
        val photo = review.shopImageUrl
        if (photo != null) {
            AsyncImage(photo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Icon(if (cart) Icons.Rounded.LocalShipping else Icons.Rounded.Storefront, contentDescription = null, tint = if (cart) colors.saffronText else colors.accent, modifier = Modifier.size(size / 2))
        }
    }
}

/** 1d: the photos to delete, which review they leave, and Delete / Cancel */
@Composable
private fun DeletePhotosSheet(photos: List<MyPhoto>, onDelete: () -> Unit, onDismiss: () -> Unit) {
    val colors = MaterialTheme.feastColors
    FeastSheet(onDismiss) { close ->
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            photos.take(4).forEach { photo ->
                AsyncImage(photo.url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(14.dp)).background(colors.lavender))
            }
            if (photos.size > 4) {
                Box(Modifier.size(64.dp).clip(RoundedCornerShape(14.dp)).background(colors.lavender), contentAlignment = Alignment.Center) {
                    Text("+${photos.size - 4}", color = colors.muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        Text(
            pluralStringResource(Res.plurals.my_photos_delete_title, photos.size, photos.size), color = colors.ink, fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 14.dp),
        )
        val shops = photos.map { it.review.id }.distinct()
        Text(
            if (shops.size == 1) stringResource(Res.string.my_photos_delete_text_shop, photos.first().review.shopName.orEmpty())
            else stringResource(Res.string.my_photos_delete_text_many),
            color = colors.muted, fontSize = 14.sp, lineHeight = 20.sp, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp),
        )
        Column(Modifier.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FeastButton(stringResource(Res.string.my_photos_delete_confirm), onClick = { close(onDelete) }, style = FeastButtonStyle.DANGER, height = 52.dp, modifier = Modifier.fillMaxWidth())
            FeastButton(stringResource(Res.string.cancel), onClick = { close {} }, style = FeastButtonStyle.SOFT, height = 52.dp, modifier = Modifier.fillMaxWidth())
        }
    }
}

/** 1e: no photos yet; points back to the map */
@Composable
private fun NoPhotosYet(onFindShop: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.feastColors
    Column(
        modifier.padding(start = 36.dp, end = 36.dp, bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(112.dp).background(colors.lavender, RoundedCornerShape(36.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = colors.accent, modifier = Modifier.size(52.dp))
            Icon(Icons.Rounded.AddCircle, contentDescription = null, tint = colors.saffron, modifier = Modifier.size(30.dp).align(Alignment.TopEnd).offset(x = 6.dp, y = (-6).dp))
        }
        Text(stringResource(Res.string.my_photos_empty_title), color = colors.ink, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge)
        Text(stringResource(Res.string.my_photos_empty_text), color = colors.muted, fontSize = 14.sp, lineHeight = 21.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        FeastButton(
            stringResource(Res.string.my_photos_find_shop), onClick = onFindShop, icon = Icons.Rounded.Map, iconTint = colors.saffron,
            height = 52.dp, modifier = Modifier.padding(top = 6.dp).padding(horizontal = 10.dp),
        )
    }
}

package com.example.bhandara.ui.fssai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.bhandara.platform.LocalAppDependencies
import com.example.bhandara.data.network.FssaiApi
import com.example.bhandara.photos.PickedPdf
import com.example.bhandara.photos.PickedPhoto
import com.example.bhandara.photos.rememberPdfPicker
import com.example.bhandara.photos.rememberPhotoPicker
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.back
import com.example.bhandara.shared.resources.fssai_add_certificate
import com.example.bhandara.shared.resources.fssai_add_page_2
import com.example.bhandara.shared.resources.fssai_certificate
import com.example.bhandara.shared.resources.fssai_checked_within
import com.example.bhandara.shared.resources.fssai_choose_pdf
import com.example.bhandara.shared.resources.fssai_number
import com.example.bhandara.shared.resources.fssai_number_hint
import com.example.bhandara.shared.resources.fssai_pages_hint
import com.example.bhandara.shared.resources.fssai_pick_date
import com.example.bhandara.shared.resources.fssai_private_note
import com.example.bhandara.shared.resources.fssai_read_from_photo
import com.example.bhandara.shared.resources.fssai_reading
import com.example.bhandara.shared.resources.fssai_remove_page
import com.example.bhandara.shared.resources.fssai_source_title
import com.example.bhandara.shared.resources.fssai_submit
import com.example.bhandara.shared.resources.fssai_title
import com.example.bhandara.shared.resources.fssai_valid_till
import com.example.bhandara.shared.resources.photo_choose_from_gallery
import com.example.bhandara.shared.resources.photo_take
import com.example.bhandara.ui.components.FeastButton
import com.example.bhandara.ui.components.SquareIconButton
import com.example.bhandara.ui.feasts.localNow
import com.example.bhandara.ui.reportfeast.FeastDateSheet
import com.example.bhandara.ui.theme.FeastTheme
import com.example.bhandara.ui.theme.feastColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Adding an FSSAI licence (5a / 7a): the certificate's pages, its number and expiry (read off it by AI, then
 * checked by the owner), and "Submit for check".
 *
 * @param shopId the shop to send it for; null while adding a shop, when [onDone] gets the licence to send with it
 * @param initial what was filled in before (adding a shop and coming back to change it)
 */
@Composable
fun FssaiScreen(shopId: String?, initial: FssaiForm?, onBack: () -> Unit, onDone: (FssaiForm) -> Unit) {
    val dependencies = LocalAppDependencies.current
    val viewModel = viewModel(key = "fssai-$shopId") { FssaiViewModel(FssaiApi(dependencies.apiClient), shopId, initial, today = { localNow().date }) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.done) { if (state.done) onDone(state.form) }
    FeastTheme { FssaiPage(state, viewModel, onBack) }
}

@Composable
private fun FssaiPage(state: FssaiState, viewModel: FssaiViewModel, onBack: () -> Unit) {
    val colors = MaterialTheme.feastColors
    var choosingSource by remember { mutableStateOf(false) }
    var pickingDate by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(colors.page).windowInsetsPadding(WindowInsets.statusBars).imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SquareIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.back), onBack, enabled = !state.submitting)
            Text(stringResource(Res.string.fssai_title), color = colors.ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleLarge)
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(Res.string.fssai_certificate), color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Pages(state.form.pages, enabled = !state.submitting, onAdd = { choosingSource = true }, onRemove = viewModel::removePage)
            when {
                state.reading -> Banner(colors.lavenderSoft, colors.accent, null, stringResource(Res.string.fssai_reading), working = true)
                state.readProblem != null -> Banner(colors.dangerSoft, colors.dangerText, Icons.Outlined.Info, state.readProblem)
                state.readFromPhoto -> Banner(colors.successSoft, colors.success, Icons.Outlined.AutoAwesome, stringResource(Res.string.fssai_read_from_photo))
            }
            NumberField(state.form.number, viewModel::onNumberChange, enabled = !state.submitting)
            Row {
                ValidTillCard(state.form.validTill?.let { fssaiDate(it) }, enabled = !state.submitting) { pickingDate = true }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = colors.muted, modifier = Modifier.size(18.dp))
                Text(stringResource(Res.string.fssai_private_note), color = colors.muted, fontSize = 13.sp, lineHeight = 18.sp, style = MaterialTheme.typography.bodySmall)
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(0.dp), ambientColor = Color(0xFF1E1B4B), spotColor = Color(0xFF1E1B4B).copy(alpha = 0.08f))
                .background(colors.surface)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 16.dp),
        ) {
            state.error?.let {
                Text(it, color = colors.danger, fontSize = 13.sp, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
            }
            FeastButton(
                stringResource(Res.string.fssai_submit),
                onClick = viewModel::submit,
                enabled = !state.submitting,
                dimmed = viewModel.problem() != null,
                icon = Icons.AutoMirrored.Rounded.Send,
                iconTint = colors.saffron,
                height = 56.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.submitting) CircularProgressIndicator(color = colors.onIndigo, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            }
            Text(
                stringResource(Res.string.fssai_checked_within), color = colors.muted, fontSize = 12.sp, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
        }
    }

    if (choosingSource) {
        SourceSheet(maxCount = FssaiViewModel.MAX_PAGES - state.form.pages.size, onPicked = viewModel::addPages, onDismiss = { choosingSource = false })
    }
    if (pickingDate) {
        FeastDateSheet(
            current = state.form.validTill,
            onPicked = { viewModel.setValidTill(it); pickingDate = false },
            onDismiss = { pickingDate = false },
            yearArrows = true,
        )
    }
}

/** The pages added so far, then (until there are two) the dashed "add" tile */
@Composable
private fun Pages(pages: List<PickedPhoto>, enabled: Boolean, onAdd: () -> Unit, onRemove: (Int) -> Unit) {
    val colors = MaterialTheme.feastColors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth().height(140.dp).padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            pages.forEachIndexed { index, page ->
                Box(Modifier.width(104.dp).height(132.dp)) {
                    PageTile(page)
                    Box(
                        Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-8).dp).size(28.dp).border(2.dp, colors.page, CircleShape).padding(2.dp).clip(CircleShape)
                            .background(colors.indigo).clickable(enabled = enabled) { onRemove(index) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Close, stringResource(Res.string.fssai_remove_page), tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
            if (pages.size < FssaiViewModel.MAX_PAGES) {
                Column(Modifier.weight(1f).height(132.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AddTile(if (pages.isEmpty()) Res.string.fssai_add_certificate else Res.string.fssai_add_page_2, enabled, onAdd, Modifier.weight(1f))
                    if (pages.isNotEmpty()) PagesHint()
                }
            }
        }
        if (pages.isEmpty() || pages.size == FssaiViewModel.MAX_PAGES) PagesHint()
    }
}

@Composable
private fun PagesHint() {
    Text(stringResource(Res.string.fssai_pages_hint), color = MaterialTheme.feastColors.muted, fontSize = 12.sp, lineHeight = 16.sp, style = MaterialTheme.typography.bodySmall)
}

/** A photo page as a picture; a PDF drawn as a document */
@Composable
private fun PageTile(page: PickedPhoto) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(16.dp)
    Box(Modifier.fillMaxSize().clip(shape).background(colors.surface).border(1.5.dp, colors.border, shape)) {
        if (page is PickedPdf) {
            Column(Modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.fillMaxWidth(0.6f).height(8.dp).background(colors.successSoft, RoundedCornerShape(4.dp)))
                listOf(1f, 0.8f, 0.7f, 0.85f).forEach { width ->
                    Box(Modifier.fillMaxWidth(width).height(5.dp).background(colors.lavender, RoundedCornerShape(3.dp)))
                }
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
                    Text(page.name, color = colors.muted, fontSize = 10.sp, maxLines = 1, style = MaterialTheme.typography.labelSmall)
                }
            }
        } else {
            AsyncImage(page.preview, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun AddTile(label: StringResource, enabled: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.feastColors
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.lavenderSoft)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                drawRoundRect(
                    DASHED_OUTLINE, cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Outlined.AddAPhoto, contentDescription = null, tint = colors.accent, modifier = Modifier.size(24.dp))
                Text(stringResource(label), color = colors.accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun Banner(fill: Color, ink: Color, icon: ImageVector?, text: String, working: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().background(fill, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (working) CircularProgressIndicator(color = ink, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
        icon?.let { Icon(it, contentDescription = null, tint = ink, modifier = Modifier.size(18.dp)) }
        Text(text, color = ink, fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 17.sp, style = MaterialTheme.typography.bodySmall)
    }
}

/** The 14 digits, shown in groups as on the certificate ("1152 3026 0001 23"), with its label always above */
@Composable
private fun NumberField(number: String, onChange: (String) -> Unit, enabled: Boolean) {
    val colors = MaterialTheme.feastColors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val shape = RoundedCornerShape(16.dp)
    val style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp, color = colors.ink)
    Row(
        Modifier.fillMaxWidth().height(58.dp).background(colors.surface, shape)
            .border(BorderStroke(1.5.dp, if (focused) colors.accent else colors.border), shape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Outlined.Pin, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(Res.string.fssai_number), color = if (focused) colors.accent else colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
            BasicTextField(
                value = number,
                onValueChange = onChange,
                enabled = enabled,
                singleLine = true,
                textStyle = style,
                cursorBrush = SolidColor(colors.ink),
                interactionSource = interaction,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = FssaiNumberGroups,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { field ->
                    Box {
                        if (number.isEmpty()) Text(stringResource(Res.string.fssai_number_hint), style = style.copy(color = colors.placeholder, letterSpacing = 0.sp))
                        field()
                    }
                },
            )
        }
    }
}

@Composable
private fun ValidTillCard(date: String?, enabled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier.height(58.dp).clip(shape).background(colors.surface).border(1.5.dp, colors.border, shape).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Outlined.Event, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
        Column {
            Text(stringResource(Res.string.fssai_valid_till), color = colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
            Text(
                date ?: stringResource(Res.string.fssai_pick_date), color = if (date == null) colors.placeholder else colors.ink, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

/** Camera, photos or a PDF */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SourceSheet(maxCount: Int, onPicked: (List<PickedPhoto>) -> Unit, onDismiss: () -> Unit) {
    val colors = MaterialTheme.feastColors
    val photos = rememberPhotoPicker { picked ->
        onPicked(picked.take(maxCount))
        onDismiss()
    }
    val pdf = rememberPdfPicker { picked ->
        onPicked(listOf(picked))
        onDismiss()
    }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = colors.surface) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(Res.string.fssai_source_title), color = colors.ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            if (photos.canTakePhoto) SourceRow(Icons.Outlined.PhotoCamera, Res.string.photo_take) { photos.takePhoto() }
            SourceRow(Icons.Outlined.Image, Res.string.photo_choose_from_gallery) { photos.chooseFromGallery(maxCount) }
            SourceRow(Icons.Outlined.PictureAsPdf, Res.string.fssai_choose_pdf) { pdf() }
        }
    }
}

@Composable
private fun SourceRow(icon: ImageVector, label: StringResource, onClick: () -> Unit) {
    val colors = MaterialTheme.feastColors
    Row(
        Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(40.dp).background(colors.lavender, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(22.dp))
        }
        Text(stringResource(label), color = colors.ink, fontSize = 16.sp, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
    }
}

/** "11523026000123" shown as "1152 3026 0001 23" */
internal object FssaiNumberGroups : VisualTransformation {
    private val breaks = listOf(4, 8, 12)

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val grouped = buildString {
            digits.forEachIndexed { i, c ->
                if (i in breaks) append(' ')
                append(c)
            }
        }
        val mapping = object : OffsetMapping {
            // A space goes before digits 5, 9 and 13; in the shown text they sit at 4, 9 and 14
            override fun originalToTransformed(offset: Int) = (offset + breaks.count { it < offset }).coerceAtMost(grouped.length)
            override fun transformedToOriginal(offset: Int) =
                (offset - breaks.withIndex().count { (i, b) -> b + i < offset }).coerceIn(0, digits.length)
        }
        return TransformedText(AnnotatedString(grouped), mapping)
    }
}

/** The dashed outline of the "add page" tile, the same in light and dark (design) */
private val DASHED_OUTLINE = Color(0xFFC7C4F0)

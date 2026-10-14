package com.example.bhandara.photos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.photo_choose_from_gallery
import com.example.bhandara.shared.resources.photo_sheet_title
import com.example.bhandara.shared.resources.photo_take
import org.jetbrains.compose.resources.stringResource

/** Taking or choosing photos with the phone's own camera and photo picker */
interface PhotoPicker {

    /** False where there is no camera (e.g. the iOS simulator) */
    val canTakePhoto: Boolean

    /** Opens the camera (asking for permission first if needed) */
    fun takePhoto()

    /** Opens the photo picker, letting the user choose up to [maxCount] photos */
    fun chooseFromGallery(maxCount: Int)
}

/** A [PhotoPicker] whose [onPicked] gets what the user took or chose; nothing when they cancel */
@Composable
expect fun rememberPhotoPicker(onPicked: (List<PickedPhoto>) -> Unit): PhotoPicker

/** "Add photos": camera or gallery, as a bottom sheet */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSourceSheet(maxCount: Int, onPicked: (List<PickedPhoto>) -> Unit, onDismiss: () -> Unit) {
    val picker = rememberPhotoPicker { photos ->
        onPicked(photos.take(maxCount))
        onDismiss()
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text(
                stringResource(Res.string.photo_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            if (picker.canTakePhoto) {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.photo_take)) },
                    leadingContent = { Icon(Icons.Default.PhotoCamera, contentDescription = null) },
                    modifier = Modifier.clickable { picker.takePhoto() },
                )
            }
            ListItem(
                headlineContent = { Text(stringResource(Res.string.photo_choose_from_gallery)) },
                leadingContent = { Icon(Icons.Default.Image, contentDescription = null) },
                modifier = Modifier.clickable { picker.chooseFromGallery(maxCount) },
            )
        }
    }
}

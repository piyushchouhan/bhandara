package com.example.bhandara.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.bhandara.ui.reviews.ReviewPhotos
import com.example.bhandara.utils.ImageUploadHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Review photos on Android: camera or gallery, uploaded to Firebase Storage */
class AndroidReviewPhotos(context: Context) : ReviewPhotos {

    private val uploader = ImageUploadHelper(context.applicationContext)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Picker(maxCount: Int, onPicked: (List<Any>) -> Unit, onDismiss: () -> Unit) {
        val context = LocalContext.current
        var cameraPhoto by rememberSaveable { mutableStateOf<Uri?>(null) }

        fun picked(uris: List<Uri>) {
            if (uris.isNotEmpty()) onPicked(uris.take(maxCount))
            onDismiss()
        }

        val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            picked(if (success) listOfNotNull(cameraPhoto) else emptyList())
        }
        fun takePhoto() {
            cameraPhoto = newPhotoFile(context).also(camera::launch)
        }
        val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                takePhoto()
            } else {
                Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
            }
        }
        // Choosing several at once needs room for at least two
        val galleryMany = rememberLauncherForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxCount.coerceAtLeast(2))
        ) { uris -> picked(uris) }
        val galleryOne = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            picked(listOfNotNull(uri))
        }

        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text("Add Photos", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                ListItem(
                    headlineContent = { Text("Take Photo") },
                    leadingContent = { Icon(Icons.Default.PhotoCamera, null) },
                    modifier = Modifier.clickable {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            takePhoto()
                        } else {
                            cameraPermission.launch(Manifest.permission.CAMERA)
                        }
                    },
                )
                ListItem(
                    headlineContent = { Text("Choose from Gallery") },
                    leadingContent = { Icon(Icons.Default.Image, null) },
                    modifier = Modifier.clickable {
                        val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        if (maxCount >= 2) galleryMany.launch(request) else galleryOne.launch(request)
                    },
                )
            }
        }
    }

    override suspend fun upload(photos: List<Any>, onProgress: (Int) -> Unit): List<String> {
        val uris = photos.map { it as Uri }
        val urls = uploader.uploadImages(uris, onProgress)
        // The helper skips photos that failed; posting without them would silently lose them
        check(urls.size == uris.size) { "${uris.size - urls.size} of ${uris.size} photos failed to upload" }
        return urls
    }

    private fun newPhotoFile(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", context.getExternalFilesDir(Environment.DIRECTORY_PICTURES))
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }
}

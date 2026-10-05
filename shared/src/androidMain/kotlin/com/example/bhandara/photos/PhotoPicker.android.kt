package com.example.bhandara.photos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
actual fun rememberPhotoPicker(onPicked: (List<PickedPhoto>) -> Unit): PhotoPicker {
    val context = LocalContext.current
    val appContext = context.applicationContext
    var cameraPhoto by rememberSaveable { mutableStateOf<Uri?>(null) }
    var limit by rememberSaveable { mutableIntStateOf(MAX_AT_ONCE) }

    fun picked(uris: List<Uri>) {
        if (uris.isNotEmpty()) onPicked(uris.take(limit).map { AndroidPickedPhoto(it, appContext) })
    }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        if (taken) picked(listOfNotNull(cameraPhoto))
    }
    val openCamera = { cameraPhoto = newCameraFile(context).also(camera::launch) }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) openCamera() else Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
    }
    // Choosing several needs a limit of at least two, fixed when registered; extra choices are dropped above
    val galleryMany = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(MAX_AT_ONCE)) { picked(it) }
    val galleryOne = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { picked(listOfNotNull(it)) }

    return object : PhotoPicker {
        override val canTakePhoto = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

        override fun takePhoto() {
            limit = 1
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (granted) openCamera() else cameraPermission.launch(Manifest.permission.CAMERA)
        }

        override fun chooseFromGallery(maxCount: Int) {
            if (maxCount <= 0) return
            limit = maxCount // read by the callback when the photos arrive
            val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            if (maxCount == 1) galleryOne.launch(request) else galleryMany.launch(request)
        }
    }
}

private const val MAX_AT_ONCE = 5

/** A file for the camera to write to; the app's FileProvider (declared in its manifest) shares it with the camera */
private fun newCameraFile(context: Context): Uri {
    val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val file = File.createTempFile("photo_", ".jpg", dir)
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

/** A photo from the camera or gallery, read from its content Uri when it's uploaded */
private class AndroidPickedPhoto(private val uri: Uri, private val context: Context) : PickedPhoto {

    override val preview: Any = uri

    override suspend fun compressed(): CompressedPhoto? = withContext(Dispatchers.IO) {
        runCatching { compressToWebp() }.getOrNull()?.let { CompressedPhoto(it, "webp", "image/webp") }
    }

    /** Scaled down to [MAX_PHOTO_SIDE], turned upright (cameras store the rotation separately), as WebP */
    private fun compressToWebp(): ByteArray? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= MAX_PHOTO_SIDE && bounds.outHeight / (sample * 2) >= MAX_PHOTO_SIDE) sample *= 2

        val decoded = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null

        val scale = MAX_PHOTO_SIDE.toFloat() / maxOf(decoded.width, decoded.height)
        val matrix = Matrix().apply {
            if (scale < 1f) postScale(scale, scale)
            val rotation = rotationDegrees()
            if (rotation != 0) postRotate(rotation.toFloat())
        }
        val upright = if (matrix.isIdentity) decoded else Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        val out = ByteArrayOutputStream()
        upright.compress(Bitmap.CompressFormat.WEBP_LOSSY, 90, out)
        if (upright !== decoded) decoded.recycle()
        upright.recycle()
        return out.toByteArray()
    }

    private fun rotationDegrees(): Int = runCatching {
        context.contentResolver.openInputStream(uri)?.use {
            when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } ?: 0
    }.getOrDefault(0)
}

package com.example.bhandara.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.UUID

/**
 * Helper class for uploading images to Firebase Storage
 */
class ImageUploadHelper(private val context: Context) {
    
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference
    
    companion object {
        private const val TAG = "ImageUpload"
        private const val FEASTS_FOLDER = "feast_images"
    }

    init {
        Log.d(TAG, "Initialized ImageUploadHelper. Target Bucket: ${storageRef.bucket}")
    }
    
    /**
     * Upload a single image to Firebase Storage
     * @param uri Image URI from device
     * @param onProgress Progress callback (0-100)
     * @return Download URL if successful, null otherwise
     */
    suspend fun uploadImage(
        uri: Uri,
        onProgress: (Int) -> Unit = {}
    ): String? {
        return try {
            val fileName = "${UUID.randomUUID()}.webp"
            val imageRef = storageRef.child("$FEASTS_FOLDER/$fileName")
            
            // Compress image to WebP format
            val webpBytes = decodeAndCompressToWebp(uri)
                ?: throw Exception("Could not compress image to WebP: $uri")
                
            val stream = ByteArrayInputStream(webpBytes)
            val uploadTask = imageRef.putStream(stream)
            
            // Monitor upload progress
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
            }
            
            // Wait for upload to complete
            uploadTask.await()
            
            // Get download URL
            val downloadUrl = imageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image: $uri", e)
            if (e is com.google.firebase.storage.StorageException) {
                Log.e(TAG, "Storage Error Code: ${e.errorCode}, Message: ${e.message}")
            }
            null
        }
    }
    
    /**
     * Upload multiple images to Firebase Storage
     * @param uris List of image URIs
     * @param onProgress Progress callback for overall progress (0-100)
     * @return List of download URLs
     */
    suspend fun uploadImages(
        uris: List<Uri>,
        onProgress: (Int) -> Unit = {}
    ): List<String> {
        val urls = mutableListOf<String>()
        
        uris.forEachIndexed { index, uri ->
            val url = uploadImage(uri) { imageProgress ->
                val overallProgress = ((index * 100 + imageProgress) / uris.size)
                onProgress(overallProgress)
            }
            
            if (url != null) {
                urls.add(url)
            }
        }
        
        return urls
    }

    /**
     * Decodes a local image URI, resizes it to a maximum dimension to prevent OOM,
     * respects EXIF orientation, and compresses it into a WebP byte array for better compression.
     */
    private fun decodeAndCompressToWebp(uri: Uri, maxDimension: Int = 1440): ByteArray? {
        return try {
            // 1. Get image bounds first
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri).use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            // 2. Compute sample size to scale down image decoding
            var inSampleSize = 1
            val height = options.outHeight
            val width = options.outWidth
            if (height > maxDimension || width > maxDimension) {
                val halfHeight = height / 2
                val halfWidth = width / 2
                while (halfHeight / inSampleSize >= maxDimension && halfWidth / inSampleSize >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            // 3. Decode Bitmap with sample size
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val rawBitmap = context.contentResolver.openInputStream(uri).use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            // 4. Perform precise scaling to maxDimension if needed
            var bitmap = if (rawBitmap.width > maxDimension || rawBitmap.height > maxDimension) {
                val ratio = rawBitmap.width.toFloat() / rawBitmap.height.toFloat()
                val (newWidth, newHeight) = if (ratio > 1) {
                    maxDimension to (maxDimension / ratio).toInt()
                } else {
                    (maxDimension * ratio).toInt() to maxDimension
                }
                Bitmap.createScaledBitmap(rawBitmap, newWidth, newHeight, true).also {
                    if (it != rawBitmap) {
                        rawBitmap.recycle()
                    }
                }
            } else {
                rawBitmap
            }

            // 4.5. Rotate bitmap if required by EXIF orientation
            val rotationDegrees = getRotationDegrees(uri)
            if (rotationDegrees != 0) {
                bitmap = rotateBitmap(bitmap, rotationDegrees)
            }

            // 5. Compress to WebP format with higher quality (90 instead of 80)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 90, outputStream)
            bitmap.recycle()
            outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image to WebP", e)
            null
        }
    }

    private fun getRotationDegrees(uri: Uri): Int {
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exifInterface = android.media.ExifInterface(stream)
                val orientation = exifInterface.getAttributeInt(
                    android.media.ExifInterface.TAG_ORIENTATION,
                    android.media.ExifInterface.ORIENTATION_NORMAL
                )
                return when (orientation) {
                    android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking EXIF orientation", e)
        }
        return 0
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        return try {
            val matrix = android.graphics.Matrix().apply { postRotate(degrees.toFloat()) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) {
                bitmap.recycle()
            }
            rotated
        } catch (e: Exception) {
            Log.e(TAG, "Error rotating bitmap", e)
            bitmap
        }
    }
}

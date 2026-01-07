package com.example.bhandara.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
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
            val fileName = "${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child("$FEASTS_FOLDER/$fileName")
            
            // Use openInputStream for robust content:// URI handling
            val stream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("Could not open input stream for URI: $uri")
                
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
}

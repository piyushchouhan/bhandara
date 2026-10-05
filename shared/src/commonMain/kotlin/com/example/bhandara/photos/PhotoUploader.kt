package com.example.bhandara.photos

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random

/**
 * Uploads the photos of one post (a review, a feast, a shop), the same on Android and iOS: all of them or none,
 * so a post never goes out silently missing a photo.
 *
 * @param folder where in Storage the photos go; the app has always used one folder for every kind of photo
 */
class PhotoUploader(
    private val storage: PlatformStorage,
    private val folder: String = PHOTO_FOLDER,
    private val newId: () -> String = ::randomId,
) {

    /**
     * The photos' download URLs, in the same order. Reports overall progress 0-100 (never going back).
     * Throws [PhotoUploadException] if any photo can't be read or uploaded; the ones already uploaded are removed.
     */
    suspend fun upload(photos: List<PickedPhoto>, onProgress: (Int) -> Unit = {}): List<String> {
        if (photos.isEmpty()) return emptyList()
        val uploadedPaths = mutableListOf<String>()
        var reported = -1
        fun report(done: Float) {
            val percent = (done * 100 / photos.size).toInt().coerceIn(0, 100)
            if (percent > reported) {
                reported = percent
                onProgress(percent)
            }
        }
        report(0f)
        try {
            val urls = photos.mapIndexed { index, photo ->
                val compressed = photo.compressed() ?: throw PhotoUploadException("Couldn't read photo ${index + 1}")
                val path = "$folder/${newId()}.${compressed.extension}"
                val url = uploadOne(path, compressed) { fraction -> report(index + fraction.coerceIn(0f, 1f)) }
                uploadedPaths += path
                report(index + 1f)
                url
            }
            return urls
        } catch (e: Exception) {
            uploadedPaths.forEach(storage::delete)
            if (e is CancellationException || e is PhotoUploadException) throw e
            throw PhotoUploadException(e.message ?: "Upload failed")
        }
    }

    private suspend fun uploadOne(path: String, photo: CompressedPhoto, onProgress: (Float) -> Unit): String =
        suspendCancellableCoroutine { continuation ->
            storage.upload(path, photo.bytes, photo.contentType, onProgress) { url, error ->
                if (!continuation.isActive) return@upload
                if (url != null) {
                    continuation.resume(url)
                } else {
                    continuation.resumeWithException(PhotoUploadException(error ?: "Upload failed"))
                }
            }
        }

    companion object {
        /** The Storage folder the Android app has always uploaded every photo to (its Storage rules allow it) */
        const val PHOTO_FOLDER = "feast_images"

        private fun randomId(): String = Random.nextBytes(16).joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
    }
}

package com.example.bhandara.photos

/** A photo the user took or chose on this phone, not uploaded yet */
interface PickedPhoto {

    /** What the screen shows as a preview, anything Coil can load (Android: a content Uri; iOS: the image bytes) */
    val preview: Any

    /** The photo upright and small enough to upload (longest side at most [MAX_PHOTO_SIDE] px); null if unreadable */
    suspend fun compressed(): CompressedPhoto?
}

/** Longest side of an uploaded photo, in pixels: sharp on a phone, small to upload on mobile data */
const val MAX_PHOTO_SIDE = 1440

class CompressedPhoto(val bytes: ByteArray, val extension: String, val contentType: String)

/**
 * Firebase Storage as each platform provides it: the Firebase SDK on Android (in the app module) and on iOS (in
 * Swift). Callbacks keep it easy to implement from Swift.
 */
interface PlatformStorage {

    /** Uploads [bytes] to [path]; reports progress 0..1, then calls back once with the download URL or an error */
    fun upload(
        path: String,
        bytes: ByteArray,
        contentType: String,
        onProgress: (Float) -> Unit,
        onResult: (url: String?, error: String?) -> Unit,
    )

    /** Removes an uploaded file, best effort (e.g. after a later photo of the same post failed) */
    fun delete(path: String)
}

/** A photo couldn't be read or uploaded; nothing of that batch is kept */
class PhotoUploadException(message: String) : Exception(message)

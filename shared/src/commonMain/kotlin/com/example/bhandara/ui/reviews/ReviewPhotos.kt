package com.example.bhandara.ui.reviews

import androidx.compose.runtime.Composable

/**
 * Adding photos to a review: picking them on the phone and uploading them. Android implements it; iPhone gets it
 * with the other photo features (phase 2), until then reviews there are text only.
 */
interface ReviewPhotos {

    /**
     * Lets the user take or choose up to [maxCount] photos, while it's in the composition. [onPicked] gets what
     * they chose, as anything Coil can show (e.g. an Android content Uri); [onDismiss] closes it either way.
     */
    @Composable
    fun Picker(maxCount: Int, onPicked: (List<Any>) -> Unit, onDismiss: () -> Unit)

    /** Uploads picked photos and returns their URLs in the same order, reporting progress 0-100; throws if any fails */
    suspend fun upload(photos: List<Any>, onProgress: (Int) -> Unit): List<String>
}

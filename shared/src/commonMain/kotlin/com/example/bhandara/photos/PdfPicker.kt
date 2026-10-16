package com.example.bhandara.photos

import androidx.compose.runtime.Composable

/** A PDF the user chose (e.g. a certificate downloaded from a government site), sent as it is */
class PickedPdf(val name: String, private val read: suspend () -> ByteArray?) : PickedPhoto {

    /** No picture to show: screens draw a document for it */
    override val preview: Any get() = this

    override suspend fun compressed(): CompressedPhoto? =
        read()?.takeIf { it.isNotEmpty() && it.size <= MAX_PDF_BYTES }?.let { CompressedPhoto(it, "pdf", "application/pdf") }

    companion object {
        /** What the backend takes per page */
        const val MAX_PDF_BYTES = 10 * 1024 * 1024
    }
}

/** Opens the phone's file picker for one PDF; [onPicked] gets it, nothing when the user cancels */
@Composable
expect fun rememberPdfPicker(onPicked: (PickedPdf) -> Unit): () -> Unit

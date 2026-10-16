package com.example.bhandara.photos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.example.bhandara.platform.topViewController
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTTypePDF
import platform.darwin.NSObject

@Composable
actual fun rememberPdfPicker(onPicked: (PickedPdf) -> Unit): () -> Unit {
    val latestOnPicked = rememberUpdatedState(onPicked)
    // UIKit holds its delegate weakly, so it's kept here
    val delegate = remember { PdfPickerDelegate { pdf -> latestOnPicked.value(pdf) } }
    return remember {
        {
            // A copy in the app's own storage, so it can be read later without asking for access again
            val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypePDF), asCopy = true)
            picker.allowsMultipleSelection = false
            picker.delegate = delegate
            topViewController()?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private class PdfPickerDelegate(private val onPicked: (PickedPdf) -> Unit) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
        onPicked(PickedPdf(url.lastPathComponent ?: "certificate.pdf") { NSData.dataWithContentsOfURL(url)?.toByteArray() })
    }
}

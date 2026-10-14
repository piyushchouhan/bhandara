package com.example.bhandara.photos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.example.bhandara.platform.topViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
actual fun rememberPhotoPicker(onPicked: (List<PickedPhoto>) -> Unit): PhotoPicker {
    val latestOnPicked = rememberUpdatedState(onPicked)
    // UIKit holds its delegates weakly, so the picker keeps them
    return remember { IosPhotoPicker { photos -> latestOnPicked.value(photos) } }
}

private class IosPhotoPicker(private val onPicked: (List<PickedPhoto>) -> Unit) : PhotoPicker {

    override val canTakePhoto: Boolean =
        UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)

    private val galleryDelegate = GalleryDelegate { photos -> if (photos.isNotEmpty()) onPicked(photos) }
    private val cameraDelegate = CameraDelegate { photo -> onPicked(listOf(photo)) }

    override fun takePhoto() {
        if (!canTakePhoto) return
        val camera = UIImagePickerController()
        camera.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        camera.delegate = cameraDelegate
        topViewController()?.presentViewController(camera, animated = true, completion = null)
    }

    override fun chooseFromGallery(maxCount: Int) {
        if (maxCount <= 0) return
        val configuration = PHPickerConfiguration()
        configuration.selectionLimit = maxCount.toLong()
        configuration.filter = PHPickerFilter.imagesFilter
        val picker = PHPickerViewController(configuration)
        picker.delegate = galleryDelegate
        topViewController()?.presentViewController(picker, animated = true, completion = null)
    }
}

/** The system photo picker's answer: loads each chosen photo, then hands them over in the order chosen */
private class GalleryDelegate(private val onPicked: (List<PickedPhoto>) -> Unit) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val results = didFinishPicking.filterIsInstance<PHPickerResult>()
        if (results.isEmpty()) return
        val loaded = arrayOfNulls<NSData>(results.size)
        var remaining = results.size
        results.forEachIndexed { index, result ->
            result.itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data, _ ->
                // Called on a background queue; collect on the main one
                dispatch_async(dispatch_get_main_queue()) {
                    loaded[index] = data
                    remaining--
                    if (remaining == 0) onPicked(loaded.filterNotNull().map { IosPickedPhoto(it) })
                }
            }
        }
    }
}

private class CameraDelegate(private val onTaken: (PickedPhoto) -> Unit) :
    NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage ?: return
        UIImageJPEGRepresentation(image, 0.95)?.let { onTaken(IosPickedPhoto(it)) }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
    }
}

/** A photo from the camera or the photo library, as the image file's bytes */
@OptIn(ExperimentalForeignApi::class)
private class IosPickedPhoto(private val data: NSData) : PickedPhoto {

    override val preview: Any by lazy { data.toByteArray() }

    override suspend fun compressed(): CompressedPhoto? = withContext(Dispatchers.Default) {
        val image = UIImage.imageWithData(data) ?: return@withContext null
        val (width, height) = image.size.useContents { width to height }
        if (width <= 0.0 || height <= 0.0) return@withContext null
        val scale = minOf(1.0, MAX_PHOTO_SIDE / max(width, height))
        val targetWidth = (width * scale).roundToInt().toDouble()
        val targetHeight = (height * scale).roundToInt().toDouble()
        // Drawing the image makes it upright (iPhone photos store their rotation separately) and smaller
        val format = UIGraphicsImageRendererFormat()
        format.scale = 1.0
        val renderer = UIGraphicsImageRenderer(size = CGSizeMake(targetWidth, targetHeight), format = format)
        val upright = renderer.imageWithActions { image.drawInRect(CGRectMake(0.0, 0.0, targetWidth, targetHeight)) }
        UIImageJPEGRepresentation(upright, 0.85)?.let { CompressedPhoto(it.toByteArray(), "jpg", "image/jpeg") }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) bytes.usePinned { memcpy(it.addressOf(0), this.bytes, length) }
    return bytes
}

@OptIn(ExperimentalForeignApi::class)
internal fun ByteArray.toNSData(): NSData =
    if (isEmpty()) NSData() else usePinned { NSData.dataWithBytes(it.addressOf(0), size.toULong()) }

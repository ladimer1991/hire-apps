package com.example.hire

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.*
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.darwin.NSObject
import org.jetbrains.skia.Image
import platform.posix.memcpy

private const val MAX_IMAGE_DIMENSION = 1280.0
private val TARGET_MAX_BYTES = 450UL * 1024UL
private const val INITIAL_JPEG_QUALITY = 0.82
private const val MIN_JPEG_QUALITY = 0.55

@Composable
actual fun rememberImagePickerLauncher(onImagePicked: (String) -> Unit): () -> Unit {
    val delegate = remember {
        object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, null)
                val result = didFinishPicking.firstOrNull() as? PHPickerResult ?: return
                val itemProvider = result.itemProvider
                
                if (itemProvider.hasItemConformingToTypeIdentifier("public.image")) {
                    itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data, _ ->
                        if (data != null) {
                            val image = UIImage.imageWithData(data)
                            if (image != null) {
                                val resized = resizeImageIfNeeded(image)
                                val jpegData = compressImageForUpload(resized)
                                val base64String = jpegData?.base64EncodedStringWithOptions(0u)
                                if (base64String != null) {
                                    onImagePicked(base64String)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    return {
        val config = PHPickerConfiguration()
        config.filter = PHPickerFilter.imagesFilter
        config.selectionLimit = 1
        val picker = PHPickerViewController(config)
        picker.delegate = delegate
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(picker, true, null)
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun decodeBase64ToBitmap(base64String: String): ImageBitmap? {
    return try {
        val nsData = NSData.create(base64EncodedString = base64String, options = 0u) ?: return null
        val bytes = ByteArray(nsData.length.toInt())
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
        }
        Image.makeFromEncoded(bytes).toComposeImageBitmap()
    } catch (_: Exception) {
        null
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun resizeImageIfNeeded(image: UIImage): UIImage {
    val size = image.size.useContents { Pair(width, height) }
    val width = size.first
    val height = size.second
    val maxSide = maxOf(width, height)
    if (maxSide <= MAX_IMAGE_DIMENSION) return image

    val scale = MAX_IMAGE_DIMENSION / maxSide
    val targetWidth = width * scale
    val targetHeight = height * scale

    UIGraphicsBeginImageContextWithOptions(CGSizeMake(targetWidth, targetHeight), false, 1.0)
    image.drawInRect(CGRectMake(0.0, 0.0, targetWidth, targetHeight))
    val resized = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()

    return resized ?: image
}

private fun compressImageForUpload(image: UIImage): NSData? {
    var quality = INITIAL_JPEG_QUALITY
    var jpegData = UIImageJPEGRepresentation(image, quality)

    while (jpegData != null && jpegData.length > TARGET_MAX_BYTES && quality > MIN_JPEG_QUALITY) {
        quality -= 0.07
        jpegData = UIImageJPEGRepresentation(image, quality)
    }

    return jpegData
}


package com.example.hire

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.*
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.darwin.NSObject
import org.jetbrains.skia.Image
import platform.posix.memcpy

@Composable
actual fun rememberImagePickerLauncher(onImagePicked: (String) -> Unit): () -> Unit {
    val delegate = remember {
        object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, null)
                val result = didFinishPicking.firstOrNull() as? PHPickerResult ?: return
                val itemProvider = result.itemProvider
                
                if (itemProvider.hasItemConformingToTypeIdentifier("public.image")) {
                    itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data, error ->
                        if (data != null) {
                            val image = UIImage.imageWithData(data)
                            if (image != null) {
                                val jpegData = UIImageJPEGRepresentation(image, 0.7)
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
    } catch (e: Exception) {
        null
    }
}

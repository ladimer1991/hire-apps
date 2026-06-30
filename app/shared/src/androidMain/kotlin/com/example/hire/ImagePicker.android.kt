package com.example.hire

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream

private const val MAX_IMAGE_DIMENSION = 1280
private const val TARGET_MAX_BYTES = 450 * 1024
private const val INITIAL_JPEG_QUALITY = 82
private const val MIN_JPEG_QUALITY = 55

@Composable
actual fun rememberImagePickerLauncher(onImagePicked: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.use { input ->
                compressForUpload(input.readBytes())
            }
            if (bytes != null) {
                val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
                onImagePicked(base64String)
            }
        }
    }

    return { launcher.launch("image/*") }
}

actual fun decodeBase64ToBitmap(base64String: String): ImageBitmap? {
    return try {
        val byteArray = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size).asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

private fun compressForUpload(rawBytes: ByteArray): ByteArray {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, bounds)

    val sampledOptions = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
    }

    val sampledBitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, sampledOptions)
        ?: return rawBytes

    val resizedBitmap = resizeBitmapIfNeeded(sampledBitmap)
    if (resizedBitmap !== sampledBitmap) {
        sampledBitmap.recycle()
    }

    val output = ByteArrayOutputStream()
    var quality = INITIAL_JPEG_QUALITY
    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)

    while (output.size() > TARGET_MAX_BYTES && quality > MIN_JPEG_QUALITY) {
        output.reset()
        quality -= 7
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
    }

    val compressed = output.toByteArray()
    resizedBitmap.recycle()
    return compressed
}

private fun calculateInSampleSize(width: Int, height: Int): Int {
    var sample = 1
    var halfWidth = width / 2
    var halfHeight = height / 2
    while (halfWidth / sample >= MAX_IMAGE_DIMENSION && halfHeight / sample >= MAX_IMAGE_DIMENSION) {
        sample *= 2
    }
    return sample.coerceAtLeast(1)
}

private fun resizeBitmapIfNeeded(source: Bitmap): Bitmap {
    val width = source.width
    val height = source.height
    val maxSide = maxOf(width, height)
    if (maxSide <= MAX_IMAGE_DIMENSION) return source

    val scale = MAX_IMAGE_DIMENSION.toFloat() / maxSide.toFloat()
    val targetWidth = (width * scale).toInt().coerceAtLeast(1)
    val targetHeight = (height * scale).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
}


package com.example.hire

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

@Composable
actual fun rememberImagePickerLauncher(onImagePicked: (String) -> Unit): () -> Unit {
    return { }
}

actual fun decodeBase64ToBitmap(base64String: String): ImageBitmap? {
    return null
}

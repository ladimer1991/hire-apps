package com.example.hire

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

@Composable
expect fun rememberImagePickerLauncher(onImagePicked: (String) -> Unit): () -> Unit

expect fun decodeBase64ToBitmap(base64String: String): ImageBitmap?

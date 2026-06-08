package com.example.hire

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun VideoBackground() {
    // iOS implementation note:
    // Add entry_background.mp4 to your Xcode project target
    // The video will be loaded from the app bundle

    // For now, we provide a black background
    // The video playback will be integrated in future updates
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    )
}



package com.example.hire

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun VideoBackground() {
    // JVM Desktop implementation - uses black background
    // Desktop video playback can be implemented using JavaFX or other libraries if needed
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    )
}


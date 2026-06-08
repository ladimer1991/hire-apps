package com.example.hire

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun VideoBackground() {
    // Web implementation - uses HTML5 video element
    // The video path should be: resources/entry_background.mp4
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // HTML5 video will be injected via JavaScript in the HTML template
        // See index.html for the actual video element
    }
}


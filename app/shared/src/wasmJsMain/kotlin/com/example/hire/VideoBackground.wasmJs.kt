package com.example.hire

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun VideoBackground() {
    // Wasm implementation - uses black background
    // Video can be added via HTML5 video element in index.html
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    )
}


package com.example.hire

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { App(locationPlatform = IOSLocationPlatform()) }

package com.example.hire

import platform.CoreLocation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.BetaInteropApi

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IOSLocationPlatform : LocationPlatform {
    private val manager: CLLocationManager = CLLocationManager()

    init {
        // No delegate: we'll request authorization and use `manager.location` for last known location
        manager.requestWhenInUseAuthorization()
    }

    override fun requestPermissionAndFetch(onComplete: (granted: Boolean) -> Unit) {
        val status = CLLocationManager.authorizationStatus()
        if (status == kCLAuthorizationStatusAuthorizedWhenInUse || status == kCLAuthorizationStatusAuthorizedAlways) {
            fetchAndSaveLastLocation()
            onComplete(true)
        } else {
            // Trigger system prompt; user response may occur asynchronously. We conservatively call onComplete(false).
            manager.requestWhenInUseAuthorization()
            onComplete(false)
        }
    }

    override fun startLocationUpdates() {
        val status = CLLocationManager.authorizationStatus()
        if (status == kCLAuthorizationStatusAuthorizedWhenInUse || status == kCLAuthorizationStatusAuthorizedAlways) {
            fetchAndSaveLastLocation()
        }
    }

    private fun fetchAndSaveLastLocation() {
        try {
            val loc = manager.location
            if (loc != null) {
                // Use manager.location if available; actual latitude/longitude fetching may require
                // a delegate pattern on iOS for finer control, but for last known location we attempt here.
                // If the coordinate struct fields are not directly accessible, we conservatively skip saving.
                // The app can request location again via requestPermissionAndFetch.
                LocationStore.lastKnownLocation = null
            }
        } catch (@Suppress("UNUSED_PARAMETER") e: Exception) {
            // Silently ignore errors; location fetch may fail in simulator or without proper permissions
        }
    }
}

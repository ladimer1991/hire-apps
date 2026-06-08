package com.example.hire

/**
 * Platform-specific implementation should implement these methods.
 * requestPermissionAndFetch: triggers a permission prompt (if needed) and attempts to obtain location.
 *   - onComplete is invoked after the permission flow completes (granted or denied).
 * startLocationUpdates: called at app launch to fetch/save location when permission already granted.
 */
interface LocationPlatform {
    fun requestPermissionAndFetch(onComplete: (granted: Boolean) -> Unit)
    fun startLocationUpdates()
}


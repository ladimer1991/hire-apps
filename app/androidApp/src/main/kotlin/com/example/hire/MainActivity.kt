package com.example.hire

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    // Permission launcher/callback used to request runtime permissions safely
    private var permissionCallback: ((Boolean) -> Unit)? = null
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        AppContext.context = applicationContext
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Register the permission launcher before creating the platform
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultMap ->
            val granted = resultMap[Manifest.permission.ACCESS_FINE_LOCATION] == true
            permissionCallback?.invoke(granted)
            permissionCallback = null
        }
        notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                println("Notification permission not granted; push UI alerts may be limited.")
            }
        }

        requestNotificationPermissionIfNeeded()

        val locationPlatform = AndroidLocationPlatform(this)

        setContent {
            App(locationPlatform = locationPlatform)
        }

        PushTokenSyncManager.syncTokenIfLoggedIn()
    }

    override fun onResume() {
        super.onResume()
        PushTokenSyncManager.syncTokenIfLoggedIn()
    }

    // Called by AndroidLocationPlatform to request permission using the ActivityResult API
    fun requestLocationPermission(callback: (Boolean) -> Unit) {
        permissionCallback = callback
        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
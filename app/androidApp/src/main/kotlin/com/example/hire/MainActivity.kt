package com.example.hire

import android.os.Bundle
import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    // Permission launcher/callback used to request runtime permissions safely
    private var permissionCallback: ((Boolean) -> Unit)? = null
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

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

        val locationPlatform = AndroidLocationPlatform(this)

        setContent {
            App(locationPlatform = locationPlatform)
        }
    }

    // Called by AndroidLocationPlatform to request permission using the ActivityResult API
    fun requestLocationPermission(callback: (Boolean) -> Unit) {
        permissionCallback = callback
        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
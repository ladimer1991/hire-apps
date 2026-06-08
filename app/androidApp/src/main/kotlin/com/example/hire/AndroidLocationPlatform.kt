@file:Suppress("MissingPermission", "UNUSED_PARAMETER")
package com.example.hire

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private const val REQUEST_LOCATION_PERMISSION = 1001

class AndroidLocationPlatform(private val activity: ComponentActivity) : LocationPlatform {

    // A temporary callback used to deliver permission result
    private var permissionCallback: ((Boolean) -> Unit)? = null

    override fun requestPermissionAndFetch(onComplete: (granted: Boolean) -> Unit) {
        val fine = Manifest.permission.ACCESS_FINE_LOCATION
        val hasFine = ContextCompat.checkSelfPermission(activity, fine) == PackageManager.PERMISSION_GRANTED

        if (hasFine) {
            // permission already granted
            fetchAndSaveLastLocation()
            onComplete(true)
            return
        }

        // Save callback and request permission via Activity
        permissionCallback = { granted ->
            if (granted) fetchAndSaveLastLocation()
            onComplete(granted)
        }

        // If the host Activity exposes a helper to request permissions using the Activity Result API,
        // call it and deliver the result to our callback. Otherwise fall back to requestPermissions.
        if (activity is MainActivity) {
            (activity as MainActivity).requestLocationPermission { granted ->
                permissionCallback?.invoke(granted)
                permissionCallback = null
            }
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(fine), REQUEST_LOCATION_PERMISSION)
        }
    }

    override fun startLocationUpdates() {
        val fine = Manifest.permission.ACCESS_FINE_LOCATION
        val hasFine = ContextCompat.checkSelfPermission(activity, fine) == PackageManager.PERMISSION_GRANTED
        if (hasFine) {
            // fetch current location on app start
            fetchAndSaveLastLocation()
        }
    }

    @Suppress("MissingPermission", "UNUSED_PARAMETER")
    private fun fetchAndSaveLastLocation() {
        try {
            // ensure permission still present
            val fine = Manifest.permission.ACCESS_FINE_LOCATION
            val hasFine = ContextCompat.checkSelfPermission(activity, fine) == PackageManager.PERMISSION_GRANTED
            if (!hasFine) return
            val lm = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            @Suppress("MissingPermission")
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            var loc: Location? = null
            for (p in providers) {
                val l = lm.getLastKnownLocation(p)
                if (l != null) {
                    loc = l
                    break
                }
            }
            loc?.let {
                LocationStore.lastKnownLocation = LocationData(it.latitude, it.longitude)
            }
        } catch (e: Exception) {
            // ignore failures; location won't be available
        }
    }
}


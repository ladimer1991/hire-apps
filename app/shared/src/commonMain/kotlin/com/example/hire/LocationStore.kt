package com.example.hire

data class LocationData(val latitude: Double, val longitude: Double)

object LocationStore {
    // Last known location (updated when permission granted and location obtained)
    // Named `lastKnownLocation` so it's clear this is the most-recent cached location
    var lastKnownLocation: LocationData? = null
}

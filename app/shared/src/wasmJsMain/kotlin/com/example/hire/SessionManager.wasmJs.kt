package com.example.hire

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings

actual fun createSecureSettings(): Settings {
    return StorageSettings()
}


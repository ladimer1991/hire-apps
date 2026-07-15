package com.example.hire

import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.ExperimentalSettingsImplementation

@OptIn(ExperimentalSettingsImplementation::class)
actual fun createSecureSettings(): Settings {
    return KeychainSettings("HireApp_Keychain")
}


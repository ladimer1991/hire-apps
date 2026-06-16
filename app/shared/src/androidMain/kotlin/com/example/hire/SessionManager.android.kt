package com.example.hire

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

actual fun createSecureSettings(): Settings {
    val context = AppContext.context ?: throw IllegalStateException("AppContext not initialized")
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    val fileName = "secret_hire_prefs"

    return try {
        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            fileName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        SharedPreferencesSettings(sharedPreferences)
    } catch (e: Exception) {
        println("Error creating EncryptedSharedPreferences: ${e.message}. Clearing and retrying.")
        try {
            // Attempt to clear the corrupted preferences file
            context.getSharedPreferences(fileName, 0).edit().clear().apply()
            
            val sharedPreferences = EncryptedSharedPreferences.create(
                context,
                fileName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            SharedPreferencesSettings(sharedPreferences)
        } catch (retryException: Exception) {
            println("Failed to recover from EncryptedSharedPreferences error: ${retryException.message}")
            // Fallback to non-encrypted if even retry fails (last resort)
            SharedPreferencesSettings(context.getSharedPreferences(fileName, 0))
        }
    }
}

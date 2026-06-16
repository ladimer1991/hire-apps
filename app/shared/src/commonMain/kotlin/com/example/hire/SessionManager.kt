package com.example.hire

import com.russhwolf.settings.Settings

expect fun createSecureSettings(): Settings

class SessionManager {
    private val settings: Settings = createSecureSettings()

    fun saveToken(token: String) {
        try {
            settings.putString("jwt_token", token)
        } catch (e: Exception) {
            println("Error saving token: ${e.message}")
        }
    }

    fun getToken(): String? {
        return try {
            settings.getStringOrNull("jwt_token")
        } catch (e: Exception) {
            println("Error retrieving token: ${e.message}")
            null
        }
    }

    fun clearSession() {
        try {
            settings.remove("jwt_token")
        } catch (e: Exception) {
            println("Error clearing session: ${e.message}")
        }
    }
}

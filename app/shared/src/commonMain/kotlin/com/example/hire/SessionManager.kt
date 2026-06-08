package com.example.hire

import com.russhwolf.settings.Settings

expect fun createSecureSettings(): Settings

class SessionManager {
    private val settings: Settings = createSecureSettings()

    fun saveToken(token: String) {
        settings.putString("jwt_token", token)
    }

    fun getToken(): String? {
        return settings.getStringOrNull("jwt_token")
    }

    fun clearSession() {
        settings.remove("jwt_token")
    }
}

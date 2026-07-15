package com.example.hire

class PushTokenStore {
    private val settings = createSecureSettings()

    fun saveToken(token: String) {
        try {
            settings.putString(KEY_FCM_TOKEN, token)
        } catch (e: Exception) {
            println("Error saving push token locally: ${e.message}")
        }
    }

    fun getToken(): String? {
        return try {
            settings.getStringOrNull(KEY_FCM_TOKEN)
        } catch (e: Exception) {
            println("Error reading push token locally: ${e.message}")
            null
        }
    }

    fun clearToken() {
        try {
            settings.remove(KEY_FCM_TOKEN)
        } catch (e: Exception) {
            println("Error clearing push token locally: ${e.message}")
        }
    }

    companion object {
        private const val KEY_FCM_TOKEN = "fcm_device_token"
    }
}


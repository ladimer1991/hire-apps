package com.example.hire

import com.google.firebase.messaging.FirebaseMessaging

object PushTokenSyncManager {
    fun syncTokenIfLoggedIn() {
        val sessionManager = SessionManager()
        val jwt = sessionManager.getToken()
        if (jwt.isNullOrBlank()) return

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                PushTokenSyncCoordinator.handleNativeToken(token)
            }
            .addOnFailureListener {
                println("FCM token fetch failed: ${it.message}")
            }
    }

    suspend fun syncTokenWithBackend(token: String?) {
        PushTokenSyncCoordinator.handleNativeToken(token)
    }
}

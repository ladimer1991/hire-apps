package com.example.hire

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PushTokenSyncManager {
    fun syncTokenIfLoggedIn() {
        val sessionManager = SessionManager()
        val jwt = sessionManager.getToken()
        if (jwt.isNullOrBlank()) return

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                PushTokenStore().saveToken(token)
                CoroutineScope(Dispatchers.IO).launch {
                    syncTokenWithBackend(token)
                }
            }
            .addOnFailureListener {
                println("FCM token fetch failed: ${it.message}")
            }
    }

    suspend fun syncTokenWithBackend(token: String?) {
        if (token.isNullOrBlank()) return

        PushTokenStore().saveToken(token)

        val sessionManager = SessionManager()
        val jwt = sessionManager.getToken()
        if (jwt.isNullOrBlank()) return

        AuthApiService(sessionManager = sessionManager)
            .updatePushToken(token)
            .onFailure {
                println("FCM token sync failed: ${it.message}")
            }
    }
}



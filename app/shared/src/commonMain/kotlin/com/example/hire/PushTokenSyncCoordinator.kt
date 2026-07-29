package com.example.hire

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PushTokenSyncCoordinator {
    fun syncTokenIfLoggedIn() {
        val sessionManager = SessionManager()
        val jwt = sessionManager.getToken()
        if (jwt.isNullOrBlank()) return

        val storedToken = PushTokenStore().getToken()
        if (storedToken.isNullOrBlank()) return

        CoroutineScope(Dispatchers.Default).launch {
            AuthApiService(sessionManager = sessionManager)
                .updatePushToken(storedToken)
                .onFailure {
                    println("FCM token sync failed: ${it.message}")
                }
        }
    }

    fun handleNativeToken(token: String?) {
        if (token.isNullOrBlank()) return

        PushTokenStore().saveToken(token)

        val sessionManager = SessionManager()
        val jwt = sessionManager.getToken()
        if (jwt.isNullOrBlank()) return

        CoroutineScope(Dispatchers.Default).launch {
            AuthApiService(sessionManager = sessionManager)
                .updatePushToken(token)
                .onFailure {
                    println("FCM token sync failed: ${it.message}")
                }
        }
    }
}


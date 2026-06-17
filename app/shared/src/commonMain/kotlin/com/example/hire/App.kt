package com.example.hire

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

@Composable
@Preview
fun App(locationPlatform: LocationPlatform? = null) {
    MaterialTheme {
        val appState = remember { mutableStateOf(AppState()) }
        val sessionManager = remember { SessionManager() }
        val apiService = remember { AuthApiService(sessionManager = sessionManager) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            locationPlatform?.startLocationUpdates()

            val token = sessionManager.getToken()
            if (token != null) {
                apiService.getCurrentUser().onSuccess { user ->
                    appState.value = appState.value.copy(
                        currentScreen = Screen.BROWSE,
                        currentUserId = user.id ?: user.email,
                        currentUserName = user.username,
                        currentUserImage = user.images.firstOrNull()
                    )
                }.onFailure {
                    // Token expired or invalid, clear it
                    sessionManager.clearSession()
                }
            }
        }

        when (appState.value.currentScreen) {
            Screen.LOADING -> {
                EntryPage(
                    onLoginClick = {
                        appState.value = appState.value.copy(currentScreen = Screen.LOGIN)
                    },
                    onSignUpClick = {
                        appState.value = appState.value.copy(currentScreen = Screen.REGISTRATION)
                    },
                    onBrowseClick = {
                        appState.value = appState.value.copy(currentScreen = Screen.BROWSE)
                    }
                )
            }
            Screen.ENTRY -> {
                EntryPage(
                    onLoginClick = {
                        appState.value = appState.value.copy(currentScreen = Screen.LOGIN)
                    },
                    onSignUpClick = {
                        appState.value = appState.value.copy(currentScreen = Screen.REGISTRATION)
                    },
                    onBrowseClick = {
                        appState.value = appState.value.copy(currentScreen = Screen.BROWSE)
                    }
                )
            }
            Screen.LOGIN -> {
                LoginPage(
                    onBackClick = {
                        appState.value = appState.value.copy(currentScreen = Screen.ENTRY)
                    },
                    onSuccessClick = { userId ->
                        locationPlatform?.requestPermissionAndFetch { _ ->
                            coroutineScope.launch {
                                val user = apiService.getCurrentUser().getOrNull()
                                appState.value = appState.value.copy(
                                    currentScreen = Screen.BROWSE,
                                    currentUserId = userId,
                                    currentUserName = user?.username,
                                    currentUserImage = user?.images?.firstOrNull()
                                )
                            }
                        } ?: run {
                            coroutineScope.launch {
                                val user = apiService.getCurrentUser().getOrNull()
                                appState.value = appState.value.copy(
                                    currentScreen = Screen.BROWSE,
                                    currentUserId = userId,
                                    currentUserName = user?.username,
                                    currentUserImage = user?.images?.firstOrNull()
                                )
                            }
                        }
                    }
                )
            }
            Screen.REGISTRATION -> {
                RegistrationPage(
                    onBackClick = {
                        appState.value = appState.value.copy(currentScreen = Screen.ENTRY)
                    },
                    onSuccessClick = {
                        // After registration, update currentUserId and navigate
                        coroutineScope.launch {
                            apiService.getCurrentUser().onSuccess { user ->
                                appState.value = appState.value.copy(
                                    currentScreen = Screen.BROWSE,
                                    currentUserId = user.id ?: user.email,
                                    currentUserName = user.username,
                                    currentUserImage = user.images.firstOrNull()
                                )
                            }.onFailure {
                                appState.value = appState.value.copy(currentScreen = Screen.BROWSE)
                            }
                        }
                    }
                )
            }
            Screen.BROWSE -> {
                HomePage(
                    selectedTab = appState.value.selectedHomePageTab,
                    currentUserId = appState.value.currentUserId,
                    currentUserName = appState.value.currentUserName,
                    currentUserImage = appState.value.currentUserImage,
                    onTabChanged = { newTab ->
                        appState.value = appState.value.copy(selectedHomePageTab = newTab)
                    },
                    onBackClick = {
                        sessionManager.clearSession()
                        appState.value = AppState()
                    },
                    onConversationClick = { userId, userName ->
                        appState.value = appState.value.copy(
                            currentScreen = Screen.CHAT,
                            chatPartnerId = userId,
                            chatPartnerName = userName
                        )
                    },
                    onUserClick = { user ->
                        appState.value = appState.value.copy(
                            currentScreen = Screen.USER_DETAILS,
                            selectedUser = user
                        )
                    },
                    onEditProfileClick = {
                        appState.value = appState.value.copy(currentScreen = Screen.EDIT_PROFILE)
                    }
                )
            }
            Screen.EDIT_PROFILE -> {
                EditProfilePage(
                    onBack = {
                        coroutineScope.launch {
                            val user = apiService.getCurrentUser().getOrNull()
                            appState.value = appState.value.copy(
                                currentScreen = Screen.BROWSE,
                                selectedHomePageTab = 3,
                                currentUserName = user?.username,
                                currentUserImage = user?.images?.firstOrNull()
                            )
                        }
                    }
                )
            }
            Screen.CHAT -> {
                ChatDetailScreen(
                    currentUserId = appState.value.currentUserId ?: "",
                    chatPartnerId = appState.value.chatPartnerId ?: "",
                    chatPartnerName = appState.value.chatPartnerName ?: "Unknown",
                    onBackClick = {
                        appState.value = appState.value.copy(currentScreen = Screen.BROWSE, selectedHomePageTab = 1)
                    }
                )
            }
            Screen.USER_DETAILS -> {
                val user = appState.value.selectedUser
                if (user != null) {
                    UserDetailsPage(
                        user = user,
                        onBackClick = {
                            appState.value = appState.value.copy(currentScreen = Screen.BROWSE)
                        },
                        onMessageClick = { userId, userName ->
                            appState.value = appState.value.copy(
                                currentScreen = Screen.CHAT,
                                chatPartnerId = userId,
                                chatPartnerName = userName
                            )
                        }
                    )
                }
            }
        }
    }
}

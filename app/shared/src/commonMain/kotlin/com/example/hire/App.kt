package com.example.hire

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
        val appErrorMessage = remember { mutableStateOf<String?>(null) }

        ApiErrorDialogHost(
            errorMessage = appErrorMessage.value,
            title = "Network error",
            onDismissError = { appErrorMessage.value = null }
        )

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
                }.onFailure { error ->
                    //appErrorMessage.value = error.toFriendlyApiMessage("Unable to restore your session.") we don't wanna show this either.
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
                            }.onFailure { error ->
                                appErrorMessage.value = error.toFriendlyApiMessage("Logged in, but we could not load your profile.")
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
                        coroutineScope.launch {
                            apiService.logout()
                            appState.value = AppState(currentScreen = Screen.ENTRY)
                        }
                    },
                    onConversationClick = { userId, userName, userImage ->
                        appState.value = appState.value.copy(
                            currentScreen = Screen.CHAT,
                            chatPartnerId = userId,
                            chatPartnerName = userName,
                            chatPartnerImage = userImage
                        )
                    },
                    onUserClick = { user ->
                        appState.value = appState.value.copy(
                            currentScreen = Screen.USER_DETAILS,
                            selectedUser = user,
                            isOwnProfileDetails = false
                        )
                    },
                    onProfileImageClick = {
                        coroutineScope.launch {
                            apiService.getCurrentUser(forceRefresh = true).onSuccess { me ->
                                val ownProfileUser = BrowseUser(
                                    id = me.id ?: me.email,
                                    name = me.username,
                                    profession = me.providedService ?: "Professional",
                                    hourlyRate = me.hourlyRate,
                                    description = me.description,
                                    color = defaultColors.first(),
                                    base64Images = me.images,
                                    rating = me.rating,
                                    isSaved = false
                                )
                                appState.value = appState.value.copy(
                                    currentScreen = Screen.USER_DETAILS,
                                    selectedUser = ownProfileUser,
                                    isOwnProfileDetails = true
                                )
                            }.onFailure { error ->
                                appErrorMessage.value = error.toFriendlyApiMessage("Failed to load your profile details.")
                            }
                        }
                    },
                    onEditProfileClick = {
                        appState.value = appState.value.copy(currentScreen = Screen.EDIT_PROFILE)
                    },
                    onSavedProfilesClick = {
                        appState.value = appState.value.copy(currentScreen = Screen.SAVED_PROFILES)
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
                    chatPartnerImage = appState.value.chatPartnerImage,
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
                        isOwnProfile = appState.value.isOwnProfileDetails,
                        onBackClick = {
                            appState.value = appState.value.copy(currentScreen = Screen.BROWSE)
                        },
                        onMessageClick = { userId, userName ->
                            appState.value = appState.value.copy(
                                currentScreen = Screen.CHAT,
                                chatPartnerId = userId,
                                chatPartnerName = userName,
                                chatPartnerImage = user.base64Images.firstOrNull()
                            )
                        }
                    )
                }
            }
            Screen.SAVED_PROFILES -> {
                SavedProfilesPage(
                    onBackClick = {
                        appState.value = appState.value.copy(
                            currentScreen = Screen.BROWSE,
                            selectedHomePageTab = 3
                        )
                    },
                    onUserClick = { user ->
                        appState.value = appState.value.copy(
                            currentScreen = Screen.USER_DETAILS,
                            selectedUser = user,
                            isOwnProfileDetails = false
                        )
                    },
                    onConversationClick = { userId, userName, userImage ->
                        appState.value = appState.value.copy(
                            currentScreen = Screen.CHAT,
                            chatPartnerId = userId,
                            chatPartnerName = userName,
                            chatPartnerImage = userImage
                        )
                    }
                )
            }
        }
    }
}

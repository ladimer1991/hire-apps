package com.example.hire

enum class Screen {
    LOADING,
    ENTRY,
    LOGIN,
    REGISTRATION,
    BROWSE,
    CHAT,
    USER_DETAILS,
    EDIT_PROFILE
}

data class AppState(
    val currentScreen: Screen = Screen.LOADING,
    val selectedHomePageTab: Int = 0,
    val chatPartnerId: String? = null,
    val chatPartnerName: String? = null,
    val currentUserId: String? = null,
    val currentUserName: String? = null,
    val currentUserImage: String? = null,
    val selectedUser: BrowseUser? = null
)

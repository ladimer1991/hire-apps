package com.example.hire

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class BrowseViewModel(
    private val apiService: AuthApiService = AuthApiService()
) : ViewModel() {
    private val _users = mutableStateOf<List<BrowseUser>>(emptyList())
    val users: State<List<BrowseUser>> = _users

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private var hasLoadedOnce = false

    fun loadUsers(forceRefresh: Boolean = false) {
        if (hasLoadedOnce && !forceRefresh) return

        viewModelScope.launch {
            _isLoading.value = true
            val meResult = apiService.getCurrentUser().getOrNull()
            val myId = meResult?.id ?: meResult?.email ?: ""
            
            apiService.getAllUsers().onSuccess { fetchedUsers ->
                _users.value = fetchedUsers.filter { (it.id ?: it.email) != myId }.mapIndexed { index, user ->
                    BrowseUser(
                        id = user.id ?: user.email,
                        name = user.fullName.takeIf { it.isNotBlank() } ?: user.username,
                        profession = "Professional",
                        color = defaultColors[index % defaultColors.size],
                        base64Images = user.images
                    )
                }
                hasLoadedOnce = true
            }.onFailure { error ->
                _errorMessage.value = error.message
            }
            _isLoading.value = false
        }
    }
    
    fun loadMore() {
        // Pagination logic would go here
    }
}

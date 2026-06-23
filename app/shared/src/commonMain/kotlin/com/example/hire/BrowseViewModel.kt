package com.example.hire

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BrowseViewModel(
    private val apiService: AuthApiService = AuthApiService()
) : ViewModel() {
    private val _displayedUsers = mutableStateOf<List<BrowseUser>>(emptyList())
    val users: State<List<BrowseUser>> = _displayedUsers

    private var _internalUsers: List<BrowseUser> = emptyList()

    private val _isInternalLoading = mutableStateOf(false)
    private val _isSearchTriggered = mutableStateOf(false)

    private val _isLoadingVisible = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoadingVisible

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private var hasLoadedOnce = false
    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private var searchJob: Job? = null
    private var prefetchJob: Job? = null
    private val prefetchedUsersByQuery = mutableMapOf<String, List<BrowseUser>>()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        prefetchUsers(query)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun triggerSearch() {
        _errorMessage.value = null
        prefetchedUsersByQuery[cacheKey(_searchQuery.value)]?.let { cachedUsers ->
            // Keep existing click-to-search behavior, but instantly show prefetched data if available.
            _internalUsers = cachedUsers
            _displayedUsers.value = cachedUsers
            hasLoadedOnce = true
        }

        _isSearchTriggered.value = true
        _isLoadingVisible.value = true
        loadUsers(forceRefresh = true)
    }

    fun triggerRefresh() {
        _isSearchTriggered.value = true
        _isLoadingVisible.value = true
        loadUsers(forceRefresh = true)
    }

    fun loadUsers(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            loadUsersAwait(forceRefresh)
        }
    }

    suspend fun loadUsersAwait(forceRefresh: Boolean = false) {
        if (hasLoadedOnce && !forceRefresh) return

        searchJob?.cancel()
        searchJob = null
        try {
            val currentQuery = _searchQuery.value
            _isInternalLoading.value = true
            
            // Show loading if we triggered a search OR if it's the very first load
            if (_isSearchTriggered.value || !hasLoadedOnce) {
                _isLoadingVisible.value = true
            }

            fetchBrowseUsers(currentQuery).onSuccess { fetchedUsers ->
                _internalUsers = fetchedUsers
                prefetchedUsersByQuery[cacheKey(currentQuery)] = fetchedUsers

                // If search was triggered, or it's the initial load, update displayed users
                if (_isSearchTriggered.value || !hasLoadedOnce) {
                    _displayedUsers.value = _internalUsers
                    _isSearchTriggered.value = false
                    _isLoadingVisible.value = false
                }

                hasLoadedOnce = true
            }.onFailure { error ->
                if (error !is CancellationException) {
                    _errorMessage.value = error.message
                    _isLoadingVisible.value = false
                    _isSearchTriggered.value = false
                }
            }
            _isInternalLoading.value = false
            if (!_isSearchTriggered.value) {
                _isLoadingVisible.value = false
            }
        } finally {
            if (searchJob != null && searchJob?.isActive == false) {
                searchJob = null
            }
        }
    }

    private fun prefetchUsers(query: String) {
        val queryKey = cacheKey(query)
        if (prefetchedUsersByQuery.containsKey(queryKey)) return

        prefetchJob?.cancel()
        prefetchJob = viewModelScope.launch {
            // Debounce typing to avoid firing a request per keystroke.
            delay(250)
            fetchBrowseUsers(query).onSuccess { fetchedUsers ->
                prefetchedUsersByQuery[queryKey] = fetchedUsers
            }
        }
    }

    private suspend fun fetchBrowseUsers(query: String): Result<List<BrowseUser>> {
        val queryParam = query.takeIf { it.isNotBlank() }
        val usersResult = apiService.getAllUsers(queryParam)
        val meResult = apiService.getCurrentUser().getOrNull()
        val myId = meResult?.id ?: meResult?.email ?: ""

        return usersResult.map { fetchedUsers ->
            fetchedUsers
                .filter { (it.id ?: it.email) != myId }
                .mapIndexed { index, user ->
                    BrowseUser(
                        id = user.id ?: user.email,
                        name = user.username,
                        profession = user.providedService ?: "Professional",
                        hourlyRate = user.hourlyRate,
                        description = user.description,
                        color = defaultColors[index % defaultColors.size],
                        base64Images = user.images,
                        reviews = user.reviews
                    )
                }
        }
    }

    private fun cacheKey(query: String): String = query.trim()

    fun loadMore() {
        // Pagination logic would go here
    }
}

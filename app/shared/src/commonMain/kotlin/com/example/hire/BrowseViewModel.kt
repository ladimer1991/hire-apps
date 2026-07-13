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

    private val _firstVisibleItemIndex = mutableStateOf(0)
    val firstVisibleItemIndex: State<Int> = _firstVisibleItemIndex

    private val _firstVisibleItemScrollOffset = mutableStateOf(0)
    val firstVisibleItemScrollOffset: State<Int> = _firstVisibleItemScrollOffset

    private var searchJob: Job? = null
    private var prefetchJob: Job? = null
    private val prefetchedUsersByQuery = mutableMapOf<String, List<BrowseUser>>()
    private val prefetchErrorsByQuery = mutableMapOf<String, String>()
    private var latestPrefetchQueryKey: String? = null
    private var lastObservedSessionGeneration = AuthApiService.getLocalSessionGeneration()

    fun resetForNewSessionIfNeeded() {
        val currentGeneration = AuthApiService.getLocalSessionGeneration()
        if (currentGeneration == lastObservedSessionGeneration) return

        searchJob?.cancel()
        prefetchJob?.cancel()
        searchJob = null
        prefetchJob = null

        _displayedUsers.value = emptyList()
        _internalUsers = emptyList()
        _searchQuery.value = ""
        _errorMessage.value = null
        _isInternalLoading.value = false
        _isSearchTriggered.value = false
        _isLoadingVisible.value = false
        hasLoadedOnce = false
        _firstVisibleItemIndex.value = 0
        _firstVisibleItemScrollOffset.value = 0
        prefetchedUsersByQuery.clear()
        prefetchErrorsByQuery.clear()
        latestPrefetchQueryKey = null

        lastObservedSessionGeneration = currentGeneration
    }

    fun toggleSavedUser(savedUserId: String) {
        viewModelScope.launch {
            apiService.getCurrentUser(forceRefresh = true)
                .onSuccess { me ->
                    val existingSavedUsers = me.savedUsers.orEmpty()
                    val isCurrentlySaved = existingSavedUsers.contains(savedUserId)
                    val updatedSavedUsers = if (isCurrentlySaved) {
                        existingSavedUsers.filterNot { it == savedUserId }
                    } else {
                        existingSavedUsers + savedUserId
                    }
                    apiService.updateProfile(me.copy(savedUsers = updatedSavedUsers))
                        .onSuccess {
                            setUserSavedState(savedUserId, !isCurrentlySaved)
                        }
                        .onFailure { error ->
                            _errorMessage.value = error.message ?: "Failed to update saved user"
                        }
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load profile"
                }
        }
    }

    fun updateSearchQuery(query: String) {
        resetForNewSessionIfNeeded()
        _searchQuery.value = query
        prefetchUsers(query)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun updateScrollPosition(index: Int, offset: Int) {
        _firstVisibleItemIndex.value = index
        _firstVisibleItemScrollOffset.value = offset
    }

    fun triggerSearch() {
        resetForNewSessionIfNeeded()
        viewModelScope.launch {
            _errorMessage.value = null
            _isLoadingVisible.value = true

            val clickedQuery = _searchQuery.value
            val queryKey = cacheKey(clickedQuery)
            val pendingPrefetch = if (latestPrefetchQueryKey == queryKey) prefetchJob else null

            pendingPrefetch?.join()

            prefetchedUsersByQuery[queryKey]?.let { cachedUsers ->
                _internalUsers = cachedUsers
                _displayedUsers.value = cachedUsers
                hasLoadedOnce = true
                _isLoadingVisible.value = false
                return@launch
            }

            prefetchErrorsByQuery[queryKey]?.let { message ->
                _errorMessage.value = message
            }
            _isLoadingVisible.value = false
        }
    }

    fun triggerRefresh() {
        resetForNewSessionIfNeeded()
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
        resetForNewSessionIfNeeded()
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

        latestPrefetchQueryKey = queryKey

        prefetchJob?.cancel()
        prefetchJob = viewModelScope.launch {
            // Debounce typing to avoid firing a request per keystroke.
            delay(250)
            fetchBrowseUsers(query).onSuccess { fetchedUsers ->
                prefetchedUsersByQuery[queryKey] = fetchedUsers
                prefetchErrorsByQuery.remove(queryKey)
            }.onFailure { error ->
                if (error !is CancellationException) {
                    prefetchErrorsByQuery[queryKey] = error.message ?: "Failed to load search results"
                }
            }
        }
    }

    private suspend fun fetchBrowseUsers(query: String): Result<List<BrowseUser>> {
        val queryParam = query.takeIf { it.isNotBlank() }
        val usersResult = apiService.getAllUsers(queryParam)
        val meResult = apiService.getCurrentUser().getOrNull()
        val myId = meResult?.id ?: meResult?.email ?: ""
        val savedUserIds = meResult?.savedUsers.orEmpty().toSet()

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
                        rating = user.rating,
                        isSaved = savedUserIds.contains(user.id ?: user.email)
                    )
                }
        }
    }

    private fun setUserSavedState(savedUserId: String, isSaved: Boolean) {
        _internalUsers = _internalUsers.map { browseUser ->
            if (browseUser.id == savedUserId) browseUser.copy(isSaved = isSaved) else browseUser
        }
        _displayedUsers.value = _internalUsers

        prefetchedUsersByQuery.keys.toList().forEach { key ->
            prefetchedUsersByQuery[key]?.let { cachedUsers ->
                prefetchedUsersByQuery[key] = cachedUsers.map { browseUser ->
                    if (browseUser.id == savedUserId) browseUser.copy(isSaved = isSaved) else browseUser
                }
            }
        }
    }

    private fun cacheKey(query: String): String = query.trim()

    fun loadMore() {
        // Pagination logic would go here
    }
}

package com.example.hire

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

class SavedProfilesViewModel(
    private val apiService: AuthApiService = AuthApiService()
) : ViewModel() {
    private val _users = mutableStateOf<List<BrowseUser>>(emptyList())
    val users: State<List<BrowseUser>> = _users

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private var allSavedUsers: List<BrowseUser> = emptyList()

    fun loadSavedProfiles(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            apiService.getCurrentUser(forceRefresh = forceRefresh)
                .onSuccess { me ->
                    val savedIds = me.savedUsers.orEmpty().distinct()
                    if (savedIds.isEmpty()) {
                        allSavedUsers = emptyList()
                        _users.value = emptyList()
                        _isLoading.value = false
                        return@onSuccess
                    }

                    apiService.getUsersByIds(savedIds)
                        .onSuccess { fetchedUsers ->
                            val meId = me.id ?: me.email
                            val usersById = fetchedUsers
                                .filter { (it.id ?: it.email) != meId }
                                .associateBy { it.id ?: it.email }

                            allSavedUsers = savedIds.mapNotNull { savedId ->
                                usersById[savedId]
                            }.mapIndexed { index, savedUser ->
                                BrowseUser(
                                    id = savedUser.id ?: savedUser.email,
                                    name = savedUser.username,
                                    profession = savedUser.providedService ?: "Professional",
                                    hourlyRate = savedUser.hourlyRate,
                                    description = savedUser.description,
                                    color = defaultColors[index % defaultColors.size],
                                    base64Images = savedUser.images,
                                    rating = savedUser.rating,
                                    isSaved = true
                                )
                            }

                            applySearchFilter(_searchQuery.value)
                        }
                        .onFailure { error ->
                            _errorMessage.value = error.toFriendlyApiMessage("Failed to load saved profiles.")
                        }
                }
                .onFailure { error ->
                    _errorMessage.value = error.toFriendlyApiMessage("Failed to load profile.")
                }

            _isLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applySearchFilter(query)
    }

    fun toggleSavedUser(userId: String) {
        viewModelScope.launch {
            apiService.getCurrentUser(forceRefresh = true)
                .onSuccess { me ->
                    val updatedSavedUsers = me.savedUsers.orEmpty().filterNot { it == userId }
                    apiService.updateProfile(me.copy(savedUsers = updatedSavedUsers))
                        .onSuccess {
                            allSavedUsers = allSavedUsers.filterNot { it.id == userId }
                            applySearchFilter(_searchQuery.value)
                        }
                        .onFailure { error ->
                            _errorMessage.value = error.toFriendlyApiMessage("Failed to update saved profiles.")
                        }
                }
                .onFailure { error ->
                    _errorMessage.value = error.toFriendlyApiMessage("Failed to load profile.")
                }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun applySearchFilter(query: String) {
        val normalized = query.trim().lowercase()
        _users.value = if (normalized.isBlank()) {
            allSavedUsers
        } else {
            allSavedUsers.filter { it.profession.lowercase().contains(normalized) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedProfilesPage(
    onBackClick: () -> Unit,
    onUserClick: (BrowseUser) -> Unit,
    onConversationClick: (String, String, String?) -> Unit,
    viewModel: SavedProfilesViewModel = viewModel { SavedProfilesViewModel() }
) {
    val users by viewModel.users
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val searchQuery by viewModel.searchQuery

    ApiErrorDialogHost(
        errorMessage = errorMessage,
        title = "Saved Profiles",
        onDismissError = { viewModel.clearError() }
    )

    LaunchedEffect(Unit) {
        viewModel.loadSavedProfiles()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Saved Profiles", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("← Back", color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 1.dp, vertical = 8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        placeholder = { Text("Search saved services...") },
                        singleLine = true
                    )
                }

                items(users) { user ->
                    UserCard(
                        user = user,
                        onUserClick = onUserClick,
                        onConversationClick = onConversationClick,
                        onSaveClick = { userId -> viewModel.toggleSavedUser(userId) }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (users.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No saved profiles found")
                }
            }
        }
    }
}

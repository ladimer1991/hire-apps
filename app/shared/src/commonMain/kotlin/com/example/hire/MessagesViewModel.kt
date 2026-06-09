package com.example.hire

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MessagesViewModel(
    private val apiService: AuthApiService = AuthApiService()
) : ViewModel() {
    private val _conversations = mutableStateOf<List<Conversation>>(emptyList())
    val conversations: State<List<Conversation>> = _conversations

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private var hasLoadedOnce = false

    fun loadConversations(forceRefresh: Boolean = false) {
        if (hasLoadedOnce && !forceRefresh) return

        viewModelScope.launch {
            _isLoading.value = true
            
            val meResult = apiService.getCurrentUser().getOrNull()
            val myId = meResult?.id ?: meResult?.email ?: ""
            val usersResult = apiService.getAllUsers().getOrNull() ?: emptyList()
            val historyResult = apiService.getHistory().getOrNull() ?: emptyList()

            val conversedUserIds = historyResult.flatMap { listOf(it.senderId, it.receiverId) }
                .filterNotNull()
                .filter { it != myId }
                .toSet()

            _conversations.value = usersResult
                .filter { it.id != myId && (it.id in conversedUserIds || it.email in conversedUserIds) }
                .mapIndexed { index, user ->
                    val userId = user.id ?: user.email
                    val msgsWithUser = historyResult.filter { 
                        (it.senderId == userId && it.receiverId == myId) || 
                        (it.receiverId == userId && it.senderId == myId) 
                    }
                    val lastMsg = msgsWithUser.maxByOrNull { it.timestamp }
                    
                    Conversation(
                        id = userId,
                        name = user.username,
                        lastMessage = lastMsg?.content ?: "",
                        timestamp = lastMsg?.timestamp?.toString() ?: "",
                        color = defaultColors[index % defaultColors.size],
                        base64Images = user.images
                    )
                }.filter { it.lastMessage.isNotBlank() }
                 .sortedByDescending { it.timestamp }

            hasLoadedOnce = true
            _isLoading.value = false
        }
    }
}

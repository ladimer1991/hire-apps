package com.example.hire

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MessagesViewModel(
    private val apiService: AuthApiService = AuthApiService()
) : ViewModel() {
    companion object {
        private var cachedConversations: List<Conversation> = emptyList()

        fun recordLocalSentMessage(
            otherUserId: String,
            otherUserName: String,
            messageContent: String,
            messageTimestamp: String? = null,
            otherUserImage: String? = null
        ) {
            val existing = cachedConversations.firstOrNull { it.id == otherUserId }
            val now = messageTimestamp ?: existing?.timestamp ?: "0"
            val updated = Conversation(
                id = otherUserId,
                name = otherUserName,
                lastMessage = messageContent,
                timestamp = now,
                color = existing?.color ?: defaultColors[kotlin.math.abs(otherUserId.hashCode()) % defaultColors.size],
                base64Images = existing?.base64Images?.takeIf { it.isNotEmpty() }
                    ?: otherUserImage?.let { listOf(it) }
                    ?: emptyList()
            )

            cachedConversations = (listOf(updated) + cachedConversations.filterNot { it.id == otherUserId })
        }

        private fun cacheConversations(conversations: List<Conversation>) {
            cachedConversations = conversations
        }

        private fun getCachedConversations(): List<Conversation> = cachedConversations
    }

    private val _conversations = mutableStateOf<List<Conversation>>(emptyList())
    val conversations: State<List<Conversation>> = _conversations

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private var hasLoadedOnce = false

    init {
        val cached = getCachedConversations()
        if (cached.isNotEmpty()) {
            _conversations.value = cached
            hasLoadedOnce = true
        }
    }

    fun loadConversations(forceRefresh: Boolean = false) {
        if (hasLoadedOnce && !forceRefresh) return

        viewModelScope.launch {
            _isLoading.value = true

            val meResult = apiService.getCurrentUser().getOrNull()
            val myId = meResult?.id ?: meResult?.email ?: ""
            val historyResult = apiService.getHistory().getOrNull() ?: emptyList()

            val conversedUserIds = historyResult.flatMap { listOf(it.senderId, it.receiverId) }
                .filterNotNull()
                .filter { it != myId }
                .toSet()

            val knownById = (getCachedConversations() + _conversations.value).associateBy { it.id }

            // Pull-to-refresh should hit message history only; enrich with user profiles only on non-forced load.
            val fetchedUsersById = if (forceRefresh) {
                emptyMap()
            } else {
                (apiService.getUsersByIds(conversedUserIds.toList()).getOrNull() ?: emptyList())
                    .flatMap { user ->
                        listOfNotNull(user.id, user.email).map { key -> key to user }
                    }
                    .toMap()
            }

            _conversations.value = conversedUserIds.mapNotNull { userId ->
                val msgsWithUser = historyResult.filter {
                    (it.senderId == userId && it.receiverId == myId) ||
                        (it.receiverId == userId && it.senderId == myId)
                }
                val lastMsg = msgsWithUser.maxByOrNull { it.timestamp } ?: return@mapNotNull null

                val profile = fetchedUsersById[userId]
                val cached = knownById[userId]

                Conversation(
                    id = userId,
                    name = profile?.username ?: cached?.name ?: userId,
                    lastMessage = lastMsg.content,
                    timestamp = lastMsg.timestamp?.toString() ?: "",
                    color = cached?.color ?: defaultColors[kotlin.math.abs(userId.hashCode()) % defaultColors.size],
                    base64Images = profile?.images ?: cached?.base64Images ?: emptyList()
                )
            }.sortedByDescending { it.timestamp }

            cacheConversations(_conversations.value)

            hasLoadedOnce = true
            _isLoading.value = false
        }
    }

    fun syncFromSharedCache() {
        val cached = getCachedConversations()
        if (cached.isNotEmpty()) {
            _conversations.value = cached
            hasLoadedOnce = true
        }
    }
}

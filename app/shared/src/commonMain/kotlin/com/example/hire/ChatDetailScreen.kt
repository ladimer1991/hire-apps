package com.example.hire

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    currentUserId: String,
    chatPartnerId: String,
    chatPartnerName: String,
    onBackClick: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { AuthApiService() }
    
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var actualUserId by remember { mutableStateOf(currentUserId) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ApiErrorDialogHost(
        errorMessage = errorMessage,
        title = "Chat failed",
        onDismissError = { errorMessage = null }
    )

    fun loadMessages() {
        coroutineScope.launch {
            apiService.getConversation(chatPartnerId).onSuccess {
                messages = it
            }.onFailure { error ->
                errorMessage = error.toFriendlyApiMessage("Failed to load messages.")
            }
            isLoading = false
        }
    }

    LaunchedEffect(chatPartnerId) {
        if (actualUserId.isBlank()) {
            apiService.getCurrentUser().onSuccess { me ->
                actualUserId = me.id ?: me.email
            }.onFailure { error ->
                errorMessage = error.toFriendlyApiMessage("Failed to load your user profile.")
            }
        }
        loadMessages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(chatPartnerName) },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message...") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val msg = Message(
                                receiverId = chatPartnerId,
                                content = inputText
                            )
                            coroutineScope.launch {
                                apiService.sendMessage(msg).onSuccess { sentMsg ->
                                    messages = messages + sentMsg
                                    inputText = ""
                                }.onFailure { error ->
                                    errorMessage = error.toFriendlyApiMessage("Failed to send message.")
                                }
                            }
                        }
                    }
                ) {
                    Text("Send")
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { msg ->
                    val isMine = msg.senderId == actualUserId
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isMine) Color(0xFF007AFF) else Color(0xFFE5E5EA),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = msg.content,
                                color = if (isMine) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

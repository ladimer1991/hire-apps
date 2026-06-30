package com.example.hire

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    currentUserId: String,
    chatPartnerId: String,
    chatPartnerName: String,
    chatPartnerImage: String? = null,
    onBackClick: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { AuthApiService() }
    val listState = rememberLazyListState()

    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var actualUserId by remember { mutableStateOf(currentUserId) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val partnerBitmap = remember(chatPartnerImage) {
        chatPartnerImage?.let { decodeBase64ToBitmap(it) }
    }

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

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5E5EA)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (partnerBitmap != null) {
                                Image(
                                    bitmap = partnerBitmap,
                                    contentDescription = chatPartnerName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = chatPartnerName.take(1).uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFF555555)
                                )
                            }
                        }
                        Text(
                            text = chatPartnerName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
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
                    .imePadding()
                    .navigationBarsPadding()
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
                                    MessagesViewModel.recordLocalSentMessage(
                                        otherUserId = chatPartnerId,
                                        otherUserName = chatPartnerName,
                                        messageContent = sentMsg.content,
                                        messageTimestamp = sentMsg.timestamp?.toString(),
                                        otherUserImage = chatPartnerImage
                                    )
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
                state = listState,
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

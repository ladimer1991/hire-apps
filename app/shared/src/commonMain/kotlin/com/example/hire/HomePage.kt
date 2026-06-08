package com.example.hire

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel

data class Conversation(
    val id: String,
    val name: String,
    val lastMessage: String,
    val timestamp: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    selectedTab: Int = 0,
    onTabChanged: (Int) -> Unit = {},
    onBackClick: () -> Unit = {},
    onConversationClick: (userId: String, userName: String) -> Unit = { _, _ -> },
    browseViewModel: BrowseViewModel = viewModel { BrowseViewModel() },
    messagesViewModel: MessagesViewModel = viewModel { MessagesViewModel() }
) {
    var currentTab by remember { mutableStateOf(selectedTab) }
    val tabs = listOf("Browse", "Messages", "Categories", "Saved")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Hire", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("Exit", color = Color(0xFF007AFF))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color(0xFF007AFF)
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = {
                            currentTab = index
                            onTabChanged(index)
                        },
                        label = { Text(title) },
                        icon = {
                            Text(
                                text = when(title) {
                                    "Browse" -> "🔍"
                                    "Messages" -> "💬"
                                    "Categories" -> "📁"
                                    "Saved" -> "🔖"
                                    else -> "•"
                                }
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF007AFF),
                            selectedTextColor = Color(0xFF007AFF),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFFE5F1FF)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when (currentTab) {
                0 -> BrowseTab(viewModel = browseViewModel, onConversationClick = onConversationClick)
                1 -> MessagesTab(viewModel = messagesViewModel, onConversationClick = onConversationClick)
                2 -> CategoriesTab()
                3 -> SavedTab()
            }
        }
    }
}

data class BrowseUser(
    val id: String,
    val name: String,
    val profession: String,
    val color: Color,
    val base64Images: List<String> = emptyList()
)

val defaultColors = listOf(
    Color(0xFFBBDEFB), Color(0xFFF8BBD0), Color(0xFFC8E6C9), 
    Color(0xFFFFF9C4), Color(0xFFD1C4E9), Color(0xFFFFE0B2), 
    Color(0xFFB2EBF2), Color(0xFFF0F4C3)
)

@Composable
fun BrowseTab(
    viewModel: BrowseViewModel,
    onConversationClick: (userId: String, userName: String) -> Unit
) {
    val users by viewModel.users
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    // Detect if we reached the end of the list
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !isLoading) {
            viewModel.loadMore()
        }
    }

    if (isLoading && users.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (errorMessage != null && users.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error loading users: $errorMessage", color = Color.Red)
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(users) { user ->
                UserCard(user, onConversationClick)
            }
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun UserCard(user: BrowseUser, onConversationClick: (String, String) -> Unit) {
    val pageCount = if (user.base64Images.isNotEmpty()) user.base64Images.size else 1
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { pageCount })

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(user.color),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.base64Images.isNotEmpty()) {
                            val base64Str = user.base64Images[page]
                            val bitmap = remember(base64Str) {
                                decodeBase64ToBitmap(base64Str)
                            }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = user.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = user.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.displayLarge,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            Text(
                                text = user.name.take(1).uppercase(),
                                style = MaterialTheme.typography.displayLarge,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            if (user.base64Images.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(user.base64Images.size) { index ->
                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(8.dp),
                            shape = CircleShape,
                            color = if (index == pagerState.currentPage) Color(0xFF007AFF) else Color.LightGray
                        ) {}
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.profession,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* Hire action */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                    ) {
                        Text("Hire", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { onConversationClick(user.id, user.name) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF007AFF))
                    ) {
                        Text("Message", fontSize = 13.sp, color = Color(0xFF007AFF), fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { /* Save action */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.Gray.copy(alpha = 0.5f))
                    ) {
                        Text("Save", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun MessagesTab(
    viewModel: MessagesViewModel,
    onConversationClick: (userId: String, userName: String) -> Unit
) {
    val conversations by viewModel.conversations
    val isLoading by viewModel.isLoading

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    if (isLoading && conversations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(conversations) { conversation ->
                ConversationCard(
                    conversation = conversation,
                    onClick = {
                        onConversationClick(conversation.id, conversation.name)
                    }
                )
            }
        }
    }
}

@Composable
fun ConversationCard(
    conversation: Conversation,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(28.dp),
                color = conversation.color
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conversation.name.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = conversation.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = conversation.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CategoriesTab() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Categories", style = MaterialTheme.typography.headlineMedium)
        Text("Browse jobs by industry or skill.", color = Color.Gray)
    }
}

@Composable
fun SavedTab() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Saved Items", style = MaterialTheme.typography.headlineMedium)
        Text("Items you have bookmarked will appear here.", color = Color.Gray)
    }
}

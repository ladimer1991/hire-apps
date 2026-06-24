package com.example.hire

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

data class Conversation(
    val id: String,
    val name: String,
    val lastMessage: String,
    val timestamp: String,
    val color: Color,
    val base64Images: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    selectedTab: Int = 0,
    currentUserId: String? = null,
    currentUserName: String? = null,
    currentUserImage: String? = null,
    onTabChanged: (Int) -> Unit = {},
    onBackClick: () -> Unit = {},
    onUserClick: (BrowseUser) -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onConversationClick: (userId: String, userName: String) -> Unit = { _, _ -> },
    browseViewModel: BrowseViewModel = viewModel { BrowseViewModel() },
    messagesViewModel: MessagesViewModel = viewModel { MessagesViewModel() },
    categoriesViewModel: CategoriesViewModel = viewModel { CategoriesViewModel() }
) {
    var currentTab by remember { mutableStateOf(selectedTab) }
    val tabs = listOf("Browse", "Messages", "Categories", "Profile")

    LaunchedEffect(browseViewModel, messagesViewModel, categoriesViewModel, currentUserId) {
        categoriesViewModel.updateCurrentUserId(currentUserId)
        browseViewModel.loadUsersAwait()
        messagesViewModel.loadConversations()
        categoriesViewModel.loadCategories()
    }

    Scaffold(
        topBar = {
            if (currentTab == 0) {
                Surface(
                    shadowElevation = 4.dp,
                    color = Color.White
                ) {
                    OutlinedTextField(
                        value = browseViewModel.searchQuery.value,
                        onValueChange = { browseViewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .statusBarsPadding()
                            .fillMaxWidth()
                            .padding(8.dp),
                        placeholder = { Text("Search services...") },
                        leadingIcon = {
                            IconButton(onClick = { browseViewModel.triggerSearch() }) {
                                Text("🔍")
                            }
                        },
                        trailingIcon = {
                            if (browseViewModel.searchQuery.value.isNotEmpty()) {
                                IconButton(onClick = { 
                                    browseViewModel.updateSearchQuery("")
                                }) {
                                    Text("✕")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color(0xFF007AFF)
                        )
                    )
                }
            } else {
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
            }
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
                                    "Profile" -> {
                                        if (currentUserImage != null) {
                                            val bitmap = remember(currentUserImage) {
                                                decodeBase64ToBitmap(currentUserImage)
                                            }
                                            if (bitmap != null) {
                                                Image(
                                                    bitmap = bitmap,
                                                    contentDescription = "Profile",
                                                    modifier = Modifier.size(24.dp).clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                                return@NavigationBarItem
                                            }
                                        }
                                        "👤"
                                    }
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
                0 -> BrowseTab(
                    viewModel = browseViewModel,
                    onUserClick = onUserClick,
                    onConversationClick = onConversationClick
                )
                1 -> MessagesTab(viewModel = messagesViewModel, onConversationClick = onConversationClick)
                2 -> CategoriesTab(
                    viewModel = categoriesViewModel,
                    currentUserId = currentUserId,
                    onUserClick = onUserClick,
                    onSeeMoreClick = { searchTerm ->
                        currentTab = 0
                        onTabChanged(0)
                        browseViewModel.updateSearchQuery(searchTerm)
                        browseViewModel.triggerSearch()
                    }
                )
                3 -> {
                    ProfileTab(
                        userName = currentUserName ?: "User",
                        userImage = currentUserImage,
                        onEditProfileClick = onEditProfileClick,
                        onLogoutClick = onBackClick
                    )
                }
            }
        }
    }
}

data class BrowseUser(
    val id: String,
    val name: String,
    val profession: String,
    val hourlyRate: Double? = null,
    val description: String? = null,
    val color: Color,
    val base64Images: List<String> = emptyList(),
    val reviews: List<Review> = emptyList()
)

val defaultColors = listOf(
    Color(0xFFBBDEFB), Color(0xFFF8BBD0), Color(0xFFC8E6C9), 
    Color(0xFFFFF9C4), Color(0xFFD1C4E9), Color(0xFFFFE0B2), 
    Color(0xFFB2EBF2), Color(0xFFF0F4C3)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseTab(
    viewModel: BrowseViewModel,
    onUserClick: (BrowseUser) -> Unit,
    onConversationClick: (userId: String, userName: String) -> Unit
) {
    val users by viewModel.users
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val listState = rememberLazyListState()

    ApiErrorDialogHost(
        errorMessage = errorMessage,
        title = "Browse failed",
        onDismissError = { viewModel.clearError() }
    )

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

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { viewModel.triggerRefresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        if (users.isEmpty() && !isLoading) {
            if (errorMessage == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No users found")
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 1.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(users) { user ->
                    UserCard(user, onUserClick, onConversationClick)
                }
                if (isLoading && users.isNotEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun UserCard(
    user: BrowseUser,
    onUserClick: (BrowseUser) -> Unit,
    onConversationClick: (String, String) -> Unit
) {
    val pageCount = if (user.base64Images.isNotEmpty()) user.base64Images.size else 1
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { pageCount })

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(264.dp)
                    .padding(
                        start = 8.dp,
                        top = 8.dp,
                        end = 8.dp,
                        bottom = if (user.base64Images.size > 1) 0.dp else 4.dp
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onUserClick(user) }
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

                if (user.base64Images.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(user.base64Images.size) { index ->
                            Surface(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(8.dp),
                                shape = CircleShape,
                                color = if (index == pagerState.currentPage) Color.White else Color.White.copy(alpha = 0.5f)
                            ) {}
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp, top = if (user.base64Images.size > 1) 0.dp else 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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
                    }
                    user.hourlyRate?.let { rate ->
                        Text(
                            text = "$${rate}/hr",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }

                user.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

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

@OptIn(ExperimentalMaterial3Api::class)
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

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { viewModel.loadConversations(forceRefresh = true) },
        modifier = Modifier.fillMaxSize()
    ) {
        if (conversations.isEmpty() && !isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No messages yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(1.dp),
                contentPadding = PaddingValues(vertical = 1.dp)
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
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
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
                    if (conversation.base64Images.isNotEmpty()) {
                        val base64Str = conversation.base64Images[0]
                        val bitmap = remember(base64Str) {
                            decodeBase64ToBitmap(base64Str)
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = conversation.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = conversation.name.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = conversation.name.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                    }
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
fun CategoriesTab(
    viewModel: CategoriesViewModel,
    currentUserId: String?,
    onUserClick: (BrowseUser) -> Unit,
    onSeeMoreClick: (String) -> Unit
) {
    val tutors by viewModel.tutors
    val handymen by viewModel.handymen
    val petSitters by viewModel.petSitters
    val electricians by viewModel.electricians
    val relaxPros by viewModel.relaxPros

    val tutorsLoading by viewModel.tutorsLoading
    val handymenLoading by viewModel.handymenLoading
    val petSittersLoading by viewModel.petSittersLoading
    val electriciansLoading by viewModel.electriciansLoading
    val relaxProsLoading by viewModel.relaxProsLoading

    val tutorsError by viewModel.tutorsError
    val handymenError by viewModel.handymenError
    val petSittersError by viewModel.petSittersError
    val electriciansError by viewModel.electriciansError
    val relaxProsError by viewModel.relaxProsError

    ApiErrorDialogHost(
        errorMessage = tutorsError,
        title = "Tutors failed",
        onDismissError = { viewModel.clearTutorsError() }
    )
    ApiErrorDialogHost(
        errorMessage = handymenError,
        title = "Handymen failed",
        onDismissError = { viewModel.clearHandymenError() }
    )
    ApiErrorDialogHost(
        errorMessage = petSittersError,
        title = "Pet sitters failed",
        onDismissError = { viewModel.clearPetSittersError() }
    )
    ApiErrorDialogHost(
        errorMessage = electriciansError,
        title = "Electricians failed",
        onDismissError = { viewModel.clearElectriciansError() }
    )
    ApiErrorDialogHost(
        errorMessage = relaxProsError,
        title = "Ready to relax? failed",
        onDismissError = { viewModel.clearRelaxProsError() }
    )

    LaunchedEffect(currentUserId) {
        viewModel.updateCurrentUserId(currentUserId)
        viewModel.loadCategories()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        if (tutorsLoading || tutors.isNotEmpty()) {
            item {
                CategorySection(
                    title = "Tutors",
                    users = tutors,
                    isLoading = tutorsLoading,
                    searchTerm = "tutor",
                    onUserClick = onUserClick,
                    onSeeMoreClick = onSeeMoreClick
                )
            }
        }
        if (handymenLoading || handymen.isNotEmpty()) {
            item {
                CategorySection(
                    title = "Hire Handymen",
                    users = handymen,
                    isLoading = handymenLoading,
                    searchTerm = "Handyman",
                    onUserClick = onUserClick,
                    onSeeMoreClick = onSeeMoreClick
                )
            }
        }
        if (petSittersLoading || petSitters.isNotEmpty()) {
            item {
                CategorySection(
                    title = "Pet Sitters",
                    users = petSitters,
                    isLoading = petSittersLoading,
                    searchTerm = "sitter",
                    onUserClick = onUserClick,
                    onSeeMoreClick = onSeeMoreClick
                )
            }
        }
        if (electriciansLoading || electricians.isNotEmpty()) {
            item {
                CategorySection(
                    title = "Need Electricians?",
                    users = electricians,
                    isLoading = electriciansLoading,
                    searchTerm = "electrician",
                    onUserClick = onUserClick,
                    onSeeMoreClick = onSeeMoreClick
                )
            }
        }
        if (relaxProsLoading || relaxPros.isNotEmpty()) {
            item {
                CategorySection(
                    title = "Ready to relax with a massage?",
                    users = relaxPros,
                    isLoading = relaxProsLoading,
                    searchTerm = "massage",
                    onUserClick = onUserClick,
                    onSeeMoreClick = onSeeMoreClick
                )
            }
        }
    }
}

@Composable
private fun CategorySection(
    title: String,
    users: List<BrowseUser>,
    isLoading: Boolean,
    searchTerm: String,
    onUserClick: (BrowseUser) -> Unit,
    onSeeMoreClick: (String) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(users) { user ->
                        CategoryUserCard(user = user, onUserClick = onUserClick)
                    }
                    item {
                        CategorySeeMoreCard(onClick = { onSeeMoreClick(searchTerm) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySeeMoreCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(188.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "See more",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF007AFF)
            )
        }
    }
}

@Composable
private fun CategoryUserCard(
    user: BrowseUser,
    onUserClick: (BrowseUser) -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .clickable { onUserClick(user) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(user.color),
                contentAlignment = Alignment.Center
            ) {
                val firstImage = user.base64Images.firstOrNull()
                if (firstImage != null) {
                    val bitmap = remember(firstImage) {
                        decodeBase64ToBitmap(firstImage)
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
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                } else {
                    Text(
                        text = user.name.take(1).uppercase(),
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = user.hourlyRate?.let { "$${it}/hr" } ?: "-",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1B5E20)
                )
            }
        }
    }
}

class CategoriesViewModel(
    private val apiService: AuthApiService = AuthApiService()
) : ViewModel() {
    private var currentUserId: String? = null

    private val _tutors = mutableStateOf<List<BrowseUser>>(emptyList())
    val tutors: State<List<BrowseUser>> = _tutors

    private val _handymen = mutableStateOf<List<BrowseUser>>(emptyList())
    val handymen: State<List<BrowseUser>> = _handymen

    private val _petSitters = mutableStateOf<List<BrowseUser>>(emptyList())
    val petSitters: State<List<BrowseUser>> = _petSitters

    private val _electricians = mutableStateOf<List<BrowseUser>>(emptyList())
    val electricians: State<List<BrowseUser>> = _electricians

    private val _relaxPros = mutableStateOf<List<BrowseUser>>(emptyList())
    val relaxPros: State<List<BrowseUser>> = _relaxPros

    private val _tutorsLoading = mutableStateOf(false)
    val tutorsLoading: State<Boolean> = _tutorsLoading

    private val _handymenLoading = mutableStateOf(false)
    val handymenLoading: State<Boolean> = _handymenLoading

    private val _petSittersLoading = mutableStateOf(false)
    val petSittersLoading: State<Boolean> = _petSittersLoading

    private val _electriciansLoading = mutableStateOf(false)
    val electriciansLoading: State<Boolean> = _electriciansLoading

    private val _relaxProsLoading = mutableStateOf(false)
    val relaxProsLoading: State<Boolean> = _relaxProsLoading

    private val _tutorsError = mutableStateOf<String?>(null)
    val tutorsError: State<String?> = _tutorsError

    private val _handymenError = mutableStateOf<String?>(null)
    val handymenError: State<String?> = _handymenError

    private val _petSittersError = mutableStateOf<String?>(null)
    val petSittersError: State<String?> = _petSittersError

    private val _electriciansError = mutableStateOf<String?>(null)
    val electriciansError: State<String?> = _electriciansError

    private val _relaxProsError = mutableStateOf<String?>(null)
    val relaxProsError: State<String?> = _relaxProsError

    private var hasLoadedOnce = false

    fun updateCurrentUserId(userId: String?) {
        currentUserId = userId?.takeIf { it.isNotBlank() }

        if (currentUserId == null) return

        _tutors.value = _tutors.value.filterNotCurrentUser()
        _handymen.value = _handymen.value.filterNotCurrentUser()
        _petSitters.value = _petSitters.value.filterNotCurrentUser()
        _electricians.value = _electricians.value.filterNotCurrentUser()
        _relaxPros.value = _relaxPros.value.filterNotCurrentUser()
    }

    fun loadCategories(forceRefresh: Boolean = false) {
        if (hasLoadedOnce && !forceRefresh) return

        loadCategory(
            searchWord = "tutor",
            listSetter = { _tutors.value = it },
            loadingSetter = { _tutorsLoading.value = it },
            errorSetter = { _tutorsError.value = it }
        )
        loadCategory(
            searchWord = "Handyman",
            listSetter = { _handymen.value = it },
            loadingSetter = { _handymenLoading.value = it },
            errorSetter = { _handymenError.value = it }
        )
        loadCategory(
            searchWord = "sitter",
            listSetter = { _petSitters.value = it },
            loadingSetter = { _petSittersLoading.value = it },
            errorSetter = { _petSittersError.value = it }
        )
        loadCategory(
            searchWord = "electrician",
            listSetter = { _electricians.value = it },
            loadingSetter = { _electriciansLoading.value = it },
            errorSetter = { _electriciansError.value = it }
        )
        loadCategory(
            searchWord = "massage",
            listSetter = { _relaxPros.value = it },
            loadingSetter = { _relaxProsLoading.value = it },
            errorSetter = { _relaxProsError.value = it },
            onComplete = { hasLoadedOnce = true }
        )
    }

    fun clearTutorsError() {
        _tutorsError.value = null
    }

    fun clearHandymenError() {
        _handymenError.value = null
    }

    fun clearPetSittersError() {
        _petSittersError.value = null
    }

    fun clearElectriciansError() {
        _electriciansError.value = null
    }

    fun clearRelaxProsError() {
        _relaxProsError.value = null
    }

    private fun loadCategory(
        searchWord: String,
        listSetter: (List<BrowseUser>) -> Unit,
        loadingSetter: (Boolean) -> Unit,
        errorSetter: (String?) -> Unit,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            loadingSetter(true)
            errorSetter(null)

            apiService.getAllUsers(searchWord).onSuccess { fetchedUsers ->
                val mappedUsers = fetchedUsers.mapIndexed { index, user ->
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
                }.filterNotCurrentUser()
                listSetter(mappedUsers)
            }.onFailure { error ->
                logApiError("categories:$searchWord", error)
                errorSetter(error.toFriendlyApiMessage("Failed to load $searchWord users."))
            }

            loadingSetter(false)
            onComplete()
        }
    }

    private fun List<BrowseUser>.filterNotCurrentUser(): List<BrowseUser> {
        val loggedInId = currentUserId ?: return this
        return filter { it.id != loggedInId }
    }
}

@Composable
fun ProfileTab(
    userName: String,
    userImage: String?,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Circular Profile Image
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (userImage != null) {
                    val bitmap = remember(userImage) {
                        decodeBase64ToBitmap(userImage)
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("👤", fontSize = 40.sp)
                    }
                } else {
                    Text("👤", fontSize = 40.sp)
                }
            }

            // User Info and Edit Button
            Column {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = onEditProfileClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Edit Profile", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Additional profile options could go here
        Text(
            text = "Account Settings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Placeholder for more settings
        repeat(4) { index ->
            val title = when(index) {
                0 -> "Notifications"
                1 -> "Privacy & Security"
                2 -> "Help & Support"
                else -> "Log Out"
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { 
                        if (index == 3) onLogoutClick()
                    },
                shape = RoundedCornerShape(12.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (index == 3) Color.Red else Color.Black
                    )
                    if (index < 3) {
                        Text("›", fontSize = 24.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

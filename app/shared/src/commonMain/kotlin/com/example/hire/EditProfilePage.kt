package com.example.hire

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfilePage(
    viewModel: EditProfileViewModel = viewModel { EditProfileViewModel() },
    onBack: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back", color = Color(0xFF007AFF))
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.saveProfile() }) {
                        Text("Save", color = Color(0xFF007AFF), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Basic Info
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text("Basic Information", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = viewModel.username.value,
                            onValueChange = viewModel::updateUsername,
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = viewModel.email.value,
                            onValueChange = { /* Email not editable */ },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false
                        )
                    }
                }

                // Profile Details
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text("Profile Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = viewModel.providedService.value,
                            onValueChange = viewModel::updateProvidedService,
                            label = { Text("Service you provide") },
                            placeholder = { Text("e.g. Dog Walking, Math Tutoring") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = viewModel.hourlyRate.value,
                            onValueChange = viewModel::updateHourlyRate,
                            label = { Text("Hourly Rate ($)") },
                            placeholder = { Text("e.g. 25.00") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = viewModel.description.value,
                            onValueChange = viewModel::updateDescription,
                            label = { Text("Description") },
                            placeholder = { Text("Tell potential clients about yourself...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }

                // Image Picker Section
                val imagePickerLauncher = rememberImagePickerLauncher { base64 ->
                    viewModel.addImage(base64)
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text("Profile Images (${viewModel.images.size}/4)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(4) { index ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                                        .clickable { 
                                            if (index < viewModel.images.size) {
                                                viewModel.removeImage(index)
                                            } else {
                                                imagePickerLauncher()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (index < viewModel.images.size) {
                                        val bitmap = remember(viewModel.images[index]) {
                                            decodeBase64ToBitmap(viewModel.images[index])
                                        }
                                        if (bitmap != null) {
                                            androidx.compose.foundation.Image(
                                                bitmap = bitmap,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                        }
                                    } else {
                                        Text("+", fontSize = 24.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                ApiErrorDialogHost(
                    errorMessage = viewModel.errorMessage.value,
                    title = "Profile update failed",
                    onDismissError = { viewModel.clearError() }
                )
                if (viewModel.successMessage.value != null) {
                    Text(viewModel.successMessage.value!!, color = Color(0xFF4CAF50), fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (viewModel.isLoading.value) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF007AFF))
                }
            }
        }
    }
}

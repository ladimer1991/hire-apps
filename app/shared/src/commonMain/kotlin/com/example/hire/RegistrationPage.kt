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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationPage(
    viewModel: RegistrationViewModel = remember { RegistrationViewModel() },
    onBackClick: () -> Unit = {},
    onSuccessClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Account", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(
                        onClick = onBackClick,
                        enabled = !viewModel.isLoading.value
                    ) {
                        Text("Back", color = if (viewModel.isLoading.value) Color.Gray else Color(0xFF007AFF))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
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
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !viewModel.isLoading.value
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.email.value,
                        onValueChange = viewModel::updateEmail,
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !viewModel.isLoading.value
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.password.value,
                        onValueChange = viewModel::updatePassword,
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !viewModel.isLoading.value
                    )
                }
            }

            // Question: Will you provide a service?
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("Service Provider", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Will you provide a service?", fontSize = 16.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable(enabled = !viewModel.isLoading.value) { viewModel.updateIsServiceProvider(true) }
                    ) {
                        RadioButton(
                            selected = viewModel.isServiceProvider.value,
                            onClick = { viewModel.updateIsServiceProvider(true) },
                            enabled = !viewModel.isLoading.value
                        )
                        Text("Yes")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable(enabled = !viewModel.isLoading.value) { viewModel.updateIsServiceProvider(false) }
                    ) {
                        RadioButton(
                            selected = !viewModel.isServiceProvider.value,
                            onClick = { viewModel.updateIsServiceProvider(false) },
                            enabled = !viewModel.isLoading.value
                        )
                        Text("No")
                    }
                }
            }

            if (viewModel.isServiceProvider.value) {
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
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !viewModel.isLoading.value
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = viewModel.hourlyRate.value,
                            onValueChange = viewModel::updateHourlyRate,
                            label = { Text("Hourly Rate ($)") },
                            placeholder = { Text("e.g. 25.00") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !viewModel.isLoading.value
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = viewModel.description.value,
                            onValueChange = viewModel::updateDescription,
                            label = { Text("Description") },
                            placeholder = { Text("Tell potential clients about yourself...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            enabled = !viewModel.isLoading.value
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
                                        .clickable(enabled = !viewModel.isLoading.value) { if (index >= viewModel.images.size) imagePickerLauncher() },
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
            }

            ApiErrorDialogHost(
                errorMessage = viewModel.errorMessage.value,
                title = "Registration failed",
                onDismissError = { viewModel.clearError() }
            )
            if (viewModel.successMessage.value != null) {
                Text(viewModel.successMessage.value!!, color = Color(0xFF4CAF50), fontSize = 14.sp)
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(1500)
                    onSuccessClick()
                }
            }

            // Submit Button
            Button(
                onClick = { viewModel.register() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF)),
                enabled = !viewModel.isLoading.value
            ) {
                if (viewModel.isLoading.value) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Register Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

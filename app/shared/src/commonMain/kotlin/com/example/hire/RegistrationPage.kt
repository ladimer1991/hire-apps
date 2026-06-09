package com.example.hire

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun RegistrationPage(
    viewModel: RegistrationViewModel = androidx.lifecycle.viewmodel.compose.viewModel { RegistrationViewModel() },
    onBackClick: () -> Unit = {},
    onSuccessClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val isLoading = viewModel.isLoading.value
    val errorMessage = viewModel.errorMessage.value
    val successMessage = viewModel.successMessage.value

    val imagePickerLauncher = rememberImagePickerLauncher { base64Image ->
        viewModel.addImage(base64Image)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // When registration succeeds, trigger the onSuccessClick callback so the host can
        // request permissions and navigate. Use a LaunchedEffect to react to state changes.
        LaunchedEffect(successMessage) {
            if (successMessage != null) {
                onSuccessClick()
            }
        }

        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "← Back",
                modifier = Modifier
                    .clickable { onBackClick() }
                    .padding(12.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF007AFF)
            )
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Error Message
                if (errorMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        color = Color(0xFFFFCDD2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Success Message
                if (successMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        color = Color(0xFFC8E6C9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = successMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Full Name Field
                OutlinedTextField(
                    value = viewModel.fullName.value,
                    onValueChange = { viewModel.updateFullName(it) },
                    label = { Text("Full Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true,
                    enabled = !isLoading
                )

                // Email Field
                OutlinedTextField(
                    value = viewModel.email.value,
                    onValueChange = { viewModel.updateEmail(it) },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !isLoading
                )

                // Username Field
                OutlinedTextField(
                    value = viewModel.username.value,
                    onValueChange = { viewModel.updateUsername(it) },
                    label = { Text("Username") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true,
                    enabled = !isLoading
                )

                // Password Field
                OutlinedTextField(
                    value = viewModel.password.value,
                    onValueChange = { viewModel.updatePassword(it) },
                    label = { Text("Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled = !isLoading
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Text("Profile Images", style = MaterialTheme.typography.titleSmall)
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color.LightGray, RoundedCornerShape(8.dp))
                                .clickable { imagePickerLauncher() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (index < viewModel.images.size) {
                                val bitmap = remember(viewModel.images[index]) {
                                    decodeBase64ToBitmap(viewModel.images[index])
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = "Selected image ${index + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text("IMG ${index + 1}")
                                }
                            } else {
                                Text("+")
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Text("Credit Card Information", style = MaterialTheme.typography.titleSmall)

                OutlinedTextField(
                    value = viewModel.cardNumber.value,
                    onValueChange = { viewModel.updateCardNumber(it) },
                    label = { Text("Card Number") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isLoading
                )
                
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = viewModel.cardExpiration.value,
                        onValueChange = { viewModel.updateCardExpiration(it) },
                        label = { Text("MM/YY") },
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = viewModel.cardCvv.value,
                        onValueChange = { viewModel.updateCardCvv(it) },
                        label = { Text("CVV") },
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !isLoading
                    )
                }
                
                OutlinedTextField(
                    value = viewModel.cardholderName.value,
                    onValueChange = { viewModel.updateCardholderName(it) },
                    label = { Text("Cardholder Name") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    enabled = !isLoading
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Text("Billing Address", style = MaterialTheme.typography.titleSmall)

                OutlinedTextField(
                    value = viewModel.billingStreet.value,
                    onValueChange = { viewModel.updateBillingStreet(it) },
                    label = { Text("Street") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    enabled = !isLoading
                )
                
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = viewModel.billingCity.value,
                        onValueChange = { viewModel.updateBillingCity(it) },
                        label = { Text("City") },
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = viewModel.billingState.value,
                        onValueChange = { viewModel.updateBillingState(it) },
                        label = { Text("State") },
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        enabled = !isLoading
                    )
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = viewModel.billingZipCode.value,
                        onValueChange = { viewModel.updateBillingZipCode(it) },
                        label = { Text("Zip Code") },
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = viewModel.billingCountry.value,
                        onValueChange = { viewModel.updateBillingCountry(it) },
                        label = { Text("Country") },
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        enabled = !isLoading
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Register Button
                Button(
                    onClick = {
                        viewModel.register()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007AFF),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Register",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                // Password Requirements
                Text(
                    text = "Password must be at least 6 characters",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview
@Composable
fun RegistrationPagePreview() {
    MaterialTheme {
        RegistrationPage()
    }
}

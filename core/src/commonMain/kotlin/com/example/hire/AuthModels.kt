package com.example.hire

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val id: String? = null,
    val description: String? = null,
    val providedService: String? = null,
    val email: String,
    val username: String,
    val password: String,
    val images: List<String> = emptyList(), // Base64 encoded images
    val creditCard: CreditCardInfo? = null,
    val billingAddress: BillingAddress? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: RegisterRequest
)

@Serializable
data class CreditCardInfo(
    val number: String,
    val expirationDate: String,
    val cvv: String,
    val cardholderName: String
)

@Serializable
data class BillingAddress(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String
)

@Serializable
data class Message(
    val id: String? = null,
    val senderId: String? = null,
    val receiverId: String,
    val content: String,
    val timestamp: Long = 0L
)

@Serializable
data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val userId: String? = null
)

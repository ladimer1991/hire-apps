package com.example.hire

import java.util.*

data class User(
    val id: String = UUID.randomUUID().toString(),
    val description: String? = null,
    val providedService: String? = null,
    val email: String,
    val username: String,
    val password: String, // In production, this should be hashed with bcrypt or similar
    val images: List<String> = emptyList(),
    val creditCard: CreditCardInfo? = null,
    val billingAddress: BillingAddress? = null,
    val createdAt: Long = System.currentTimeMillis()
)

object UserRepository {
    private val users = mutableMapOf<String, User>()

    fun registerUser(request: RegisterRequest): Result<User> {
        // Check if email already exists
        if (users.values.any { it.email == request.email }) {
            return Result.failure(Exception("Email already registered"))
        }

        // Check if username already exists
        if (users.values.any { it.username == request.username }) {
            return Result.failure(Exception("Username already taken"))
        }

        // Create new user
        val user = User(
            description = request.description,
            providedService = request.providedService,
            email = request.email,
            username = request.username,
            password = request.password,
            images = request.images,
            creditCard = request.creditCard,
            billingAddress = request.billingAddress
        )

        users[user.id] = user
        return Result.success(user)
    }

    fun getUserByUsername(username: String): User? {
        return users.values.firstOrNull { it.username == username }
    }

    fun getUserByEmail(email: String): User? {
        return users.values.firstOrNull { it.email == email }
    }

    fun getUserById(id: String): User? {
        return users[id]
    }

    fun getAllUsers(): List<User> {
        return users.values.toList()
    }
}

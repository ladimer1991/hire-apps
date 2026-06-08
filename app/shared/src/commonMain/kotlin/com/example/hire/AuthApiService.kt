package com.example.hire

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AuthApiService(
    private val baseUrl: String = "https://user-service-71791662068.us-central1.run.app",
    private val sessionManager: SessionManager = SessionManager()
) {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        defaultRequest {
            sessionManager.getToken()?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }

    suspend fun register(request: RegisterRequest): Result<AuthResponse> = runCatching {
        val response: AuthResponse = httpClient.post("$baseUrl/api/users/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        sessionManager.saveToken(response.token)
        response
    }

    suspend fun login(request: LoginRequest): Result<AuthResponse> = runCatching {
        val response: AuthResponse = httpClient.post("$baseUrl/api/users/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        sessionManager.saveToken(response.token)
        response
    }
    
    suspend fun getAllUsers(): Result<List<RegisterRequest>> = runCatching {
        httpClient.get("$baseUrl/api/users").body()
    }

    suspend fun getCurrentUser(): Result<RegisterRequest> = runCatching {
        httpClient.get("$baseUrl/api/users/me").body()
    }

    suspend fun sendMessage(message: Message): Result<Message> = runCatching {
        httpClient.post("$baseUrl/api/messages/send") {
            contentType(ContentType.Application.Json)
            setBody(message)
        }.body()
    }

    suspend fun getConversation(otherUserId: String): Result<List<Message>> = runCatching {
        httpClient.get("$baseUrl/api/messages/conversation/$otherUserId").body()
    }

    suspend fun getHistory(): Result<List<Message>> = runCatching {
        httpClient.get("$baseUrl/api/messages/history").body()
    }

    fun close() {
        httpClient.close()
    }
}

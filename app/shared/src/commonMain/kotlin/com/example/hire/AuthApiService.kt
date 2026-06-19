package com.example.hire

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.statement.bodyAsText
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
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
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    // Filter out large image data from logs using regex
                    // (?s) allows . to match newlines
                    val sanitized = if (message.contains("\"images\"") || message.contains("images=")) {
                        message.replace(Regex("""(?s)"images"\s*:\s*\[.*?]"""), "\"images\": []")
                               .replace(Regex("""(?s)images=\[.*?]"""), "images=[]")
                    } else {
                        message
                    }

                    // Split long messages for Logcat compatibility (4KB limit)
                    sanitized.chunked(3500).forEach { 
                        println("HTTP_LOG: $it")
                    }
                }
            }
            level = LogLevel.ALL
            sanitizeHeader { false }
        }
        install(ResponseObserver) {
            onResponse { response ->
                val body = response.bodyAsText()
                val sanitizedBody = body.replace(Regex("""(?s)"images"\s*:\s*\[.*?]"""), "\"images\": []")
                println("HTTP_LOG: RESPONSE_BODY: $sanitizedBody")
            }
        }
        defaultRequest {
            sessionManager.getToken()?.let { token ->
                header("Authorization", "Bearer $token")
            }
        }
    }

    private suspend fun <T> apiCall(
        source: String,
        block: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            logApiError(source, e)
            Result.failure(e)
        }
    }

    suspend fun register(request: RegisterRequest): Result<AuthResponse> =
        apiCall("register") {
            val requestLog = request.copy(images = emptyList())
            println("HTTP_LOG: REQUEST_BODY: $requestLog")
            val response: AuthResponse = httpClient.post("$baseUrl/api/users/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            sessionManager.saveToken(response.token)
            response
        }

    suspend fun login(request: LoginRequest): Result<AuthResponse> =
        apiCall("login") {
            val response: AuthResponse = httpClient.post("$baseUrl/api/users/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            sessionManager.saveToken(response.token)
            response
        }

    suspend fun getAllUsers(searchQuery: String? = null): Result<List<RegisterRequest>> =
        apiCall("getAllUsers") {
            httpClient.get("$baseUrl/api/users") {
                searchQuery?.let { parameter("q", it) }
            }.body<List<RegisterRequest>>()
        }

    suspend fun getCurrentUser(): Result<RegisterRequest> =
        apiCall("getCurrentUser") {
            httpClient.get("$baseUrl/api/users/me").body<RegisterRequest>()
        }

    suspend fun updateProfile(request: RegisterRequest): Result<RegisterRequest> =
        apiCall("updateProfile") {
            httpClient.put("$baseUrl/api/users/profile") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<RegisterRequest>()
        }

    suspend fun sendMessage(message: Message): Result<Message> =
        apiCall("sendMessage") {
            httpClient.post("$baseUrl/api/messages/send") {
                contentType(ContentType.Application.Json)
                setBody(message)
            }.body<Message>()
        }

    suspend fun getConversation(otherUserId: String): Result<List<Message>> =
        apiCall("getConversation") {
            httpClient.get("$baseUrl/api/messages/conversation/$otherUserId").body<List<Message>>()
        }

    suspend fun addReview(targetUserId: String, content: String, rating: Double): Result<RegisterRequest> =
        apiCall("addReview") {
            httpClient.post("$baseUrl/api/users/$targetUserId/reviews") {
                contentType(ContentType.Application.Json)
                setBody(Review(content = content, rating = rating))
            }.body<RegisterRequest>()
        }

    suspend fun getHistory(): Result<List<Message>> =
        apiCall("getHistory") {
            httpClient.get("$baseUrl/api/messages/history").body<List<Message>>()
        }

    fun close() {
        httpClient.close()
    }
}

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
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class AuthApiService(
    private val baseUrl: String = "https://user-service-71791662068.us-central1.run.app",
    private val sessionManager: SessionManager = SessionManager()
) {
    companion object {
        private val meCacheMutex = Mutex()
        private var meCache: RegisterRequest? = null
    }

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

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
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
            val response = httpClient.post("$baseUrl/api/users/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.bodyAsText()

            if (!response.status.isSuccess()) {
                throw Exception(extractServerMessage(body, "Registration failed"))
            }

            val authResponse = runCatching { json.decodeFromString<AuthResponse>(body) }.getOrNull()
            if (authResponse != null) {
                sessionManager.saveToken(authResponse.token)
                invalidateMeCache()
                return@apiCall authResponse
            }

            val registerResponse = runCatching { json.decodeFromString<RegisterResponse>(body) }.getOrNull()
            if (registerResponse?.success == true) {
                val loginResponse = login(
                    LoginRequest(
                        email = request.email,
                        password = request.password
                    )
                ).getOrElse { throw it }

                return@apiCall loginResponse
            }

            throw Exception(registerResponse?.message ?: extractServerMessage(body, "Registration failed"))
        }

    suspend fun login(request: LoginRequest): Result<AuthResponse> =
        apiCall("login") {
            val response = httpClient.post("$baseUrl/api/users/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.bodyAsText()

            if (!response.status.isSuccess()) {
                throw Exception(extractServerMessage(body, "Login failed"))
            }

            val authResponse = runCatching { json.decodeFromString<AuthResponse>(body) }
                .getOrElse { throw Exception(extractServerMessage(body, "Login response was invalid")) }

            sessionManager.saveToken(authResponse.token)
            invalidateMeCache()
            authResponse
        }

    suspend fun logout(): Result<Unit> =
        apiCall("logout") {
            httpClient.post("$baseUrl/api/users/logout")
            sessionManager.clearSession()
            invalidateMeCache()
            Unit
        }

    suspend fun getAllUsers(searchQuery: String? = null): Result<List<RegisterRequest>> =
        apiCall("getAllUsers") {
            val url = if (searchQuery != null) "$baseUrl/api/users?q=$searchQuery" else "$baseUrl/api/users"
            httpClient.get(url).body<List<RegisterRequest>>()
        }

    suspend fun getCurrentUser(forceRefresh: Boolean = false): Result<RegisterRequest> =
        apiCall("getCurrentUser") {
            meCacheMutex.withLock {
                if (meCache == null || forceRefresh) {
                    meCache = httpClient.get("$baseUrl/api/users/me").body<RegisterRequest>()
                }
                meCache!!
            }
        }

    suspend fun updateProfile(request: RegisterRequest): Result<RegisterRequest> =
        apiCall("updateProfile") {
            httpClient.put("$baseUrl/api/users/profile") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<RegisterRequest>().also {
                invalidateMeCache()
            }
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

    suspend fun addReview(targetUserId: String, content: String, rating: Double): Result<Review> =
        apiCall("addReview") {
            httpClient.post("$baseUrl/api/users/$targetUserId/reviews") {
                contentType(ContentType.Application.Json)
                setBody(Review(content = content, rating = rating))
            }.body<Review>()
        }

    suspend fun getUserReviews(targetUserId: String): Result<List<Review>> =
        apiCall("getUserReviews") {
            httpClient.get("$baseUrl/api/users/$targetUserId/reviews").body<List<Review>>()
        }

    suspend fun getHistory(): Result<List<Message>> =
        apiCall("getHistory") {
            httpClient.get("$baseUrl/api/messages/history").body<List<Message>>()
        }

    fun close() {
        httpClient.close()
    }

    private fun invalidateMeCache() {
        meCache = null
    }

    private fun extractServerMessage(body: String, fallback: String): String {
        if (body.isBlank()) return fallback

        val parsed = runCatching { json.parseToJsonElement(body) }.getOrNull()
        val jsonObject = parsed as? JsonObject ?: return fallback

        return jsonObject["message"]?.jsonPrimitive?.contentOrNull
            ?: jsonObject["error"]?.jsonPrimitive?.contentOrNull
            ?: fallback
    }
}

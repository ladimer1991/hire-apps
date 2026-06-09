package com.example.hire

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }

    routing {
        get("/") {
            call.respondText(sayHello("Ktor"))
        }

        // Registration endpoint
        post("/api/auth/register") {
            try {
                val request = call.receive<RegisterRequest>()

                // Validate request
                if (request.email.isBlank() || !request.email.contains("@")) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        RegisterResponse(
                            success = false,
                            message = "Invalid email format"
                        )
                    )
                    return@post
                }

                if (request.username.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        RegisterResponse(
                            success = false,
                            message = "Username cannot be empty"
                        )
                    )
                    return@post
                }

                if (request.password.length < 6) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        RegisterResponse(
                            success = false,
                            message = "Password must be at least 6 characters"
                        )
                    )
                    return@post
                }

                // Register user
                val result = UserRepository.registerUser(request)

                result.onSuccess { user ->
                    call.respond(
                        HttpStatusCode.Created,
                        RegisterResponse(
                            success = true,
                            message = "User registered successfully",
                            userId = user.id
                        )
                    )
                }.onFailure { error ->
                    call.respond(
                        HttpStatusCode.BadRequest,
                        RegisterResponse(
                            success = false,
                            message = error.message ?: "Registration failed"
                        )
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    RegisterResponse(
                        success = false,
                        message = "Server error: ${e.message}"
                    )
                )
            }
        }

        // Get all users endpoint (for testing)
        get("/api/users") {
            val users = UserRepository.getAllUsers()
            call.respond(users)
        }
    }
}
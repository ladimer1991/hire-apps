package com.example.hire

actual fun logApiError(source: String, error: Throwable) {
    println("API_ERROR[$source]: ${error.message ?: error::class.simpleName}")
}


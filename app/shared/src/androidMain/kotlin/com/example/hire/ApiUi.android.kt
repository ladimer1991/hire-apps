package com.example.hire

import android.util.Log

actual fun logApiError(source: String, error: Throwable) {
    Log.e("HireApi", "[$source] ${error.message ?: error::class.simpleName}", error)
}


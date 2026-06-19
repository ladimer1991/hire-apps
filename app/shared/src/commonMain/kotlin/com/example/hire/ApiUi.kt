package com.example.hire

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ApiErrorDialogHost(
    errorMessage: String?,
    title: String,
    confirmText: String = "OK",
    onDismissError: () -> Unit
) {
    var dialogMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            dialogMessage = errorMessage
        }
    }

    val message = dialogMessage ?: return

    AlertDialog(
        onDismissRequest = {
            dialogMessage = null
            onDismissError()
        },
        title = { Text(title) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = {
                dialogMessage = null
                onDismissError()
            }) {
                Text(confirmText)
            }
        }
    )
}

fun Throwable.toFriendlyApiMessage(defaultMessage: String): String {
    val rawMessage = message?.trim().orEmpty()
    if (rawMessage.isBlank()) return defaultMessage

    val looksTechnical = listOf(
        "NoTransformationFoundException",
        "SourceByteReadChannel",
        "Response status",
        "text/html",
        "Client request",
        "Server response",
        "HttpResponseException",
        "SerializationException"
    ).any { rawMessage.contains(it, ignoreCase = true) }

    if (!looksTechnical) return rawMessage

    return when {
//        rawMessage.contains("401", ignoreCase = true) || rawMessage.contains("Unauthorized", ignoreCase = true) -> {
//            "Your session may have expired. Please log in again."
//        } we don't want to show this one since it is on the video/start up page.
        rawMessage.contains("403", ignoreCase = true) || rawMessage.contains("Forbidden", ignoreCase = true) -> {
            "You do not have permission to complete this action."
        }
        rawMessage.contains("404", ignoreCase = true) || rawMessage.contains("Not Found", ignoreCase = true) -> {
            "$defaultMessage The service could not be found."
        }
        rawMessage.contains("503", ignoreCase = true) || rawMessage.contains("Service Unavailable", ignoreCase = true) -> {
            "$defaultMessage The service is temporarily unavailable. Please try again."
        }
        else -> "$defaultMessage Please try again."
    }
}

expect fun logApiError(source: String, error: Throwable)



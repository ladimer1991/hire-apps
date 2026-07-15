package com.example.hire

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HireFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "New message"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: message.data["senderName"]?.let { "Message from $it" }
            ?: "You received a new message"

        createNotificationChannelIfNeeded()

        val notification = NotificationCompat.Builder(this, CHAT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(Random.nextInt(), notification)
    }

    override fun onNewToken(token: String) {
        // Keep backend token fresh whenever Firebase rotates the device token.
        CoroutineScope(Dispatchers.IO).launch {
            PushTokenSyncManager.syncTokenWithBackend(token)
        }
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java)
        val existing = manager?.getNotificationChannel(CHAT_CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            CHAT_CHANNEL_ID,
            "Chat messages",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for new chat messages"
        }

        manager?.createNotificationChannel(channel)
    }

    companion object {
        private const val CHAT_CHANNEL_ID = "chat_messages"
    }
}


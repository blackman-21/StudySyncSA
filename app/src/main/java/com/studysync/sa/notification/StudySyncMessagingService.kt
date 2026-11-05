package com.studysync.sa.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Service to receive Firebase Cloud Messaging (FCM) push notifications in real-time.
 */
class StudySyncMessagingService : FirebaseMessagingService() {

    private val TAG = "StudySyncMessaging"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Device Token: $token")
        // Store or send token to backend REST API
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Received FCM Push Notification from: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "StudySync Alert"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "You have a new real-time update."

        val notificationManager = LocalNotificationManager(applicationContext)
        notificationManager.showNotification(
            notificationId = System.currentTimeMillis().toInt(),
            title = title,
            message = body
        )
    }
}

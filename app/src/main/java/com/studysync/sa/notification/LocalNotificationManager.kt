package com.studysync.sa.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.studysync.sa.MainActivity
import com.studysync.sa.R

/**
 * Manages push notifications and real-time alert notifications across the application.
 */
class LocalNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID_TASKS = "studysync_tasks_channel"
        const val CHANNEL_NAME_TASKS = "StudySync Task Alerts"
        const val CHANNEL_ID_SYNC = "studysync_sync_channel"
        const val CHANNEL_NAME_SYNC = "StudySync Cloud Sync Alerts"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val taskChannel = NotificationChannel(
                CHANNEL_ID_TASKS,
                CHANNEL_NAME_TASKS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time notifications for task deadlines and study reminders."
            }

            val syncChannel = NotificationChannel(
                CHANNEL_ID_SYNC,
                CHANNEL_NAME_SYNC,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Real-time notifications for data sync status updates."
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(taskChannel)
            notificationManager.createNotificationChannel(syncChannel)
        }
    }

    /**
     * Triggers a real-time notification alert for task deadline or study updates.
     */
    fun showNotification(notificationId: Int, title: String, message: String, channelId: String = CHANNEL_ID_TASKS) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}

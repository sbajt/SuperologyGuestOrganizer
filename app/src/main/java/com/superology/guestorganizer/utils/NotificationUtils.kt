package com.superology.guestorganizer.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.superology.guestorganizer.R
import com.superology.guestorganizer.activities.MainActivity

object NotificationUtils {

    private val TAG = NotificationUtils::class.java.canonicalName
    private const val ID_GUEST_COMING = 9001
    private const val ID_ELEMENT_CHANGED = 9002

    fun init(context: Context, notificationManager: NotificationManager) {
        context.run {
            createTopic(this)
            createChannel(this, notificationManager)
            createDefaultNotification(this)
        }
    }

    fun guestIncoming(context: Context, notificationManager: NotificationManager) {
        if (notificationManager.activeNotifications?.find { it.id == ID_GUEST_COMING } == null) {
            val notification = NotificationCompat.Builder(
                context,
                context.getString(R.string.notification_channel_id)
            )
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle(context.getString(R.string.notification_upcoming_title))
                .setContentText(context.getString(R.string.notification_upcoming_text))
                .setSmallIcon(R.drawable.ic_attendees)
                .setContentIntent(
                    PendingIntent.getActivity(
                        context,
                        0,
                        Intent(context, MainActivity::class.java).apply {
                            flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        },
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
                .build()
            notificationManager.notify(ID_GUEST_COMING, notification)
        }
    }

    fun createElementChanged(context: Context, notificationManager: NotificationManager) {
        if (notificationManager.activeNotifications?.find { it.id == ID_ELEMENT_CHANGED } == null) {
            val notification = NotificationCompat.Builder(
                context,
                context.getString(R.string.notification_channel_id)
            )
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentTitle(context.getString(R.string.notification_change_element_title))
                .setContentText(context.getString(R.string.notification_change_element_text))
                .setSmallIcon(R.drawable.ic_attendees)
                .setContentIntent(
                    PendingIntent.getActivity(
                        context,
                        0,
                        Intent(context, MainActivity::class.java).apply {
                            flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        },
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
                .build()
            notificationManager.notify(ID_ELEMENT_CHANGED, notification)
        }
    }

    private fun createChannel(context: Context, notificationManager: NotificationManager) {
        val channel =
            NotificationChannel(
                context.getString(R.string.notification_channel_id),
                context.getString(R.string.notification_channel_id),
                NotificationManager.IMPORTANCE_DEFAULT
            )

        if (notificationManager.getNotificationChannel(context.getString(R.string.notification_channel_id)) == null)
            notificationManager.createNotificationChannel(channel)
    }

    private fun createTopic(context: Context) {
        FirebaseMessaging.getInstance()
            .subscribeToTopic(context.getString(R.string.notification_topic))
            .addOnFailureListener {
                Log.e(TAG, context.getString(R.string.notification_topic_error), it)
            }
    }

    private fun createDefaultNotification(context: Context): Notification {
        return context.run {
            NotificationCompat.Builder(this, this.getString(R.string.notification_channel_id))
                .setContentTitle(this.getString(R.string.notification_service_title))
                .setContentText(this.getString(R.string.notification_service_text))
                .setSmallIcon(R.drawable.ic_attendees)
                .build()
        }
    }
}

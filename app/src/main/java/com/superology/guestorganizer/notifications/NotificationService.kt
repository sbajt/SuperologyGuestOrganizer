package com.superology.guestorganizer.notifications

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.superology.guestorganizer.R
import com.superology.guestorganizer.enums.NotificationType
import com.superology.guestorganizer.utils.NotificationUtils

class NotificationService : FirebaseMessagingService() {

    private val TAG = NotificationService::class.java.canonicalName
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    override fun onNewToken(token: String) {
        Log.d(TAG, getString(R.string.notification_token_regenerated))
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (!message.data.isNullOrEmpty()) {
            when (message.data.getValue("notificationType")) {
                NotificationType.GUEST_INCOMING.getId() -> NotificationUtils.guestIncoming(this, notificationManager)
                NotificationType.DATA_CHANGE.getId() -> NotificationUtils.createElementChanged(this, notificationManager)
            }
        }
    }

    override fun onMessageSent(msg: String) {
        Log.d(TAG, "Message $msg sent")
    }

    override fun onSendError(msgId: String, ex: Exception) {
        Log.e(TAG, "Sending message error", ex)
    }
}
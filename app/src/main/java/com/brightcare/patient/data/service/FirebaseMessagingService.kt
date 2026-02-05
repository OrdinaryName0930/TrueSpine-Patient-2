package com.brightcare.patient.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.brightcare.patient.MainActivity
import com.brightcare.patient.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint

/**
 * Firebase Cloud Messaging Service
 * Handles incoming push notifications and FCM token updates
 * 
 * Service para sa Firebase Cloud Messaging
 * Nag-handle ng mga incoming push notifications at FCM token updates
 */
@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "brightcare_notifications"
        private const val CHANNEL_NAME = "BrightCare Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for appointments and messages"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    /**
     * Called when a new FCM token is generated
     * Tinatawag kapag may bagong FCM token na na-generate
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        
        // TODO: Send token to your server or save to Firestore
        // You can save this token to the user's profile in Firestore
        // to enable sending push notifications to specific users
        sendTokenToServer(token)
    }
    
    /**
     * Called when a message is received
     * Tinatawag kapag may natanggap na message
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        
        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
        
        // Check if message contains a notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message notification body: ${notification.body}")
            showNotification(
                title = notification.title ?: "BrightCare",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }
    }
    
    /**
     * Handle data messages (background/foreground)
     * I-handle ang mga data messages (background/foreground)
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val title = data["title"] ?: "BrightCare"
        val body = data["body"] ?: "You have a new notification"
        val type = data["type"] ?: "general"
        val appointmentId = data["appointmentId"]
        
        Log.d(TAG, "Handling data message - Type: $type, AppointmentId: $appointmentId")
        
        // Show notification for data messages
        showNotification(title, body, data)
    }
    
    /**
     * Show notification to user
     * Ipakita ang notification sa user
     */
    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            // Add extra data to intent
            data["type"]?.let { putExtra("notification_type", it) }
            data["appointmentId"]?.let { putExtra("appointment_id", it) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Determine notification icon based on type
        val iconRes = when (data["type"]) {
            "appointment_approved" -> R.drawable.ic_check_circle
            "appointment_rejected" -> R.drawable.ic_cancel
            "new_booking" -> R.drawable.ic_calendar
            "message_received" -> R.drawable.ic_message
            else -> R.drawable.ic_notification
        }
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(getColor(R.color.Blue500))
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt() // Unique ID for each notification
        
        notificationManager.notify(notificationId, notificationBuilder.build())
        
        Log.d(TAG, "Notification shown: $title")
    }
    
    /**
     * Create notification channel for Android 8.0+
     * Gumawa ng notification channel para sa Android 8.0+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            
            Log.d(TAG, "Notification channel created: $CHANNEL_ID")
        }
    }
    
    /**
     * Send FCM token to server (implement as needed)
     * Ipadala ang FCM token sa server (i-implement kung kailangan)
     */
    private fun sendTokenToServer(token: String) {
        // TODO: Implement sending token to Firestore or your backend
        // You can save this to the user's profile document in Firestore
        // Example:
        // firestore.collection("users").document(userId).update("fcmToken", token)
        
        Log.d(TAG, "FCM Token should be sent to server: $token")
    }
}

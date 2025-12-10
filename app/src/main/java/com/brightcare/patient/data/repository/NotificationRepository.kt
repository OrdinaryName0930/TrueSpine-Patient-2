package com.brightcare.patient.data.repository

import android.util.Log
import com.brightcare.patient.data.model.Notification
import com.brightcare.patient.data.model.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling notification operations
 * Repository para sa pag-handle ng notification operations
 */
@Singleton
class NotificationRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    companion object {
        private const val TAG = "NotificationRepository"
        private const val COLLECTION_NOTIFICATIONS = "notifications"
    }
    
    /**
     * Get user's notifications as a Flow for real-time updates
     * Kumuha ng mga notification ng user bilang Flow para sa real-time updates
     */
    fun getUserNotifications(): Flow<List<Notification>> = callbackFlow {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found")
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        Log.d(TAG, "Setting up notifications listener for user: ${currentUser.uid}")
        
        val listener = firestore.collection(COLLECTION_NOTIFICATIONS)
            .whereEqualTo("clientId", currentUser.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to notifications", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val notifications = snapshot.documents.mapNotNull { document ->
                        try {
                            val data = document.data
                            if (data != null) {
                                Notification.fromMap(document.id, data)
                            } else null
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parsing notification document: ${document.id}", e)
                            null
                        }
                    }
                    
                    Log.d(TAG, "Fetched ${notifications.size} notifications")
                    trySend(notifications)
                } else {
                    trySend(emptyList())
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get user's notifications (one-time fetch)
     * Kumuha ng mga notification ng user (one-time fetch)
     */
    suspend fun getUserNotificationsOnce(): Result<List<Notification>> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.failure(Exception("User must be logged in"))
            }
            
            Log.d(TAG, "Fetching notifications for user: ${currentUser.uid}")
            
            val querySnapshot = firestore.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("clientId", currentUser.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val notifications = querySnapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data
                    if (data != null) {
                        Notification.fromMap(document.id, data)
                    } else null
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing notification document: ${document.id}", e)
                    null
                }
            }
            
            Log.d(TAG, "Successfully fetched ${notifications.size} notifications")
            Result.success(notifications)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user notifications", e)
            Result.failure(Exception("Failed to fetch notifications: ${e.message}"))
        }
    }
    
    /**
     * Get unread notifications count
     * Kumuha ng bilang ng unread notifications
     */
    suspend fun getUnreadNotificationsCount(): Result<Int> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User must be logged in"))
            }
            
            val querySnapshot = firestore.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("clientId", currentUser.uid)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            val count = querySnapshot.size()
            Log.d(TAG, "Unread notifications count: $count")
            Result.success(count)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching unread notifications count", e)
            Result.failure(Exception("Failed to fetch unread count: ${e.message}"))
        }
    }
    
    /**
     * Mark notification as read
     * Markahan ang notification bilang nabasa na
     */
    suspend fun markNotificationAsRead(notificationId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Marking notification as read: $notificationId")
            
            firestore.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("isRead", true)
                .await()
            
            Log.d(TAG, "Notification marked as read successfully: $notificationId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as read", e)
            Result.failure(Exception("Failed to mark notification as read: ${e.message}"))
        }
    }
    
    /**
     * Mark all notifications as read
     * Markahan ang lahat ng notification bilang nabasa na
     */
    suspend fun markAllNotificationsAsRead(): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User must be logged in"))
            }
            
            Log.d(TAG, "Marking all notifications as read for user: ${currentUser.uid}")
            
            val querySnapshot = firestore.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("clientId", currentUser.uid)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            val batch = firestore.batch()
            querySnapshot.documents.forEach { document ->
                batch.update(document.reference, "isRead", true)
            }
            
            batch.commit().await()
            
            Log.d(TAG, "All notifications marked as read successfully")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error marking all notifications as read", e)
            Result.failure(Exception("Failed to mark all notifications as read: ${e.message}"))
        }
    }
    
    /**
     * Create a new notification (for testing purposes)
     * Gumawa ng bagong notification (para sa testing)
     */
    suspend fun createNotification(
        title: String,
        message: String,
        type: NotificationType = NotificationType.GENERAL,
        appointmentId: String? = null
    ): Result<String> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User must be logged in"))
            }
            
            val notificationId = firestore.collection(COLLECTION_NOTIFICATIONS).document().id
            val currentTime = System.currentTimeMillis()
            
            val notification = Notification(
                id = notificationId,
                clientId = currentUser.uid,
                clientName = currentUser.displayName ?: "User",
                clientEmail = currentUser.email ?: "",
                title = title,
                message = message,
                type = type,
                appointmentId = appointmentId,
                createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                timestamp = currentTime,
                isRead = false
            )
            
            firestore.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .set(notification.toMap())
                .await()
            
            Log.d(TAG, "Notification created successfully: $notificationId")
            Result.success(notificationId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification", e)
            Result.failure(Exception("Failed to create notification: ${e.message}"))
        }
    }
}







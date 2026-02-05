package com.brightcare.patient.data.service

import android.util.Log
import com.brightcare.patient.data.model.Notification
import com.brightcare.patient.data.model.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local notification manager that creates notifications directly in Firestore
 * Simple alternative to Firebase Cloud Functions
 * 
 * Local notification manager na gumagawa ng notifications directly sa Firestore
 * Simple na alternatibo sa Firebase Cloud Functions
 */
@Singleton
class LocalNotificationManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    companion object {
        private const val TAG = "LocalNotificationManager"
        private const val COLLECTION_NOTIFICATIONS = "notifications"
    }
    
    /**
     * Create notification for new appointment booking
     * Gumawa ng notification para sa bagong appointment booking
     */
    suspend fun createNewBookingNotification(
        appointmentId: String,
        date: String,
        time: String,
        chiropractorName: String = "Doctor"
    ): Result<String> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.failure(Exception("User must be logged in"))
            }
            
            val notificationId = firestore.collection(COLLECTION_NOTIFICATIONS).document().id
            val currentTime = System.currentTimeMillis()
            
            val notification = Notification(
                clientId = currentUser.uid,
                clientName = currentUser.displayName ?: "Patient",
                clientEmail = currentUser.email ?: "",
                title = "New Appointment Booking",
                message = "New appointment booked by ${currentUser.displayName ?: "Patient"} for Chiropractic Care",
                type = NotificationType.NEW_BOOKING,
                appointmentId = appointmentId,
                service = "Chiropractic Care",
                date = date,
                time = time,
                createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                timestamp = currentTime,
                isRead = false,
                clientPhone = currentUser.phoneNumber
            )
            
            firestore.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .set(notification.toMap())
                .await()
            
            Log.d(TAG, "New booking notification created: $notificationId")
            Result.success(notificationId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating new booking notification", e)
            Result.failure(e)
        }
    }
    
    /**
     * Create notification for appointment approval
     * Gumawa ng notification para sa appointment approval
     */
    suspend fun createApprovalNotification(
        appointmentId: String,
        date: String,
        time: String,
        chiropractorName: String = "Doctor"
    ): Result<String> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.failure(Exception("User must be logged in"))
            }
            
            val notificationId = firestore.collection(COLLECTION_NOTIFICATIONS).document().id
            val currentTime = System.currentTimeMillis()
            
            val notification = Notification(
                clientId = currentUser.uid,
                clientName = currentUser.displayName ?: "Patient",
                clientEmail = currentUser.email ?: "",
                title = "Appointment Approved ✅",
                message = "Your appointment for $date at $time has been approved!",
                type = NotificationType.APPOINTMENT_APPROVED,
                appointmentId = appointmentId,
                service = "Chiropractic Care",
                date = date,
                time = time,
                createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                timestamp = currentTime,
                isRead = false,
                clientPhone = currentUser.phoneNumber
            )
            
            firestore.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .set(notification.toMap())
                .await()
            
            Log.d(TAG, "Approval notification created: $notificationId")
            Result.success(notificationId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating approval notification", e)
            Result.failure(e)
        }
    }
    
    /**
     * Create notification for appointment rejection
     * Gumawa ng notification para sa appointment rejection
     */
    suspend fun createRejectionNotification(
        appointmentId: String,
        date: String,
        time: String,
        chiropractorName: String = "Doctor",
        reason: String = ""
    ): Result<String> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.failure(Exception("User must be logged in"))
            }
            
            val notificationId = firestore.collection(COLLECTION_NOTIFICATIONS).document().id
            val currentTime = System.currentTimeMillis()
            
            val message = if (reason.isNotBlank()) {
                "Your appointment with $chiropractorName on $date at $time has been declined. Reason: $reason"
            } else {
                "Your appointment with $chiropractorName on $date at $time has been declined. Please contact the clinic for more information."
            }
            
            val notification = Notification(
                clientId = currentUser.uid,
                clientName = currentUser.displayName ?: "Patient",
                clientEmail = currentUser.email ?: "",
                title = "Appointment Rejected ❌",
                message = "Your appointment for $date at $time has been rejected.",
                type = NotificationType.APPOINTMENT_REJECTED,
                appointmentId = appointmentId,
                service = "Chiropractic Care",
                date = date,
                time = time,
                createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                timestamp = currentTime,
                isRead = false,
                clientPhone = currentUser.phoneNumber
            )
            
            firestore.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .set(notification.toMap())
                .await()
            
            Log.d(TAG, "Rejection notification created: $notificationId")
            Result.success(notificationId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating rejection notification", e)
            Result.failure(e)
        }
    }
    
    /**
     * Create notification for new message
     * Gumawa ng notification para sa bagong message
     */
    suspend fun createMessageNotification(
        senderName: String,
        messagePreview: String,
        conversationId: String
    ): Result<String> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.failure(Exception("User must be logged in"))
            }
            
            val notificationId = firestore.collection(COLLECTION_NOTIFICATIONS).document().id
            val currentTime = System.currentTimeMillis()
            
            val notification = Notification(
                id = notificationId,
                clientId = currentUser.uid,
                clientName = currentUser.displayName ?: "Patient",
                clientEmail = currentUser.email ?: "",
                title = "New Message from $senderName 💬",
                message = messagePreview,
                type = NotificationType.MESSAGE_RECEIVED,
                appointmentId = conversationId, // Using appointmentId field for conversationId
                service = "Message",
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                timestamp = currentTime,
                isRead = false
            )
            
            firestore.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .set(notification.toMap())
                .await()
            
            Log.d(TAG, "Message notification created: $notificationId")
            Result.success(notificationId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating message notification", e)
            Result.failure(e)
        }
    }
    
    /**
     * Create a general notification
     * Gumawa ng general notification
     */
    suspend fun createGeneralNotification(
        title: String,
        message: String,
        type: NotificationType = NotificationType.GENERAL
    ): Result<String> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.failure(Exception("User must be logged in"))
            }
            
            val notificationId = firestore.collection(COLLECTION_NOTIFICATIONS).document().id
            val currentTime = System.currentTimeMillis()
            
            val notification = Notification(
                id = notificationId,
                clientId = currentUser.uid,
                clientName = currentUser.displayName ?: "Patient",
                clientEmail = currentUser.email ?: "",
                title = title,
                message = message,
                type = type,
                appointmentId = null,
                service = "BrightCare",
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                timestamp = currentTime,
                isRead = false
            )
            
            firestore.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .set(notification.toMap())
                .await()
            
            Log.d(TAG, "General notification created: $notificationId")
            Result.success(notificationId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error creating general notification", e)
            Result.failure(e)
        }
    }
}

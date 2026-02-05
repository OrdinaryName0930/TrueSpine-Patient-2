package com.brightcare.patient.data.service

import android.util.Log
import com.brightcare.patient.data.model.NotificationType
import com.brightcare.patient.data.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for testing notification functionality
 * Service para sa pag-test ng notification functionality
 */
@Singleton
class NotificationTestService @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    companion object {
        private const val TAG = "NotificationTestService"
    }
    
    /**
     * Test creating a notification manually
     * I-test ang pag-create ng notification manually
     */
    suspend fun createTestNotification(): Result<String> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User must be logged in"))
            }
            
            Log.d(TAG, "Creating test notification for user: ${currentUser.uid}")
            
            val result = notificationRepository.createNotification(
                title = "Test Notification 🔔",
                message = "This is a test notification to verify the system is working correctly!",
                type = NotificationType.GENERAL,
                appointmentId = null
            )
            
            result.fold(
                onSuccess = { notificationId ->
                    Log.d(TAG, "Test notification created successfully: $notificationId")
                    Result.success(notificationId)
                },
                onFailure = { exception ->
                    Log.e(TAG, "Failed to create test notification", exception)
                    Result.failure(exception)
                }
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in createTestNotification", e)
            Result.failure(e)
        }
    }
    
    /**
     * Test appointment status change notification
     * I-test ang notification para sa appointment status change
     */
    suspend fun testAppointmentStatusNotification(
        appointmentId: String,
        newStatus: String
    ): Result<String> {
        return try {
            Log.d(TAG, "Testing appointment status notification: $appointmentId -> $newStatus")
            
            // Update appointment status in Firestore to trigger the Cloud Function
            val updateData = mapOf(
                "status" to newStatus,
                "updatedAt" to System.currentTimeMillis() / 1000
            )
            
            firestore.collection("appointment")
                .document(appointmentId)
                .update(updateData)
                .await()
            
            Log.d(TAG, "Appointment status updated, Cloud Function should trigger notification")
            Result.success("Status updated to $newStatus")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating appointment status", e)
            Result.failure(e)
        }
    }
    
    /**
     * Simulate booking approval notification
     * I-simulate ang booking approval notification
     */
    suspend fun simulateBookingApproval(appointmentId: String): Result<String> {
        return testAppointmentStatusNotification(appointmentId, "approved")
    }
    
    /**
     * Simulate booking rejection notification
     * I-simulate ang booking rejection notification
     */
    suspend fun simulateBookingRejection(appointmentId: String): Result<String> {
        return testAppointmentStatusNotification(appointmentId, "rejected")
    }
    
    /**
     * Get user's recent appointments for testing
     * Kumuha ng mga recent appointments ng user para sa testing
     */
    suspend fun getUserRecentAppointments(): Result<List<Map<String, Any>>> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User must be logged in"))
            }
            
            val snapshot = firestore.collection("appointment")
                .whereEqualTo("clientId", currentUser.uid)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .await()
            
            val appointments = snapshot.documents.map { doc ->
                mapOf(
                    "id" to doc.id,
                    "status" to (doc.data?.get("status") ?: "unknown"),
                    "date" to (doc.data?.get("date") ?: ""),
                    "time" to (doc.data?.get("time") ?: "")
                )
            }
            
            Log.d(TAG, "Retrieved ${appointments.size} recent appointments")
            Result.success(appointments)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent appointments", e)
            Result.failure(e)
        }
    }
}

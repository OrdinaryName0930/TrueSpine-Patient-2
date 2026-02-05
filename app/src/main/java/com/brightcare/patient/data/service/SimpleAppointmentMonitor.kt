package com.brightcare.patient.data.service

import android.util.Log
import com.brightcare.patient.data.model.NotificationType
import com.brightcare.patient.data.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple appointment status monitor using existing NotificationRepository
 * Simple na appointment status monitor gamit ang existing NotificationRepository
 */
@Singleton
class SimpleAppointmentMonitor @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository
) {
    
    companion object {
        private const val TAG = "SimpleAppointmentMonitor"
        private const val COLLECTION_APPOINTMENTS = "appointment"
    }
    
    private var appointmentListener: ListenerRegistration? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Start monitoring appointment status changes
     * Simulan ang pag-monitor ng mga pagbabago sa status ng appointment
     */
    fun startMonitoring() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found")
            return
        }
        
        Log.d(TAG, "Starting simple appointment monitoring for user: ${currentUser.uid}")
        
        // Stop any existing listener
        stopMonitoring()
        
        // Listen for changes to user's appointments
        appointmentListener = firestore.collection(COLLECTION_APPOINTMENTS)
            .whereEqualTo("clientId", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to appointment changes", error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    Log.d(TAG, "Received appointment changes: ${snapshot.documentChanges.size} changes")
                    
                    for (docChange in snapshot.documentChanges) {
                        when (docChange.type) {
                            com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                Log.d(TAG, "Appointment modified: ${docChange.document.id}")
                                handleAppointmentStatusChange(docChange)
                            }
                            com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                                Log.d(TAG, "Appointment added: ${docChange.document.id}")
                            }
                            com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                Log.d(TAG, "Appointment removed: ${docChange.document.id}")
                            }
                        }
                    }
                }
            }
    }
    
    /**
     * Stop monitoring appointment status changes
     * Ihinto ang pag-monitor ng mga pagbabago sa status ng appointment
     */
    fun stopMonitoring() {
        appointmentListener?.remove()
        appointmentListener = null
        Log.d(TAG, "Stopped appointment monitoring")
    }
    
    /**
     * Handle appointment status changes
     * I-handle ang mga pagbabago sa status ng appointment
     */
    private fun handleAppointmentStatusChange(docChange: com.google.firebase.firestore.DocumentChange) {
        coroutineScope.launch {
            try {
                val appointmentData = docChange.document.data
                val appointmentId = docChange.document.id
                val status = appointmentData["status"] as? String ?: return@launch
                val date = appointmentData["date"] as? String ?: ""
                val time = appointmentData["time"] as? String ?: ""
                val chiropractorName = appointmentData["chiropractorName"] as? String ?: "Doctor"
                
                Log.d(TAG, "Processing status change for appointment $appointmentId: status = $status")
                
                when (status) {
                    "approved" -> {
                        Log.d(TAG, "Creating approval notification...")
                        val result = notificationRepository.createNotification(
                            title = "Appointment Approved ✅",
                            message = "Great news! Your appointment with $chiropractorName on $date at $time has been approved.",
                            type = NotificationType.APPOINTMENT_APPROVED,
                            appointmentId = appointmentId
                        )
                        result.fold(
                            onSuccess = { notificationId ->
                                Log.d(TAG, "Approval notification created: $notificationId")
                            },
                            onFailure = { exception ->
                                Log.e(TAG, "Failed to create approval notification: ${exception.message}")
                            }
                        )
                    }
                    
                    "rejected" -> {
                        Log.d(TAG, "Creating rejection notification...")
                        val message = appointmentData["message"] as? String ?: ""
                        val notificationMessage = if (message.isNotBlank()) {
                            "Your appointment with $chiropractorName on $date at $time has been declined. Reason: $message"
                        } else {
                            "Your appointment with $chiropractorName on $date at $time has been declined. Please contact the clinic for more information."
                        }
                        
                        val result = notificationRepository.createNotification(
                            title = "Appointment Declined ❌",
                            message = notificationMessage,
                            type = NotificationType.APPOINTMENT_REJECTED,
                            appointmentId = appointmentId
                        )
                        result.fold(
                            onSuccess = { notificationId ->
                                Log.d(TAG, "Rejection notification created: $notificationId")
                            },
                            onFailure = { exception ->
                                Log.e(TAG, "Failed to create rejection notification: ${exception.message}")
                            }
                        )
                    }
                    
                    else -> {
                        Log.d(TAG, "Status '$status' doesn't require notification")
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error handling appointment status change", e)
            }
        }
    }
}

package com.brightcare.patient.data.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors appointment status changes and creates notifications
 * Simple alternative to Firebase Cloud Functions
 * 
 * Nag-monitor ng mga pagbabago sa status ng appointment at gumagawa ng notifications
 * Simple na alternatibo sa Firebase Cloud Functions
 */
@Singleton
class AppointmentStatusMonitor @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val localNotificationManager: LocalNotificationManager
) {
    
    companion object {
        private const val TAG = "AppointmentStatusMonitor"
        private const val COLLECTION_APPOINTMENTS = "appointment"
    }
    
    private var appointmentListener: ListenerRegistration? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Start monitoring appointment status changes for the current user
     * Simulan ang pag-monitor ng mga pagbabago sa status ng appointment para sa current user
     */
    fun startMonitoring() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found")
            return
        }
        
        Log.d(TAG, "Starting appointment status monitoring for user: ${currentUser.uid}")
        
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
                    for (docChange in snapshot.documentChanges) {
                        when (docChange.type) {
                            com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                handleAppointmentStatusChange(docChange)
                            }
                            else -> {
                                // We only care about modifications (status changes)
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
        Log.d(TAG, "Stopped appointment status monitoring")
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
                
                Log.d(TAG, "Appointment $appointmentId status changed to: $status")
                
                when (status) {
                    "approved" -> {
                        localNotificationManager.createApprovalNotification(
                            appointmentId = appointmentId,
                            date = date,
                            time = time,
                            chiropractorName = chiropractorName
                        )
                        Log.d(TAG, "Created approval notification for appointment: $appointmentId")
                    }
                    
                    "rejected" -> {
                        val message = appointmentData["message"] as? String ?: ""
                        localNotificationManager.createRejectionNotification(
                            appointmentId = appointmentId,
                            date = date,
                            time = time,
                            chiropractorName = chiropractorName,
                            reason = message
                        )
                        Log.d(TAG, "Created rejection notification for appointment: $appointmentId")
                    }
                    
                    else -> {
                        Log.d(TAG, "Status $status doesn't require notification")
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error handling appointment status change", e)
            }
        }
    }
}

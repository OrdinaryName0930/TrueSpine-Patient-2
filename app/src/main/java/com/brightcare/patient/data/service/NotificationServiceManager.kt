package com.brightcare.patient.data.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages all notification-related services
 * Nag-manage ng lahat ng notification-related services
 */
@Singleton
class NotificationServiceManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val appointmentStatusMonitor: AppointmentStatusMonitor,
    private val simpleAppointmentMonitor: SimpleAppointmentMonitor
) {
    
    companion object {
        private const val TAG = "NotificationServiceManager"
    }
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var isMonitoringStarted = false
    
    /**
     * Start all notification monitoring services
     * Simulan ang lahat ng notification monitoring services
     */
    fun startNotificationServices() {
        coroutineScope.launch {
            try {
                val currentUser = firebaseAuth.currentUser
                if (currentUser == null) {
                    Log.w(TAG, "No authenticated user found, cannot start notification services")
                    return@launch
                }
                
                if (isMonitoringStarted) {
                    Log.d(TAG, "Notification services already started")
                    return@launch
                }
                
                Log.d(TAG, "🚀 Starting notification services for user: ${currentUser.uid}")
                
                // Start appointment status monitoring
                appointmentStatusMonitor.startMonitoring()
                Log.d(TAG, "✅ AppointmentStatusMonitor started")
                
                // Start simple appointment monitoring as backup
                simpleAppointmentMonitor.startMonitoring()
                Log.d(TAG, "✅ SimpleAppointmentMonitor started")
                
                isMonitoringStarted = true
                Log.d(TAG, "🎉 All notification services started successfully!")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error starting notification services", e)
            }
        }
    }
    
    /**
     * Stop all notification monitoring services
     * Ihinto ang lahat ng notification monitoring services
     */
    fun stopNotificationServices() {
        try {
            Log.d(TAG, "🛑 Stopping notification services...")
            
            appointmentStatusMonitor.stopMonitoring()
            simpleAppointmentMonitor.stopMonitoring()
            
            isMonitoringStarted = false
            Log.d(TAG, "✅ All notification services stopped")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error stopping notification services", e)
        }
    }
    
    /**
     * Restart notification services (useful after login/logout)
     * I-restart ang notification services (kapaki-pakinabang pagkatapos ng login/logout)
     */
    fun restartNotificationServices() {
        Log.d(TAG, "🔄 Restarting notification services...")
        stopNotificationServices()
        startNotificationServices()
    }
    
    /**
     * Check if monitoring services are running
     * Tingnan kung tumatakbo ang monitoring services
     */
    fun isMonitoringActive(): Boolean {
        return isMonitoringStarted && firebaseAuth.currentUser != null
    }
}

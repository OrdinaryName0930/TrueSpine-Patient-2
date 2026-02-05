package com.brightcare.patient.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.model.Appointment
import com.brightcare.patient.data.model.Notification
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.brightcare.patient.data.repository.BookingRepository
import com.brightcare.patient.data.repository.NotificationRepository
import com.brightcare.patient.data.repository.CompleteProfileRepository
import com.brightcare.patient.data.repository.MessagingRepository
import com.brightcare.patient.data.model.NotificationType
import com.brightcare.patient.data.service.SimpleAppointmentMonitor
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for Home Screen
 * ViewModel para sa Home Screen
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val notificationRepository: NotificationRepository,
    private val completeProfileRepository: CompleteProfileRepository,
    private val messagingRepository: MessagingRepository,
    private val firebaseAuth: FirebaseAuth,
    private val simpleAppointmentMonitor: SimpleAppointmentMonitor
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Today's appointments
    private val _todaysAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val todaysAppointments: StateFlow<List<Appointment>> = _todaysAppointments.asStateFlow()

    // All upcoming appointments (excluding today)
    private val _upcomingAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val upcomingAppointments: StateFlow<List<Appointment>> = _upcomingAppointments.asStateFlow()

    // Notifications
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    // Unread notifications count
    private val _unreadNotificationsCount = MutableStateFlow(0)
    val unreadNotificationsCount: StateFlow<Int> = _unreadNotificationsCount.asStateFlow()

    // User's first name
    private val _userFirstName = MutableStateFlow<String?>(null)
    val userFirstName: StateFlow<String?> = _userFirstName.asStateFlow()

    // User's profile picture URL
    private val _userProfilePictureUrl = MutableStateFlow<String?>(null)
    val userProfilePictureUrl: StateFlow<String?> = _userProfilePictureUrl.asStateFlow()

    // Loading states
    private val _isLoadingAppointments = MutableStateFlow(false)
    val isLoadingAppointments: StateFlow<Boolean> = _isLoadingAppointments.asStateFlow()

    private val _isLoadingNotifications = MutableStateFlow(false)
    val isLoadingNotifications: StateFlow<Boolean> = _isLoadingNotifications.asStateFlow()

    // Error states
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadUserData()
        loadAppointments()
        loadNotifications()
        observeNotifications()
        
        // Start monitoring appointment status changes
        simpleAppointmentMonitor.startMonitoring()
    }

    /**
     * Load user data including first name and profile picture
     * I-load ang user data kasama ang first name at profile picture
     */
    private fun loadUserData() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // Extract first name from display name or use email
            val displayName = currentUser.displayName
            val firstName = if (!displayName.isNullOrBlank()) {
                displayName.split(" ").firstOrNull() ?: "User"
            } else {
                currentUser.email?.split("@")?.firstOrNull()?.capitalize() ?: "User"
            }
            _userFirstName.value = firstName
            
            // Load profile picture from Firestore
            viewModelScope.launch {
                try {
                    val profileResult = completeProfileRepository.getProfileData()
                    profileResult.onSuccess { profileData ->
                        _userProfilePictureUrl.value = profileData?.profilePictureUrl
                    }.onFailure {
                        _userProfilePictureUrl.value = null
                    }
                } catch (e: Exception) {
                    _userProfilePictureUrl.value = null
                }
            }
        }
    }

    /**
     * Load appointments and filter for today and upcoming
     * I-load ang mga appointment at i-filter para sa ngayon at upcoming
     */
    fun loadAppointments() {
        viewModelScope.launch {
            _isLoadingAppointments.value = true
            _errorMessage.value = null

            try {
                val result = bookingRepository.getUserAppointments()
                result.fold(
                    onSuccess = { appointments ->
                        filterAppointments(appointments)
                        updateUiState { it.copy(isLoadingAppointments = false) }
                    },
                    onFailure = { exception ->
                        _errorMessage.value = exception.message
                        updateUiState { it.copy(isLoadingAppointments = false, errorMessage = exception.message) }
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message
                updateUiState { it.copy(isLoadingAppointments = false, errorMessage = e.message) }
            } finally {
                _isLoadingAppointments.value = false
            }
        }
    }

    /**
     * Filter appointments into today's and upcoming
     * I-filter ang mga appointment sa ngayon at upcoming
     */
    private fun filterAppointments(appointments: List<Appointment>) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Filter today's appointments
        val todaysAppts = appointments.filter { appointment ->
            appointment.date == today && 
            (appointment.status == "pending" || appointment.status == "confirmed" || appointment.status == "approved")
        }

        // Filter ALL upcoming appointments (excluding today) - no date limit
        val upcomingAppts = appointments.filter { appointment ->
            appointment.date > today &&
            (appointment.status == "pending" || appointment.status == "confirmed" || appointment.status == "approved")
        }.sortedBy { it.date } // Sort by date to show nearest appointments first

        _todaysAppointments.value = todaysAppts
        _upcomingAppointments.value = upcomingAppts

        updateUiState { currentState ->
            currentState.copy(
                todaysAppointments = todaysAppts,
                upcomingAppointments = upcomingAppts
            )
        }
    }

    /**
     * Load notifications (one-time fetch)
     * I-load ang mga notification (one-time fetch)
     */
    private fun loadNotifications() {
        viewModelScope.launch {
            _isLoadingNotifications.value = true

            try {
                val result = notificationRepository.getUserNotificationsOnce()
                result.fold(
                    onSuccess = { notifications ->
                        _notifications.value = notifications.take(5) // Show only latest 5
                        val unreadCount = notifications.count { !it.isRead }
                        _unreadNotificationsCount.value = unreadCount
                        
                        updateUiState { currentState ->
                            currentState.copy(
                                notifications = notifications.take(5),
                                unreadNotificationsCount = unreadCount,
                                isLoadingNotifications = false
                            )
                        }
                    },
                    onFailure = { exception ->
                        _errorMessage.value = exception.message
                        updateUiState { it.copy(isLoadingNotifications = false, errorMessage = exception.message) }
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message
                updateUiState { it.copy(isLoadingNotifications = false, errorMessage = e.message) }
            } finally {
                _isLoadingNotifications.value = false
            }
        }
    }

    /**
     * Observe notifications for real-time updates
     * Mag-observe ng mga notification para sa real-time updates
     */
    private fun observeNotifications() {
        viewModelScope.launch {
            notificationRepository.getUserNotifications()
                .catch { exception ->
                    _errorMessage.value = exception.message
                }
                .collect { notifications ->
                    _notifications.value = notifications.take(5) // Show only latest 5
                    val unreadCount = notifications.count { !it.isRead }
                    _unreadNotificationsCount.value = unreadCount
                    
                    updateUiState { currentState ->
                        currentState.copy(
                            notifications = notifications.take(5),
                            unreadNotificationsCount = unreadCount
                        )
                    }
                }
        }
    }

    /**
     * Mark notification as read
     * Markahan ang notification bilang nabasa na
     */
    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markNotificationAsRead(notificationId)
        }
    }

    /**
     * Mark all notifications as read
     * Markahan ang lahat ng notification bilang nabasa na
     */
    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllNotificationsAsRead()
        }
    }

    /**
     * Refresh all data
     * I-refresh ang lahat ng data
     */
    fun refreshData() {
        loadAppointments()
        loadNotifications()
    }

    /**
     * Clear error message
     * I-clear ang error message
     */
    fun clearError() {
        _errorMessage.value = null
        updateUiState { it.copy(errorMessage = null) }
    }

    /**
     * Make phone call to chiropractor
     * Tumawag sa chiropractor
     */
    fun makePhoneCall(context: Context, chiropractorId: String) {
        viewModelScope.launch {
            try {
                val phoneResult = messagingRepository.getChiropractorPhoneNumber(chiropractorId)
                
                phoneResult.fold(
                    onSuccess = { phoneNumber ->
                        val intent = Intent(Intent.ACTION_CALL).apply {
                            data = Uri.parse("tel:$phoneNumber")
                        }
                        context.startActivity(intent)
                    },
                    onFailure = { exception ->
                        _errorMessage.value = exception.message ?: "Phone number not available"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to make phone call"
            }
        }
    }

    /**
     * Helper function to update UI state
     * Helper function para sa pag-update ng UI state
     */
    private fun updateUiState(update: (HomeUiState) -> HomeUiState) {
        _uiState.value = update(_uiState.value)
    }
    
    /**
     * Create a test notification for development/testing purposes
     * Gumawa ng test notification para sa development/testing
     */
    fun createTestNotification() {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "Creating test notification using NotificationRepository...")
                
                val result = notificationRepository.createNotification(
                    title = "Test Notification 🔔",
                    message = "This is a test notification created at ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}. The notification system is working!",
                    type = NotificationType.GENERAL
                )
                
                result.fold(
                    onSuccess = { notificationId ->
                        Log.d("HomeViewModel", "Test notification created successfully: $notificationId")
                        // Refresh notifications to show the new one
                        loadNotifications()
                    },
                    onFailure = { exception ->
                        Log.e("HomeViewModel", "Failed to create test notification", exception)
                        _errorMessage.value = "Failed to create test notification: ${exception.message}"
                    }
                )
                
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error creating test notification", e)
                _errorMessage.value = "Error creating test notification: ${e.message}"
            }
        }
    }
    
    /**
     * Simulate appointment approval for testing
     * I-simulate ang appointment approval para sa testing
     */
    fun simulateAppointmentApproval(appointmentId: String) {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "Simulating appointment approval for: $appointmentId")
                
                // Update appointment status to approved
                val updateData = mapOf(
                    "status" to "approved",
                    "updatedAt" to System.currentTimeMillis() / 1000
                )
                
                firebaseAuth.currentUser?.let { user ->
                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    firestore.collection("appointment")
                        .document(appointmentId)
                        .update(updateData)
                        .addOnSuccessListener {
                            Log.d("HomeViewModel", "Appointment status updated to approved")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("HomeViewModel", "Failed to update appointment status", exception)
                        }
                }
                
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error simulating appointment approval", e)
            }
        }
    }
    
    /**
     * Simulate appointment rejection for testing
     * I-simulate ang appointment rejection para sa testing
     */
    fun simulateAppointmentRejection(appointmentId: String) {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "Simulating appointment rejection for: $appointmentId")
                
                // Update appointment status to rejected
                val updateData = mapOf(
                    "status" to "rejected",
                    "message" to "Unfortunately, this time slot is no longer available. Please book another time.",
                    "updatedAt" to System.currentTimeMillis() / 1000
                )
                
                firebaseAuth.currentUser?.let { user ->
                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    firestore.collection("appointment")
                        .document(appointmentId)
                        .update(updateData)
                        .addOnSuccessListener {
                            Log.d("HomeViewModel", "Appointment status updated to rejected")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("HomeViewModel", "Failed to update appointment status", exception)
                        }
                }
                
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error simulating appointment rejection", e)
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Stop monitoring when ViewModel is cleared
        simpleAppointmentMonitor.stopMonitoring()
    }
}

/**
 * UI State for Home Screen
 * UI State para sa Home Screen
 */
data class HomeUiState(
    val todaysAppointments: List<Appointment> = emptyList(),
    val upcomingAppointments: List<Appointment> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val unreadNotificationsCount: Int = 0,
    val isLoadingAppointments: Boolean = false,
    val isLoadingNotifications: Boolean = false,
    val errorMessage: String? = null
)














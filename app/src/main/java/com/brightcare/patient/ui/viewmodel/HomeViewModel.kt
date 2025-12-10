package com.brightcare.patient.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.model.Appointment
import com.brightcare.patient.data.model.Notification
import com.brightcare.patient.data.repository.BookingRepository
import com.brightcare.patient.data.repository.NotificationRepository
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
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Today's appointments
    private val _todaysAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val todaysAppointments: StateFlow<List<Appointment>> = _todaysAppointments.asStateFlow()

    // Upcoming appointments (next 7 days)
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
    }

    /**
     * Load user data including first name
     * I-load ang user data kasama ang first name
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
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7) // Next 7 days
        val nextWeek = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        // Filter today's appointments
        val todaysAppts = appointments.filter { appointment ->
            appointment.date == today && 
            (appointment.status == "pending" || appointment.status == "confirmed" || appointment.status == "approved")
        }

        // Filter upcoming appointments (next 7 days, excluding today)
        val upcomingAppts = appointments.filter { appointment ->
            appointment.date > today && 
            appointment.date <= nextWeek &&
            (appointment.status == "pending" || appointment.status == "confirmed" || appointment.status == "approved")
        }

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
     * Helper function to update UI state
     * Helper function para sa pag-update ng UI state
     */
    private fun updateUiState(update: (HomeUiState) -> HomeUiState) {
        _uiState.value = update(_uiState.value)
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







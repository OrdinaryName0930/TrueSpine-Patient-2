package com.brightcare.patient.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.model.*
import com.brightcare.patient.data.repository.BookingRepository
import com.brightcare.patient.data.repository.ChiropractorSearchRepository
import com.brightcare.patient.data.repository.ProfileValidationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for booking appointments
 * ViewModel para sa pag-book ng appointments
 */
@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val profileValidationService: ProfileValidationService,
    private val chiropractorSearchRepository: ChiropractorSearchRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "BookingViewModel"
    }
    
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()
    
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()
    
    private val _upcomingAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val upcomingAppointments: StateFlow<List<Appointment>> = _upcomingAppointments.asStateFlow()
    
    private val _userBookedDates = MutableStateFlow<Set<String>>(emptySet())
    val userBookedDates: StateFlow<Set<String>> = _userBookedDates.asStateFlow()
    
    private val _doctorBookedTimes = MutableStateFlow<Set<String>>(emptySet())
    val doctorBookedTimes: StateFlow<Set<String>> = _doctorBookedTimes.asStateFlow()
    
    init {
        loadUserAppointments()
        loadUserBookedDates()
    }
    
    /**
     * Validate profile for booking
     * I-validate ang profile para sa booking
     */
    fun validateProfileForBooking() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isValidatingProfile = true)
                
                Log.d(TAG, "Validating profile for booking")
                val validation = profileValidationService.validateProfileForBooking()
                
                Log.d(TAG, "Profile validation result: isValid=${validation.isValid}, hasPersonalDetails=${validation.hasPersonalDetails}, hasEmergencyContact=${validation.hasEmergencyContact}")
                Log.d(TAG, "Missing fields: ${validation.missingFields}")
                
                _uiState.value = _uiState.value.copy(
                    isValidatingProfile = false,
                    profileValidation = validation,
                    showProfileIncompleteDialog = !validation.isValid
                )
                
                if (validation.isValid) {
                    Log.d(TAG, "Profile validation passed - user can proceed with booking")
                } else {
                    Log.d(TAG, "Profile validation failed - showing incomplete dialog")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error validating profile", e)
                _uiState.value = _uiState.value.copy(
                    isValidatingProfile = false,
                    profileValidation = ProfileValidationResult(
                        isValid = false,
                        errorMessage = "Unable to validate profile. Please try again."
                    ),
                    showProfileIncompleteDialog = true
                )
            }
        }
    }
    
    /**
     * Set selected chiropractor for booking
     * I-set ang selected chiropractor para sa booking
     */
    fun setSelectedChiropractor(chiropractor: Chiropractor) {
        Log.d(TAG, "Setting selected chiropractor: ${chiropractor.name}")
        _uiState.value = _uiState.value.copy(
            selectedChiropractor = chiropractor,
            formState = _uiState.value.formState.copy(
                selectedChiropractorId = chiropractor.id
            )
        )
        
        // Load chiropractor unavailability data
        loadChiropractorUnavailability(chiropractor.id)
    }
    
    /**
     * Load chiropractor by ID - used when navigating from BookAppointmentActivity to PaymentActivity
     * I-load ang chiropractor gamit ang ID - ginagamit kapag nag-navigate mula sa BookAppointmentActivity patungo sa PaymentActivity
     */
    fun loadChiropractorById(chiropractorId: String) {
        if (chiropractorId.isBlank()) {
            Log.w(TAG, "Cannot load chiropractor: ID is blank")
            return
        }
        
        // Check if already loaded
        if (_uiState.value.selectedChiropractor?.id == chiropractorId) {
            Log.d(TAG, "Chiropractor $chiropractorId already loaded")
            return
        }
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading chiropractor by ID: $chiropractorId")
                
                val result = chiropractorSearchRepository.getChiropractorById(chiropractorId)
                result.fold(
                    onSuccess = { chiropractor ->
                        if (chiropractor != null) {
                            Log.d(TAG, "Loaded chiropractor: ${chiropractor.name}")
                            _uiState.value = _uiState.value.copy(
                                selectedChiropractor = chiropractor,
                                formState = _uiState.value.formState.copy(
                                    selectedChiropractorId = chiropractor.id
                                )
                            )
                        } else {
                            Log.w(TAG, "Chiropractor not found: $chiropractorId")
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error loading chiropractor: $chiropractorId", exception)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading chiropractor", e)
            }
        }
    }
    
    /**
     * Load chiropractor unavailability data
     * I-load ang unavailability data ng chiropractor
     */
    private fun loadChiropractorUnavailability(chiropractorId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading unavailability data for chiropractor: $chiropractorId")
                
                val result = bookingRepository.getChiropractorUnavailability(chiropractorId)
                result.fold(
                    onSuccess = { unavailability ->
                        Log.d(TAG, "Loaded unavailability data with ${unavailability.dates.size} entries")
                        _uiState.value = _uiState.value.copy(
                            chiropractorUnavailability = unavailability
                        )
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error loading unavailability data", exception)
                        // Don't show error to user, just proceed without unavailability data
                        _uiState.value = _uiState.value.copy(
                            chiropractorUnavailability = ChiropractorUnavailability(chiropractorId = chiropractorId)
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading unavailability data", e)
                _uiState.value = _uiState.value.copy(
                    chiropractorUnavailability = ChiropractorUnavailability(chiropractorId = chiropractorId)
                )
            }
        }
    }
    
    /**
     * Load available time slots for selected date
     * I-load ang available time slots para sa selected date
     */
    fun loadAvailableTimeSlots(chiropractorId: String, date: Date) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                Log.d(TAG, "Loading time slots for chiropractor: $chiropractorId on date: $date")
                
                val result = bookingRepository.getAvailableTimeSlots(chiropractorId, date)
                result.fold(
                    onSuccess = { timeSlots ->
                        Log.d(TAG, "Loaded ${timeSlots.size} time slots, ${timeSlots.count { it.isAvailable }} available")
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            availableTimeSlots = timeSlots,
                            formState = _uiState.value.formState.copy(
                                selectedDate = date
                            )
                        )
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error loading time slots", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading time slots", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load available time slots"
                )
            }
        }
    }
    
    /**
     * Filter out unavailable time slots based on chiropractor unavailability
     * I-filter ang unavailable time slots base sa chiropractor unavailability
     */
    private fun filterUnavailableTimeSlots(timeSlots: List<TimeSlot>, date: Date): List<TimeSlot> {
        val unavailability = _uiState.value.chiropractorUnavailability
        if (unavailability == null) return timeSlots
        
        // Convert date to string format (YYYY-MM-DD)
        val dateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(date)
        
        // Check if the entire day is unavailable
        if (unavailability.isDateFullyUnavailable(dateString)) {
            Log.d(TAG, "Date $dateString is fully unavailable")
            return timeSlots.map { it.copy(isAvailable = false) }
        }
        
        // Filter individual time slots
        return timeSlots.map { timeSlot ->
            val isAvailable = bookingRepository.isTimeSlotAvailable(unavailability, dateString, timeSlot.time)
            timeSlot.copy(isAvailable = isAvailable && timeSlot.isAvailable)
        }
    }
    
    /**
     * Check if a date is available for booking
     * I-check kung available ang date para sa booking
     */
    fun isDateAvailable(date: Date): Boolean {
        val unavailability = _uiState.value.chiropractorUnavailability
        if (unavailability == null) return true
        
        val dateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(date)
        return bookingRepository.isDateAvailable(unavailability, dateString)
    }
    
    /**
     * Update booking form state
     * I-update ang booking form state
     */
    fun updateFormState(updatedFormState: BookingFormState) {
        _uiState.value = _uiState.value.copy(formState = updatedFormState)
    }
    
    /**
     * Select time slot
     * Pumili ng time slot
     */
    fun selectTimeSlot(time: String) {
        Log.d(TAG, "Selecting time slot: $time")
        _uiState.value = _uiState.value.copy(
            formState = _uiState.value.formState.copy(
                selectedTime = time,
                isTimeError = false,
                timeErrorMessage = ""
            )
        )
    }
    
    /**
     * Book appointment
     * Mag-book ng appointment
     */
    fun bookAppointment() {
        viewModelScope.launch {
            try {
                val formState = _uiState.value.formState
                val selectedChiropractor = _uiState.value.selectedChiropractor
                
                // Validate form
                if (!validateBookingForm(formState, selectedChiropractor)) {
                    return@launch
                }
                
                _uiState.value = _uiState.value.copy(isSaving = true)
                
                // Use chiropractor ID from formState if selectedChiropractor is null
                val chiropractorIdToUse = selectedChiropractor?.id ?: formState.selectedChiropractorId
                Log.d(TAG, "Booking appointment with chiropractor: $chiropractorIdToUse")
                
                val result = bookingRepository.bookAppointment(
                    chiropractorId = formState.selectedChiropractorId,
                    appointmentDate = formState.selectedDate!!,
                    appointmentTime = formState.selectedTime,
                    appointmentType = formState.appointmentType,
                    symptoms = if (formState.symptoms.isNotBlank()) formState.symptoms else "General consultation",
                    notes = formState.notes,
                    isFirstVisit = formState.isFirstVisit,
                    paymentOption = formState.paymentOption,
                    paymentProofUri = formState.paymentProofUri
                )
                
                result.fold(
                    onSuccess = { appointmentId ->
                        Log.d(TAG, "Appointment booked successfully: $appointmentId")
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            successMessage = "Appointment booked successfully!\nMatagumpay na na-book ang appointment!",
                            formState = BookingFormState() // Reset form
                        )
                        // Reload appointments
                        loadUserAppointments()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error booking appointment", exception)
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            errorMessage = exception.message
                        )
                    }
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error booking appointment", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Failed to book appointment. Please try again."
                )
            }
        }
    }
    
    /**
     * Validate booking form
     * I-validate ang booking form
     */
    private fun validateBookingForm(formState: BookingFormState, chiropractor: Chiropractor?): Boolean {
        Log.d(TAG, "Validating booking form...")
        Log.d(TAG, "Chiropractor object: ${chiropractor?.id}")
        Log.d(TAG, "Chiropractor ID from formState: ${formState.selectedChiropractorId}")
        Log.d(TAG, "Selected Date: ${formState.selectedDate}")
        Log.d(TAG, "Selected Time: ${formState.selectedTime}")
        Log.d(TAG, "Symptoms: '${formState.symptoms}'")
        Log.d(TAG, "Notes: '${formState.notes}'")
        Log.d(TAG, "Payment Option: '${formState.paymentOption}'")
        Log.d(TAG, "Payment Proof URI: '${formState.paymentProofUri}'")
        
        val errors = mutableMapOf<String, String>()
        
        // Check for chiropractor ID - either from object or from formState
        if (chiropractor == null && formState.selectedChiropractorId.isBlank()) {
            errors["chiropractor"] = "Please select a chiropractor"
            Log.w(TAG, "Validation failed: No chiropractor selected")
        }
        
        if (formState.selectedDate == null) {
            errors["date"] = "Please select a date"
            Log.w(TAG, "Validation failed: No date selected")
        }
        
        if (formState.selectedTime.isBlank()) {
            errors["time"] = "Please select a time"
            Log.w(TAG, "Validation failed: No time selected")
        }
        
        if (formState.symptoms.isBlank()) {
            errors["symptoms"] = "Please describe your symptoms"
            Log.w(TAG, "Validation failed: No symptoms provided")
        }
        
        if (formState.paymentProofUri.isBlank()) {
            errors["paymentProof"] = "Please upload proof of payment"
            Log.w(TAG, "Validation failed: No payment proof uploaded")
        }
        
        if (formState.paymentOption.isBlank()) {
            errors["paymentOption"] = "Please select payment option"
            Log.w(TAG, "Validation failed: No payment option selected")
        }
        
        val hasErrors = errors.isNotEmpty()
        
        if (hasErrors) {
            Log.e(TAG, "Form validation failed with errors: $errors")
            _uiState.value = _uiState.value.copy(
                formState = formState.copy(
                    isDateError = errors.containsKey("date"),
                    dateErrorMessage = errors["date"] ?: "",
                    isTimeError = errors.containsKey("time"),
                    timeErrorMessage = errors["time"] ?: "",
                    isSymptomsError = errors.containsKey("symptoms"),
                    symptomsErrorMessage = errors["symptoms"] ?: ""
                ),
                errorMessage = "Please fill in all required fields: ${errors.values.joinToString(", ")}"
            )
        } else {
            Log.d(TAG, "Form validation passed successfully")
        }
        
        return !hasErrors
    }
    
    /**
     * Load user appointments
     * I-load ang user appointments
     */
    fun loadUserAppointments() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading user appointments")
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val result = bookingRepository.getUserAppointments()
                result.fold(
                    onSuccess = { appointments ->
                        Log.d(TAG, "Loaded ${appointments.size} appointments")
                        
                        // Debug: Log appointment details
                        appointments.forEach { appointment ->
                            Log.d(TAG, "Appointment ${appointment.id}: " +
                                "chiroId=${appointment.chiroId}, " +
                                "chiropractorName='${appointment.chiropractorName}', " +
                                "chiropractorSpecialization='${appointment.chiropractorSpecialization}', " +
                                "date=${appointment.date}, " +
                                "time=${appointment.time}, " +
                                "status=${appointment.status}")
                        }
                        
                        _appointments.value = appointments
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        
                        // Also load upcoming appointments and refresh booked dates
                        loadUpcomingAppointments()
                        loadUserBookedDates()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error loading appointments", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading appointments", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load appointments"
                )
            }
        }
    }
    
    /**
     * Load user's booked dates to prevent double booking
     * I-load ang mga booked dates ng user para maiwasan ang double booking
     */
    fun loadUserBookedDates() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading user booked dates")
                
                val result = bookingRepository.getUserBookedDates()
                result.fold(
                    onSuccess = { bookedDates ->
                        Log.d(TAG, "Loaded ${bookedDates.size} booked dates: $bookedDates")
                        _userBookedDates.value = bookedDates
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error loading booked dates", exception)
                        // Don't show error to user for this, just keep empty set
                        _userBookedDates.value = emptySet()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading booked dates", e)
                _userBookedDates.value = emptySet()
            }
        }
    }
    
    /**
     * Check if a date is already booked by the user
     * I-check kung booked na ng user ang date
     */
    fun isDateAlreadyBooked(date: Date): Boolean {
        val dateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(date)
        return _userBookedDates.value.contains(dateString)
    }
    
    /**
     * Load booked times for a specific doctor and date
     * I-load ang mga booked times para sa specific doctor at date
     */
    fun loadDoctorBookedTimes(chiropractorId: String, date: Date) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading booked times for chiropractor: $chiropractorId on date: $date")
                
                val result = bookingRepository.getBookedTimesForDoctorAndDate(chiropractorId, date)
                result.fold(
                    onSuccess = { bookedTimes ->
                        Log.d(TAG, "Loaded ${bookedTimes.size} booked times: $bookedTimes")
                        _doctorBookedTimes.value = bookedTimes
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error loading doctor booked times", exception)
                        // Don't show error to user for this, just keep empty set
                        _doctorBookedTimes.value = emptySet()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading doctor booked times", e)
                _doctorBookedTimes.value = emptySet()
            }
        }
    }
    
    /**
     * Load upcoming appointments
     * I-load ang upcoming appointments
     */
    private fun loadUpcomingAppointments() {
        viewModelScope.launch {
            try {
                val result = bookingRepository.getUpcomingAppointments()
                result.fold(
                    onSuccess = { appointments ->
                        Log.d(TAG, "Loaded ${appointments.size} upcoming appointments")
                        _upcomingAppointments.value = appointments
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error loading upcoming appointments", exception)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading upcoming appointments", e)
            }
        }
    }
    
    /**
     * Cancel appointment
     * I-cancel ang appointment
     */
    fun cancelAppointment(appointmentId: String, reason: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cancelling appointment: $appointmentId")
                
                val result = bookingRepository.cancelAppointment(appointmentId, reason)
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "Appointment cancelled successfully")
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Appointment cancelled successfully.\nMatagumpay na na-cancel ang appointment."
                        )
                        // Reload appointments
                        loadUserAppointments()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error cancelling appointment", exception)
                        _uiState.value = _uiState.value.copy(
                            errorMessage = exception.message
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error cancelling appointment", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to cancel appointment"
                )
            }
        }
    }
    
    /**
     * Clear error message
     * I-clear ang error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Clear success message
     * I-clear ang success message
     */
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
    
    /**
     * Hide profile incomplete dialog
     * I-hide ang profile incomplete dialog
     */
    fun hideProfileIncompleteDialog() {
        _uiState.value = _uiState.value.copy(showProfileIncompleteDialog = false)
    }
    
    /**
     * Refresh appointments - force reload from Firestore
     * I-refresh ang appointments - pilitin ang reload mula sa Firestore
     */
    fun refreshAppointments() {
        Log.d(TAG, "Refreshing appointments...")
        loadUserAppointments()
    }
    
}

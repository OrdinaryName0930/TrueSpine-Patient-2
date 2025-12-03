package com.brightcare.patient.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.repository.CompleteProfileRepository
import com.brightcare.patient.ui.screens.CompleteProfileFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for complete profile screen
 */
data class CompleteProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val formState: CompleteProfileFormState = CompleteProfileFormState()
)

/**
 * ViewModel for managing complete profile screen state and operations
 */
@HiltViewModel
class CompleteProfileViewModel @Inject constructor(
    private val completeProfileRepository: CompleteProfileRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "CompleteProfileViewModel"
    }
    
    private val _uiState = MutableStateFlow(CompleteProfileUiState())
    val uiState: StateFlow<CompleteProfileUiState> = _uiState.asStateFlow()
    
    init {
        // Load existing profile data if available
        loadExistingProfile()
    }
    
    /**
     * Load existing profile data for editing
     */
    private fun loadExistingProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                val result = completeProfileRepository.getProfileData()
                result.fold(
                    onSuccess = { profileData ->
                        if (profileData != null) {
                            Log.d(TAG, "Existing profile data loaded")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                formState = profileData
                            )
                        } else {
                            Log.d(TAG, "No existing profile data found")
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error loading profile data", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to load profile data: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading profile", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unexpected error occurred"
                )
            }
        }
    }
    
    /**
     * Update form state
     */
    fun updateFormState(updater: (CompleteProfileFormState) -> CompleteProfileFormState) {
        val currentState = _uiState.value
        val newFormState = updater(currentState.formState)
        _uiState.value = currentState.copy(formState = newFormState)
    }
    
    /**
     * Save complete profile data to Firestore
     */
    fun saveProfile() {
        val currentFormState = _uiState.value.formState
        
        // Validate required fields before saving
        if (!isFormValid(currentFormState)) {
            Log.w(TAG, "Form validation failed")
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please fill in all required fields correctly"
            )
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isSaving = true, 
                    errorMessage = null,
                    isSuccess = false
                )
                
                Log.d(TAG, "Saving profile data...")
                
                val result = completeProfileRepository.saveCompleteProfile(currentFormState)
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "Profile saved successfully")
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isSuccess = true
                        )
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error saving profile", exception)
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isSuccess = false,
                            errorMessage = getErrorMessage(exception)
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error saving profile", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isSuccess = false,
                    errorMessage = "An unexpected error occurred. Please try again."
                )
            }
        }
    }
    
    /**
     * Update existing profile data
     */
    fun updateProfile() {
        val currentFormState = _uiState.value.formState
        
        // Validate required fields before updating
        if (!isFormValid(currentFormState)) {
            Log.w(TAG, "Form validation failed")
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please fill in all required fields correctly"
            )
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isSaving = true, 
                    errorMessage = null,
                    isSuccess = false
                )
                
                Log.d(TAG, "Updating profile data...")
                
                val result = completeProfileRepository.updateProfile(currentFormState)
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "Profile updated successfully")
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isSuccess = true
                        )
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error updating profile", exception)
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isSuccess = false,
                            errorMessage = getErrorMessage(exception)
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error updating profile", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isSuccess = false,
                    errorMessage = "An unexpected error occurred. Please try again."
                )
            }
        }
    }
    
    /**
     * Check if profile is already completed
     */
    fun checkProfileCompletion(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val result = completeProfileRepository.isProfileCompleted()
                result.fold(
                    onSuccess = { isCompleted ->
                        Log.d(TAG, "Profile completion status: $isCompleted")
                        onResult(isCompleted)
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error checking profile completion", exception)
                        onResult(false) // Default to false if error
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error checking profile completion", e)
                onResult(false)
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Reset success state
     */
    fun resetSuccessState() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
    
    /**
     * Validate form fields
     */
    private fun isFormValid(formState: CompleteProfileFormState): Boolean {
        return formState.firstName.trim().isNotBlank() &&
               formState.lastName.trim().isNotBlank() &&
               formState.birthDate.trim().isNotBlank() &&
               formState.sex.isNotBlank() &&
               formState.phoneNumber.isNotBlank() &&
               formState.province.isNotBlank() &&
               formState.municipality.isNotBlank() &&
               !formState.isFirstNameError &&
               !formState.isLastNameError &&
               !formState.isBirthDateError &&
               !formState.isSexError &&
               !formState.isPhoneNumberError &&
               !formState.isProvinceError &&
               !formState.isMunicipalityError &&
               !formState.isAdditionalAddressError
    }
    
    /**
     * Get user-friendly error message
     */
    private fun getErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("network", ignoreCase = true) == true -> 
                "Network error. Please check your internet connection and try again."
            exception.message?.contains("permission", ignoreCase = true) == true -> 
                "Permission denied. Please try logging in again."
            exception.message?.contains("not authenticated", ignoreCase = true) == true -> 
                "Session expired. Please log in again."
            else -> "Failed to save profile. Please try again."
        }
    }
}

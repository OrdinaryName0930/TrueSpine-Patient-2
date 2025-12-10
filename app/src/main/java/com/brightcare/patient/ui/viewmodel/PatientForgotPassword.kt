package com.brightcare.patient.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.model.*
import com.brightcare.patient.data.repository.PatientForgotPasswordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing forgot password flow with email link approach
 * Handles state management and business logic for the forgot password process
 */
@HiltViewModel
class PatientForgotPasswordViewModel @Inject constructor(
    private val forgotPasswordRepository: PatientForgotPasswordRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()
    
    private val _currentStep = MutableStateFlow(ForgotPasswordStep.EMAIL_INPUT)
    val currentStep: StateFlow<ForgotPasswordStep> = _currentStep.asStateFlow()
    
    companion object {
        private const val TAG = "ForgotPasswordViewModel"
    }
    
    /**
     * Send password reset link to user's email
     */
    fun sendResetLink(email: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Sending reset link to: $email")
                
                // Validate email
                val validation = forgotPasswordRepository.validateEmail(email)
                if (!validation.isEmailValid) {
                    _uiState.value = _uiState.value.copy(
                        validationState = validation,
                        errorMessage = validation.emailError
                    )
                    return@launch
                }
                
                // Update UI state to loading and clear previous results
                _uiState.value = _uiState.value.copy(
                    isSendingResetLink = true,
                    email = email.lowercase(),
                    errorMessage = null,
                    sendResetLinkResult = null, // Clear previous result before new attempt
                    validationState = ForgotPasswordValidationState()
                )
                
                // Send reset link request
                val request = SendResetLinkRequest(email = email.lowercase())
                val result = forgotPasswordRepository.sendResetLink(request)
                
                when (result) {
                    is ForgotPasswordResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isSendingResetLink = false,
                            sendResetLinkResult = result,
                            successMessage = ForgotPasswordToastMessages.RESET_LINK_SENT_SUCCESS,
                            showSuccessDialog = true
                        )
                        
                        // Move to completed step
                        _currentStep.value = ForgotPasswordStep.COMPLETED
                        
                        Log.d(TAG, "Reset link sent successfully")
                    }
                    
                    is ForgotPasswordResult.Error -> {
                        Log.d(TAG, "Processing forgot password error: ${result.exception}")
                        val errorMessage = when (result.exception) {
                            is ForgotPasswordException.EmailNotFound -> {
                                Log.d(TAG, "Email not found - setting error result")
                                ForgotPasswordToastMessages.EMAIL_NOT_FOUND
                            }
                            is ForgotPasswordException.NetworkError -> ForgotPasswordToastMessages.NETWORK_ERROR
                            else -> result.exception.message ?: "Failed to send reset link"
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isSendingResetLink = false,
                            sendResetLinkResult = result,
                            errorMessage = errorMessage,
                            showErrorDialog = true
                        )
                        
                        Log.e(TAG, "Failed to send reset link: ${result.exception.message}")
                    }
                    
                    is ForgotPasswordResult.Loading -> {
                        // Already handled above
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in sendResetLink", e)
                _uiState.value = _uiState.value.copy(
                    isSendingResetLink = false,
                    errorMessage = "An unexpected error occurred",
                    showErrorDialog = true
                )
            }
        }
    }
    
    
    /**
     * Update email in UI state
     */
    fun updateEmail(email: String) {
        val validation = forgotPasswordRepository.validateEmail(email)
        _uiState.value = _uiState.value.copy(
            email = email.lowercase(),
            validationState = validation,
            errorMessage = null, // Clear error message when user types
            // Don't clear sendResetLinkResult immediately - let it persist until next attempt
            showErrorDialog = false
        )
    }
    
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            showErrorDialog = false
        )
    }
    
    /**
     * Clear success message
     */
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            showSuccessDialog = false
        )
    }
    
    /**
     * Reset to initial state
     */
    fun resetState() {
        _uiState.value = ForgotPasswordUiState()
        _currentStep.value = ForgotPasswordStep.EMAIL_INPUT
        forgotPasswordRepository.clearState()
    }
    
    /**
     * Go back to previous step
     */
    fun goBackToPreviousStep() {
        when (_currentStep.value) {
            ForgotPasswordStep.COMPLETED -> {
                _currentStep.value = ForgotPasswordStep.EMAIL_INPUT
            }
            ForgotPasswordStep.EMAIL_INPUT -> {
                // Already at first step
            }
        }
    }
}
package com.brightcare.patient.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.model.*
import com.brightcare.patient.data.repository.PatientForgotPasswordRepository
import com.brightcare.patient.utils.EmailThrottleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val forgotPasswordRepository: PatientForgotPasswordRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    // Email throttle manager for preventing spam
    private val emailThrottleManager = EmailThrottleManager.getInstance(context)
    
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()
    
    private val _currentStep = MutableStateFlow(ForgotPasswordStep.EMAIL_INPUT)
    val currentStep: StateFlow<ForgotPasswordStep> = _currentStep.asStateFlow()
    
    companion object {
        private const val TAG = "ForgotPasswordViewModel"
    }
    
    /**
     * Send password reset link to user's email with throttling
     */
    fun sendResetLink(email: String): EmailThrottleManager.ThrottleResult {
        val normalizedEmail = email.lowercase().trim()
        
        // Check throttle before attempting to send
        val throttleResult = emailThrottleManager.canSendEmail(
            EmailThrottleManager.EmailType.PASSWORD_RESET, 
            normalizedEmail
        )
        
        if (!throttleResult.canSend) {
            Log.w(TAG, "Password reset throttled for $normalizedEmail. ${throttleResult.remainingTimeFormatted} remaining")
            
            // Update UI state to show throttle message
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please wait ${throttleResult.remainingTimeFormatted} before requesting another password reset email",
                showErrorDialog = true,
                isSendingResetLink = false
            )
            
            return throttleResult
        }
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Sending reset link to: $normalizedEmail")
                
                // Validate email
                val validation = forgotPasswordRepository.validateEmail(normalizedEmail)
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
                    email = normalizedEmail,
                    errorMessage = null,
                    sendResetLinkResult = null, // Clear previous result before new attempt
                    validationState = ForgotPasswordValidationState()
                )
                
                // Send reset link request
                val request = SendResetLinkRequest(email = normalizedEmail)
                val result = forgotPasswordRepository.sendResetLink(request)
                
                when (result) {
                    is ForgotPasswordResult.Success -> {
                        // Record successful email send
                        emailThrottleManager.recordEmailSent(
                            EmailThrottleManager.EmailType.PASSWORD_RESET,
                            normalizedEmail
                        )
                        
                        _uiState.value = _uiState.value.copy(
                            isSendingResetLink = false,
                            sendResetLinkResult = result,
                            successMessage = ForgotPasswordToastMessages.RESET_LINK_SENT_SUCCESS,
                            showSuccessDialog = true
                        )
                        
                        // Move to completed step
                        _currentStep.value = ForgotPasswordStep.COMPLETED
                        
                        Log.d(TAG, "Reset link sent successfully to $normalizedEmail")
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
                        
                        Log.e(TAG, "Failed to send reset link to $normalizedEmail: ${result.exception.message}")
                    }
                    
                    is ForgotPasswordResult.Loading -> {
                        // Already handled above
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in sendResetLink for $normalizedEmail", e)
                _uiState.value = _uiState.value.copy(
                    isSendingResetLink = false,
                    errorMessage = "An unexpected error occurred",
                    showErrorDialog = true
                )
            }
        }
        
        return throttleResult
    }
    
    /**
     * Check if password reset email can be sent (for UI state)
     */
    fun canSendPasswordReset(email: String): EmailThrottleManager.ThrottleResult {
        val normalizedEmail = email.lowercase().trim()
        return emailThrottleManager.canSendEmail(
            EmailThrottleManager.EmailType.PASSWORD_RESET, 
            normalizedEmail
        )
    }
    
    /**
     * Get throttle message for password reset
     */
    fun getPasswordResetThrottleMessage(email: String): String {
        val normalizedEmail = email.lowercase().trim()
        return emailThrottleManager.getThrottleMessage(
            EmailThrottleManager.EmailType.PASSWORD_RESET,
            normalizedEmail
        )
    }
    
    /**
     * Get toast message for password reset throttling
     */
    fun getPasswordResetThrottleToastMessage(email: String): String {
        val normalizedEmail = email.lowercase().trim()
        return emailThrottleManager.getThrottleToastMessage(
            EmailThrottleManager.EmailType.PASSWORD_RESET,
            normalizedEmail
        )
    }
    
    /**
     * Get real-time throttle result for password reset (for UI updates)
     */
    fun getPasswordResetThrottleResult(email: String): EmailThrottleManager.ThrottleResult {
        val normalizedEmail = email.lowercase().trim()
        return emailThrottleManager.canSendEmail(
            EmailThrottleManager.EmailType.PASSWORD_RESET,
            normalizedEmail
        )
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
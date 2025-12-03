package com.brightcare.patient.data.model

import java.util.Date

/**
 * Data models for forgot password functionality with email link approach
 * Handles sending password reset links via email
 */

/**
 * Request model for sending reset link to email
 */
data class SendResetLinkRequest(
    val email: String
)

/**
 * Response model for reset link send operation
 */
data class SendResetLinkResponse(
    val success: Boolean,
    val message: String
)

/**
 * Forgot password result wrapper
 */
sealed class ForgotPasswordResult {
    data class Success(val message: String, val data: Any? = null) : ForgotPasswordResult()
    data class Error(val exception: ForgotPasswordException) : ForgotPasswordResult()
    object Loading : ForgotPasswordResult()
}

/**
 * Custom exceptions for forgot password operations
 */
sealed class ForgotPasswordException(message: String) : Exception(message) {
    object EmailNotFound : ForgotPasswordException("No account found with this email address")
    object NetworkError : ForgotPasswordException("Network error. Please check your connection")
    data class Unknown(val originalMessage: String) : ForgotPasswordException("An unknown error occurred: $originalMessage")
}

/**
 * Forgot password validation state
 */
data class ForgotPasswordValidationState(
    val emailError: String? = null,
    val isEmailValid: Boolean = false
) {
    companion object {
        // Validation error messages
        const val EMAIL_REQUIRED = "Email is required"
        const val EMAIL_INVALID_FORMAT = "Please enter a valid email address"
    }
}

/**
 * Forgot password UI state
 */
data class ForgotPasswordUiState(
    // Loading states
    val isSendingResetLink: Boolean = false,
    
    // Results
    val sendResetLinkResult: ForgotPasswordResult? = null,
    
    // Form data
    val email: String = "",
    
    // Validation
    val validationState: ForgotPasswordValidationState = ForgotPasswordValidationState(),
    
    // Error states
    val errorMessage: String? = null,
    val showErrorDialog: Boolean = false,
    
    // Success states
    val showSuccessDialog: Boolean = false,
    val successMessage: String? = null
)

/**
 * Toast messages for forgot password flow
 */
object ForgotPasswordToastMessages {
    const val RESET_LINK_SENT_SUCCESS = "Password reset link sent to your email successfully! Please check your inbox."
    
    // Error messages
    const val EMAIL_NOT_FOUND = "No account found with this email address"
    const val NETWORK_ERROR = "Network error. Please check your connection and try again"
    
    // Action labels
    const val ACTION_OK = "OK"
    const val ACTION_RETRY = "Retry"
    
    // Durations (in milliseconds)
    const val SUCCESS_DURATION = 3000L
    const val ERROR_DURATION = 5000L
    const val INFO_DURATION = 4000L
}

/**
 * Forgot password flow steps
 */
enum class ForgotPasswordStep {
    EMAIL_INPUT,    // Enter email to receive reset link
    COMPLETED       // Reset link sent, redirect to login
}
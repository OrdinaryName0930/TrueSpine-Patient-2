package com.brightcare.patient.data.model

/**
 * Data models for patient login functionality
 * Handles login requests, responses, and authentication states
 */
/**
 * Request model for email/password login
 */
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Response model for successful login
 */
data class LoginResponse(
    val userId: String,
    val email: String,
    val isEmailVerified: Boolean,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val providerId: String, // "password", "google.com", "facebook.com"
    val isProfileComplete: Boolean = false // Whether user has completed their profile
)

/**
 * Login result wrapper
 */
sealed class LoginResult {
    data class Success(val response: LoginResponse) : LoginResult()
    data class Error(val exception: LoginException) : LoginResult()
    object Loading : LoginResult()
}

/**
 * Custom exception for login errors
 * Enhanced with timeout and slow network handling
 */
sealed class LoginException(message: String) : Exception(message) {
    object InvalidCredential : LoginException("Incorrect login credentials")
    object UserNotFound : LoginException("Incorrect login credentials")
    object EmailNotVerified : LoginException("Please verify your email before signing in.")
    object UserDisabled : LoginException("This user account has been disabled.")
    
    // Enhanced network/timeout exceptions for slow internet handling
    object TimeoutError : LoginException("Connection timed out. Your internet may be slow. Please try again.")
    object NoNetworkConnection : LoginException("No internet connection. Please check your network and try again.")
    object SlowNetworkError : LoginException("Your internet connection is slow. The operation is taking longer than expected.")
    data class RetryableError(val originalMessage: String, val attemptsMade: Int) : 
        LoginException("Failed after $attemptsMade attempts: $originalMessage. Please try again.")
    
    data class Unknown(val originalMessage: String) : LoginException("An unknown error occurred: $originalMessage")
    
    companion object {
        /**
         * User-friendly messages for slow connection scenarios
         */
        const val SLOW_NETWORK_TIP = "Tip: Move to an area with better signal or try using Wi-Fi."
        const val RETRY_MESSAGE = "Please tap to try again."
    }
}

// Note: SocialProvider and SocialLoginRequest are defined in patient-signup.kt
// We'll reuse those to avoid duplication

/**
 * Login validation state
 */
data class LoginValidationState(
    val emailError: String? = null,
    val passwordError: String? = null,
    val isValid: Boolean = false
) {
    companion object {
        /**
         * Validation error messages
         */
        const val EMAIL_REQUIRED = "Email is required"
        const val EMAIL_INVALID_FORMAT = "Please enter a valid email address"
        const val PASSWORD_REQUIRED = "Password is required"
    }
}

/**
 * Profile completion check result
 */
data class ProfileCompletionStatus(
    val isComplete: Boolean,
    val missingFields: List<String> = emptyList()
) {
    companion object {
        /**
         * Required profile fields
         */
        val REQUIRED_FIELDS = listOf(
            "firstName",
            "lastName",
            "phoneNumber",
            "dateOfBirth"
        )
    }
}

/**
 * Login UI state
 * Enhanced with retry support for slow network conditions
 */
data class LoginUiState(
    val isLoading: Boolean = false, // Keep for backward compatibility
    val isEmailPasswordLoading: Boolean = false, // Loading state for email/password login
    val isGoogleLoading: Boolean = false, // Loading state for Google login
    val isFacebookLoading: Boolean = false, // Loading state for Facebook login
    val loginResult: LoginResult? = null,
    val validationState: LoginValidationState = LoginValidationState(),
    val showEmailVerificationDialog: Boolean = false,
    val profileCompletionStatus: ProfileCompletionStatus? = null,
    val errorMessage: String? = null,
    val credentialError: String? = null, // Field-level credential error (UserNotFound, InvalidCredential)
    // Indicates if the error can be resolved by retrying (e.g., network issues)
    val isRetryable: Boolean = false
)

/**
 * Toast messages for login flow
 */
object LoginToastMessages {
    const val LOGIN_SUCCESS = "Login Successful!"
    const val EMAIL_VERIFICATION_REQUIRED = "Please verify your email before signing in. Check your inbox or spam for the verification link."
    const val PROFILE_INCOMPLETE = "Please complete your profile to continue."
    const val GOOGLE_LOGIN_SUCCESS = "Successfully signed in with Google"
    const val FACEBOOK_LOGIN_SUCCESS = "Successfully signed in with Facebook"
    const val SOCIAL_LOGIN_CANCELLED = "Sign in was cancelled"
    
    /**
     * Toast action labels
     */
    const val ACTION_OK = "OK"
    const val ACTION_RESEND = "Resend"
    const val ACTION_COMPLETE_PROFILE = "Complete Profile"
    
    /**
     * Toast durations (in milliseconds)
     */
    const val SUCCESS_DURATION = 3000L
    const val ERROR_DURATION = 5000L
    const val INFO_DURATION = 4000L
}
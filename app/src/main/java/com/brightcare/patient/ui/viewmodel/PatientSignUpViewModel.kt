package com.brightcare.patient.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.model.*
import com.brightcare.patient.data.repository.PatientSignUpRepository
import com.brightcare.patient.data.repository.toSignUpRequest
import com.brightcare.patient.ui.component.signup_component.SignUpFormState
import com.brightcare.patient.utils.DeviceUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Patient Sign Up Screen
 * Handles authentication logic and UI state management
 */
class PatientSignUpViewModel(
    private val context: Context
) : ViewModel() {
    
    private val signUpRepository = PatientSignUpRepository(context = context)
    
    // Expose repository states
    val authState: StateFlow<AuthResult> = signUpRepository.authState
    val emailVerificationState: StateFlow<EmailVerificationState> = signUpRepository.emailVerificationState
    
    // UI state for error handling
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()
    
    /**
     * Sign up with email and password
     */
    fun signUpWithEmailAndPassword(formState: SignUpFormState) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    emailFieldError = null
                )
                
                val deviceId = DeviceUtils.getDeviceId(context)
                val request = formState.toSignUpRequest(deviceId)
                val result = signUpRepository.signUpWithEmailAndPassword(request)
                
                when (result) {
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSignUpSuccessful = true
                        )
                    }
                    is AuthResult.Error -> {
                        handleAuthError(result.exception)
                    }
                    is AuthResult.Loading -> {
                        // Keep loading state
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Sign in with Google
     */
    fun signInWithGoogle(activity: androidx.activity.ComponentActivity) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )
                
                val result = signUpRepository.signInWithGoogle(activity)
                
                when (result) {
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSocialLoginSuccessful = true
                        )
                    }
                    is AuthResult.Error -> {
                        handleAuthError(result.exception)
                    }
                    is AuthResult.Loading -> {
                        // Keep loading state
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google sign-in failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Sign in with Facebook
     */
    fun signInWithFacebook(activity: androidx.activity.ComponentActivity) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )
                
                val result = signUpRepository.signInWithFacebook(activity)
                
                when (result) {
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSocialLoginSuccessful = true
                        )
                    }
                    is AuthResult.Error -> {
                        handleAuthError(result.exception)
                    }
                    is AuthResult.Loading -> {
                        // Keep loading state
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Facebook sign-in failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Resend email verification
     */
    fun resendEmailVerification() {
        viewModelScope.launch {
            signUpRepository.sendEmailVerification()
        }
    }
    
    /**
     * Check email verification status
     */
    fun checkEmailVerification() {
        viewModelScope.launch {
            signUpRepository.checkEmailVerification()
        }
    }
    
    /**
     * Clear error messages
     */
    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            emailFieldError = null
        )
    }
    
    /**
     * Reset UI state (for navigation)
     */
    fun resetUiState() {
        _uiState.value = SignUpUiState()
    }
    
    /**
     * Handle authentication errors and update UI state accordingly
     * Enhanced with slow network error handling
     */
    private fun handleAuthError(exception: AuthException) {
        when (exception) {
            is AuthException.EmailAlreadyInUse -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    emailFieldError = "This email address is already in use by another account."
                )
            }
            is AuthException.WeakPassword -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "The password is too weak."
                )
            }
            is AuthException.InvalidEmail -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    emailFieldError = "The email address is not valid."
                )
            }
            is AuthException.NetworkError -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Network error. Please check your internet connection and try again.",
                    isRetryable = true
                )
            }
            is AuthException.TimeoutError -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Connection timed out due to a slow internet connection. Please try again or move to a place with a better connection.",
                    isRetryable = true
                )
            }
            is AuthException.NoNetworkConnection -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No internet connection. Please check your network and try again.",
                    isRetryable = true
                )
            }
            is AuthException.SlowNetworkError -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Your internet connection is slow. Please wait or move to an area with better signal.",
                    isRetryable = true
                )
            }
            is AuthException.RetryableError -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed after ${exception.attemptsMade} attempts. Please check your connection and try again.",
                    isRetryable = true
                )
            }
            is AuthException.UserDisabled -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "This user account has been disabled."
                )
            }
            is AuthException.TooManyRequests -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Too many requests. Please try again later."
                )
            }
             is AuthException.OperationNotAllowed -> {
                 _uiState.value = _uiState.value.copy(
                     isLoading = false,
                     errorMessage = "This operation is not allowed."
                 )
             }
            is AuthException.Unknown -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Something went wrong. Please try again."
                )
            }
        }
    }
}

/**
 * UI state for the signup screen
 * Enhanced with retry support for slow network conditions
 */
data class SignUpUiState(
    val isLoading: Boolean = false,
    val isSignUpSuccessful: Boolean = false,
    val isSocialLoginSuccessful: Boolean = false,
    val errorMessage: String? = null,
    val emailFieldError: String? = null,
    // Indicates if the error can be resolved by retrying (e.g., network issues)
    val isRetryable: Boolean = false
)

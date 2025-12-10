package com.brightcare.patient.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.model.LoginRequest
import com.brightcare.patient.data.model.LoginResponse
import com.brightcare.patient.data.model.LoginResult
import com.brightcare.patient.data.model.LoginUiState
import com.brightcare.patient.data.model.LoginException
import com.brightcare.patient.data.model.LoginValidationState
import com.brightcare.patient.data.model.ProfileCompletionStatus
import com.brightcare.patient.data.repository.PatientLoginRepository
import com.brightcare.patient.ui.component.signup_component.ValidationUtils
import com.facebook.CallbackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for patient sign-in functionality
 * Manages login state, validation, and authentication flows
 */
@HiltViewModel
class PatientSignInViewModel @Inject constructor(
    private val loginRepository: PatientLoginRepository,
    private val authenticationManager: com.brightcare.patient.utils.AuthenticationManager
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    // Form fields
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    
    // Validation state
    private val _validationState = MutableStateFlow(LoginValidationState())
    val validationState: StateFlow<LoginValidationState> = _validationState.asStateFlow()
    
    // Login result from repository
    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult: StateFlow<LoginResult?> = _loginResult.asStateFlow()
    
    // Facebook callback manager
    val facebookCallbackManager: CallbackManager = CallbackManager.Factory.create()
    
    companion object {
        private const val TAG = "PatientSignInViewModel"
    }
    
    init {
        // Observe repository login state
        viewModelScope.launch {
            loginRepository.loginState.collect { result ->
                result?.let {
                    _loginResult.value = it
                    updateUiState()
                }
            }
        }
        
        // Observe form fields for real-time validation
        viewModelScope.launch {
            combine(_email, _password) { email, password ->
                validateForm(email, password)
            }.collect { validationState ->
                _validationState.value = validationState
                updateUiState()
            }
        }
    }
    
    /**
     * Update email field
     */
    fun updateEmail(email: String) {
        _email.value = email
        // Clear email-specific errors when user starts typing
        if (_validationState.value.emailError != null) {
            _validationState.value = _validationState.value.copy(emailError = null)
            updateUiState()
        }
        // Clear error message when user starts typing, but keep credential error until new login attempt
        clearErrorMessage()
        // Don't clear credential error immediately - let it persist until next login attempt
    }
    
    /**
     * Update password field
     */
    fun updatePassword(password: String) {
        _password.value = password
        // Clear password-specific errors when user starts typing
        if (_validationState.value.passwordError != null) {
            _validationState.value = _validationState.value.copy(passwordError = null)
            updateUiState()
        }
        // Clear error message when user starts typing, but keep credential error until new login attempt
        clearErrorMessage()
        // Don't clear credential error immediately - let it persist until next login attempt
    }
    
    /**
     * Sign in with email and password
     */
    fun signInWithEmailAndPassword() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting email/password sign-in")
                
                val currentEmail = _email.value
                val currentPassword = _password.value
                
                // Validate form before proceeding
                val validation = validateForm(currentEmail, currentPassword)
                _validationState.value = validation
                
                if (!validation.isValid) {
                    Log.w(TAG, "Form validation failed")
                    updateUiState()
                    return@launch
                }
                
                // Clear previous credential errors before new attempt
                clearCredentialError()
                
                // Set email/password loading state
                setEmailPasswordLoading(true)
                
                val request = LoginRequest(
                    email = currentEmail.trim(),
                    password = currentPassword
                )
                
                val result = loginRepository.signInWithEmailAndPassword(request)
                _loginResult.value = result
                
                when (result) {
                    is LoginResult.Success -> {
                        Log.d(TAG, "Email/password sign-in successful")
                        
                        // Save login state for persistent login
                        authenticationManager.saveLoginState(
                            userId = result.response.userId,
                            userEmail = result.response.email,
                            userName = result.response.displayName ?: "",
                            accessToken = null, // Add when available from backend
                            refreshToken = null // Add when available from backend
                        )
                        
                        clearForm()
                        clearCredentialError()
                        setEmailPasswordLoading(false)
                    }
                    is LoginResult.Error -> {
                        Log.e(TAG, "Email/password sign-in failed: ${result.exception.message}")
                        setEmailPasswordLoading(false)
                    }
                    is LoginResult.Loading -> {
                        Log.d(TAG, "Email/password sign-in in progress")
                        // Loading state already set above
                    }
                }
                
                updateUiState()
                
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during email/password sign-in", e)
                setEmailPasswordLoading(false)
                _loginResult.value = LoginResult.Error(LoginException.Unknown(e.message ?: "Sign-in failed"))
                updateUiState()
            }
        }
    }
    
    /**
     * Sign in with Google
     */
    fun signInWithGoogle() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting Google sign-in")
                
                // Set Google loading state
                setGoogleLoading(true)
                
                val result = loginRepository.signInWithGoogle()
                _loginResult.value = result
                
                when (result) {
                    is LoginResult.Success -> {
                        Log.d(TAG, "Google sign-in successful")
                        
                        // Save login state for persistent login
                        authenticationManager.saveLoginState(
                            userId = result.response.userId,
                            userEmail = result.response.email,
                            userName = result.response.displayName ?: "",
                            accessToken = null, // Add when available from backend
                            refreshToken = null // Add when available from backend
                        )
                        
                        setGoogleLoading(false)
                    }
                    is LoginResult.Error -> {
                        Log.e(TAG, "Google sign-in failed: ${result.exception.message}")
                        setGoogleLoading(false)
                    }
                    is LoginResult.Loading -> {
                        Log.d(TAG, "Google sign-in in progress")
                        // Loading state already set above
                    }
                }
                
                updateUiState()
                
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during Google sign-in", e)
                setGoogleLoading(false)
                _loginResult.value = LoginResult.Error(LoginException.Unknown(e.message ?: "Google sign-in failed"))
                updateUiState()
            }
        }
    }
    
    /**
     * Sign in with Facebook
     */
    fun signInWithFacebook() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting Facebook sign-in")
                
                // Set Facebook loading state
                setFacebookLoading(true)
                
                val result = loginRepository.signInWithFacebook(facebookCallbackManager)
                _loginResult.value = result
                
                when (result) {
                    is LoginResult.Success -> {
                        Log.d(TAG, "Facebook sign-in successful")
                        
                        // Save login state for persistent login
                        authenticationManager.saveLoginState(
                            userId = result.response.userId,
                            userEmail = result.response.email,
                            userName = result.response.displayName ?: "",
                            accessToken = null, // Add when available from backend
                            refreshToken = null // Add when available from backend
                        )
                        
                        setFacebookLoading(false)
                    }
                    is LoginResult.Error -> {
                        Log.e(TAG, "Facebook sign-in failed: ${result.exception.message}")
                        setFacebookLoading(false)
                    }
                    is LoginResult.Loading -> {
                        Log.d(TAG, "Facebook sign-in in progress")
                        // Loading state already set above
                    }
                }
                
                updateUiState()
                
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during Facebook sign-in", e)
                setFacebookLoading(false)
                _loginResult.value = LoginResult.Error(LoginException.Unknown(e.message ?: "Facebook sign-in failed"))
                updateUiState()
            }
        }
    }
    
    /**
     * Send email verification
     */
    fun sendEmailVerification() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Sending email verification")
                
                val success = loginRepository.sendEmailVerification()
                if (success) {
                    Log.d(TAG, "Email verification sent successfully")
                    // You can show a toast or update UI state here
                } else {
                    Log.w(TAG, "Failed to send email verification")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error sending email verification", e)
            }
        }
    }
    
    /**
     * Clear login result (typically called after handling the result)
     */
    fun clearLoginResult() {
        _loginResult.value = null
        updateUiState()
    }
    
    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Set credential error for field-level display
     */
    fun setCredentialError(errorMessage: String) {
        _uiState.value = _uiState.value.copy(credentialError = errorMessage)
    }
    
    /**
     * Clear credential error
     */
    fun clearCredentialError() {
        _uiState.value = _uiState.value.copy(credentialError = null)
    }
    
    /**
     * Clear all login states and errors - used when navigating between screens
     */
    fun clearAllLoginStates() {
        // Clear form fields
        _email.value = ""
        _password.value = ""
        
        // Clear validation state
        _validationState.value = LoginValidationState()
        
        // Clear login result
        _loginResult.value = null
        
        // Clear all UI state errors and dialogs
        _uiState.value = LoginUiState()
        
        Log.d(TAG, "All login states cleared")
    }
    
    /**
     * Set email/password loading state
     */
    private fun setEmailPasswordLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(
            isEmailPasswordLoading = isLoading,
            isLoading = isLoading // Keep for backward compatibility
        )
    }
    
    /**
     * Set Google loading state
     */
    private fun setGoogleLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(
            isGoogleLoading = isLoading,
            isLoading = isLoading // Keep for backward compatibility
        )
    }
    
    /**
     * Set Facebook loading state
     */
    private fun setFacebookLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(
            isFacebookLoading = isLoading,
            isLoading = isLoading // Keep for backward compatibility
        )
    }
    
    /**
     * Set error message
     */
    private fun setErrorMessage(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }
    
    /**
     * Clear email verification dialog
     */
    fun clearEmailVerificationDialog() {
        _uiState.value = _uiState.value.copy(showEmailVerificationDialog = false)
    }
    
    /**
     * Show email verification dialog
     */
    fun showEmailVerificationDialog() {
        _uiState.value = _uiState.value.copy(showEmailVerificationDialog = true)
    }
    
    /**
     * Validate form fields
     */
    private fun validateForm(email: String, password: String): LoginValidationState {
        val emailError = when {
            email.isBlank() -> LoginValidationState.EMAIL_REQUIRED
            !ValidationUtils.isValidEmail(email) -> LoginValidationState.EMAIL_INVALID_FORMAT
            else -> null
        }
        
        val passwordError = when {
            password.isBlank() -> LoginValidationState.PASSWORD_REQUIRED
            else -> null
        }
        
        return LoginValidationState(
            emailError = emailError,
            passwordError = passwordError,
            isValid = emailError == null && passwordError == null
        )
    }
    
    
    /**
     * Update UI state based on current values
     */
    private fun updateUiState() {
        val currentResult = _loginResult.value
        val currentValidation = _validationState.value
        
        // Set error message and credential error based on login result
        val (errorMessage, credentialError, isRetryable) = when (currentResult) {
            is LoginResult.Error -> {
                Log.d(TAG, "Processing login error: ${currentResult.exception}")
                when (currentResult.exception) {
                    is LoginException.EmailNotVerified -> Triple(null, null, false) // Handle separately with dialog
                    is LoginException.UserNotFound -> {
                        Log.d(TAG, "User not found - setting credential error")
                        Triple(null, "Account does not exist", false)
                    }
                    is LoginException.InvalidCredential -> {
                        Log.d(TAG, "Invalid credential - setting credential error")
                        Triple(null, "Incorrect login credentials", false)
                    }
                    is LoginException.UserDisabled -> Triple("This user account has been disabled.", null, false) // Keep in error card
                    is LoginException.TimeoutError -> Triple(
                        "Connection timed out due to a slow internet connection. Please try again or move to a place with a better signal.", 
                        null, 
                        true
                    )
                    is LoginException.NoNetworkConnection -> Triple(
                        "No internet connection. Please check your network and try again.", 
                        null, 
                        true
                    )
                    is LoginException.SlowNetworkError -> Triple(
                        "Your internet connection is slow. Please wait or move to an area with better signal.", 
                        null, 
                        true
                    )
                    is LoginException.RetryableError -> Triple(
                        "Failed after ${currentResult.exception.attemptsMade} attempts. Please check your connection and try again.", 
                        null, 
                        true
                    )
                    is LoginException.Unknown -> Triple("Something went wrong. Please try again.", null, false) // Keep in error card
                }
            }
            else -> Triple(_uiState.value.errorMessage, _uiState.value.credentialError, _uiState.value.isRetryable) // Keep existing values
        }
        
        _uiState.value = _uiState.value.copy(
            isLoading = currentResult is LoginResult.Loading,
            loginResult = currentResult,
            validationState = currentValidation,
            profileCompletionStatus = when (currentResult) {
                is LoginResult.Success -> ProfileCompletionStatus(currentResult.response.isProfileComplete)
                else -> null
            },
            errorMessage = errorMessage,
            credentialError = credentialError,
            isRetryable = isRetryable
        )
        
        Log.d(TAG, "UI State updated - credentialError: $credentialError, errorMessage: $errorMessage")
    }
    
    /**
     * Clear form fields
     */
    private fun clearForm() {
        _email.value = ""
        _password.value = ""
        _validationState.value = LoginValidationState()
    }
    
    /**
     * Check if user is currently signed in
     */
    fun isUserSignedIn(): Boolean {
        return loginRepository.isUserSignedIn()
    }
    
    /**
     * Sign out current user
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Signing out user")
                loginRepository.signOut()
                // Clear persistent authentication state
                authenticationManager.clearLoginState()
                clearForm()
                _loginResult.value = null
                // Clear all UI state to prevent automatic redirects
                _uiState.value = LoginUiState()
                updateUiState()
            } catch (e: Exception) {
                Log.e(TAG, "Error during sign out", e)
            }
        }
    }
    
    /**
     * Handle login success navigation
     */
    fun getNavigationDestination(): String? {
        val result = _loginResult.value
        return when (result) {
            is LoginResult.Success -> {
                if (!result.response.isEmailVerified) {
                    // Show email verification dialog instead of navigating
                    showEmailVerificationDialog()
                    null
                } else if (!result.response.isProfileComplete) {
                    "complete_profile"
                } else {
                    "home" // or dashboard
                }
            }
            else -> null
        }
    }
    
    /**
     * Logout function - clears persistent login state
     * Logout function - nag-clear ng persistent login state
     */
    fun logout() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting logout process")
                
                // Clear authentication state
                authenticationManager.clearLoginState()
                
                // Clear any cached data or states in the ViewModel
                clearForm()
                clearCredentialError()
                _loginResult.value = null
                
                // You can also call repository logout if needed
                // loginRepository.logout()
                
                Log.d(TAG, "Logout completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error during logout", e)
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared")
    }
}

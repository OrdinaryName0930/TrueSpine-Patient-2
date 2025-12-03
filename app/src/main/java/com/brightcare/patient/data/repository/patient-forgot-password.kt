package com.brightcare.patient.data.repository

import android.content.Context
import android.util.Log
import com.brightcare.patient.data.model.*
import com.brightcare.patient.ui.component.signup_component.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository for handling forgot password operations with email link approach
 * Integrates with Firebase Auth and Firestore for secure password reset
 */
class PatientForgotPasswordRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val context: Context
) {
    
    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordResult?>(null)
    val forgotPasswordState: StateFlow<ForgotPasswordResult?> = _forgotPasswordState.asStateFlow()
    
    companion object {
        private const val TAG = "ForgotPasswordRepo"
        private const val USERS_COLLECTION = "client"
    }
    
    /**
     * Send password reset link to user's email
     */
    suspend fun sendResetLink(request: SendResetLinkRequest): ForgotPasswordResult {
        return try {
            _forgotPasswordState.value = ForgotPasswordResult.Loading
            
            Log.d(TAG, "Sending reset link to email: ${request.email}")
            
            // Validate email format
            if (!ValidationUtils.isValidEmail(request.email)) {
                return ForgotPasswordResult.Error(ForgotPasswordException.Unknown("Invalid email format"))
            }
            
            // Check if user exists in Firestore
            Log.d(TAG, "Checking user existence for: ${request.email}")
            val userExists = checkUserExists(request.email)
            if (!userExists) {
                Log.w(TAG, "User not found, returning EmailNotFound error: ${request.email}")
                return ForgotPasswordResult.Error(ForgotPasswordException.EmailNotFound)
            }
            Log.d(TAG, "User exists, proceeding with reset link sending: ${request.email}")
            
            // Send password reset email using Firebase Auth
            firebaseAuth.sendPasswordResetEmail(request.email).await()
            
            val response = SendResetLinkResponse(
                success = true,
                message = "Password reset link sent successfully to ${request.email}"
            )
            
            Log.d(TAG, "Reset link sent successfully to: ${request.email}")
            ForgotPasswordResult.Success("Reset link sent successfully", response)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending reset link", e)
            val exception = when (e) {
                is FirebaseAuthException -> mapFirebaseAuthException(e)
                else -> ForgotPasswordException.Unknown(e.message ?: "Failed to send reset link")
            }
            ForgotPasswordResult.Error(exception)
        }.also { result ->
            _forgotPasswordState.value = result
        }
    }
    
    
    /**
     * Check if user exists by looking in Firestore users collection
     * This avoids sending unwanted password reset emails
     */
    private suspend fun checkUserExists(email: String): Boolean {
        return try {
            Log.d(TAG, "Checking if user exists in Firestore: $email")
            
            // Try multiple approaches to find the user due to potential case sensitivity issues
            
            // First, try with lowercase email (most common case)
            Log.d(TAG, "Trying lowercase email query: ${email.lowercase()}")
            var userQuery = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("email", email.lowercase())
                .limit(1)
                .get()
                .await()
            
            if (!userQuery.isEmpty) {
                Log.d(TAG, "User found with lowercase email: $email")
                return true
            }
            
            // If not found with lowercase, try with original case
            Log.d(TAG, "Trying original case email query: $email")
            userQuery = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
            
            if (!userQuery.isEmpty) {
                Log.d(TAG, "User found with original case email: $email")
                return true
            }
            
            // If still not found, try with uppercase (edge case)
            Log.d(TAG, "Trying uppercase email query: ${email.uppercase()}")
            userQuery = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("email", email.uppercase())
                .limit(1)
                .get()
                .await()
            
            if (!userQuery.isEmpty) {
                Log.d(TAG, "User found with uppercase email: $email")
                return true
            }
            
            // Also try using Firebase Auth as a fallback to double-check
            Log.d(TAG, "Trying Firebase Auth fallback check for: $email")
            try {
                val signInMethods = firebaseAuth.fetchSignInMethodsForEmail(email).await()
                val authUserExists = signInMethods.signInMethods?.isNotEmpty() == true
                
                if (authUserExists) {
                    Log.d(TAG, "User found in Firebase Auth but not in Firestore: $email")
                    Log.w(TAG, "Data inconsistency detected - user exists in Auth but not in Firestore")
                    return true
                }
            } catch (authException: Exception) {
                Log.w(TAG, "Firebase Auth check failed for $email", authException)
            }
            
            Log.w(TAG, "User not found in Firestore or Firebase Auth: $email")
            return false
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking user existence in Firestore", e)
            // On error, assume user doesn't exist to be safe
            false
        }
    }
    
    
    /**
     * Map Firebase Auth exceptions to custom exceptions
     */
    private fun mapFirebaseAuthException(exception: FirebaseAuthException): ForgotPasswordException {
        return when (exception.errorCode) {
            "ERROR_USER_NOT_FOUND" -> ForgotPasswordException.EmailNotFound
            "ERROR_INVALID_EMAIL" -> ForgotPasswordException.Unknown("Invalid email format")
            "ERROR_NETWORK_REQUEST_FAILED" -> ForgotPasswordException.NetworkError
            "ERROR_TOO_MANY_REQUESTS" -> ForgotPasswordException.Unknown("Too many requests. Please try again later.")
            else -> {
                Log.w(TAG, "Unmapped Firebase Auth error: ${exception.errorCode}")
                ForgotPasswordException.Unknown(exception.message ?: "Authentication error")
            }
        }
    }
    
    /**
     * Validate email format
     */
    fun validateEmail(email: String): ForgotPasswordValidationState {
        val emailError = when {
            email.isBlank() -> ForgotPasswordValidationState.EMAIL_REQUIRED
            !ValidationUtils.isValidEmail(email) -> ForgotPasswordValidationState.EMAIL_INVALID_FORMAT
            else -> null
        }
        
        return ForgotPasswordValidationState(
            emailError = emailError,
            isEmailValid = emailError == null
        )
    }
    
    
    /**
     * Clear current state
     */
    fun clearState() {
        _forgotPasswordState.value = null
    }
}
package com.brightcare.patient.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.brightcare.patient.data.model.LoginRequest
import com.brightcare.patient.data.model.LoginResponse
import com.brightcare.patient.data.model.LoginResult
import com.brightcare.patient.data.model.LoginException
import com.brightcare.patient.data.model.LoginValidationState
import com.brightcare.patient.data.model.ProfileCompletionStatus
import com.brightcare.patient.data.model.FirestoreUserData
import com.brightcare.patient.ui.component.signup_component.ValidationUtils
import com.brightcare.patient.utils.DeviceUtils
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Repository for handling patient login operations
 * Integrates with Firebase Authentication for email/password, Google, and Facebook sign-in
 */
class PatientLoginRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val context: Context
) {
    
    private val _loginState = MutableStateFlow<LoginResult?>(null)
    val loginState: StateFlow<LoginResult?> = _loginState.asStateFlow()
    
    private val credentialManager by lazy { CredentialManager.create(context) }
    
    companion object {
        private const val TAG = "PatientLoginRepository"
        private const val GOOGLE_WEB_CLIENT_ID = "29002840483-drkqs4grtuou6gddsdn717ojdu5s3uah.apps.googleusercontent.com"
        private const val USERS_COLLECTION = "client"
    }
    
    /**
     * Sign in with email and password
     */
    suspend fun signInWithEmailAndPassword(request: LoginRequest): LoginResult {
        return try {
            _loginState.value = LoginResult.Loading
            
            // Validate input
            val validationResult = validateLoginInput(request.email, request.password)
            if (!validationResult.isValid) {
                val errorMessage = validationResult.emailError ?: validationResult.passwordError ?: "Invalid input"
                return LoginResult.Error(LoginException.Unknown(errorMessage))
            }
            
            Log.d(TAG, "Attempting to sign in with email: ${request.email}")
            
            val authResult = firebaseAuth.signInWithEmailAndPassword(request.email, request.password).await()
            val user = authResult.user
            
            if (user != null) {
                // Check if email is verified
                if (!user.isEmailVerified) {
                    Log.w(TAG, "Email not verified for user: ${user.uid}")
                    return LoginResult.Error(LoginException.EmailNotVerified)
                }
                
                // Email verification status is handled by Firebase Auth directly
                
                // Check profile completion status
                val profileStatus = checkProfileCompletion(user.uid)
                
                val loginResponse = LoginResponse(
                    userId = user.uid,
                    email = user.email ?: "",
                    isEmailVerified = user.isEmailVerified,
                    displayName = user.displayName,
                    photoUrl = user.photoUrl?.toString(),
                    providerId = "password",
                    isProfileComplete = profileStatus.isComplete
                )
                
                Log.d(TAG, "Login successful for user: ${user.uid}")
                LoginResult.Success(loginResponse)
            } else {
                Log.e(TAG, "User is null after successful authentication")
                LoginResult.Error(LoginException.Unknown("Authentication failed"))
            }
            
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Firebase auth exception during login", e)
            val loginException = mapFirebaseAuthException(e)
            LoginResult.Error(loginException)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during login", e)
            LoginResult.Error(LoginException.Unknown(e.message ?: "Unknown error occurred"))
        }.also { result ->
            _loginState.value = result
        }
    }
    
    /**
     * Sign in with Google using Credential Manager
     */
    suspend fun signInWithGoogle(): LoginResult {
        return try {
            _loginState.value = LoginResult.Loading
            
            Log.d(TAG, "Starting Google sign-in")
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(GOOGLE_WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .build()
            
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            
            handleGoogleCredentialResult(result)
            
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed", e)
            LoginResult.Error(LoginException.Unknown(e.message ?: "Google sign-in failed"))
        }.also { result ->
            _loginState.value = result
        }
    }
    
    /**
     * Sign in with Facebook
     */
    suspend fun signInWithFacebook(callbackManager: CallbackManager): LoginResult {
        return suspendCancellableCoroutine { continuation ->
            _loginState.value = LoginResult.Loading
            
            Log.d(TAG, "Starting Facebook sign-in")
            
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<com.facebook.login.LoginResult> {
                    override fun onSuccess(result: com.facebook.login.LoginResult) {
                        Log.d(TAG, "Facebook login success, getting access token")
                        val token = result.accessToken
                        
                        // Get user profile information
                        val request = GraphRequest.newMeRequest(token) { jsonObject, _ ->
                            try {
                                val credential = FacebookAuthProvider.getCredential(token.token)
                                
                                firebaseAuth.signInWithCredential(credential)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val user = task.result?.user
                                            if (user != null) {
                                                // Check if this is a new user and store data if needed
                                                val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                                                
                                                if (isNewUser) {
                                                    Log.d(TAG, "New Facebook user detected, storing user data")
                                                    try {
                                                        val deviceId = DeviceUtils.getDeviceId(context)
                                                        val userData = FirestoreUserData(
                                                            email = (user.email ?: "").lowercase(),
                                                            deviceId = deviceId,
                                                            createdAt = System.currentTimeMillis(),
                                                            updatedAt = System.currentTimeMillis()
                                                        )
                                                        
                                                        firestore.collection(FirestoreUserData.COLLECTION_NAME)
                                                            .document(user.uid)
                                                            .set(userData)
                                                        
                                                        Log.d(TAG, "New Facebook user data stored successfully")
                                                    } catch (e: Exception) {
                                                        Log.e(TAG, "Failed to store new Facebook user data", e)
                                                        // Continue with login even if storage fails
                                                    }
                                                }
                                                
                                                // Check profile completion in background
                                                checkProfileCompletionAsync(user) { profileStatus ->
                                                    val loginResponse = LoginResponse(
                                                        userId = user.uid,
                                                        email = user.email ?: "",
                                                        isEmailVerified = user.isEmailVerified,
                                                        displayName = user.displayName,
                                                        photoUrl = user.photoUrl?.toString(),
                                                        providerId = "facebook.com",
                                                        isProfileComplete = profileStatus.isComplete
                                                    )
                                                    
                                                    val loginResult = LoginResult.Success(loginResponse)
                                                    _loginState.value = loginResult
                                                    continuation.resume(loginResult)
                                                }
                                            } else {
                                                val error = LoginResult.Error(LoginException.Unknown("Facebook authentication failed"))
                                                _loginState.value = error
                                                continuation.resume(error)
                                            }
                                        } else {
                                            val exception = task.exception
                                            Log.e(TAG, "Firebase auth with Facebook failed", exception)
                                            val loginException = if (exception is FirebaseAuthException) {
                                                mapFirebaseAuthException(exception)
                                            } else {
                                                LoginException.Unknown(exception?.message ?: "Facebook authentication failed")
                                            }
                                            val error = LoginResult.Error(loginException)
                                            _loginState.value = error
                                            continuation.resume(error)
                                        }
                                    }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing Facebook login", e)
                                val error = LoginResult.Error(LoginException.Unknown(e.message ?: "Facebook login processing failed"))
                                _loginState.value = error
                                continuation.resume(error)
                            }
                        }
                        
                        val parameters = android.os.Bundle()
                        parameters.putString("fields", "id,name,email,picture.type(large)")
                        request.parameters = parameters
                        request.executeAsync()
                    }
                    
                    override fun onCancel() {
                        Log.d(TAG, "Facebook login cancelled")
                        val error = LoginResult.Error(LoginException.Unknown("Facebook login was cancelled"))
                        _loginState.value = error
                        continuation.resume(error)
                    }
                    
                    override fun onError(error: FacebookException) {
                        Log.e(TAG, "Facebook login error", error)
                        val loginError = LoginResult.Error(LoginException.Unknown(error.message ?: "Facebook login failed"))
                        _loginState.value = loginError
                        continuation.resume(loginError)
                    }
                })
            
            // Start the login process
            if (context is androidx.fragment.app.FragmentActivity) {
                LoginManager.getInstance().logInWithReadPermissions(
                    context,
                    listOf("email", "public_profile")
                )
            } else {
                val error = LoginResult.Error(LoginException.Unknown("Facebook login requires FragmentActivity context"))
                _loginState.value = error
                continuation.resume(error)
            }
        }
    }
    
    /**
     * Send email verification
     */
    suspend fun sendEmailVerification(): Boolean {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null && !user.isEmailVerified) {
                user.sendEmailVerification().await()
                Log.d(TAG, "Email verification sent to: ${user.email}")
                true
            } else {
                Log.w(TAG, "Cannot send email verification - user is null or already verified")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email verification", e)
            false
        }
    }
    
    /**
     * Check email verification status
     * This method can be called to refresh the email verification status
     */
    suspend fun checkEmailVerification(): Boolean {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                // Reload user to get latest verification status
                user.reload().await()
                
                if (user.isEmailVerified) {
                    Log.d(TAG, "Email is verified for user: ${user.uid}")
                    true
                } else {
                    Log.d(TAG, "Email is still not verified for user: ${user.uid}")
                    false
                }
            } else {
                Log.w(TAG, "Cannot check email verification - user is null")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check email verification status", e)
            false
        }
    }
    
    
    /**
     * Check if user's profile is complete
     * Simplified to only check profileCompleted boolean field
     */
    private suspend fun checkProfileCompletion(userId: String): ProfileCompletionStatus {
        return try {
            val document = firestore.collection(USERS_COLLECTION).document(userId).get().await()
            
            if (document.exists()) {
                // Simply check the profileCompleted boolean field
                val isCompleted = document.getBoolean("profileCompleted") ?: false
                
                Log.d(TAG, "Profile completion status for $userId: $isCompleted")
                
                ProfileCompletionStatus(
                    isComplete = isCompleted,
                    missingFields = if (isCompleted) emptyList() else listOf("profile")
                )
            } else {
                // User document doesn't exist, profile is incomplete
                Log.d(TAG, "User document doesn't exist for $userId, profile incomplete")
                ProfileCompletionStatus(
                    isComplete = false,
                    missingFields = listOf("profile")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking profile completion", e)
            // Assume incomplete on error
            ProfileCompletionStatus(
                isComplete = false,
                missingFields = listOf("profile")
            )
        }
    }
    
    /**
     * Check profile completion asynchronously (for callback-based operations)
     * Simplified to only check profileCompleted boolean field
     */
    private fun checkProfileCompletionAsync(user: FirebaseUser, callback: (ProfileCompletionStatus) -> Unit) {
        firestore.collection(USERS_COLLECTION).document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Simply check the profileCompleted boolean field
                    val isCompleted = document.getBoolean("profileCompleted") ?: false
                    
                    Log.d(TAG, "Async profile completion status for ${user.uid}: $isCompleted")
                    
                    callback(ProfileCompletionStatus(
                        isComplete = isCompleted,
                        missingFields = if (isCompleted) emptyList() else listOf("profile")
                    ))
                } else {
                    Log.d(TAG, "User document doesn't exist for ${user.uid}, profile incomplete")
                    callback(ProfileCompletionStatus(
                        isComplete = false,
                        missingFields = listOf("profile")
                    ))
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking profile completion", e)
                callback(ProfileCompletionStatus(
                    isComplete = false,
                    missingFields = listOf("profile")
                ))
            }
    }
    
    /**
     * Handle Google credential result
     */
    private suspend fun handleGoogleCredentialResult(result: GetCredentialResponse): LoginResult {
        return try {
            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        
                        Log.d(TAG, "Got Google ID token, authenticating with Firebase")
                        
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
                        val user = authResult.user
                        
                        if (user != null) {
                            // Check if this is a new user and store data if needed
                            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
                            
                            if (isNewUser) {
                                Log.d(TAG, "New Google user detected, storing user data")
                                try {
                                    val deviceId = DeviceUtils.getDeviceId(context)
                                    val userData = FirestoreUserData(
                                        email = (user.email ?: "").lowercase(),
                                        deviceId = deviceId,
                                        createdAt = System.currentTimeMillis(),
                                        updatedAt = System.currentTimeMillis()
                                    )
                                    
                                    firestore.collection(FirestoreUserData.COLLECTION_NAME)
                                        .document(user.uid)
                                        .set(userData)
                                        .await()
                                    
                                    Log.d(TAG, "New Google user data stored successfully")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to store new Google user data", e)
                                    // Continue with login even if storage fails
                                }
                            }
                            
                            // Check profile completion status
                            val profileStatus = checkProfileCompletion(user.uid)
                            
                            val loginResponse = LoginResponse(
                                userId = user.uid,
                                email = user.email ?: "",
                                isEmailVerified = user.isEmailVerified,
                                displayName = user.displayName,
                                photoUrl = user.photoUrl?.toString(),
                                providerId = "google.com",
                                isProfileComplete = profileStatus.isComplete
                            )
                            
                            Log.d(TAG, "Google sign-in successful for user: ${user.uid}")
                            LoginResult.Success(loginResponse)
                        } else {
                            Log.e(TAG, "User is null after Google authentication")
                            LoginResult.Error(LoginException.Unknown("Google authentication failed"))
                        }
                    } else {
                        Log.e(TAG, "Unexpected credential type: ${credential.type}")
                        LoginResult.Error(LoginException.Unknown("Unexpected credential type"))
                    }
                }
                else -> {
                    Log.e(TAG, "Unexpected credential class: ${credential::class.java}")
                    LoginResult.Error(LoginException.Unknown("Unexpected credential type"))
                }
            }
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "Google ID token parsing failed", e)
            LoginResult.Error(LoginException.Unknown("Google authentication failed"))
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Firebase auth with Google failed", e)
            LoginResult.Error(mapFirebaseAuthException(e))
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed", e)
            LoginResult.Error(LoginException.Unknown(e.message ?: "Google sign-in failed"))
        }
    }
    
    /**
     * Validate login input
     */
    private fun validateLoginInput(email: String, password: String): LoginValidationState {
        val emailError = when {
            email.isBlank() -> LoginValidationState.EMAIL_REQUIRED
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
     * Map Firebase Auth exceptions to custom login exceptions
     */
    private fun mapFirebaseAuthException(exception: FirebaseAuthException): LoginException {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL",
            "ERROR_WRONG_PASSWORD",
            "ERROR_INVALID_CREDENTIAL" -> LoginException.InvalidCredential
            "ERROR_USER_NOT_FOUND" -> LoginException.UserNotFound
            "ERROR_USER_DISABLED" -> LoginException.UserDisabled
            else -> {
                Log.w(TAG, "Unmapped Firebase Auth error: ${exception.errorCode}")
                LoginException.Unknown(exception.message ?: "Authentication failed")
            }
        }
    }
    
    /**
     * Sign out current user
     */
    fun signOut() {
        try {
            firebaseAuth.signOut()
            LoginManager.getInstance().logOut() // Facebook logout
            // Clear login state to prevent automatic redirects
            _loginState.value = null
            Log.d(TAG, "User signed out successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign out", e)
        }
    }
    
    /**
     * Get current user
     */
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
    
    /**
     * Check if user is currently signed in
     */
    fun isUserSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}
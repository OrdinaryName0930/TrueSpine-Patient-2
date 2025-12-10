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
import com.brightcare.patient.utils.NetworkNotAvailableException
import com.brightcare.patient.utils.NetworkUtils
import com.brightcare.patient.utils.TimeoutException
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
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
        private const val SUBCOLLECTION_PERSONAL_DATA = "personal_data"
        private const val DOCUMENT_INFO = "info"
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
                
                Log.d(TAG, "Email/Password Login Response created:")
                Log.d(TAG, "  - userId: ${user.uid}")
                Log.d(TAG, "  - isEmailVerified: ${user.isEmailVerified}")
                Log.d(TAG, "  - isProfileComplete: ${profileStatus.isComplete}")
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
     * Enhanced with retry logic and timeout handling for slow connections
     */
    suspend fun signInWithGoogle(): LoginResult {
        return try {
            _loginState.value = LoginResult.Loading
            
            Log.d(TAG, "Starting Google sign-in")
            
            // Check network availability first
            if (!NetworkUtils.isNetworkAvailable(context)) {
                val errorResult = LoginResult.Error(LoginException.NoNetworkConnection)
                _loginState.value = errorResult
                return errorResult
            }
            
            val networkQuality = NetworkUtils.getNetworkQuality(context)
            Log.d(TAG, "Network quality: $networkQuality")
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(GOOGLE_WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .build()
            
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            
            // Get credential with extended timeout for slow connections
            val credentialTimeout = NetworkUtils.getAdjustedTimeout(
                NetworkUtils.Timeouts.CREDENTIAL_MANAGER_TIMEOUT_MS, 
                context
            )
            
            Log.d(TAG, "Getting Google credential with timeout: ${credentialTimeout}ms")
            
            val result = try {
                withTimeout(credentialTimeout) {
                    credentialManager.getCredential(
                        request = request,
                        context = context
                    )
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.e(TAG, "Google credential request timed out after ${credentialTimeout}ms")
                val errorResult = LoginResult.Error(LoginException.TimeoutError)
                _loginState.value = errorResult
                return errorResult
            }
            
            handleGoogleCredentialResultEnhanced(result)
            
        } catch (e: CancellationException) {
            // Rethrow cancellation exceptions
            throw e
        } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
            Log.d(TAG, "Google Sign-In cancelled by user")
            val errorResult = LoginResult.Error(LoginException.Unknown("Sign-in cancelled"))
            _loginState.value = errorResult
            errorResult
        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
            Log.e(TAG, "No Google credentials available", e)
            val errorResult = LoginResult.Error(LoginException.Unknown("No Google account found"))
            _loginState.value = errorResult
            errorResult
        } catch (e: NetworkNotAvailableException) {
            Log.e(TAG, "No network connection", e)
            val errorResult = LoginResult.Error(LoginException.NoNetworkConnection)
            _loginState.value = errorResult
            errorResult
        } catch (e: TimeoutException) {
            Log.e(TAG, "Google Sign-In timed out", e)
            val errorResult = LoginResult.Error(LoginException.TimeoutError)
            _loginState.value = errorResult
            errorResult
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "Google Sign-In timed out", e)
            val errorResult = LoginResult.Error(LoginException.TimeoutError)
            _loginState.value = errorResult
            errorResult
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed", e)
            val errorResult = if (NetworkUtils.isTimeoutError(e)) {
                LoginResult.Error(LoginException.TimeoutError)
            } else if (NetworkUtils.isNetworkUnavailableError(e)) {
                LoginResult.Error(LoginException.NoNetworkConnection)
            } else if (NetworkUtils.isRetryableError(e)) {
                LoginResult.Error(LoginException.Unknown(NetworkUtils.getNetworkErrorMessage(e)))
            } else {
                LoginResult.Error(LoginException.Unknown(e.message ?: "Google sign-in failed"))
            }
            _loginState.value = errorResult
            errorResult
        }
    }
    
    /**
     * Sign in with Facebook
     * Enhanced with retry logic and timeout handling for slow connections
     */
    suspend fun signInWithFacebook(callbackManager: CallbackManager): LoginResult {
        return try {
            _loginState.value = LoginResult.Loading
            
            Log.d(TAG, "Starting Facebook sign-in")
            
            // Check network availability first
            if (!NetworkUtils.isNetworkAvailable(context)) {
                val errorResult = LoginResult.Error(LoginException.NoNetworkConnection)
                _loginState.value = errorResult
                return errorResult
            }
            
            val networkQuality = NetworkUtils.getNetworkQuality(context)
            Log.d(TAG, "Network quality for Facebook auth: $networkQuality")
            
            // Extended timeout for slow connections
            val facebookTimeout = NetworkUtils.getAdjustedTimeout(
                NetworkUtils.Timeouts.SOCIAL_LOGIN_TIMEOUT_MS,
                context
            )
            
            Log.d(TAG, "Facebook Sign-In timeout: ${facebookTimeout}ms")
            
            // Use suspendCoroutine with timeout for slow connections
            try {
                withTimeout(facebookTimeout) {
                    suspendCancellableCoroutine<LoginResult> { continuation ->
                        
                        LoginManager.getInstance().registerCallback(callbackManager,
                            object : FacebookCallback<com.facebook.login.LoginResult> {
                                override fun onSuccess(result: com.facebook.login.LoginResult) {
                                    Log.d(TAG, "Facebook login success, getting access token")
                                    val token = result.accessToken
                                    
                                    // Get user profile information
                                    val request = GraphRequest.newMeRequest(token) { jsonObject, _ ->
                                        try {
                                            val credential = FacebookAuthProvider.getCredential(token.token)
                                            
                                            // Use enhanced Firebase auth with retry logic
                                            proceedWithFirebaseAuthEnhancedForLogin(credential, continuation)
                                            
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error processing Facebook login", e)
                                            val error = if (NetworkUtils.isTimeoutError(e)) {
                                                LoginResult.Error(LoginException.TimeoutError)
                                            } else if (NetworkUtils.isNetworkUnavailableError(e)) {
                                                LoginResult.Error(LoginException.NoNetworkConnection)
                                            } else {
                                                LoginResult.Error(LoginException.Unknown(e.message ?: "Facebook login processing failed"))
                                            }
                                            _loginState.value = error
                                            if (continuation.isActive) {
                                                continuation.resume(error)
                                            }
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
                                    if (continuation.isActive) {
                                        continuation.resume(error)
                                    }
                                }
                                
                                override fun onError(error: FacebookException) {
                                    Log.e(TAG, "Facebook login error", error)
                                    // Determine if error is network-related
                                    val loginError = when {
                                        NetworkUtils.isTimeoutError(error) -> {
                                            LoginResult.Error(LoginException.TimeoutError)
                                        }
                                        NetworkUtils.isNetworkUnavailableError(error) -> {
                                            LoginResult.Error(LoginException.NoNetworkConnection)
                                        }
                                        NetworkUtils.isRetryableError(error) -> {
                                            LoginResult.Error(LoginException.Unknown(NetworkUtils.getNetworkErrorMessage(error)))
                                        }
                                        else -> {
                                            LoginResult.Error(LoginException.Unknown(error.message ?: "Facebook login failed"))
                                        }
                                    }
                                    _loginState.value = loginError
                                    if (continuation.isActive) {
                                        continuation.resume(loginError)
                                    }
                                }
                            })
                        
                        // Start the login process
                        if (context is androidx.fragment.app.FragmentActivity) {
                            try {
                                LoginManager.getInstance().logInWithReadPermissions(
                                    context,
                                    listOf("email", "public_profile")
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to start Facebook login", e)
                                val error = if (NetworkUtils.isNetworkUnavailableError(e)) {
                                    LoginResult.Error(LoginException.NoNetworkConnection)
                                } else {
                                    LoginResult.Error(LoginException.Unknown("Failed to start Facebook login: ${e.message}"))
                                }
                                _loginState.value = error
                                if (continuation.isActive) {
                                    continuation.resume(error)
                                }
                            }
                        } else {
                            val error = LoginResult.Error(LoginException.Unknown("Facebook login requires FragmentActivity context"))
                            _loginState.value = error
                            if (continuation.isActive) {
                                continuation.resume(error)
                            }
                        }
                        
                        // Handle cancellation
                        continuation.invokeOnCancellation {
                            Log.d(TAG, "Facebook login cancelled via coroutine cancellation")
                            LoginManager.getInstance().logOut()
                        }
                    }
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.e(TAG, "Facebook Sign-In timed out after ${facebookTimeout}ms")
                val errorResult = LoginResult.Error(LoginException.TimeoutError)
                _loginState.value = errorResult
                errorResult
            }
            
        } catch (e: CancellationException) {
            throw e
        } catch (e: NetworkNotAvailableException) {
            Log.e(TAG, "No network connection for Facebook sign-in", e)
            val errorResult = LoginResult.Error(LoginException.NoNetworkConnection)
            _loginState.value = errorResult
            errorResult
        } catch (e: TimeoutException) {
            Log.e(TAG, "Facebook Sign-In timed out", e)
            val errorResult = LoginResult.Error(LoginException.TimeoutError)
            _loginState.value = errorResult
            errorResult
        } catch (e: Exception) {
            Log.e(TAG, "Facebook Sign-In error", e)
            val errorResult = if (NetworkUtils.isTimeoutError(e)) {
                LoginResult.Error(LoginException.TimeoutError)
            } else if (NetworkUtils.isNetworkUnavailableError(e)) {
                LoginResult.Error(LoginException.NoNetworkConnection)
            } else if (NetworkUtils.isRetryableError(e)) {
                LoginResult.Error(LoginException.Unknown(NetworkUtils.getNetworkErrorMessage(e)))
            } else {
                LoginResult.Error(LoginException.Unknown("Facebook sign-in failed: ${e.message}"))
            }
            _loginState.value = errorResult
            errorResult
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
            // Check profile completion in nested structure: client/{clientId}/personal_data/info
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SUBCOLLECTION_PERSONAL_DATA)
                .document(DOCUMENT_INFO)
                .get()
                .await()
            
            if (document.exists()) {
                // Simply check the profileCompleted boolean field
                val isCompleted = document.getBoolean("profileCompleted") ?: false
                
                Log.d(TAG, "Profile completion check for $userId:")
                Log.d(TAG, "  - Document exists: true")
                Log.d(TAG, "  - profileCompleted field: ${document.getBoolean("profileCompleted")}")
                Log.d(TAG, "  - Final status: $isCompleted")
                
                ProfileCompletionStatus(
                    isComplete = isCompleted,
                    missingFields = if (isCompleted) emptyList() else listOf("profile")
                )
            } else {
                // User document doesn't exist, profile is incomplete
                Log.d(TAG, "Profile completion check for $userId:")
                Log.d(TAG, "  - Document exists: false")
                Log.d(TAG, "  - Final status: false (profile incomplete)")
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
        // Check profile completion in nested structure: client/{clientId}/personal_data/info
        firestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .collection(SUBCOLLECTION_PERSONAL_DATA)
            .document(DOCUMENT_INFO)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Simply check the profileCompleted boolean field
                    val isCompleted = document.getBoolean("profileCompleted") ?: false
                    
                    Log.d(TAG, "Async profile completion check for ${user.uid}:")
                    Log.d(TAG, "  - Document exists: true")
                    Log.d(TAG, "  - profileCompleted field: ${document.getBoolean("profileCompleted")}")
                    Log.d(TAG, "  - Final status: $isCompleted")
                    
                    callback(ProfileCompletionStatus(
                        isComplete = isCompleted,
                        missingFields = if (isCompleted) emptyList() else listOf("profile")
                    ))
                } else {
                    Log.d(TAG, "Async profile completion check for ${user.uid}:")
                    Log.d(TAG, "  - Document exists: false")
                    Log.d(TAG, "  - Final status: false (profile incomplete)")
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
     * Handle Google credential result (legacy method for backward compatibility)
     */
    private suspend fun handleGoogleCredentialResult(result: GetCredentialResponse): LoginResult {
        return handleGoogleCredentialResultEnhanced(result)
    }
    
    /**
     * Enhanced Google credential result handler with retry logic for slow connections
     */
    private suspend fun handleGoogleCredentialResultEnhanced(result: GetCredentialResponse): LoginResult {
        return try {
            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        
                        Log.d(TAG, "Got Google ID token, authenticating with Firebase")
                        
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        
                        // Proceed with Firebase authentication using retry for slow connections
                        val authResult = NetworkUtils.executeWithRetry(
                            context = context,
                            baseTimeoutMs = NetworkUtils.Timeouts.FIREBASE_AUTH_TIMEOUT_MS,
                            maxRetries = NetworkUtils.RetryConfig.MAX_RETRIES,
                            operationName = "Google Firebase Auth"
                        ) {
                            firebaseAuth.signInWithCredential(firebaseCredential).await()
                        }
                        
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
                                        profileCompleted = false, // Explicitly set for new Google users
                                        createdAt = System.currentTimeMillis(),
                                        updatedAt = System.currentTimeMillis()
                                    )
                                    
                                    // Save with retry logic for reliability
                                    NetworkUtils.executeWithRetry(
                                        context = context,
                                        baseTimeoutMs = NetworkUtils.Timeouts.FIRESTORE_WRITE_TIMEOUT_MS,
                                        maxRetries = 2, // Less retries for non-critical operation
                                        operationName = "Firestore Write"
                                    ) {
                                        firestore.collection(FirestoreUserData.COLLECTION_NAME)
                                            .document(user.uid)
                                            .collection("personal_data")
                                            .document("info")
                                            .set(userData)
                                            .await()
                                    }
                                    
                                    Log.d(TAG, "New Google user data stored successfully")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to store new Google user data (non-critical)", e)
                                    // Continue with login even if storage fails
                                }
                            }
                            
                            // Check profile completion status with retry
                            val profileStatus = try {
                                NetworkUtils.executeWithRetry(
                                    context = context,
                                    baseTimeoutMs = NetworkUtils.Timeouts.FIRESTORE_WRITE_TIMEOUT_MS,
                                    maxRetries = 2,
                                    operationName = "Profile Completion Check"
                                ) {
                                    checkProfileCompletion(user.uid)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to check profile completion, assuming incomplete", e)
                                ProfileCompletionStatus(isComplete = false, missingFields = listOf("profile"))
                            }
                            
                            val loginResponse = LoginResponse(
                                userId = user.uid,
                                email = user.email ?: "",
                                isEmailVerified = user.isEmailVerified,
                                displayName = user.displayName,
                                photoUrl = user.photoUrl?.toString(),
                                providerId = "google.com",
                                isProfileComplete = profileStatus.isComplete
                            )
                            
                            Log.d(TAG, "Google Login Response created:")
                            Log.d(TAG, "  - userId: ${user.uid}")
                            Log.d(TAG, "  - isEmailVerified: ${user.isEmailVerified}")
                            Log.d(TAG, "  - isProfileComplete: ${profileStatus.isComplete}")
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
            LoginResult.Error(mapFirebaseAuthExceptionEnhanced(e))
        } catch (e: NetworkNotAvailableException) {
            Log.e(TAG, "No network connection", e)
            LoginResult.Error(LoginException.NoNetworkConnection)
        } catch (e: TimeoutException) {
            Log.e(TAG, "Google Sign-In timed out", e)
            LoginResult.Error(LoginException.TimeoutError)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "Google Sign-In timed out", e)
            LoginResult.Error(LoginException.TimeoutError)
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed", e)
            if (NetworkUtils.isTimeoutError(e)) {
                LoginResult.Error(LoginException.TimeoutError)
            } else if (NetworkUtils.isNetworkUnavailableError(e)) {
                LoginResult.Error(LoginException.NoNetworkConnection)
            } else if (NetworkUtils.isRetryableError(e)) {
                LoginResult.Error(LoginException.Unknown(NetworkUtils.getNetworkErrorMessage(e)))
            } else {
                LoginResult.Error(LoginException.Unknown(e.message ?: "Google sign-in failed"))
            }
        }
    }
    
    /**
     * Enhanced Firebase authentication for Facebook login with retry logic
     */
    private fun proceedWithFirebaseAuthEnhancedForLogin(
        credential: com.google.firebase.auth.AuthCredential,
        continuation: kotlinx.coroutines.CancellableContinuation<LoginResult>,
        retryCount: Int = 0,
        maxRetries: Int = NetworkUtils.RetryConfig.MAX_RETRIES
    ) {
        // Calculate delay based on retry count (exponential backoff)
        val retryDelay = if (retryCount > 0) {
            (NetworkUtils.RetryConfig.INITIAL_DELAY_MS * Math.pow(NetworkUtils.RetryConfig.BACKOFF_MULTIPLIER, (retryCount - 1).toDouble())).toLong()
                .coerceAtMost(NetworkUtils.RetryConfig.MAX_DELAY_MS)
        } else 0L
        
        if (retryDelay > 0) {
            Log.d(TAG, "Waiting ${retryDelay}ms before retry attempt ${retryCount + 1}")
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                executeFirebaseAuthForLogin(credential, continuation, retryCount, maxRetries)
            }, retryDelay)
        } else {
            executeFirebaseAuthForLogin(credential, continuation, retryCount, maxRetries)
        }
    }
    
    /**
     * Execute Firebase authentication for login with error handling
     */
    private fun executeFirebaseAuthForLogin(
        credential: com.google.firebase.auth.AuthCredential,
        continuation: kotlinx.coroutines.CancellableContinuation<LoginResult>,
        retryCount: Int,
        maxRetries: Int
    ) {
        // Check network before attempting
        if (!NetworkUtils.isNetworkAvailable(context)) {
            val errorResult = LoginResult.Error(LoginException.NoNetworkConnection)
            _loginState.value = errorResult
            if (continuation.isActive) {
                continuation.resume(errorResult)
            }
            return
        }
        
        Log.d(TAG, "Firebase auth attempt ${retryCount + 1} of $maxRetries")
        
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        Log.d(TAG, "Firebase authentication successful for social user")
                        
                        // Check if this is a new user and store data if needed
                        val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                        
                        if (isNewUser) {
                            Log.d(TAG, "New social user detected, storing user data")
                            try {
                                val deviceId = DeviceUtils.getDeviceId(context)
                                val userData = FirestoreUserData(
                                    email = (user.email ?: "").lowercase(),
                                    deviceId = deviceId,
                                    profileCompleted = false, // Explicitly set for new social users
                                    createdAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                
                                // Fire-and-forget Firestore write (don't block on slow connections)
                                firestore.collection(FirestoreUserData.COLLECTION_NAME)
                                    .document(user.uid)
                                    .collection("personal_data")
                                    .document("info")
                                    .set(userData)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "User data stored in Firestore successfully")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "Failed to store user data in Firestore (non-critical)", e)
                                    }
                                
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to initiate Firestore storage", e)
                                // Continue with success even if Firestore storage fails
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
                                providerId = credential.provider,
                                isProfileComplete = profileStatus.isComplete
                            )
                            
                            Log.d(TAG, "Social Login Response created:")
                            Log.d(TAG, "  - userId: ${user.uid}")
                            Log.d(TAG, "  - provider: ${credential.provider}")
                            Log.d(TAG, "  - isEmailVerified: ${user.isEmailVerified}")
                            Log.d(TAG, "  - isProfileComplete: ${profileStatus.isComplete}")
                            
                            val successResult = LoginResult.Success(loginResponse)
                            _loginState.value = successResult
                            
                            Log.d(TAG, "Social Sign-In successful: ${loginResponse.userId}")
                            if (continuation.isActive) {
                                continuation.resume(successResult)
                            }
                        }
                    } else {
                        val errorResult = LoginResult.Error(LoginException.Unknown("Authentication failed - no user returned"))
                        _loginState.value = errorResult
                        if (continuation.isActive) {
                            continuation.resume(errorResult)
                        }
                    }
                } else {
                    val exception = task.exception
                    Log.e(TAG, "Firebase authentication failed (attempt ${retryCount + 1})", exception)
                    
                    // Check if we should retry
                    val shouldRetry = retryCount < maxRetries - 1 && 
                        exception != null && 
                        isRetryableFirebaseErrorForLogin(exception)
                    
                    if (shouldRetry) {
                        Log.d(TAG, "Will retry Firebase auth (retryable error detected)")
                        proceedWithFirebaseAuthEnhancedForLogin(credential, continuation, retryCount + 1, maxRetries)
                    } else {
                        val errorResult = when (exception) {
                            is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                                LoginResult.Error(LoginException.Unknown("Account already exists with different sign-in method"))
                            }
                            is com.google.firebase.auth.FirebaseAuthException -> {
                                LoginResult.Error(mapFirebaseAuthExceptionEnhanced(exception))
                            }
                            else -> {
                                if (exception != null && NetworkUtils.isTimeoutError(exception)) {
                                    LoginResult.Error(LoginException.TimeoutError)
                                } else if (exception != null && NetworkUtils.isNetworkUnavailableError(exception)) {
                                    LoginResult.Error(LoginException.NoNetworkConnection)
                                } else {
                                    LoginResult.Error(LoginException.Unknown("Authentication failed: ${exception?.message}"))
                                }
                            }
                        }
                        _loginState.value = errorResult
                        if (continuation.isActive) {
                            continuation.resume(errorResult)
                        }
                    }
                }
            }
    }
    
    /**
     * Check if Firebase error is retryable (network-related) for login
     */
    private fun isRetryableFirebaseErrorForLogin(exception: Exception): Boolean {
        // Check for network-related Firebase errors
        if (exception is FirebaseAuthException) {
            return when (exception.errorCode) {
                "ERROR_NETWORK_REQUEST_FAILED" -> true
                else -> NetworkUtils.isRetryableError(exception)
            }
        }
        return NetworkUtils.isRetryableError(exception)
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
     * Map Firebase Auth exceptions to custom login exceptions (legacy method)
     */
    private fun mapFirebaseAuthException(exception: FirebaseAuthException): LoginException {
        return mapFirebaseAuthExceptionEnhanced(exception)
    }
    
    /**
     * Enhanced Firebase Auth exception mapping with network error handling
     */
    private fun mapFirebaseAuthExceptionEnhanced(exception: FirebaseAuthException): LoginException {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL",
            "ERROR_WRONG_PASSWORD",
            "ERROR_INVALID_CREDENTIAL" -> LoginException.InvalidCredential
            "ERROR_USER_NOT_FOUND" -> LoginException.UserNotFound
            "ERROR_USER_DISABLED" -> LoginException.UserDisabled
            "ERROR_NETWORK_REQUEST_FAILED" -> {
                if (NetworkUtils.isNetworkAvailable(context)) {
                    LoginException.TimeoutError
                } else {
                    LoginException.NoNetworkConnection
                }
            }
            "ERROR_TOO_MANY_REQUESTS" -> LoginException.RetryableError("Too many requests. Please try again later.", 1)
            else -> {
                Log.w(TAG, "Unmapped Firebase Auth error: ${exception.errorCode}")
                // Check if it's a network-related error based on message
                val message = exception.message?.lowercase() ?: ""
                when {
                    NetworkUtils.isTimeoutError(exception) -> LoginException.TimeoutError
                    NetworkUtils.isNetworkUnavailableError(exception) -> LoginException.NoNetworkConnection
                    NetworkUtils.isRetryableError(exception) -> LoginException.RetryableError(
                        exception.message ?: "Network error occurred", 1
                    )
                    else -> LoginException.Unknown(exception.message ?: "Authentication failed")
                }
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
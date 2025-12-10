package com.brightcare.patient.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.brightcare.patient.data.model.*
import com.brightcare.patient.ui.component.signup_component.ValidationUtils
import com.brightcare.patient.utils.DeviceUtils
import com.brightcare.patient.utils.NetworkNotAvailableException
import com.brightcare.patient.utils.NetworkUtils
import com.brightcare.patient.utils.TimeoutException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Repository for handling patient signup operations
 * Integrates with Firebase Authentication for email/password, Google, and Facebook sign-in
 */
class PatientSignUpRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val context: Context
) {
    
    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Loading)
    val authState: StateFlow<AuthResult> = _authState.asStateFlow()
    
    private val _emailVerificationState = MutableStateFlow(EmailVerificationState())
    val emailVerificationState: StateFlow<EmailVerificationState> = _emailVerificationState.asStateFlow()
    
    companion object {
        private const val TAG = "PatientSignUpRepository"
        private const val GOOGLE_WEB_CLIENT_ID = "29002840483-drkqs4grtuou6gddsdn717ojdu5s3uah.apps.googleusercontent.com"
    }
    
    /**
     * Sign up with email and password
     * Validates input, creates user, and sends verification email
     */
    suspend fun signUpWithEmailAndPassword(request: SignUpRequest): AuthResult {
        return try {
            _authState.value = AuthResult.Loading
            
            // Validate input before Firebase call
            val validationError = validateSignUpRequest(request)
            if (validationError != null) {
                val errorResult = AuthResult.Error(validationError)
                _authState.value = errorResult
                return errorResult
            }
            
            Log.d(TAG, "Creating user with email: ${request.email}")
            
            // Create user with Firebase Auth
            val authResult = firebaseAuth.createUserWithEmailAndPassword(
                request.email.lowercase().trim(),
                request.password
            ).await()
            
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                // Send email verification
                sendEmailVerification()
                
                // Store user data in Firestore
                try {
                    val userData = FirestoreUserData(
                        email = (firebaseUser.email ?: request.email).lowercase(),
                        deviceId = request.deviceId,
                        profileCompleted = false, // Explicitly set for new email/password users
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    // Save initial user data in nested structure: client/{clientId}/personal_data/info
                    firestore.collection(FirestoreUserData.COLLECTION_NAME)
                        .document(firebaseUser.uid)
                        .collection("personal_data")
                        .document("info")
                        .set(userData)
                        .await()
                    
                    Log.d(TAG, "User data stored in Firestore successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to store user data in Firestore", e)
                    // Continue with success even if Firestore fails
                    // The user is still created in Firebase Auth
                }
                
                // Create response
                val response = SignUpResponse(
                    userId = firebaseUser.uid,
                    email = (firebaseUser.email ?: request.email).lowercase(),
                    isEmailVerified = firebaseUser.isEmailVerified,
                    displayName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    providerId = "password",
                    isProfileComplete = false // New email/password users always need to complete profile
                )
                
                val successResult = AuthResult.Success(response)
                _authState.value = successResult
                
                Log.d(TAG, "User created successfully: ${response.userId}")
                successResult
            } else {
                val errorResult = AuthResult.Error(AuthException.Unknown("User creation failed"))
                _authState.value = errorResult
                errorResult
            }
            
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Firebase Auth Exception: ${e.errorCode}", e)
            val authException = mapFirebaseException(e)
            val errorResult = AuthResult.Error(authException)
            _authState.value = errorResult
            errorResult
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during signup", e)
            val errorResult = AuthResult.Error(AuthException.Unknown(e.message ?: "Unknown error"))
            _authState.value = errorResult
            errorResult
        }
    }
    
    /**
     * Sign in with Google using Credential Manager
     * Enhanced with retry logic and timeout handling for slow connections
     */
    suspend fun signInWithGoogle(activity: androidx.activity.ComponentActivity): AuthResult {
        return try {
            _authState.value = AuthResult.Loading
            
            Log.d(TAG, "Starting Google Sign-In")
            
            // Check network availability first
            if (!NetworkUtils.isNetworkAvailable(context)) {
                val errorResult = AuthResult.Error(AuthException.NoNetworkConnection)
                _authState.value = errorResult
                return errorResult
            }
            
            val networkQuality = NetworkUtils.getNetworkQuality(context)
            Log.d(TAG, "Network quality: $networkQuality")
            
            val credentialManager = CredentialManager.create(context)
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(GOOGLE_WEB_CLIENT_ID)
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
                        context = activity
                    )
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.e(TAG, "Google credential request timed out after ${credentialTimeout}ms")
                val errorResult = AuthResult.Error(AuthException.TimeoutError)
                _authState.value = errorResult
                return errorResult
            }
            
            val credential = result.credential
            
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    
                    Log.d(TAG, "Google ID Token received, checking for existing email")
                    
                    // First, get the email from the Google token to check for existing accounts
                    val tempCredential = GoogleAuthProvider.getCredential(idToken, null)
                    
                    // Check if email is already registered with a different provider
                    val email = googleIdTokenCredential.id // This is the email
                    Log.d(TAG, "Checking if email exists: $email")
                    
                    // Check email with timeout
                    try {
                        val emailCheckTimeout = NetworkUtils.getAdjustedTimeout(
                            NetworkUtils.Timeouts.FIREBASE_AUTH_TIMEOUT_MS,
                            context
                        )
                        
                        val signInMethods = withTimeoutOrNull(emailCheckTimeout) {
                            firebaseAuth.fetchSignInMethodsForEmail(email).await()
                        }
                        
                        if (signInMethods?.signInMethods?.isNotEmpty() == true) {
                            val existingMethods = signInMethods.signInMethods!!
                            Log.d(TAG, "Email exists with methods: $existingMethods")
                            
                            if (existingMethods.contains("password") && !existingMethods.contains("google.com")) {
                                val errorResult = AuthResult.Error(AuthException.EmailAlreadyInUse)
                                _authState.value = errorResult
                                return errorResult
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not check existing sign-in methods, proceeding with sign-in", e)
                    }
                    
                    // Proceed with Firebase authentication using retry for slow connections
                    val authResult = NetworkUtils.executeWithRetry(
                        context = context,
                        baseTimeoutMs = NetworkUtils.Timeouts.FIREBASE_AUTH_TIMEOUT_MS,
                        maxRetries = NetworkUtils.RetryConfig.MAX_RETRIES,
                        operationName = "Google Firebase Auth"
                    ) {
                        firebaseAuth.signInWithCredential(tempCredential).await()
                    }
                    
                    val firebaseUser = authResult.user
                    if (firebaseUser != null) {
                        // Check if user already has profile data (existing user)
                        var isProfileComplete = false
                        
                        try {
                            // First check if profile already exists
                            val existingDoc = NetworkUtils.executeWithRetry(
                                context = context,
                                baseTimeoutMs = NetworkUtils.Timeouts.FIRESTORE_READ_TIMEOUT_MS,
                                maxRetries = 2,
                                operationName = "Profile Check"
                            ) {
                                firestore.collection(FirestoreUserData.COLLECTION_NAME)
                                    .document(firebaseUser.uid)
                                    .collection("personal_data")
                                    .document("info")
                                    .get()
                                    .await()
                            }
                            
                            if (existingDoc.exists()) {
                                // User already exists, check profile completion status
                                isProfileComplete = existingDoc.getBoolean("profileCompleted") ?: false
                                Log.d(TAG, "Existing Google user found, profile complete: $isProfileComplete")
                                
                                // Only update email and device ID for existing users, don't overwrite profile data
                                val updateData = mapOf(
                                    "email" to (firebaseUser.email ?: "").lowercase(),
                                    "deviceId" to DeviceUtils.getDeviceId(context),
                                    "updatedAt" to System.currentTimeMillis()
                                )
                                
                                NetworkUtils.executeWithRetry(
                                    context = context,
                                    baseTimeoutMs = NetworkUtils.Timeouts.FIRESTORE_WRITE_TIMEOUT_MS,
                                    maxRetries = 2,
                                    operationName = "User Update"
                                ) {
                                    firestore.collection(FirestoreUserData.COLLECTION_NAME)
                                        .document(firebaseUser.uid)
                                        .collection("personal_data")
                                        .document("info")
                                        .update(updateData)
                                        .await()
                                }
                                
                                Log.d(TAG, "Existing Google user data updated successfully")
                            } else {
                                // New user, create initial document
                                Log.d(TAG, "New Google signup user, creating initial profile")
                                val deviceId = DeviceUtils.getDeviceId(context)
                                val userData = FirestoreUserData(
                                    email = (firebaseUser.email ?: "").lowercase(),
                                    deviceId = deviceId,
                                    profileCompleted = false, // New users need to complete profile
                                    createdAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                
                                // Save with retry logic for reliability
                                NetworkUtils.executeWithRetry(
                                    context = context,
                                    baseTimeoutMs = NetworkUtils.Timeouts.FIRESTORE_WRITE_TIMEOUT_MS,
                                    maxRetries = 2,
                                    operationName = "New User Creation"
                                ) {
                                    firestore.collection(FirestoreUserData.COLLECTION_NAME)
                                        .document(firebaseUser.uid)
                                        .collection("personal_data")
                                        .document("info")
                                        .set(userData)
                                        .await()
                                }
                                
                                Log.d(TAG, "New Google user data stored in Firestore successfully")
                                isProfileComplete = false
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to check/store Google user data in Firestore", e)
                            // On error, assume profile is incomplete to be safe
                            isProfileComplete = false
                        }
                        
                        val response = SignUpResponse(
                            userId = firebaseUser.uid,
                            email = (firebaseUser.email ?: "").lowercase(),
                            isEmailVerified = firebaseUser.isEmailVerified,
                            displayName = firebaseUser.displayName,
                            photoUrl = firebaseUser.photoUrl?.toString(),
                            providerId = "google.com",
                            isProfileComplete = isProfileComplete
                        )
                        
                        val successResult = AuthResult.Success(response)
                        _authState.value = successResult
                        
                        Log.d(TAG, "Google Sign-In successful: ${response.userId}")
                        successResult
                    } else {
                        val errorResult = AuthResult.Error(AuthException.Unknown("Google sign-in failed - no user returned"))
                        _authState.value = errorResult
                        errorResult
                    }
                    
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e(TAG, "Google ID token parsing failed", e)
                    val errorResult = AuthResult.Error(AuthException.Unknown("Google authentication failed: ${e.message}"))
                    _authState.value = errorResult
                    errorResult
                } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                    Log.e(TAG, "Email already in use with different provider", e)
                    val errorResult = AuthResult.Error(AuthException.EmailAlreadyInUse)
                    _authState.value = errorResult
                    errorResult
                }
            } else {
                Log.e(TAG, "Invalid credential type: ${credential.type}")
                val errorResult = AuthResult.Error(AuthException.Unknown("Invalid Google credential type"))
                _authState.value = errorResult
                errorResult
            }
            
        } catch (e: CancellationException) {
            // Rethrow cancellation exceptions
            throw e
        } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
            Log.d(TAG, "Google Sign-In cancelled by user")
            val errorResult = AuthResult.Error(AuthException.Unknown("Sign-in cancelled"))
            _authState.value = errorResult
            errorResult
        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
            Log.e(TAG, "No Google credentials available", e)
            val errorResult = AuthResult.Error(AuthException.Unknown("No Google account found"))
            _authState.value = errorResult
            errorResult
        } catch (e: NetworkNotAvailableException) {
            Log.e(TAG, "No network connection", e)
            val errorResult = AuthResult.Error(AuthException.NoNetworkConnection)
            _authState.value = errorResult
            errorResult
        } catch (e: TimeoutException) {
            Log.e(TAG, "Google Sign-In timed out", e)
            val errorResult = AuthResult.Error(AuthException.TimeoutError)
            _authState.value = errorResult
            errorResult
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "Google Sign-In timed out", e)
            val errorResult = AuthResult.Error(AuthException.TimeoutError)
            _authState.value = errorResult
            errorResult
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In error", e)
            val errorResult = if (NetworkUtils.isTimeoutError(e)) {
                AuthResult.Error(AuthException.TimeoutError)
            } else if (NetworkUtils.isNetworkUnavailableError(e)) {
                AuthResult.Error(AuthException.NoNetworkConnection)
            } else if (NetworkUtils.isRetryableError(e)) {
                AuthResult.Error(AuthException.NetworkError)
            } else {
                AuthResult.Error(AuthException.Unknown(NetworkUtils.getNetworkErrorMessage(e)))
            }
            _authState.value = errorResult
            errorResult
        }
    }
    
    /**
     * Sign in with Facebook using Facebook SDK
     * Enhanced with retry logic and timeout handling for slow connections
     */
    suspend fun signInWithFacebook(activity: androidx.activity.ComponentActivity): AuthResult {
        return try {
            _authState.value = AuthResult.Loading
            
            Log.d(TAG, "Starting Facebook Sign-In")
            
            // Check network availability first
            if (!NetworkUtils.isNetworkAvailable(context)) {
                val errorResult = AuthResult.Error(AuthException.NoNetworkConnection)
                _authState.value = errorResult
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
                    kotlinx.coroutines.suspendCancellableCoroutine<AuthResult> { continuation ->
                        
                        // Check if user is already logged in with Facebook
                        val accessToken = AccessToken.getCurrentAccessToken()
                        if (accessToken != null && !accessToken.isExpired) {
                            Log.d(TAG, "Using existing Facebook access token")
                            val credential = FacebookAuthProvider.getCredential(accessToken.token)
                            proceedWithFirebaseAuthEnhanced(credential, continuation)
                            return@suspendCancellableCoroutine
                        }
                        
                        // Create callback manager for this session
                        val callbackManager = CallbackManager.Factory.create()
                        
                        LoginManager.getInstance().registerCallback(callbackManager,
                            object : FacebookCallback<com.facebook.login.LoginResult> {
                                override fun onSuccess(result: com.facebook.login.LoginResult) {
                                    Log.d(TAG, "Facebook login successful, authenticating with Firebase")
                                    
                                    val token = result.accessToken
                                    val credential = FacebookAuthProvider.getCredential(token.token)
                                    
                                    proceedWithFirebaseAuthEnhanced(credential, continuation)
                                }
                                
                                override fun onCancel() {
                                    Log.d(TAG, "Facebook Sign-In cancelled by user")
                                    val errorResult = AuthResult.Error(AuthException.Unknown("Facebook sign-in was cancelled"))
                                    _authState.value = errorResult
                                    if (continuation.isActive) {
                                        continuation.resume(errorResult) {}
                                    }
                                }
                                
                                override fun onError(error: FacebookException) {
                                    Log.e(TAG, "Facebook Sign-In error", error)
                                    // Determine if error is network-related
                                    val errorResult = when {
                                        NetworkUtils.isTimeoutError(error) -> {
                                            AuthResult.Error(AuthException.TimeoutError)
                                        }
                                        NetworkUtils.isNetworkUnavailableError(error) -> {
                                            AuthResult.Error(AuthException.NoNetworkConnection)
                                        }
                                        NetworkUtils.isRetryableError(error) -> {
                                            AuthResult.Error(AuthException.NetworkError)
                                        }
                                        else -> {
                                            AuthResult.Error(AuthException.Unknown("Facebook login failed: ${error.message}"))
                                        }
                                    }
                                    _authState.value = errorResult
                                    if (continuation.isActive) {
                                        continuation.resume(errorResult) {}
                                    }
                                }
                            })
                        
                        // Start Facebook login
                        try {
                            LoginManager.getInstance().logInWithReadPermissions(
                                activity,
                                listOf("email", "public_profile")
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to start Facebook login", e)
                            val errorResult = if (NetworkUtils.isNetworkUnavailableError(e)) {
                                AuthResult.Error(AuthException.NoNetworkConnection)
                            } else {
                                AuthResult.Error(AuthException.Unknown("Failed to start Facebook login: ${e.message}"))
                            }
                            _authState.value = errorResult
                            if (continuation.isActive) {
                                continuation.resume(errorResult) {}
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
                val errorResult = AuthResult.Error(AuthException.TimeoutError)
                _authState.value = errorResult
                errorResult
            }
            
        } catch (e: CancellationException) {
            throw e
        } catch (e: NetworkNotAvailableException) {
            Log.e(TAG, "No network connection for Facebook sign-in", e)
            val errorResult = AuthResult.Error(AuthException.NoNetworkConnection)
            _authState.value = errorResult
            errorResult
        } catch (e: TimeoutException) {
            Log.e(TAG, "Facebook Sign-In timed out", e)
            val errorResult = AuthResult.Error(AuthException.TimeoutError)
            _authState.value = errorResult
            errorResult
        } catch (e: Exception) {
            Log.e(TAG, "Facebook Sign-In error", e)
            val errorResult = if (NetworkUtils.isTimeoutError(e)) {
                AuthResult.Error(AuthException.TimeoutError)
            } else if (NetworkUtils.isNetworkUnavailableError(e)) {
                AuthResult.Error(AuthException.NoNetworkConnection)
            } else if (NetworkUtils.isRetryableError(e)) {
                AuthResult.Error(AuthException.NetworkError)
            } else {
                AuthResult.Error(AuthException.Unknown("Facebook sign-in failed: ${e.message}"))
            }
            _authState.value = errorResult
            errorResult
        }
    }
    
    /**
     * Proceed with Firebase authentication using Facebook credential (legacy method for backward compatibility)
     */
    private fun proceedWithFirebaseAuth(
        credential: com.google.firebase.auth.AuthCredential,
        continuation: kotlinx.coroutines.CancellableContinuation<AuthResult>
    ) {
        proceedWithFirebaseAuthEnhanced(credential, continuation)
    }
    
    /**
     * Enhanced Firebase authentication with retry logic for slow connections
     */
    private fun proceedWithFirebaseAuthEnhanced(
        credential: com.google.firebase.auth.AuthCredential,
        continuation: kotlinx.coroutines.CancellableContinuation<AuthResult>,
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
                executeFirebaseAuth(credential, continuation, retryCount, maxRetries)
            }, retryDelay)
        } else {
            executeFirebaseAuth(credential, continuation, retryCount, maxRetries)
        }
    }
    
    /**
     * Execute Firebase authentication with error handling
     */
    private fun executeFirebaseAuth(
        credential: com.google.firebase.auth.AuthCredential,
        continuation: kotlinx.coroutines.CancellableContinuation<AuthResult>,
        retryCount: Int,
        maxRetries: Int
    ) {
        // Check network before attempting
        if (!NetworkUtils.isNetworkAvailable(context)) {
            val errorResult = AuthResult.Error(AuthException.NoNetworkConnection)
            _authState.value = errorResult
            if (continuation.isActive) {
                continuation.resume(errorResult) {}
            }
            return
        }
        
        Log.d(TAG, "Firebase auth attempt ${retryCount + 1} of $maxRetries")
        
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        Log.d(TAG, "Firebase authentication successful for social user")
                        
                        // Check if user already has profile data (existing user)
                        var isProfileComplete = false
                        
                        try {
                            // First check if profile already exists
                            // For now, assume new user (simpler approach to fix compilation)
                            // TODO: Implement proper profile check in future iteration
                            isProfileComplete = false
                            
                            // Create initial document for social signup users
                            val deviceId = DeviceUtils.getDeviceId(context)
                            val userData = FirestoreUserData(
                                email = (firebaseUser.email ?: "").lowercase(),
                                deviceId = deviceId,
                                profileCompleted = false, // Social signup users need to complete profile
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            
                            // Store user data (non-blocking)
                            firestore.collection(FirestoreUserData.COLLECTION_NAME)
                                .document(firebaseUser.uid)
                                .collection("personal_data")
                                .document("info")
                                .set(userData, com.google.firebase.firestore.SetOptions.merge())
                                .addOnSuccessListener {
                                    Log.d(TAG, "Social user data stored/updated successfully")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Failed to store social user data (non-critical)", e)
                                }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to check/store user data in Firestore", e)
                            // On error, assume profile is incomplete to be safe
                            isProfileComplete = false
                        }
                        
                        val response = SignUpResponse(
                            userId = firebaseUser.uid,
                            email = (firebaseUser.email ?: "").lowercase(),
                            isEmailVerified = firebaseUser.isEmailVerified,
                            displayName = firebaseUser.displayName,
                            photoUrl = firebaseUser.photoUrl?.toString(),
                            providerId = credential.provider,
                            isProfileComplete = isProfileComplete
                        )
                        
                        val successResult = AuthResult.Success(response)
                        _authState.value = successResult
                        
                        Log.d(TAG, "Social Sign-In successful: ${response.userId}")
                        if (continuation.isActive) {
                            continuation.resume(successResult) {}
                        }
                    } else {
                        val errorResult = AuthResult.Error(AuthException.Unknown("Authentication failed - no user returned"))
                        _authState.value = errorResult
                        if (continuation.isActive) {
                            continuation.resume(errorResult) {}
                        }
                    }
                } else {
                    val exception = task.exception
                    Log.e(TAG, "Firebase authentication failed (attempt ${retryCount + 1})", exception)
                    
                    // Check if we should retry
                    val shouldRetry = retryCount < maxRetries - 1 && 
                        exception != null && 
                        isRetryableFirebaseError(exception)
                    
                    if (shouldRetry) {
                        Log.d(TAG, "Will retry Firebase auth (retryable error detected)")
                        proceedWithFirebaseAuthEnhanced(credential, continuation, retryCount + 1, maxRetries)
                    } else {
                        val errorResult = when (exception) {
                            is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                                AuthResult.Error(AuthException.EmailAlreadyInUse)
                            }
                            is com.google.firebase.auth.FirebaseAuthException -> {
                                AuthResult.Error(mapFirebaseException(exception))
                            }
                            else -> {
                                if (exception != null && NetworkUtils.isTimeoutError(exception)) {
                                    AuthResult.Error(AuthException.TimeoutError)
                                } else if (exception != null && NetworkUtils.isNetworkUnavailableError(exception)) {
                                    AuthResult.Error(AuthException.NoNetworkConnection)
                                } else {
                                    AuthResult.Error(AuthException.Unknown("Authentication failed: ${exception?.message}"))
                                }
                            }
                        }
                        _authState.value = errorResult
                        if (continuation.isActive) {
                            continuation.resume(errorResult) {}
                        }
                    }
                }
            }
    }
    
    /**
     * Check if Firebase error is retryable (network-related)
     */
    private fun isRetryableFirebaseError(exception: Exception): Boolean {
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
     * Send email verification to current user
     */
    suspend fun sendEmailVerification() {
        try {
            val user = firebaseAuth.currentUser
            if (user != null && !user.isEmailVerified) {
                _emailVerificationState.value = _emailVerificationState.value.copy(
                    isVerificationSent = false,
                    error = null
                )
                
                user.sendEmailVerification().await()
                
                _emailVerificationState.value = _emailVerificationState.value.copy(
                    isVerificationSent = true,
                    error = null
                )
                
                Log.d(TAG, "Email verification sent to: ${user.email}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email verification", e)
            _emailVerificationState.value = _emailVerificationState.value.copy(
                isVerificationSent = false,
                error = "Failed to send verification email: ${e.message}"
            )
        }
    }
    
    /**
     * Check if current user's email is verified
     */
    suspend fun checkEmailVerification(): Boolean {
        return try {
            val user = firebaseAuth.currentUser
            user?.reload()?.await()
            val isVerified = user?.isEmailVerified ?: false
            
            _emailVerificationState.value = _emailVerificationState.value.copy(
                isVerified = isVerified
            )
            
            isVerified
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check email verification", e)
            false
        }
    }
    
    /**
     * Sign out current user
     */
    fun signOut() {
        firebaseAuth.signOut()
        _authState.value = AuthResult.Loading
        _emailVerificationState.value = EmailVerificationState()
        Log.d(TAG, "User signed out")
    }
    
    /**
     * Get current user
     */
    fun getCurrentUser() = firebaseAuth.currentUser
    
    /**
     * Validate signup request before sending to Firebase
     */
    private fun validateSignUpRequest(request: SignUpRequest): AuthException? {
        // Validate email
        if (request.email.isBlank()) {
            return AuthException.InvalidEmail
        }
        
        if (!ValidationUtils.isValidEmail(request.email.lowercase().trim())) {
            return AuthException.InvalidEmail
        }
        
        // Validate password
        if (request.password.isBlank()) {
            return AuthException.WeakPassword
        }
        
        if (!ValidationUtils.isStrongPassword(request.password)) {
            return AuthException.WeakPassword
        }
        
        if (!ValidationUtils.hasNoWhitespace(request.password)) {
            return AuthException.WeakPassword
        }
        
        return null
    }
    
    /**
     * Map Firebase exceptions to custom AuthException
     */
    private fun mapFirebaseException(e: FirebaseAuthException): AuthException {
        return when (e.errorCode) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> AuthException.EmailAlreadyInUse
            "ERROR_WEAK_PASSWORD" -> AuthException.WeakPassword
            "ERROR_INVALID_EMAIL" -> AuthException.InvalidEmail
            "ERROR_USER_DISABLED" -> AuthException.UserDisabled
            "ERROR_TOO_MANY_REQUESTS" -> AuthException.TooManyRequests
            "ERROR_OPERATION_NOT_ALLOWED" -> AuthException.OperationNotAllowed
            "ERROR_NETWORK_REQUEST_FAILED" -> AuthException.NetworkError
            else -> AuthException.Unknown(e.message ?: "Unknown Firebase error")
        }
    }
}

/**
 * Extension function to create SignUpRequest from form state
 */
fun com.brightcare.patient.ui.component.signup_component.SignUpFormState.toSignUpRequest(
    deviceId: String
): SignUpRequest {
    return SignUpRequest(
        email = this.email.lowercase().trim(),
        password = this.password,
        deviceId = deviceId
    )
}


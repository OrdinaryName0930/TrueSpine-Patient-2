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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

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
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    firestore.collection(FirestoreUserData.COLLECTION_NAME)
                        .document(firebaseUser.uid)
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
                    providerId = "password"
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
     */
    suspend fun signInWithGoogle(activity: androidx.activity.ComponentActivity): AuthResult {
        return try {
            _authState.value = AuthResult.Loading
            
            Log.d(TAG, "Starting Google Sign-In")
            
            val credentialManager = CredentialManager.create(context)
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(GOOGLE_WEB_CLIENT_ID)
                .build()
            
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            
            val result = credentialManager.getCredential(
                request = request,
                context = activity
            )
            
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
                    
                    try {
                        // Try to fetch sign-in methods for this email
                        val signInMethods = firebaseAuth.fetchSignInMethodsForEmail(email).await()
                        
                        if (signInMethods.signInMethods?.isNotEmpty() == true) {
                            // Email exists with other providers
                            val existingMethods = signInMethods.signInMethods!!
                            Log.d(TAG, "Email exists with methods: $existingMethods")
                            
                            if (existingMethods.contains("password") && !existingMethods.contains("google.com")) {
                                // Email is registered with email/password but not Google
                                val errorResult = AuthResult.Error(AuthException.EmailAlreadyInUse)
                                _authState.value = errorResult
                                return errorResult
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not check existing sign-in methods, proceeding with sign-in", e)
                    }
                    
                    // Proceed with Firebase authentication
                    val authResult = firebaseAuth.signInWithCredential(tempCredential).await()
                    
                    val firebaseUser = authResult.user
                    if (firebaseUser != null) {
                        // Store user data in Firestore for Google sign-in
                        try {
                            val deviceId = DeviceUtils.getDeviceId(context)
                            val userData = FirestoreUserData(
                                email = (firebaseUser.email ?: "").lowercase(),
                                deviceId = deviceId,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                            
                            firestore.collection(FirestoreUserData.COLLECTION_NAME)
                                .document(firebaseUser.uid)
                                .set(userData)
                                .await()
                            
                            Log.d(TAG, "Google user data stored in Firestore successfully")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to store Google user data in Firestore", e)
                        }
                        
                        val response = SignUpResponse(
                            userId = firebaseUser.uid,
                            email = (firebaseUser.email ?: "").lowercase(),
                            isEmailVerified = firebaseUser.isEmailVerified,
                            displayName = firebaseUser.displayName,
                            photoUrl = firebaseUser.photoUrl?.toString(),
                            providerId = "google.com"
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
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In error", e)
            val errorResult = AuthResult.Error(AuthException.NetworkError)
            _authState.value = errorResult
            errorResult
        }
    }
    
    /**
     * Sign in with Facebook using Facebook SDK
     */
    suspend fun signInWithFacebook(activity: androidx.activity.ComponentActivity): AuthResult {
        return try {
            _authState.value = AuthResult.Loading
            
            Log.d(TAG, "Starting Facebook Sign-In")
            
            // Create callback manager
            val callbackManager = CallbackManager.Factory.create()
            
            // Use suspendCoroutine to convert callback to suspend function
            kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
                
                fun proceedWithFacebookAuth(
                    credential: com.google.firebase.auth.AuthCredential
                ) {
                    firebaseAuth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val firebaseUser = task.result?.user
                                if (firebaseUser != null) {
                                    // Store user data in Firestore for Facebook sign-in
                                    try {
                                        val deviceId = DeviceUtils.getDeviceId(context)
                                        val userData = FirestoreUserData(
                                            email = (firebaseUser.email ?: "").lowercase(),
                                            deviceId = deviceId,
                                            createdAt = System.currentTimeMillis(),
                                            updatedAt = System.currentTimeMillis()
                                        )
                                        
                                        firestore.collection(FirestoreUserData.COLLECTION_NAME)
                                            .document(firebaseUser.uid)
                                            .set(userData)
                                        
                                        Log.d(TAG, "Facebook user data stored in Firestore successfully")
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Failed to store Facebook user data in Firestore", e)
                                    }
                                    
                                    val response = SignUpResponse(
                                        userId = firebaseUser.uid,
                                        email = (firebaseUser.email ?: "").lowercase(),
                                        isEmailVerified = firebaseUser.isEmailVerified,
                                        displayName = firebaseUser.displayName,
                                        photoUrl = firebaseUser.photoUrl?.toString(),
                                        providerId = "facebook.com"
                                    )
                                    
                                    val successResult = AuthResult.Success(response)
                                    _authState.value = successResult
                                    
                                    Log.d(TAG, "Facebook Sign-In successful: ${response.userId}")
                                    continuation.resume(successResult) {}
                                } else {
                                    val errorResult = AuthResult.Error(AuthException.Unknown("Facebook sign-in failed - no user returned"))
                                    _authState.value = errorResult
                                    continuation.resume(errorResult) {}
                                }
                            } else {
                                val exception = task.exception
                                Log.e(TAG, "Firebase authentication failed", exception)
                                
                                val errorResult = if (exception is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                                    AuthResult.Error(AuthException.EmailAlreadyInUse)
                                } else {
                                    AuthResult.Error(AuthException.Unknown("Firebase authentication failed: ${exception?.message}"))
                                }
                                _authState.value = errorResult
                                continuation.resume(errorResult) {}
                            }
                        }
                }
                
                LoginManager.getInstance().registerCallback(callbackManager,
                    object : FacebookCallback<LoginResult> {
                        override fun onSuccess(result: LoginResult) {
                            Log.d(TAG, "Facebook login successful, checking for existing email")
                            
                            val token = result.accessToken
                            val credential = FacebookAuthProvider.getCredential(token.token)
                            
                            // First check if email is already registered with different provider
                            // We need to get the email from Facebook first
                            val graphRequest = GraphRequest.newMeRequest(token) { jsonObject, _ ->
                                try {
                                    val email = jsonObject?.optString("email")
                                    if (!email.isNullOrEmpty()) {
                                        Log.d(TAG, "Checking if Facebook email exists: $email")
                                        
                                        // Check existing sign-in methods
                                        firebaseAuth.fetchSignInMethodsForEmail(email)
                                            .addOnCompleteListener { methodsTask ->
                                                if (methodsTask.isSuccessful) {
                                                    val signInMethods = methodsTask.result?.signInMethods
                                                    if (signInMethods?.isNotEmpty() == true) {
                                                        Log.d(TAG, "Email exists with methods: $signInMethods")
                                                        
                                                        if (signInMethods.contains("password") && !signInMethods.contains("facebook.com")) {
                                                            // Email is registered with email/password but not Facebook
                                                            val errorResult = AuthResult.Error(AuthException.EmailAlreadyInUse)
                                                            _authState.value = errorResult
                                                            continuation.resume(errorResult) {}
                                                            return@addOnCompleteListener
                                                        }
                                                    }
                                                }
                                                
                                                // Proceed with Firebase authentication
                                                proceedWithFacebookAuth(credential)
                                            }
                                    } else {
                                        // No email from Facebook, proceed anyway
                                        proceedWithFacebookAuth(credential)
                                    }
                                } catch (e: Exception) {
                                    Log.w(TAG, "Could not get Facebook email, proceeding with sign-in", e)
                                    proceedWithFacebookAuth(credential)
                                }
                            }
                            
                            val parameters = android.os.Bundle()
                            parameters.putString("fields", "email,name")
                            graphRequest.parameters = parameters
                            graphRequest.executeAsync()
                        }
                        
                        override fun onCancel() {
                            Log.d(TAG, "Facebook Sign-In cancelled by user")
                            val errorResult = AuthResult.Error(AuthException.Unknown("Sign-in cancelled"))
                            _authState.value = errorResult
                            continuation.resume(errorResult) {}
                        }
                        
                        override fun onError(error: FacebookException) {
                            Log.e(TAG, "Facebook Sign-In error", error)
                            val errorResult = AuthResult.Error(AuthException.Unknown("Facebook login failed: ${error.message}"))
                            _authState.value = errorResult
                            continuation.resume(errorResult) {}
                        }
                    })
                
                // Start Facebook login
                LoginManager.getInstance().logInWithReadPermissions(
                    activity,
                    listOf("email", "public_profile")
                )
                
                // Handle cancellation
                continuation.invokeOnCancellation {
                    LoginManager.getInstance().logOut()
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Facebook Sign-In error", e)
            val errorResult = AuthResult.Error(AuthException.NetworkError)
            _authState.value = errorResult
            errorResult
        }
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


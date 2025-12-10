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
        private const val SUBCOLLECTION_PERSONAL_DATA = "personal_data"
        private const val DOCUMENT_INFO = "info"
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
            
            // Always send reset link regardless of account existence (security best practice)
            // This prevents email enumeration attacks where attackers can discover valid emails
            Log.d(TAG, "Proceeding to send reset link to: ${request.email}")
            Log.d(TAG, "Note: Sending reset link regardless of account existence for security")
            
            // Send password reset email using Firebase Auth
            firebaseAuth.sendPasswordResetEmail(request.email).await()
            
            val response = SendResetLinkResponse(
                success = true,
                message = "If an account with this email exists, a password reset link has been sent to ${request.email}"
            )
            
            Log.d(TAG, "Reset link request processed for: ${request.email}")
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
     * Check if user exists by checking Firebase Auth, new nested structure, AND legacy flat structure
     * This handles both old and new database formats during migration period
     */
    private suspend fun checkUserExists(email: String): Boolean {
        return try {
            Log.d(TAG, "=== Starting user existence check for email: $email ===")
            
            var authUserExists = false
            var newStructureExists = false
            var legacyStructureExists = false
            
            // Step 1: Check Firebase Auth as it's the authoritative source
            Log.d(TAG, "Step 1: Checking Firebase Auth for: $email")
            try {
                val signInMethods = firebaseAuth.fetchSignInMethodsForEmail(email).await()
                authUserExists = signInMethods.signInMethods?.isNotEmpty() == true
                
                if (authUserExists) {
                    Log.d(TAG, "✅ User found in Firebase Auth: $email")
                    Log.d(TAG, "Sign-in methods: ${signInMethods.signInMethods}")
                } else {
                    Log.d(TAG, "❌ User not found in Firebase Auth: $email")
                }
            } catch (authException: Exception) {
                Log.w(TAG, "⚠️ Firebase Auth check failed for: $email", authException)
            }
            
            // Step 2: Check NEW nested structure (client/{id}/personal_data/info)
            Log.d(TAG, "Step 2: Checking NEW nested structure for: $email")
            try {
                // Use collection group query to search all 'info' documents across all clients
                Log.d(TAG, "Trying collection group query with lowercase email...")
                val nestedQuery = firestore.collectionGroup(DOCUMENT_INFO)
                    .whereEqualTo("email", email.lowercase())
                    .limit(1)
                    .get()
                    .await()
                
                Log.d(TAG, "Nested structure query result: ${nestedQuery.size()} documents found")
                
                if (!nestedQuery.isEmpty) {
                    newStructureExists = true
                    Log.d(TAG, "✅ User found in NEW nested structure: $email")
                    nestedQuery.documents.forEach { doc ->
                        Log.d(TAG, "Found document at path: ${doc.reference.path}")
                    }
                } else {
                    // Try with original case
                    val nestedQueryOriginal = firestore.collectionGroup(DOCUMENT_INFO)
                        .whereEqualTo("email", email)
                        .limit(1)
                        .get()
                        .await()
                    
                    if (!nestedQueryOriginal.isEmpty) {
                        newStructureExists = true
                        Log.d(TAG, "✅ User found in NEW nested structure (original case): $email")
                    }
                }
            } catch (nestedException: Exception) {
                Log.e(TAG, "⚠️ Nested structure check failed for: $email", nestedException)
            }
            
            // Step 3: Check LEGACY flat structure (client/{id} with email field directly)
            Log.d(TAG, "Step 3: Checking LEGACY flat structure for: $email")
            try {
                // Check old flat structure where email was directly in client document
                Log.d(TAG, "Trying legacy flat structure query with lowercase email...")
                val legacyQuery = firestore.collection(USERS_COLLECTION)
                    .whereEqualTo("email", email.lowercase())
                    .limit(1)
                    .get()
                    .await()
                
                Log.d(TAG, "Legacy structure query result: ${legacyQuery.size()} documents found")
                
                if (!legacyQuery.isEmpty) {
                    legacyStructureExists = true
                    Log.d(TAG, "✅ User found in LEGACY flat structure: $email")
                    legacyQuery.documents.forEach { doc ->
                        Log.d(TAG, "Found legacy document at path: ${doc.reference.path}")
                        Log.d(TAG, "Legacy document data: ${doc.data}")
                    }
                } else {
                    // Try with original case
                    val legacyQueryOriginal = firestore.collection(USERS_COLLECTION)
                        .whereEqualTo("email", email)
                        .limit(1)
                        .get()
                        .await()
                    
                    if (!legacyQueryOriginal.isEmpty) {
                        legacyStructureExists = true
                        Log.d(TAG, "✅ User found in LEGACY flat structure (original case): $email")
                        legacyQueryOriginal.documents.forEach { doc ->
                            Log.d(TAG, "Found legacy document at path: ${doc.reference.path}")
                        }
                    }
                }
            } catch (legacyException: Exception) {
                Log.e(TAG, "⚠️ Legacy structure check failed for: $email", legacyException)
            }
            
            // Decision logic: User exists if found in ANY of the sources
            val userExists = authUserExists || newStructureExists || legacyStructureExists
            
            Log.d(TAG, "=== Final decision for $email ===")
            Log.d(TAG, "Firebase Auth: $authUserExists")
            Log.d(TAG, "New Structure: $newStructureExists") 
            Log.d(TAG, "Legacy Structure: $legacyStructureExists")
            Log.d(TAG, "Final result: $userExists")
            
            if (legacyStructureExists && !newStructureExists) {
                Log.w(TAG, "⚠️ User found in legacy structure but not new structure - needs migration!")
            }
            
            if (!userExists) {
                Log.w(TAG, "❌ User not found in Firebase Auth, new structure, or legacy structure: $email")
            }
            
            return userExists
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Unexpected error checking user existence for: $email", e)
            
            // On unexpected error, be conservative and return false
            return false
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
     * Debug function to check what's actually in the database
     * This helps identify if the issue is with data structure or query logic
     */
    suspend fun debugDatabaseStructure(email: String): String {
        val debugInfo = StringBuilder()
        debugInfo.append("=== DATABASE DEBUG INFO ===\n")
        debugInfo.append("Email being searched: $email\n\n")
        
        try {
            // Check Firebase Auth
            debugInfo.append("1. FIREBASE AUTH CHECK:\n")
            try {
                val signInMethods = firebaseAuth.fetchSignInMethodsForEmail(email).await()
                debugInfo.append("   Sign-in methods: ${signInMethods.signInMethods}\n")
                debugInfo.append("   User exists in Auth: ${signInMethods.signInMethods?.isNotEmpty() == true}\n")
            } catch (e: Exception) {
                debugInfo.append("   Auth check failed: ${e.message}\n")
            }
            
            debugInfo.append("\n2. FIRESTORE COLLECTION GROUP QUERY:\n")
            try {
                // Check collection group for 'info' documents
                val allInfoDocs = firestore.collectionGroup(DOCUMENT_INFO)
                    .limit(10)  // Get first 10 to see what's there
                    .get()
                    .await()
                
                debugInfo.append("   Total 'info' documents found: ${allInfoDocs.size()}\n")
                
                if (allInfoDocs.isEmpty) {
                    debugInfo.append("   ❌ No 'info' documents found in any client/personal_data subcollection\n")
                } else {
                    debugInfo.append("   📄 Found documents:\n")
                    allInfoDocs.documents.forEachIndexed { index, doc ->
                        val docEmail = doc.getString("email") ?: "NO_EMAIL"
                        debugInfo.append("      ${index + 1}. Path: ${doc.reference.path}\n")
                        debugInfo.append("         Email: $docEmail\n")
                    }
                }
                
                // Specific email search
                debugInfo.append("\n   Searching for specific email: $email\n")
                val emailQuery = firestore.collectionGroup(DOCUMENT_INFO)
                    .whereEqualTo("email", email.lowercase())
                    .get()
                    .await()
                    
                debugInfo.append("   Documents matching email: ${emailQuery.size()}\n")
                
            } catch (e: Exception) {
                debugInfo.append("   Firestore query failed: ${e.message}\n")
            }
            
            debugInfo.append("\n3. LEGACY FLAT STRUCTURE CHECK:\n")
            try {
                // Check if there are any documents in the old flat structure
                debugInfo.append("   Checking client collection directly for email field...\n")
                val legacyQuery = firestore.collection(USERS_COLLECTION)
                    .whereEqualTo("email", email.lowercase())
                    .limit(1)
                    .get()
                    .await()
                    
                debugInfo.append("   Documents in legacy flat structure: ${legacyQuery.size()}\n")
                if (!legacyQuery.isEmpty) {
                    debugInfo.append("   ✅ Found user in legacy flat structure!\n")
                    legacyQuery.documents.forEach { doc ->
                        debugInfo.append("      Path: ${doc.reference.path}\n")
                        debugInfo.append("      Email: ${doc.getString("email")}\n")
                        debugInfo.append("      Profile Completed: ${doc.getBoolean("profileCompleted")}\n")
                    }
                } else {
                    debugInfo.append("   ❌ No user found in legacy flat structure\n")
                }
                
                // Also check all documents in client collection to see what's there
                debugInfo.append("\n   Checking ALL documents in client collection:\n")
                val allClientDocs = firestore.collection(USERS_COLLECTION)
                    .limit(5)
                    .get()
                    .await()
                    
                debugInfo.append("   Total client documents: ${allClientDocs.size()}\n")
                if (allClientDocs.isEmpty) {
                    debugInfo.append("   ❌ Client collection is completely empty\n")
                } else {
                    debugInfo.append("   📄 Found client documents:\n")
                    allClientDocs.documents.forEachIndexed { index, doc ->
                        val docEmail = doc.getString("email") ?: "NO_EMAIL"
                        debugInfo.append("      ${index + 1}. ID: ${doc.id}\n")
                        debugInfo.append("         Email: $docEmail\n")
                        debugInfo.append("         Profile Completed: ${doc.getBoolean("profileCompleted")}\n")
                    }
                }
            } catch (e: Exception) {
                debugInfo.append("   Legacy check failed: ${e.message}\n")
            }
            
        } catch (e: Exception) {
            debugInfo.append("Debug failed: ${e.message}\n")
        }
        
        debugInfo.append("=== END DEBUG INFO ===")
        return debugInfo.toString()
    }
    
    /**
     * Clear current state
     */
    fun clearState() {
        _forgotPasswordState.value = null
    }
}
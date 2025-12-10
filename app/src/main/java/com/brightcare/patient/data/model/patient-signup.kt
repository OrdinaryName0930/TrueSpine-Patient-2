package com.brightcare.patient.data.model

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory

/**
 * Data models for patient signup functionality
 * Handles signup requests, responses, and authentication states
 */

/**
 * Request model for email/password signup
 */
data class SignUpRequest(
    val email: String,
    val password: String,
    val deviceId: String
)

/**
 * Response model for successful signup
 */
data class SignUpResponse(
    val userId: String,
    val email: String,
    val isEmailVerified: Boolean,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val providerId: String, // "password", "google.com", "facebook.com"
    val isProfileComplete: Boolean = false // Whether user has completed their profile
)

/**
 * Authentication result wrapper
 */
sealed class AuthResult {
    data class Success(val response: SignUpResponse) : AuthResult()
    data class Error(val exception: AuthException) : AuthResult()
    object Loading : AuthResult()
}

/**
 * Custom exception for authentication errors
 * Enhanced with timeout and slow network handling
 */
sealed class AuthException(message: String) : Exception(message) {
    object EmailAlreadyInUse : AuthException("The email address is already in use by another account.")
    object WeakPassword : AuthException("The password is too weak.")
    object InvalidEmail : AuthException("The email address is badly formatted.")
    object NetworkError : AuthException("Network error occurred. Please check your connection.")
    object UserDisabled : AuthException("This user account has been disabled.")
    object TooManyRequests : AuthException("Too many requests. Please try again later.")
    object OperationNotAllowed : AuthException("This operation is not allowed.")
    
    // Enhanced network/timeout exceptions for slow internet handling
    object TimeoutError : AuthException("Connection timed out. Your internet may be slow. Please try again.")
    object NoNetworkConnection : AuthException("No internet connection. Please check your network and try again.")
    object SlowNetworkError : AuthException("Your internet connection is slow. The operation is taking longer than expected.")
    data class RetryableError(val originalMessage: String, val attemptsMade: Int) : 
        AuthException("Failed after $attemptsMade attempts: $originalMessage. Please try again.")
    
    data class Unknown(val originalMessage: String) : AuthException("An unknown error occurred: $originalMessage")
    
    companion object {
        /**
         * User-friendly messages for slow connection scenarios
         */
        const val SLOW_NETWORK_TIP = "Tip: Move to an area with better signal or try using Wi-Fi."
        const val RETRY_MESSAGE = "Please tap to try again."
    }
}

/**
 * Social login provider types
 */
enum class SocialProvider {
    GOOGLE,
    FACEBOOK
}

/**
 * Social login request
 */
data class SocialLoginRequest(
    val provider: SocialProvider,
    val idToken: String? = null, // For Google
    val accessToken: String? = null // For Facebook
)

/**
 * Email verification state
 */
data class EmailVerificationState(
    val isVerificationSent: Boolean = false,
    val isVerified: Boolean = false,
    val error: String? = null
) {
    companion object {
        /**
         * Toast messages for email verification
         */
        const val VERIFICATION_SENT_MESSAGE = "Verification email sent! Please check your inbox or spams and verify your email before signing in."
        const val VERIFICATION_FAILED_MESSAGE = "Failed to send verification email. Please try again."
        const val CHECK_EMAIL_MESSAGE = "Please check your email and click the verification link."
        
        /**
         * Toast action labels
         */
        const val ACTION_OK = "OK"
        const val ACTION_TRY_AGAIN = "Try Again"
        
        /**
         * Toast durations (in milliseconds)
         */
        const val SUCCESS_DURATION = 6000L // Longer for important messages
        const val ERROR_DURATION = 5000L
    }
}

/**
 * Firestore client data model for storing client registration information
 */
data class FirestoreUserData(
    val email: String = "",
    val deviceId: String = "",
    val profileCompleted: Boolean = false, // Explicitly set to false for new users
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val COLLECTION_NAME = "client"
    }
}

/**
 * Password hashing utility for secure password storage
 */
object PasswordHasher {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 100000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 32
    
    /**
     * Hash a password with a random salt using PBKDF2
     */
    fun hashPassword(password: String): String {
        val salt = generateSalt()
        val hash = pbkdf2(password, salt, ITERATIONS, KEY_LENGTH)
        return "${salt.toHex()}:${hash.toHex()}"
    }
    
    /**
     * Verify a password against a stored hash
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            val parts = storedHash.split(":")
            if (parts.size != 2) return false
            
            val salt = parts[0].fromHex()
            val hash = parts[1].fromHex()
            val testHash = pbkdf2(password, salt, ITERATIONS, KEY_LENGTH)
            
            hash.contentEquals(testHash)
        } catch (e: Exception) {
            false
        }
    }
    
    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt
    }
    
    private fun pbkdf2(password: String, salt: ByteArray, iterations: Int, keyLength: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }
    
    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }
    
    private fun String.fromHex(): ByteArray {
        return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}


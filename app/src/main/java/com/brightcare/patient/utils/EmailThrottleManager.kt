package com.brightcare.patient.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.concurrent.TimeUnit

/**
 * Email Throttle Manager
 * Manages email sending intervals to prevent spam and abuse
 * Implements 5-minute cooldown period for email verification and password reset
 */
class EmailThrottleManager private constructor(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    companion object {
        private const val TAG = "EmailThrottleManager"
        private const val PREFS_NAME = "email_throttle_prefs"
        private const val THROTTLE_DURATION_MS = 5 * 60 * 1000L // 5 minutes in milliseconds
        
        // Keys for different email types
        private const val KEY_EMAIL_VERIFICATION_PREFIX = "email_verification_"
        private const val KEY_PASSWORD_RESET_PREFIX = "password_reset_"
        
        @Volatile
        private var INSTANCE: EmailThrottleManager? = null
        
        fun getInstance(context: Context): EmailThrottleManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EmailThrottleManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Email types for throttling
     */
    enum class EmailType(val keyPrefix: String) {
        EMAIL_VERIFICATION(KEY_EMAIL_VERIFICATION_PREFIX),
        PASSWORD_RESET(KEY_PASSWORD_RESET_PREFIX)
    }
    
    /**
     * Result of throttle check
     */
    data class ThrottleResult(
        val canSend: Boolean,
        val remainingTimeMs: Long = 0L,
        val remainingTimeFormatted: String = ""
    ) {
        val remainingMinutes: Int
            get() = (remainingTimeMs / (60 * 1000)).toInt()
        
        val remainingSeconds: Int
            get() = ((remainingTimeMs % (60 * 1000)) / 1000).toInt()
    }
    
    /**
     * Check if email can be sent for the given type and email address
     * @param emailType Type of email (verification or password reset)
     * @param email Email address to check
     * @return ThrottleResult indicating if email can be sent and remaining time if not
     */
    fun canSendEmail(emailType: EmailType, email: String): ThrottleResult {
        val key = generateKey(emailType, email)
        val lastSentTime = sharedPreferences.getLong(key, 0L)
        val currentTime = System.currentTimeMillis()
        val timeSinceLastSent = currentTime - lastSentTime
        
        Log.d(TAG, "Checking throttle for ${emailType.name} to $email")
        Log.d(TAG, "Last sent: $lastSentTime, Current: $currentTime, Time since: $timeSinceLastSent")
        
        return if (timeSinceLastSent >= THROTTLE_DURATION_MS || lastSentTime == 0L) {
            Log.d(TAG, "✅ Email can be sent to $email")
            ThrottleResult(canSend = true)
        } else {
            val remainingTime = THROTTLE_DURATION_MS - timeSinceLastSent
            val formattedTime = formatRemainingTime(remainingTime)
            
            Log.d(TAG, "❌ Email throttled for $email. Remaining time: $formattedTime")
            ThrottleResult(
                canSend = false,
                remainingTimeMs = remainingTime,
                remainingTimeFormatted = formattedTime
            )
        }
    }
    
    /**
     * Record that an email was sent
     * @param emailType Type of email that was sent
     * @param email Email address that received the email
     */
    fun recordEmailSent(emailType: EmailType, email: String) {
        val key = generateKey(emailType, email)
        val currentTime = System.currentTimeMillis()
        
        sharedPreferences.edit()
            .putLong(key, currentTime)
            .apply()
        
        Log.d(TAG, "📧 Recorded ${emailType.name} sent to $email at $currentTime")
    }
    
    /**
     * Clear throttle record for specific email type and address
     * @param emailType Type of email to clear
     * @param email Email address to clear
     */
    fun clearThrottle(emailType: EmailType, email: String) {
        val key = generateKey(emailType, email)
        sharedPreferences.edit()
            .remove(key)
            .apply()
        
        Log.d(TAG, "🗑️ Cleared throttle for ${emailType.name} to $email")
    }
    
    /**
     * Clear all throttle records (for testing or admin purposes)
     */
    fun clearAllThrottles() {
        sharedPreferences.edit()
            .clear()
            .apply()
        
        Log.d(TAG, "🗑️ Cleared all email throttles")
    }
    
    /**
     * Get remaining time until next email can be sent
     * @param emailType Type of email to check
     * @param email Email address to check
     * @return Remaining time in milliseconds, 0 if can send immediately
     */
    fun getRemainingTime(emailType: EmailType, email: String): Long {
        return canSendEmail(emailType, email).remainingTimeMs
    }
    
    /**
     * Generate unique key for email type and address combination
     */
    private fun generateKey(emailType: EmailType, email: String): String {
        return "${emailType.keyPrefix}${email.lowercase().trim()}"
    }
    
    /**
     * Format remaining time into human-readable string
     */
    private fun formatRemainingTime(remainingTimeMs: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTimeMs)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingTimeMs) % 60
        
        return when {
            minutes > 0 && seconds > 0 -> "${minutes}m ${seconds}s"
            minutes > 0 -> "${minutes}m"
            seconds > 0 -> "${seconds}s"
            else -> "0s"
        }
    }
    
    /**
     * Check if throttle is active for given email type and address
     */
    fun isThrottled(emailType: EmailType, email: String): Boolean {
        return !canSendEmail(emailType, email).canSend
    }
    
    /**
     * Get formatted message for throttled email attempts
     */
    fun getThrottleMessage(emailType: EmailType, email: String): String {
        val result = canSendEmail(emailType, email)
        
        return if (result.canSend) {
            "Email can be sent"
        } else {
            val emailTypeText = when (emailType) {
                EmailType.EMAIL_VERIFICATION -> "verification email"
                EmailType.PASSWORD_RESET -> "password reset email"
            }
            "Please wait ${result.remainingTimeFormatted} before requesting another $emailTypeText"
        }
    }
    
    /**
     * Get toast message for throttled attempts with countdown
     */
    fun getThrottleToastMessage(emailType: EmailType, email: String): String {
        val result = canSendEmail(emailType, email)
        
        return if (result.canSend) {
            "Email can be sent"
        } else {
            val emailTypeText = when (emailType) {
                EmailType.EMAIL_VERIFICATION -> "verification email"
                EmailType.PASSWORD_RESET -> "password reset link"
            }
            "Please wait ${result.remainingTimeFormatted} to send new $emailTypeText"
        }
    }
}

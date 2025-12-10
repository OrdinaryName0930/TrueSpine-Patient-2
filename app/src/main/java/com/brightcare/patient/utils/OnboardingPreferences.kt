package com.brightcare.patient.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class for managing onboarding preferences
 * Handles storing and retrieving whether user has seen onboarding
 * 
 * Utility class para sa pag-manage ng onboarding preferences
 * Nag-hahandle ng pag-store at pag-retrieve kung nakita na ng user ang onboarding
 */
object OnboardingPreferences {
    
    private const val PREFS_NAME = "brightcare_onboarding_prefs"
    private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding"
    private const val KEY_HAS_REQUESTED_PERMISSIONS = "has_requested_permissions"
    
    /**
     * Get SharedPreferences instance
     * Kumuha ng SharedPreferences instance
     */
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Check if user has seen onboarding before
     * Tingnan kung nakita na ng user ang onboarding dati
     * 
     * @param context Application context
     * @return true if user has seen onboarding, false otherwise
     */
    fun hasSeenOnboarding(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HAS_SEEN_ONBOARDING, false)
    }
    
    /**
     * Mark that user has seen onboarding
     * Markahan na nakita na ng user ang onboarding
     * 
     * @param context Application context
     */
    fun setOnboardingSeen(context: Context) {
        getPrefs(context)
            .edit()
            .putBoolean(KEY_HAS_SEEN_ONBOARDING, true)
            .apply()
    }
    
    /**
     * Reset onboarding preference (for testing purposes)
     * I-reset ang onboarding preference (para sa testing)
     * 
     * @param context Application context
     */
    fun resetOnboarding(context: Context) {
        getPrefs(context)
            .edit()
            .putBoolean(KEY_HAS_SEEN_ONBOARDING, false)
            .apply()
    }
    
    /**
     * Check if permissions have been requested before
     * Tingnan kung na-request na ang mga permissions dati
     * 
     * @param context Application context
     * @return true if permissions have been requested, false otherwise
     */
    fun hasRequestedPermissions(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HAS_REQUESTED_PERMISSIONS, false)
    }
    
    /**
     * Mark that permissions have been requested
     * Markahan na na-request na ang mga permissions
     * 
     * @param context Application context
     */
    fun setPermissionsRequested(context: Context) {
        getPrefs(context)
            .edit()
            .putBoolean(KEY_HAS_REQUESTED_PERMISSIONS, true)
            .apply()
    }
    
    /**
     * Clear all onboarding preferences
     * I-clear ang lahat ng onboarding preferences
     * 
     * @param context Application context
     */
    fun clearAll(context: Context) {
        getPrefs(context)
            .edit()
            .clear()
            .apply()
    }
}



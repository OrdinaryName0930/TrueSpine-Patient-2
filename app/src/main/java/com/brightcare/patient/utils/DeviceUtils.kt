package com.brightcare.patient.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import java.util.UUID

/**
 * Utility class for device-related operations
 * Provides secure device identification for user tracking and analytics
 */
object DeviceUtils {
    
    /**
     * Get a unique device identifier
     * Uses Android ID as the primary identifier, falls back to generated UUID if unavailable
     * 
     * @param context Application context
     * @return Unique device identifier string
     */
    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return try {
            // Try to get Android ID first (most reliable)
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            
            // Check if Android ID is valid (not null, not empty, not the known bad value)
            if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
                androidId
            } else {
                // Fallback to generated UUID stored in SharedPreferences
                getOrCreateFallbackDeviceId(context)
            }
        } catch (e: Exception) {
            // If all else fails, use fallback
            getOrCreateFallbackDeviceId(context)
        }
    }
    
    /**
     * Get or create a fallback device ID using SharedPreferences
     * This ensures consistency across app sessions even if Android ID is unavailable
     */
    private fun getOrCreateFallbackDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)
        val existingId = prefs.getString("device_id", null)
        
        return if (!existingId.isNullOrBlank()) {
            existingId
        } else {
            // Generate new UUID and store it
            val newId = UUID.randomUUID().toString()
            prefs.edit().putString("device_id", newId).apply()
            newId
        }
    }
    
    /**
     * Get device information for debugging/logging purposes
     * 
     * @param context Application context
     * @return Map containing device information
     */
    fun getDeviceInfo(context: Context): Map<String, String> {
        return mapOf(
            "deviceId" to getDeviceId(context),
            "model" to android.os.Build.MODEL,
            "manufacturer" to android.os.Build.MANUFACTURER,
            "androidVersion" to android.os.Build.VERSION.RELEASE,
            "sdkVersion" to android.os.Build.VERSION.SDK_INT.toString(),
            "brand" to android.os.Build.BRAND
        )
    }
}

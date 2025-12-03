package com.brightcare.patient.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.brightcare.patient.ui.screens.CompleteProfileFormState
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling complete profile operations
 * Manages Firestore operations for storing client profile information
 */
@Singleton
class CompleteProfileRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val context: Context
) {
    
    companion object {
        private const val TAG = "CompleteProfileRepository"
        private const val COLLECTION_CLIENTS = "client"
    }
    
    /**
     * Save complete profile data to Firestore
     * Stores the information in "client" collection with user's UID as document ID
     */
    suspend fun saveCompleteProfile(formState: CompleteProfileFormState): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.failure(Exception("User not authenticated"))
            }
            
            Log.d(TAG, "Saving profile for user: ${currentUser.uid}")
            
            // Prepare the profile data including terms and privacy policy
            val profileData = hashMapOf(
                "firstName" to formState.firstName.trim(),
                "lastName" to formState.lastName.trim(),
                "suffix" to formState.suffix.trim(),
                "birthDate" to formState.birthDate.trim(),
                "sex" to formState.sex,
                "phoneNumber" to formState.phoneNumber,
                "country" to "Philippines", // Default as specified
                "province" to formState.province,
                "municipality" to formState.municipality,
                "barangay" to formState.barangay,
                "additionalAddress" to formState.additionalAddress.trim(),
                "agreedToTerms" to formState.agreedToTerms,
                "agreedToPrivacy" to formState.agreedToPrivacy,
                "profileCompleted" to true
            )
            
            // Update existing document with profile data (merge to preserve existing fields)
            firestore.collection(COLLECTION_CLIENTS)
                .document(currentUser.uid)
                .set(profileData, com.google.firebase.firestore.SetOptions.merge())
                .await()
            
            Log.d(TAG, "Profile saved successfully for user: ${currentUser.uid}")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving profile", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if user's profile is already completed
     */
    suspend fun isProfileCompleted(): Result<Boolean> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.success(false)
            }
            
            val document = firestore.collection(COLLECTION_CLIENTS)
                .document(currentUser.uid)
                .get()
                .await()
            
            val isCompleted = document.exists() && 
                            document.getBoolean("profileCompleted") == true
            
            Log.d(TAG, "Profile completion status for ${currentUser.uid}: $isCompleted")
            Result.success(isCompleted)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking profile completion status", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get existing profile data for the current user
     */
    suspend fun getProfileData(): Result<CompleteProfileFormState?> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.success(null)
            }
            
            val document = firestore.collection(COLLECTION_CLIENTS)
                .document(currentUser.uid)
                .get()
                .await()
            
            if (!document.exists()) {
                Log.d(TAG, "No profile data found for user: ${currentUser.uid}")
                return Result.success(null)
            }
            
            // Convert Firestore document to CompleteProfileFormState
            val formState = CompleteProfileFormState(
                firstName = document.getString("firstName") ?: "",
                lastName = document.getString("lastName") ?: "",
                suffix = document.getString("suffix") ?: "",
                birthDate = document.getString("birthDate") ?: "",
                sex = document.getString("sex") ?: "",
                phoneNumber = document.getString("phoneNumber") ?: "",
                country = document.getString("country") ?: "Philippines",
                province = document.getString("province") ?: "",
                municipality = document.getString("municipality") ?: "",
                barangay = document.getString("barangay") ?: "",
                additionalAddress = document.getString("additionalAddress") ?: "",
                agreedToTerms = document.getBoolean("agreedToTerms") ?: false,
                agreedToPrivacy = document.getBoolean("agreedToPrivacy") ?: false
            )
            
            Log.d(TAG, "Profile data retrieved for user: ${currentUser.uid}")
            Result.success(formState)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving profile data", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update existing profile data
     */
    suspend fun updateProfile(formState: CompleteProfileFormState): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found")
                return Result.failure(Exception("User not authenticated"))
            }
            
            Log.d(TAG, "Updating profile for user: ${currentUser.uid}")
            
            // Prepare the update data including terms and privacy policy
            val updateData = hashMapOf(
                "firstName" to formState.firstName.trim(),
                "lastName" to formState.lastName.trim(),
                "suffix" to formState.suffix.trim(),
                "birthDate" to formState.birthDate.trim(),
                "sex" to formState.sex,
                "phoneNumber" to formState.phoneNumber,
                "country" to "Philippines",
                "province" to formState.province,
                "municipality" to formState.municipality,
                "barangay" to formState.barangay,
                "additionalAddress" to formState.additionalAddress.trim(),
                "agreedToTerms" to formState.agreedToTerms,
                "agreedToPrivacy" to formState.agreedToPrivacy,
                "profileCompleted" to true
            )
            
            // Update the document
            firestore.collection(COLLECTION_CLIENTS)
                .document(currentUser.uid)
                .update(updateData as Map<String, Any>)
                .await()
            
            Log.d(TAG, "Profile updated successfully for user: ${currentUser.uid}")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile", e)
            Result.failure(e)
        }
    }
}

package com.brightcare.patient.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import com.brightcare.patient.ui.screens.CompleteProfileFormState
import com.brightcare.patient.utils.ImageCompressionUtils
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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
    private val firebaseStorage: FirebaseStorage,
    private val context: Context
) {
    
    companion object {
        private const val TAG = "CompleteProfileRepository"
        private const val COLLECTION_CLIENTS = "client"
        private const val SUBCOLLECTION_PERSONAL_DATA = "personal_data"
        private const val DOCUMENT_INFO = "info"
        private const val STORAGE_PATH_ID_IMAGES = "client_id_images"
    }
    
    /**
     * Upload ID image to Firebase Storage with compression
     * I-upload ang ID image sa Firebase Storage na may compression
     */
    private suspend fun uploadIdImage(imageUri: String, userId: String, imageType: String): Result<String> {
        return try {
            if (imageUri.isBlank()) {
                return Result.failure(Exception("Image URI is empty"))
            }
            
            Log.d(TAG, "Starting $imageType image upload with compression for user: $userId")
            
            // Compress image first for faster upload and storage efficiency
            // I-compress muna ang image para sa mas mabilis na upload at storage efficiency
            val compressionResult = withContext(Dispatchers.IO) {
                ImageCompressionUtils.compressIdImage(
                    context = context,
                    imageUri = Uri.parse(imageUri),
                    isIdDocument = true // Use high quality settings for ID documents
                )
            }
            
            if (compressionResult.isFailure) {
                Log.e(TAG, "Failed to compress $imageType image: ${compressionResult.exceptionOrNull()?.message}")
                return Result.failure(Exception("Failed to compress $imageType image: ${compressionResult.exceptionOrNull()?.message}"))
            }
            
            val compressedImagePath = compressionResult.getOrNull()!!
            val compressedFile = File(compressedImagePath)
            val compressedUri = Uri.fromFile(compressedFile)
            
            // Log compression results
            val originalSize = try {
                context.contentResolver.openInputStream(Uri.parse(imageUri))?.use { it.available() } ?: 0
            } catch (e: Exception) { 0 }
            val compressedSize = compressedFile.length()
            val compressionRatio = if (originalSize > 0) {
                ((originalSize - compressedSize).toFloat() / originalSize * 100).toInt()
            } else 0
            
            Log.d(TAG, "$imageType image compression completed:")
            Log.d(TAG, "  Original size: ${originalSize / 1024}KB")
            Log.d(TAG, "  Compressed size: ${compressedSize / 1024}KB")
            Log.d(TAG, "  Compression ratio: $compressionRatio%")
            
            // Upload compressed image to Firebase Storage
            val fileName = "${userId}_${imageType}_${System.currentTimeMillis()}.jpg"
            val storageRef = firebaseStorage.reference
                .child(STORAGE_PATH_ID_IMAGES)
                .child(userId)
                .child(fileName)
            
            Log.d(TAG, "Uploading compressed $imageType image to Firebase Storage...")
            
            val uploadTask = storageRef.putFile(compressedUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            
            // Clean up compressed file after upload
            withContext(Dispatchers.IO) {
                try {
                    compressedFile.delete()
                    Log.d(TAG, "Cleaned up temporary compressed file")
                } catch (e: Exception) {
                    Log.w(TAG, "Could not delete temporary compressed file", e)
                }
            }
            
            Log.d(TAG, "$imageType image uploaded successfully. URL: $downloadUrl")
            Log.d(TAG, "Upload completed with ${compressionRatio}% size reduction")
            
            Result.success(downloadUrl.toString())
            
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading $imageType image", e)
            Result.failure(Exception("Failed to upload $imageType image: ${e.message}"))
        }
    }

    /**
     * Save complete profile data to Firestore
     * Stores the information in "client" collection with user's UID as document ID
     * I-save ang complete profile data sa Firestore
     */
    suspend fun saveCompleteProfile(formState: CompleteProfileFormState): Result<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found when saving profile")
                return Result.failure(Exception("User must be logged in to save profile"))
            }
            
            Log.d(TAG, "Saving profile for authenticated user: ${currentUser.uid}")
            
            // Upload profile picture if provided
            var profilePictureUrl = formState.profilePictureUrl
            
            if (formState.profilePictureUri.isNotBlank() && formState.profilePictureUri != formState.profilePictureUrl) {
                Log.d(TAG, "Uploading profile picture with compression...")
                val profileUploadResult = uploadProfilePictureInternal(formState.profilePictureUri, currentUser.uid)
                if (profileUploadResult.isFailure) {
                    return profileUploadResult.map { }
                }
                profilePictureUrl = profileUploadResult.getOrNull() ?: ""
            }
            
            // Upload ID images if provided
            var idFrontUrl = formState.idFrontImageUrl
            var idBackUrl = formState.idBackImageUrl
            
            if (formState.idFrontImageUri.isNotBlank() && formState.idFrontImageUri != formState.idFrontImageUrl) {
                val frontUploadResult = uploadIdImage(formState.idFrontImageUri, currentUser.uid, "front")
                if (frontUploadResult.isFailure) {
                    return frontUploadResult.map { }
                }
                idFrontUrl = frontUploadResult.getOrNull() ?: ""
            }
            
            if (formState.idBackImageUri.isNotBlank() && formState.idBackImageUri != formState.idBackImageUrl) {
                val backUploadResult = uploadIdImage(formState.idBackImageUri, currentUser.uid, "back")
                if (backUploadResult.isFailure) {
                    return backUploadResult.map { }
                }
                idBackUrl = backUploadResult.getOrNull() ?: ""
            }
            
            // Prepare the profile data including terms and privacy policy and ID images
            val profileData = hashMapOf(
                "firstName" to formState.firstName.trim(),
                "middleName" to formState.middleName.trim(),
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
                "profilePictureUrl" to profilePictureUrl,
                "idFrontImageUrl" to idFrontUrl,
                "idBackImageUrl" to idBackUrl,
                "agreedToTerms" to formState.agreedToTerms,
                "agreedToPrivacy" to formState.agreedToPrivacy,
                "profileCompleted" to true,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            
            // Save profile data in nested structure: client/{clientId}/personal_data/info
            firestore.collection(COLLECTION_CLIENTS)
                .document(currentUser.uid)
                .collection(SUBCOLLECTION_PERSONAL_DATA)
                .document(DOCUMENT_INFO)
                .set(profileData, com.google.firebase.firestore.SetOptions.merge())
                .await()
            
            Log.d(TAG, "Profile saved successfully for user: ${currentUser.uid}")
            Result.success(Unit)
            
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore permission error saving profile: ${e.code} - ${e.message}", e)
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Result.failure(Exception("Access denied. Please make sure you are logged in and have permission to save your profile."))
                }
                FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                    Result.failure(Exception("Authentication required. Please log in to save your profile."))
                }
                else -> {
                    Result.failure(Exception("Failed to save profile: ${e.message}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error saving profile", e)
            Result.failure(Exception("Failed to save profile: ${e.message}"))
        }
    }
    
    /**
     * Check if user's profile is already completed
     * Tignan kung tapos na ang profile ng user
     */
    suspend fun isProfileCompleted(): Result<Boolean> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found when checking profile completion")
                return Result.success(false)
            }
            
            Log.d(TAG, "Checking profile completion for user: ${currentUser.uid}")
            
            // Check profile completion in nested structure: client/{clientId}/personal_data/info
            val document = firestore.collection(COLLECTION_CLIENTS)
                .document(currentUser.uid)
                .collection(SUBCOLLECTION_PERSONAL_DATA)
                .document(DOCUMENT_INFO)
                .get()
                .await()
            
            val isCompleted = document.exists() && 
                            document.getBoolean("profileCompleted") == true
            
            Log.d(TAG, "Profile completion status for ${currentUser.uid}: $isCompleted")
            Result.success(isCompleted)
            
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore permission error checking profile completion: ${e.code} - ${e.message}", e)
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Result.failure(Exception("Access denied. Please make sure you are logged in and have permission to check your profile."))
                }
                FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                    Result.failure(Exception("Authentication required. Please log in to check your profile."))
                }
                else -> {
                    Result.failure(Exception("Failed to check profile completion: ${e.message}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error checking profile completion status", e)
            Result.failure(Exception("Failed to check profile completion: ${e.message}"))
        }
    }
    
    /**
     * Get existing profile data for the current user
     * Kumuha ng existing profile data para sa current user
     */
    suspend fun getProfileData(): Result<CompleteProfileFormState?> {
        return try {
            // Wait for Firebase Auth to initialize if needed
            var currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.w(TAG, "No authenticated user found initially, waiting for auth state...")
                // Give Firebase Auth a moment to initialize
                kotlinx.coroutines.delay(1000)
                currentUser = firebaseAuth.currentUser
            }
            
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found when getting profile data")
                return Result.failure(Exception("User must be logged in to view profile data"))
            }
            
            Log.d(TAG, "Fetching profile data for authenticated user: ${currentUser.uid}")
            Log.d(TAG, "User email: ${currentUser.email}, isEmailVerified: ${currentUser.isEmailVerified}")
            
            // Get profile data from nested structure: client/{clientId}/personal_data/info
            val document = firestore.collection(COLLECTION_CLIENTS)
                .document(currentUser.uid)
                .collection(SUBCOLLECTION_PERSONAL_DATA)
                .document(DOCUMENT_INFO)
                .get()
                .await()
            
            if (!document.exists()) {
                Log.d(TAG, "No profile data found for user: ${currentUser.uid}")
                return Result.success(null)
            }
            
            // Convert Firestore document to CompleteProfileFormState
            val formState = CompleteProfileFormState(
                firstName = document.getString("firstName") ?: "",
                middleName = document.getString("middleName") ?: "",
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
                profilePictureUri = document.getString("profilePictureUrl") ?: "", // Set URI to URL for display
                profilePictureUrl = document.getString("profilePictureUrl") ?: "",
                idFrontImageUrl = document.getString("idFrontImageUrl") ?: "",
                idBackImageUrl = document.getString("idBackImageUrl") ?: "",
                idFrontImageUri = document.getString("idFrontImageUrl") ?: "", // Set URI to URL for display
                idBackImageUri = document.getString("idBackImageUrl") ?: "", // Set URI to URL for display
                agreedToTerms = document.getBoolean("agreedToTerms") ?: false,
                agreedToPrivacy = document.getBoolean("agreedToPrivacy") ?: false
            )
            
            Log.d(TAG, "Profile data retrieved successfully for user: ${currentUser.uid}")
            Result.success(formState)
            
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore permission error getting profile data: ${e.code} - ${e.message}", e)
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Result.failure(Exception("Access denied. Please make sure you are logged in and have permission to view your profile."))
                }
                FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                    Result.failure(Exception("Authentication required. Please log in to view your profile."))
                }
                else -> {
                    Result.failure(Exception("Failed to load profile: ${e.message}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error retrieving profile data", e)
            Result.failure(Exception("Failed to load profile: ${e.message}"))
        }
    }
    
    /**
     * Internal method to upload profile picture without updating Firestore
     * Internal na method para sa pag-upload ng profile picture nang hindi nag-update ng Firestore
     */
    private suspend fun uploadProfilePictureInternal(imageUri: String, userId: String): Result<String> {
        return try {
            if (imageUri.isBlank()) {
                return Result.failure(Exception("Image URI is empty"))
            }
            
            Log.d(TAG, "Starting profile picture upload with compression for user: $userId")
            
            // Compress image for faster upload
            // I-compress ang image para sa mas mabilis na upload
            val compressionResult = withContext(Dispatchers.IO) {
                ImageCompressionUtils.compressIdImage(
                    context = context,
                    imageUri = Uri.parse(imageUri),
                    isIdDocument = false // Use medium quality for profile pictures
                )
            }
            
            if (compressionResult.isFailure) {
                Log.e(TAG, "Failed to compress profile picture: ${compressionResult.exceptionOrNull()?.message}")
                return Result.failure(Exception("Failed to compress profile picture: ${compressionResult.exceptionOrNull()?.message}"))
            }
            
            val compressedImagePath = compressionResult.getOrNull()!!
            val compressedFile = File(compressedImagePath)
            val compressedUri = Uri.fromFile(compressedFile)
            
            // Log compression results
            val originalSize = try {
                context.contentResolver.openInputStream(Uri.parse(imageUri))?.use { it.available() } ?: 0
            } catch (e: Exception) { 0 }
            val compressedSize = compressedFile.length()
            val compressionRatio = if (originalSize > 0) {
                ((originalSize - compressedSize).toFloat() / originalSize * 100).toInt()
            } else 0
            
            Log.d(TAG, "Profile picture compression completed:")
            Log.d(TAG, "  Original size: ${originalSize / 1024}KB")
            Log.d(TAG, "  Compressed size: ${compressedSize / 1024}KB")
            Log.d(TAG, "  Compression ratio: $compressionRatio%")
            
            // Upload compressed image
            val fileName = "profile_${userId}_${System.currentTimeMillis()}.jpg"
            val storageRef = firebaseStorage.reference
                .child("profile_pictures")
                .child(userId)
                .child(fileName)
            
            Log.d(TAG, "Uploading compressed profile picture to Firebase Storage...")
            
            val uploadTask = storageRef.putFile(compressedUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            
            // Clean up compressed file after upload
            withContext(Dispatchers.IO) {
                try {
                    compressedFile.delete()
                    Log.d(TAG, "Cleaned up temporary compressed file")
                } catch (e: Exception) {
                    Log.w(TAG, "Could not delete temporary compressed file", e)
                }
            }
            
            Log.d(TAG, "Profile picture uploaded successfully. URL: $downloadUrl")
            Log.d(TAG, "Upload completed with ${compressionRatio}% size reduction")
            
            Result.success(downloadUrl.toString())
            
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile picture", e)
            Result.failure(Exception("Failed to upload profile picture: ${e.message}"))
        }
    }
    
    /**
     * Upload profile picture to Firebase Storage with compression
     * I-upload ang profile picture sa Firebase Storage na may compression
     */
    suspend fun uploadProfilePicture(imageUri: String): Result<String> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found when uploading profile picture")
                return Result.failure(Exception("User must be logged in to upload profile picture"))
            }
            
            if (imageUri.isBlank()) {
                return Result.failure(Exception("Image URI is empty"))
            }
            
            Log.d(TAG, "Starting profile picture upload with compression for user: ${currentUser.uid}")
            
            // Compress image for faster upload
            // I-compress ang image para sa mas mabilis na upload
            val compressionResult = withContext(Dispatchers.IO) {
                ImageCompressionUtils.compressIdImage(
                    context = context,
                    imageUri = Uri.parse(imageUri),
                    isIdDocument = false // Use medium quality for profile pictures
                )
            }
            
            if (compressionResult.isFailure) {
                Log.e(TAG, "Failed to compress profile picture: ${compressionResult.exceptionOrNull()?.message}")
                return Result.failure(Exception("Failed to compress profile picture: ${compressionResult.exceptionOrNull()?.message}"))
            }
            
            val compressedImagePath = compressionResult.getOrNull()!!
            val compressedFile = File(compressedImagePath)
            val compressedUri = Uri.fromFile(compressedFile)
            
            // Log compression results
            val originalSize = try {
                context.contentResolver.openInputStream(Uri.parse(imageUri))?.use { it.available() } ?: 0
            } catch (e: Exception) { 0 }
            val compressedSize = compressedFile.length()
            val compressionRatio = if (originalSize > 0) {
                ((originalSize - compressedSize).toFloat() / originalSize * 100).toInt()
            } else 0
            
            Log.d(TAG, "Profile picture compression completed:")
            Log.d(TAG, "  Original size: ${originalSize / 1024}KB")
            Log.d(TAG, "  Compressed size: ${compressedSize / 1024}KB")
            Log.d(TAG, "  Compression ratio: $compressionRatio%")
            
            // Upload compressed image
            val fileName = "profile_${currentUser.uid}_${System.currentTimeMillis()}.jpg"
            val storageRef = firebaseStorage.reference
                .child("profile_pictures")
                .child(currentUser.uid)
                .child(fileName)
            
            Log.d(TAG, "Uploading compressed profile picture to Firebase Storage...")
            
            val uploadTask = storageRef.putFile(compressedUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            
            // Clean up compressed file after upload
            withContext(Dispatchers.IO) {
                try {
                    compressedFile.delete()
                    Log.d(TAG, "Cleaned up temporary compressed file")
                } catch (e: Exception) {
                    Log.w(TAG, "Could not delete temporary compressed file", e)
                }
            }
            
            Log.d(TAG, "Profile picture uploaded successfully. URL: $downloadUrl")
            Log.d(TAG, "Upload completed with ${compressionRatio}% size reduction")
            
            // Update user profile with new picture URL
            val updateData = hashMapOf<String, Any>(
                "profilePictureUrl" to downloadUrl.toString(),
                "updatedAt" to System.currentTimeMillis()
            )
            
            firestore.collection(COLLECTION_CLIENTS)
                .document(currentUser.uid)
                .collection(SUBCOLLECTION_PERSONAL_DATA)
                .document(DOCUMENT_INFO)
                .update(updateData)
                .await()
            
            Result.success(downloadUrl.toString())
            
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile picture", e)
            Result.failure(Exception("Failed to upload profile picture: ${e.message}"))
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
            
            // Upload profile picture if provided and different from existing URL
            var profilePictureUrl = formState.profilePictureUrl
            
            if (formState.profilePictureUri.isNotBlank() && formState.profilePictureUri != formState.profilePictureUrl) {
                Log.d(TAG, "Updating profile picture with compression...")
                val profileUploadResult = uploadProfilePictureInternal(formState.profilePictureUri, currentUser.uid)
                if (profileUploadResult.isFailure) {
                    return profileUploadResult.map { }
                }
                profilePictureUrl = profileUploadResult.getOrNull() ?: ""
            }
            
            // Upload ID images if provided and different from existing URLs
            var idFrontUrl = formState.idFrontImageUrl
            var idBackUrl = formState.idBackImageUrl
            
            if (formState.idFrontImageUri.isNotBlank() && formState.idFrontImageUri != formState.idFrontImageUrl) {
                val frontUploadResult = uploadIdImage(formState.idFrontImageUri, currentUser.uid, "front")
                if (frontUploadResult.isFailure) {
                    return frontUploadResult.map { }
                }
                idFrontUrl = frontUploadResult.getOrNull() ?: ""
            }
            
            if (formState.idBackImageUri.isNotBlank() && formState.idBackImageUri != formState.idBackImageUrl) {
                val backUploadResult = uploadIdImage(formState.idBackImageUri, currentUser.uid, "back")
                if (backUploadResult.isFailure) {
                    return backUploadResult.map { }
                }
                idBackUrl = backUploadResult.getOrNull() ?: ""
            }
            
            // Prepare the update data including terms and privacy policy and ID images
            val updateData = hashMapOf(
                "firstName" to formState.firstName.trim(),
                "middleName" to formState.middleName.trim(),
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
                "profilePictureUrl" to profilePictureUrl,
                "idFrontImageUrl" to idFrontUrl,
                "idBackImageUrl" to idBackUrl,
                "agreedToTerms" to formState.agreedToTerms,
                "agreedToPrivacy" to formState.agreedToPrivacy,
                "profileCompleted" to true,
                "updatedAt" to System.currentTimeMillis()
            )
            
            // Update the document in nested structure: client/{clientId}/personal_data/info
            firestore.collection(COLLECTION_CLIENTS)
                .document(currentUser.uid)
                .collection(SUBCOLLECTION_PERSONAL_DATA)
                .document(DOCUMENT_INFO)
                .update(updateData as Map<String, Any>)
                .await()
            
            Log.d(TAG, "Profile updated successfully for user: ${currentUser.uid}")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if phone number already exists in another account
     * Tingnan kung may ibang account na na may ganitong phone number
     */
    suspend fun isPhoneNumberTaken(phoneNumber: String): Result<Boolean> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No authenticated user found when checking phone number")
                return Result.failure(Exception("User must be logged in to check phone number"))
            }
            
            if (phoneNumber.isBlank()) {
                return Result.success(false)
            }
            
            Log.d(TAG, "Checking if phone number $phoneNumber is already taken...")
            
            // Query all client documents to find if phone number exists
            val querySnapshot = firestore.collectionGroup(SUBCOLLECTION_PERSONAL_DATA)
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .await()
            
            // Check if any document exists with this phone number from a different user
            val isPhoneTaken = querySnapshot.documents.any { document ->
                // Get the client ID from the document path
                val clientId = document.reference.parent.parent?.id
                // Phone number is taken if it belongs to a different user
                clientId != null && clientId != currentUser.uid
            }
            
            Log.d(TAG, "Found ${querySnapshot.documents.size} documents with phone number $phoneNumber")
            querySnapshot.documents.forEach { doc ->
                val clientId = doc.reference.parent.parent?.id
                Log.d(TAG, "Document client ID: $clientId, Current user: ${currentUser.uid}")
            }
            
            Log.d(TAG, "Phone number $phoneNumber taken status: $isPhoneTaken")
            Result.success(isPhoneTaken)
            
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error checking phone number: ${e.code} - ${e.message}", e)
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Result.failure(Exception("Access denied. Please make sure you are logged in and have permission to check phone numbers."))
                }
                FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                    Result.failure(Exception("Authentication required. Please log in to check phone number."))
                }
                else -> {
                    Result.failure(Exception("Failed to check phone number: ${e.message}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking phone number", e)
            Result.failure(e)
        }
    }
}

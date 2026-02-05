package com.brightcare.patient.data.repository

import android.util.Log
import com.brightcare.patient.data.model.ChiropractorModel
import com.brightcare.patient.ui.component.chirocomponents.ChiropractorInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing chiropractor data from Firestore
 * Handles data fetching, transformation, and caching
 * Repository para sa pag-manage ng chiropractor data mula sa Firestore
 */
@Singleton
class ChiropractorRepository @Inject constructor() {
    
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val chiropractorsCollection = "chiropractors"
    
    companion object {
        private const val TAG = "ChiropractorRepository"
    }
    
    /**
     * Fetch all chiropractors from Firestore
     * @return Result containing list of ChiropractorInfo or error
     * Kumuha ng lahat ng chiropractors mula sa Firestore
     */
    suspend fun getAllChiropractors(): Result<List<ChiropractorInfo>> {
        return try {
            // Check if user is authenticated
            // Tignan kung authenticated ang user
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "User not authenticated when trying to fetch chiropractors")
                return Result.failure(Exception("User must be logged in to view chiropractors"))
            }
            
            Log.d(TAG, "Fetching chiropractors for authenticated user: ${currentUser.uid}")
            
            val snapshot = firestore.collection(chiropractorsCollection)
                .get()
                .await()
            
            Log.d(TAG, "Successfully fetched ${snapshot.documents.size} chiropractor documents")
            
            val chiropractors = snapshot.documents.mapNotNull { document ->
                try {
                    val model = document.toObject(ChiropractorModel::class.java)
                    model?.let { mapToChiropractorInfo(document.id, it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing chiropractor document ${document.id}", e)
                    null
                }
            }
            
            Log.d(TAG, "Successfully parsed ${chiropractors.size} chiropractors")
            Result.success(chiropractors)
            
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore permission error: ${e.code} - ${e.message}", e)
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Result.failure(Exception("Access denied. Please make sure you are logged in and have permission to view chiropractors."))
                }
                FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                    Result.failure(Exception("Authentication required. Please log in to view chiropractors."))
                }
                else -> {
                    Result.failure(Exception("Failed to load chiropractors: ${e.message}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error fetching chiropractors", e)
            Result.failure(Exception("Failed to load chiropractors: ${e.message}"))
        }
    }
    
    /**
     * Fetch chiropractor by ID from Firestore
     * @param chiropractorId The document ID of the chiropractor
     * @return Result containing ChiropractorInfo or error
     * Kumuha ng chiropractor gamit ang ID mula sa Firestore
     */
    suspend fun getChiropractorById(chiropractorId: String): Result<ChiropractorInfo?> {
        return try {
            // Check authentication
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "User not authenticated when trying to fetch chiropractor by ID")
                return Result.failure(Exception("User must be logged in to view chiropractor details"))
            }
            
            Log.d(TAG, "Fetching chiropractor by ID: $chiropractorId")
            
            val document = firestore.collection(chiropractorsCollection)
                .document(chiropractorId)
                .get()
                .await()
            
            val model = document.toObject(ChiropractorModel::class.java)
            val chiropractorInfo = model?.let { mapToChiropractorInfo(document.id, it) }
            
            Log.d(TAG, "Successfully fetched chiropractor: ${chiropractorInfo?.name ?: "null"}")
            Result.success(chiropractorInfo)
            
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error fetching chiropractor $chiropractorId: ${e.code} - ${e.message}", e)
            Result.failure(Exception("Failed to load chiropractor details: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error fetching chiropractor $chiropractorId", e)
            Result.failure(Exception("Failed to load chiropractor details: ${e.message}"))
        }
    }
    
    /**
     * Search chiropractors by specialization
     * @param specialization The specialization to search for
     * @return Result containing filtered list of ChiropractorInfo
     */
    suspend fun searchBySpecialization(specialization: String): Result<List<ChiropractorInfo>> {
        return try {
            val snapshot = firestore.collection(chiropractorsCollection)
                .whereEqualTo("specialization", specialization)
                .get()
                .await()
            
            val chiropractors = snapshot.documents.mapNotNull { document ->
                try {
                    val model = document.toObject(ChiropractorModel::class.java)
                    model?.let { mapToChiropractorInfo(document.id, it) }
                } catch (e: Exception) {
                    null
                }
            }
            
            Result.success(chiropractors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get available chiropractors only
     * @return Result containing list of available ChiropractorInfo
     */
    suspend fun getAvailableChiropractors(): Result<List<ChiropractorInfo>> {
        return try {
            val allChiropractors = getAllChiropractors().getOrThrow()
            val availableChiropractors = allChiropractors.filter { it.isAvailable }
            Result.success(availableChiropractors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Map ChiropractorModel from Firestore to ChiropractorInfo for UI
     * Uses real rating and reviewCount data from Firestore
     * Ginagamit ang totoong rating at reviewCount data mula sa Firestore
     */
    private fun mapToChiropractorInfo(documentId: String, model: ChiropractorModel): ChiropractorInfo {
        return ChiropractorInfo(
            id = documentId,
            name = model.name,
            specialization = model.specialization,
            experience = "${model.yearsOfExperience} years",
            rating = model.getRealRating(), // Use real rating from reviews
            location = model.generateDummyLocation(),
            isAvailable = model.generateAvailabilityStatus(),
            yearsOfExperience = model.yearsOfExperience,
            contactNumber = model.contactNumber,
            email = model.email,
            profileImageUrl = model.profileImageUrl,
            role = model.role,
            reviewCount = model.getRealReviewCount() // Use real review count
        )
    }
}

/**
 * Data class for repository responses
 */
sealed class ChiropractorRepositoryResult<T> {
    data class Success<T>(val data: T) : ChiropractorRepositoryResult<T>()
    data class Error<T>(val exception: Exception) : ChiropractorRepositoryResult<T>()
    data class Loading<T>(val message: String = "Loading...") : ChiropractorRepositoryResult<T>()
}




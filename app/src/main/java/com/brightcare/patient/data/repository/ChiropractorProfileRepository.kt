package com.brightcare.patient.data.repository

import android.util.Log
import com.brightcare.patient.data.model.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for chiropractor profile data
 * Repository para sa chiropractor profile data
 */
@Singleton
class ChiropractorProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "ChiropractorProfileRepo"
        private const val CHIROPRACTORS_COLLECTION = "chiropractors"
    }

    /**
     * Get chiropractor profile by ID
     * Kunin ang chiropractor profile gamit ang ID
     */
    fun getChiropractorProfile(chiropractorId: String): Flow<Result<ChiropractorProfileModel?>> = flow {
        try {
            Log.d(TAG, "Fetching chiropractor profile for ID: $chiropractorId")
            
            val document = firestore.collection(CHIROPRACTORS_COLLECTION)
                .document(chiropractorId)
                .get()
                .await()
            
            if (document.exists()) {
                try {
                    // First try automatic deserialization
                    val chiropractor = document.toObject(ChiropractorProfileModel::class.java)
                    Log.d(TAG, "Successfully fetched chiropractor profile: ${chiropractor?.name}")
                    Log.d(TAG, "Education data from document: ${chiropractor?.education}")
                    Log.d(TAG, "Experience data from document: ${chiropractor?.experienceHistory}")
                    
                    // Try to get data from subcollections (like personal data does)
                    val finalChiropractor = getChiropractorWithSubcollections(chiropractorId, chiropractor)
                    
                    emit(Result.success(finalChiropractor))
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing chiropractor document", e)
                    emit(Result.failure(e))
                }
            } else {
                Log.w(TAG, "Chiropractor profile not found for ID: $chiropractorId")
                emit(Result.success(null))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching chiropractor profile", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Get all chiropractors profiles
     * Kunin ang lahat ng chiropractor profiles
     */
    fun getAllChiropractorProfiles(): Flow<Result<List<ChiropractorProfileModel>>> = flow {
        try {
            Log.d(TAG, "Fetching all chiropractor profiles")
            
            val querySnapshot = firestore.collection(CHIROPRACTORS_COLLECTION)
                .get()
                .await()
            
            val chiropractors = querySnapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(ChiropractorProfileModel::class.java)
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing chiropractor document: ${document.id}", e)
                    null
                }
            }
            
            Log.d(TAG, "Successfully fetched ${chiropractors.size} chiropractor profiles")
            emit(Result.success(chiropractors))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching chiropractor profiles", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Search chiropractors by name or specialization
     * Maghanap ng chiropractors gamit ang name o specialization
     */
    fun searchChiropractors(query: String): Flow<Result<List<ChiropractorProfileModel>>> = flow {
        try {
            Log.d(TAG, "Searching chiropractors with query: $query")
            
            val querySnapshot = firestore.collection(CHIROPRACTORS_COLLECTION)
                .get()
                .await()
            
            val chiropractors = querySnapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(ChiropractorProfileModel::class.java)
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing chiropractor document: ${document.id}", e)
                    null
                }
            }.filter { chiropractor ->
                chiropractor.name.contains(query, ignoreCase = true) ||
                chiropractor.specialization.contains(query, ignoreCase = true) ||
                chiropractor.firstName.contains(query, ignoreCase = true) ||
                chiropractor.lastName.contains(query, ignoreCase = true)
            }
            
            Log.d(TAG, "Found ${chiropractors.size} chiropractors matching query: $query")
            emit(Result.success(chiropractors))
        } catch (e: Exception) {
            Log.e(TAG, "Error searching chiropractors", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Get chiropractors by specialization
     * Kunin ang chiropractors gamit ang specialization
     */
    fun getChiropractorsBySpecialization(specialization: String): Flow<Result<List<ChiropractorProfileModel>>> = flow {
        try {
            Log.d(TAG, "Fetching chiropractors by specialization: $specialization")
            
            val querySnapshot = firestore.collection(CHIROPRACTORS_COLLECTION)
                .whereEqualTo("specialization", specialization)
                .get()
                .await()
            
            val chiropractors = querySnapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(ChiropractorProfileModel::class.java)
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing chiropractor document: ${document.id}", e)
                    null
                }
            }
            
            Log.d(TAG, "Found ${chiropractors.size} chiropractors with specialization: $specialization")
            emit(Result.success(chiropractors))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching chiropractors by specialization", e)
            emit(Result.failure(e))
        }
    }

    /**
     * Get chiropractors with minimum years of experience
     * Kunin ang chiropractors na may minimum years of experience
     */
    fun getChiropractorsByExperience(minYears: Int): Flow<Result<List<ChiropractorProfileModel>>> = flow {
        try {
            Log.d(TAG, "Fetching chiropractors with minimum $minYears years of experience")
            
            val querySnapshot = firestore.collection(CHIROPRACTORS_COLLECTION)
                .whereGreaterThanOrEqualTo("yearsOfExperience", minYears)
                .get()
                .await()
            
            val chiropractors = querySnapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(ChiropractorProfileModel::class.java)
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing chiropractor document: ${document.id}", e)
                    null
                }
            }
            
            Log.d(TAG, "Found ${chiropractors.size} chiropractors with minimum $minYears years of experience")
            emit(Result.success(chiropractors))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching chiropractors by experience", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get chiropractor data with subcollections (following personal data pattern)
     * Kunin ang chiropractor data kasama ang subcollections
     */
    private suspend fun getChiropractorWithSubcollections(
        chiropractorId: String,
        baseChiropractor: ChiropractorProfileModel?
    ): ChiropractorProfileModel? {
        if (baseChiropractor == null) return null
        
        try {
            Log.d(TAG, "Fetching subcollections for chiropractor: $chiropractorId")
            
            val chiropractorRef = firestore.collection(CHIROPRACTORS_COLLECTION).document(chiropractorId)
            
            // Try to get education from subcollection
            val educationSnapshot = chiropractorRef.collection("education").get().await()
            val educationMap = mutableMapOf<String, EducationItem>()
            
            educationSnapshot.documents.forEach { doc ->
                try {
                    val educationItem = doc.toObject(EducationItem::class.java)
                    if (educationItem != null) {
                        educationMap[doc.id] = educationItem.copy(id = doc.id)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing education document ${doc.id}", e)
                }
            }
            
            // Try to get experience from subcollection
            val experienceSnapshot = chiropractorRef.collection("experienceHistory").get().await()
            val experienceMap = mutableMapOf<String, ExperienceItem>()
            
            experienceSnapshot.documents.forEach { doc ->
                try {
                    val experienceItem = doc.toObject(ExperienceItem::class.java)
                    if (experienceItem != null) {
                        experienceMap[doc.id] = experienceItem.copy(id = doc.id)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing experience document ${doc.id}", e)
                }
            }
            
            // Try to get credentials from subcollection
            val credentialsSnapshot = chiropractorRef.collection("professionalCredentials").get().await()
            val credentialsMap = mutableMapOf<String, ProfessionalCredentialItem>()
            
            credentialsSnapshot.documents.forEach { doc ->
                try {
                    val credentialItem = doc.toObject(ProfessionalCredentialItem::class.java)
                    if (credentialItem != null) {
                        credentialsMap[doc.id] = credentialItem.copy(id = doc.id)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing credential document ${doc.id}", e)
                }
            }
            
            // Try to get others from subcollection
            val othersSnapshot = chiropractorRef.collection("others").get().await()
            val othersMap = mutableMapOf<String, OtherItem>()
            
            othersSnapshot.documents.forEach { doc ->
                try {
                    val otherItem = doc.toObject(OtherItem::class.java)
                    if (otherItem != null) {
                        othersMap[doc.id] = otherItem.copy(id = doc.id)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing other document ${doc.id}", e)
                }
            }
            
            Log.d(TAG, "Subcollection data retrieved - Education: ${educationMap.size}, Experience: ${experienceMap.size}, Credentials: ${credentialsMap.size}, Others: ${othersMap.size}")
            
            // Use subcollection data if available, otherwise fall back to nested document data
            val finalEducation = if (educationMap.isNotEmpty()) educationMap else baseChiropractor.education
            val finalExperience = if (experienceMap.isNotEmpty()) experienceMap else baseChiropractor.experienceHistory
            val finalCredentials = if (credentialsMap.isNotEmpty()) credentialsMap else baseChiropractor.professionalCredentials
            val finalOthers = if (othersMap.isNotEmpty()) othersMap else baseChiropractor.others
            
            return baseChiropractor.copy(
                education = finalEducation,
                experienceHistory = finalExperience,
                professionalCredentials = finalCredentials,
                others = finalOthers
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching subcollections, using document data", e)
            // If subcollection fetch fails, try manual parsing of nested document data
            return parseChiropractorManually(
                firestore.collection(CHIROPRACTORS_COLLECTION).document(chiropractorId).get().await(),
                baseChiropractor
            )
        }
    }
    
    /**
     * Manually parse chiropractor document to handle nested collections
     * Manual na pag-parse ng chiropractor document para sa nested collections
     */
    private fun parseChiropractorManually(
        document: DocumentSnapshot, 
        baseChiropractor: ChiropractorProfileModel
    ): ChiropractorProfileModel {
        try {
            val data = document.data ?: return baseChiropractor
            
            // Parse education
            val educationMap = mutableMapOf<String, EducationItem>()
            val educationData = data["education"] as? Map<String, Any>
            educationData?.forEach { (key, value) ->
                val educationMap_inner = value as? Map<String, Any>
                if (educationMap_inner != null) {
                    val educationItem = EducationItem(
                        id = educationMap_inner["id"] as? String,
                        institution = educationMap_inner["institution"] as? String ?: "",
                        degree = educationMap_inner["degree"] as? String ?: "",
                        description = educationMap_inner["description"] as? String ?: "",
                        startDate = educationMap_inner["startDate"] as? String ?: "",
                        endDate = educationMap_inner["endDate"] as? String ?: "",
                        current = educationMap_inner["current"] as? Boolean ?: false
                    )
                    educationMap[key] = educationItem
                }
            }
            
            // Parse experience history
            val experienceMap = mutableMapOf<String, ExperienceItem>()
            val experienceData = data["experienceHistory"] as? Map<String, Any>
            experienceData?.forEach { (key, value) ->
                val experienceMap_inner = value as? Map<String, Any>
                if (experienceMap_inner != null) {
                    val experienceItem = ExperienceItem(
                        id = experienceMap_inner["id"] as? String,
                        organization = experienceMap_inner["organization"] as? String ?: "",
                        position = experienceMap_inner["position"] as? String ?: "",
                        description = experienceMap_inner["description"] as? String ?: "",
                        startDate = experienceMap_inner["startDate"] as? String ?: "",
                        endDate = experienceMap_inner["endDate"] as? String ?: "",
                        current = experienceMap_inner["current"] as? Boolean ?: false
                    )
                    experienceMap[key] = experienceItem
                }
            }
            
            // Parse professional credentials
            val credentialsMap = mutableMapOf<String, ProfessionalCredentialItem>()
            val credentialsData = data["professionalCredentials"] as? Map<String, Any>
            credentialsData?.forEach { (key, value) ->
                val credentialMap_inner = value as? Map<String, Any>
                if (credentialMap_inner != null) {
                    val credentialItem = ProfessionalCredentialItem(
                        id = credentialMap_inner["id"] as? String,
                        title = credentialMap_inner["title"] as? String ?: "",
                        institution = credentialMap_inner["institution"] as? String ?: "",
                        description = credentialMap_inner["description"] as? String ?: "",
                        year = credentialMap_inner["year"] as? String ?: "",
                        type = credentialMap_inner["type"] as? String ?: "",
                        imageUrl = credentialMap_inner["imageUrl"] as? String
                    )
                    credentialsMap[key] = credentialItem
                }
            }
            
            // Parse others
            val othersMap = mutableMapOf<String, OtherItem>()
            val othersData = data["others"] as? Map<String, Any>
            othersData?.forEach { (key, value) ->
                val otherMap_inner = value as? Map<String, Any>
                if (otherMap_inner != null) {
                    val otherItem = OtherItem(
                        id = otherMap_inner["id"] as? String,
                        title = otherMap_inner["title"] as? String ?: "",
                        category = otherMap_inner["category"] as? String ?: "",
                        description = otherMap_inner["description"] as? String ?: "",
                        date = otherMap_inner["date"] as? String ?: ""
                    )
                    othersMap[key] = otherItem
                }
            }
            
            Log.d(TAG, "Manual parsing completed - Education: ${educationMap.size}, Experience: ${experienceMap.size}, Credentials: ${credentialsMap.size}, Others: ${othersMap.size}")
            
            // Return updated chiropractor with manually parsed data
            return baseChiropractor.copy(
                education = educationMap,
                experienceHistory = experienceMap,
                professionalCredentials = credentialsMap,
                others = othersMap
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in manual parsing", e)
            return baseChiropractor
        }
    }
}

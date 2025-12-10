package com.brightcare.patient.data.repository

import com.brightcare.patient.data.model.Chiropractor
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for searching and managing chiropractors
 * Repository para sa paghahanap at pag-manage ng mga chiropractor
 */
@Singleton
class ChiropractorSearchRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val CHIROPRACTORS_COLLECTION = "chiropractors"
    }

    /**
     * Search chiropractors by name or specialization
     * Maghanap ng mga chiropractor gamit ang pangalan o specialization
     */
    suspend fun searchChiropractors(
        query: String,
        limit: Int = 20
    ): Result<List<Chiropractor>> {
        return try {
            val searchQuery = query.lowercase().trim()
            
            if (searchQuery.isEmpty()) {
                // Return all active chiropractors if no search query
                return getAllActiveChiropractors(limit)
            }

            // Search by name (case-insensitive)
            val nameResults = firestore.collection(CHIROPRACTORS_COLLECTION)
                .whereEqualTo("isAvailable", true)
                .orderBy("name")
                .startAt(searchQuery)
                .endAt(searchQuery + '\uf8ff')
                .limit(limit.toLong())
                .get()
                .await()

            // Search by specialization (case-insensitive)
            val specializationResults = firestore.collection(CHIROPRACTORS_COLLECTION)
                .whereEqualTo("isAvailable", true)
                .orderBy("specialization")
                .startAt(searchQuery)
                .endAt(searchQuery + '\uf8ff')
                .limit(limit.toLong())
                .get()
                .await()

            val chiropractors = mutableSetOf<Chiropractor>()
            
            // Add results from name search
            nameResults.documents.forEach { doc ->
                doc.toObject<Chiropractor>()?.let { chiropractor ->
                    chiropractors.add(chiropractor.copy(id = doc.id))
                }
            }
            
            // Add results from specialization search
            specializationResults.documents.forEach { doc ->
                doc.toObject<Chiropractor>()?.let { chiropractor ->
                    chiropractors.add(chiropractor.copy(id = doc.id))
                }
            }

            Result.success(chiropractors.toList().sortedBy { it.name })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all active chiropractors
     * Kunin ang lahat ng active na chiropractor
     */
    suspend fun getAllActiveChiropractors(limit: Int = 50): Result<List<Chiropractor>> {
        return try {
            val result = firestore.collection(CHIROPRACTORS_COLLECTION)
                .whereEqualTo("isAvailable", true)
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val chiropractors = result.documents.mapNotNull { doc ->
                doc.toObject<Chiropractor>()?.copy(id = doc.id)
            }

            Result.success(chiropractors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get chiropractors by specialization
     * Kunin ang mga chiropractor ayon sa specialization
     */
    suspend fun getChiropractorsBySpecialization(
        specialization: String,
        limit: Int = 20
    ): Result<List<Chiropractor>> {
        return try {
            val result = firestore.collection(CHIROPRACTORS_COLLECTION)
                .whereEqualTo("isAvailable", true)
                .whereEqualTo("specialization", specialization)
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val chiropractors = result.documents.mapNotNull { doc ->
                doc.toObject<Chiropractor>()?.copy(id = doc.id)
            }

            Result.success(chiropractors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get chiropractor by ID
     * Kunin ang chiropractor gamit ang ID
     */
    suspend fun getChiropractorById(chiropractorId: String): Result<Chiropractor?> {
        return try {
            val doc = firestore.collection(CHIROPRACTORS_COLLECTION)
                .document(chiropractorId)
                .get()
                .await()

            val chiropractor = doc.toObject<Chiropractor>()?.copy(id = doc.id)
            Result.success(chiropractor)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get top rated chiropractors
     * Kunin ang mga top rated na chiropractor
     */
    suspend fun getTopRatedChiropractors(limit: Int = 10): Result<List<Chiropractor>> {
        return try {
            val result = firestore.collection(CHIROPRACTORS_COLLECTION)
                .whereEqualTo("isAvailable", true)
                .whereGreaterThan("rating", 4.0)
                .orderBy("rating", Query.Direction.DESCENDING)
                .orderBy("reviewCount", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val chiropractors = result.documents.mapNotNull { doc ->
                doc.toObject<Chiropractor>()?.copy(id = doc.id)
            }

            Result.success(chiropractors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get available specializations
     * Kunin ang mga available na specialization
     */
    suspend fun getAvailableSpecializations(): Result<List<String>> {
        return try {
            val result = firestore.collection(CHIROPRACTORS_COLLECTION)
                .whereEqualTo("isAvailable", true)
                .get()
                .await()

            val specializations = result.documents
                .mapNotNull { doc -> doc.toObject<Chiropractor>()?.specialization }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()

            Result.success(specializations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Real-time search with Flow
     * Real-time search gamit ang Flow
     */
    fun searchChiropractorsFlow(query: String): Flow<List<Chiropractor>> = callbackFlow {
        val searchQuery = query.lowercase().trim()
        
        val listener = if (searchQuery.isEmpty()) {
            // Show all active chiropractors if no search query
            firestore.collection(CHIROPRACTORS_COLLECTION)
                .whereEqualTo("isAvailable", true)
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val chiropractors = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject<Chiropractor>()?.copy(id = doc.id)
                    } ?: emptyList()

                    trySend(chiropractors)
                }
        } else {
            // Search by name
            firestore.collection(CHIROPRACTORS_COLLECTION)
                .whereEqualTo("isAvailable", true)
                .orderBy("name")
                .startAt(searchQuery)
                .endAt(searchQuery + '\uf8ff')
                .limit(20)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val chiropractors = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject<Chiropractor>()?.copy(id = doc.id)
                    } ?: emptyList()

                    trySend(chiropractors)
                }
        }

        awaitClose { listener.remove() }
    }
}








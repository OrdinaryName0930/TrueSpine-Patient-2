package com.brightcare.patient.data.repository

import android.util.Log
import com.brightcare.patient.data.model.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling chiropractor reviews
 * Repository para sa paghawak ng mga review ng chiropractor
 */
@Singleton
class ReviewRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    companion object {
        private const val TAG = "ReviewRepository"
        private const val COLLECTION_REVIEWS = "reviews"
        private const val COLLECTION_APPOINTMENTS = "appointment"
        private const val COLLECTION_CHIROPRACTORS = "chiropractors"
        private const val COLLECTION_PATIENTS = "patients"
    }

    /**
     * Submit a review for a chiropractor after completed appointment
     * Mag-submit ng review para sa chiropractor pagkatapos ng completed appointment
     */
    suspend fun submitReview(
        appointmentId: String,
        chiropractorId: String,
        rating: Int,
        comment: String,
        isAnonymous: Boolean = false
    ): Result<String> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            Log.d(TAG, "Submitting review for appointment: $appointmentId, chiropractor: $chiropractorId")

            // Get client name from patients collection
            val clientName = try {
                val patientDoc = firestore.collection(COLLECTION_PATIENTS)
                    .document(currentUser.uid)
                    .get()
                    .await()

                if (patientDoc.exists()) {
                    val firstName = patientDoc.getString("firstName") ?: ""
                    val lastName = patientDoc.getString("lastName") ?: ""
                    "$firstName $lastName".trim().ifEmpty { "Anonymous" }
                } else {
                    currentUser.displayName ?: "Anonymous"
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not fetch patient name, using default", e)
                currentUser.displayName ?: "Anonymous"
            }

            // Check if review already exists for this appointment
            val existingReview = firestore.collection(COLLECTION_REVIEWS)
                .whereEqualTo("appointmentId", appointmentId)
                .whereEqualTo("clientId", currentUser.uid)
                .get()
                .await()

            if (!existingReview.isEmpty) {
                Log.d(TAG, "Review already exists for this appointment")
                return Result.failure(Exception("You have already reviewed this appointment"))
            }

            // Create review object
            val review = Review(
                appointmentId = appointmentId,
                chiropractorId = chiropractorId,
                clientId = currentUser.uid,
                clientName = if (isAnonymous) "Anonymous" else clientName,
                rating = rating,
                comment = comment,
                createdAt = System.currentTimeMillis(),
                isAnonymous = isAnonymous
            )

            // Save review to Firestore
            val reviewRef = firestore.collection(COLLECTION_REVIEWS)
                .add(review.toMap())
                .await()

            Log.d(TAG, "Review submitted successfully: ${reviewRef.id}")

            // Mark appointment as reviewed
            firestore.collection(COLLECTION_APPOINTMENTS)
                .document(appointmentId)
                .update("isReviewed", true, "reviewId", reviewRef.id)
                .await()

            Log.d(TAG, "Appointment marked as reviewed")

            // Update chiropractor's average rating
            updateChiropractorRating(chiropractorId)

            Result.success(reviewRef.id)

        } catch (e: Exception) {
            Log.e(TAG, "Error submitting review", e)
            Result.failure(Exception("Failed to submit review: ${e.message}"))
        }
    }

    /**
     * Update chiropractor's average rating based on all reviews
     * I-update ang average rating ng chiropractor batay sa lahat ng reviews
     */
    private suspend fun updateChiropractorRating(chiropractorId: String) {
        try {
            val reviews = firestore.collection(COLLECTION_REVIEWS)
                .whereEqualTo("chiropractorId", chiropractorId)
                .get()
                .await()

            if (!reviews.isEmpty) {
                val totalRating = reviews.documents.sumOf { doc ->
                    (doc.getLong("rating") ?: 0L).toInt()
                }
                val averageRating = totalRating.toDouble() / reviews.size()
                val reviewCount = reviews.size()

                // Update chiropractor document with new rating
                firestore.collection(COLLECTION_CHIROPRACTORS)
                    .document(chiropractorId)
                    .update(
                        mapOf(
                            "rating" to averageRating,
                            "reviewCount" to reviewCount
                        )
                    )
                    .await()

                Log.d(TAG, "Updated chiropractor rating: $averageRating ($reviewCount reviews)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating chiropractor rating", e)
            // Don't fail the review submission if rating update fails
        }
    }

    /**
     * Check if appointment has been reviewed
     * I-check kung na-review na ang appointment
     */
    suspend fun isAppointmentReviewed(appointmentId: String): Boolean {
        return try {
            val currentUser = firebaseAuth.currentUser ?: return false

            val review = firestore.collection(COLLECTION_REVIEWS)
                .whereEqualTo("appointmentId", appointmentId)
                .whereEqualTo("clientId", currentUser.uid)
                .get()
                .await()

            !review.isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if appointment is reviewed", e)
            false
        }
    }

    /**
     * Get review for an appointment
     * Kunin ang review para sa appointment
     */
    suspend fun getReviewForAppointment(appointmentId: String): Result<Review?> {
        return try {
            val currentUser = firebaseAuth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            val reviews = firestore.collection(COLLECTION_REVIEWS)
                .whereEqualTo("appointmentId", appointmentId)
                .whereEqualTo("clientId", currentUser.uid)
                .get()
                .await()

            if (reviews.isEmpty) {
                Result.success(null)
            } else {
                val doc = reviews.documents.first()
                Result.success(Review.fromMap(doc.id, doc.data ?: emptyMap()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting review", e)
            Result.failure(Exception("Failed to get review: ${e.message}"))
        }
    }

    /**
     * Get all reviews for a chiropractor
     * Kunin ang lahat ng reviews para sa chiropractor
     */
    suspend fun getChiropractorReviews(chiropractorId: String): Result<List<Review>> {
        return try {
            val reviews = firestore.collection(COLLECTION_REVIEWS)
                .whereEqualTo("chiropractorId", chiropractorId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val reviewList = reviews.documents.map { doc ->
                Review.fromMap(doc.id, doc.data ?: emptyMap())
            }

            Result.success(reviewList)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chiropractor reviews", e)
            Result.failure(Exception("Failed to get reviews: ${e.message}"))
        }
    }
}


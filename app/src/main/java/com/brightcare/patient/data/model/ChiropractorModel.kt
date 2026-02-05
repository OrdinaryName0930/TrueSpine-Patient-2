package com.brightcare.patient.data.model

import com.google.firebase.firestore.PropertyName
import kotlin.random.Random

/**
 * Firestore data model for chiropractor
 * Maps directly to Firestore document structure
 * Modelo ng Firestore para sa chiropractor na direktang naka-map sa structure ng document
 */
data class ChiropractorModel(
    @PropertyName("role")
    val role: String = "",
    
    @PropertyName("yearsOfExperience")
    val yearsOfExperience: Int = 0,
    
    @PropertyName("name")
    val name: String = "",
    
    @PropertyName("contactNumber")
    val contactNumber: String = "",
    
    @PropertyName("specialization")
    val specialization: String = "",
    
    @PropertyName("profileImageUrl")
    val profileImageUrl: String = "",
    
    @PropertyName("email")
    val email: String = "",
    
    // Real rating fields from Firestore (updated when reviews are submitted)
    // Totoong rating fields mula sa Firestore (na-update kapag may nag-submit ng review)
    @PropertyName("rating")
    val rating: Double = 0.0,
    
    @PropertyName("reviewCount")
    val reviewCount: Int = 0
) {
    /**
     * Get rating value - returns real rating or 0 if no reviews yet
     * Kunin ang rating value - ibabalik ang totoong rating o 0 kung wala pang review
     */
    fun getRealRating(): Float {
        return if (reviewCount > 0) {
            // Round to 1 decimal place
            ((rating * 10).toInt() / 10.0f)
        } else {
            0.0f // No reviews yet
        }
    }
    
    /**
     * Get review count - returns actual count from Firestore
     * Kunin ang review count - ibabalik ang totoong bilang mula sa Firestore
     */
    fun getRealReviewCount(): Int {
        return reviewCount
    }
    
    /**
     * Check if chiropractor has reviews
     * I-check kung may reviews ang chiropractor
     */
    fun hasReviews(): Boolean {
        return reviewCount > 0
    }
    
    fun generateDummyLocation(): String {
        // Generate location based on common areas in Philippines
        val locations = listOf(
            "Makati City", "Quezon City", "Manila", "Pasig City", 
            "Taguig City", "Mandaluyong City", "San Juan City", "Marikina City"
        )
        return locations.random()
    }
    
    fun generateAvailabilityStatus(): Boolean {
        // 80% chance of being available
        return Random.nextFloat() < 0.8f
    }
}




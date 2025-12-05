package com.brightcare.patient.data.model

import com.google.firebase.firestore.PropertyName
import kotlin.random.Random

/**
 * Firestore data model for chiropractor
 * Maps directly to Firestore document structure
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
    val email: String = ""
) {
    // Generate dummy data for fields not present in Firestore
    fun generateDummyRating(): Float {
        // Generate rating between 4.0 and 5.0 based on years of experience
        val baseRating = when {
            yearsOfExperience >= 15 -> 4.7f
            yearsOfExperience >= 10 -> 4.5f
            yearsOfExperience >= 5 -> 4.3f
            else -> 4.1f
        }
        val rating = baseRating + Random.nextFloat() * 0.3f // Add some variation
        // Round to 1 decimal place
        return (rating * 10).toInt() / 10.0f
    }
    
    fun generateDummyReviewCount(): Int {
        // Generate review count based on years of experience
        return when {
            yearsOfExperience >= 15 -> Random.nextInt(80, 150)
            yearsOfExperience >= 10 -> Random.nextInt(50, 100)
            yearsOfExperience >= 5 -> Random.nextInt(20, 60)
            else -> Random.nextInt(10, 30)
        }
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




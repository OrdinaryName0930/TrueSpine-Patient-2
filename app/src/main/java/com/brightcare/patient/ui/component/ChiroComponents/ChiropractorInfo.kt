package com.brightcare.patient.ui.component.chirocomponents

/**
 * Data class representing chiropractor information
 * Reusable across different screens and components
 * Maps to Firestore chiropractor data structure
 */
data class ChiropractorInfo(
    val id: String = "",
    val name: String = "",
    val specialization: String = "",
    val experience: String = "",
    val rating: Float = 0.0f,
    val location: String = "",
    val isAvailable: Boolean = true,
    val yearsOfExperience: Int = 0,
    val contactNumber: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val role: String = "",
    val reviewCount: Int = 0 // Dummy data for reviews
)
package com.brightcare.patient.ui.component.chiro

/**
 * Data class representing chiropractor information
 * Reusable across different screens and components
 */
data class ChiropractorInfo(
    val id: String,
    val name: String,
    val specialization: String,
    val experience: String,
    val rating: Float,
    val location: String,
    val isAvailable: Boolean
)
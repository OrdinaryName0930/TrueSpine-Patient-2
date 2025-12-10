package com.brightcare.patient.data.model

/**
 * Data model for emergency contact information
 * Model ng data para sa emergency contact information
 */
data class EmergencyContact(
    val id: String = "",
    val fullName: String = "",
    val relationship: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val address: String = "",
    val isPrimary: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Convert to map for Firestore storage
     * I-convert sa map para sa Firestore storage
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "fullName" to fullName,
            "relationship" to relationship,
            "phoneNumber" to phoneNumber,
            "email" to email,
            "address" to address,
            "isPrimary" to isPrimary,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
    
    companion object {
        /**
         * Create EmergencyContact from Firestore document data
         * Gumawa ng EmergencyContact mula sa Firestore document data
         */
        fun fromMap(data: Map<String, Any>): EmergencyContact {
            return EmergencyContact(
                id = data["id"] as? String ?: "",
                fullName = data["fullName"] as? String ?: "",
                relationship = data["relationship"] as? String ?: "",
                phoneNumber = data["phoneNumber"] as? String ?: "",
                email = data["email"] as? String ?: "",
                address = data["address"] as? String ?: "",
                isPrimary = data["isPrimary"] as? Boolean ?: false,
                createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis(),
                updatedAt = data["updatedAt"] as? Long ?: System.currentTimeMillis()
            )
        }
        
        /**
         * Available relationship options
         * Mga available na relationship options
         */
        val relationshipOptions = listOf(
            "Parent",
            "Spouse",
            "Sibling",
            "Child",
            "Grandparent",
            "Grandchild",
            "Friend",
            "Relative",
            "Guardian",
            "Other"
        )
    }
}

/**
 * Form state for emergency contact input
 * Form state para sa emergency contact input
 */
data class EmergencyContactFormState(
    val fullName: String = "",
    val relationship: String = "",
    val customRelationship: String = "", // For when "Other" is selected
    val phoneNumber: String = "",
    val email: String = "",
    val address: String = "",
    val isPrimary: Boolean = false,
    
    // Validation states
    val isFullNameError: Boolean = false,
    val isRelationshipError: Boolean = false,
    val isCustomRelationshipError: Boolean = false,
    val isPhoneNumberError: Boolean = false,
    val isEmailError: Boolean = false,
    val isAddressError: Boolean = false,
    
    // Error messages
    val fullNameErrorMessage: String = "",
    val relationshipErrorMessage: String = "",
    val customRelationshipErrorMessage: String = "",
    val phoneNumberErrorMessage: String = "",
    val emailErrorMessage: String = "",
    val addressErrorMessage: String = ""
)

/**
 * UI state for emergency contacts screen
 * UI state para sa emergency contacts screen
 */
data class EmergencyContactsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val emergencyContacts: List<EmergencyContact> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showAddContactDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val contactToDelete: EmergencyContact? = null,
    val editingContact: EmergencyContact? = null,
    val formState: EmergencyContactFormState = EmergencyContactFormState()
)

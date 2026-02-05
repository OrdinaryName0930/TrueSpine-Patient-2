package com.brightcare.patient.data.model

/**
 * Data model for emergency contact information
 * Model ng data para sa emergency contact information
 */
data class EmergencyContact(
    val id: String = "",
    // Separate name fields like in complete profile
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val suffix: String = "",
    val relationship: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    // Detailed address fields like in complete profile
    val country: String = "Philippines",
    val province: String = "",
    val municipality: String = "",
    val barangay: String = "",
    val additionalAddress: String = "",
    val isPrimary: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Get full name from separate name fields
     * Kumuha ng full name mula sa hiwalay na name fields
     */
    val fullName: String
        get() {
            val nameParts = listOfNotNull(
                firstName.takeIf { it.isNotBlank() },
                middleName.takeIf { it.isNotBlank() },
                lastName.takeIf { it.isNotBlank() },
                suffix.takeIf { it.isNotBlank() }
            )
            return nameParts.joinToString(" ")
        }
    
    /**
     * Get full address from separate address fields
     * Kumuha ng full address mula sa hiwalay na address fields
     */
    val fullAddress: String
        get() {
            val addressParts = listOfNotNull(
                additionalAddress.takeIf { it.isNotBlank() },
                barangay.takeIf { it.isNotBlank() },
                municipality.takeIf { it.isNotBlank() },
                province.takeIf { it.isNotBlank() },
                country.takeIf { it.isNotBlank() }
            )
            return addressParts.joinToString(", ")
        }

    /**
     * Convert to map for Firestore storage
     * I-convert sa map para sa Firestore storage
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "firstName" to firstName,
            "middleName" to middleName,
            "lastName" to lastName,
            "suffix" to suffix,
            "relationship" to relationship,
            "phoneNumber" to phoneNumber,
            "email" to email,
            "country" to country,
            "province" to province,
            "municipality" to municipality,
            "barangay" to barangay,
            "additionalAddress" to additionalAddress,
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
                firstName = data["firstName"] as? String ?: "",
                middleName = data["middleName"] as? String ?: "",
                lastName = data["lastName"] as? String ?: "",
                suffix = data["suffix"] as? String ?: "",
                relationship = data["relationship"] as? String ?: "",
                phoneNumber = data["phoneNumber"] as? String ?: "",
                email = data["email"] as? String ?: "",
                country = data["country"] as? String ?: "Philippines",
                province = data["province"] as? String ?: "",
                municipality = data["municipality"] as? String ?: "",
                barangay = data["barangay"] as? String ?: "",
                additionalAddress = data["additionalAddress"] as? String ?: "",
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
    // Separate name fields like in complete profile
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val suffix: String = "",
    val relationship: String = "",
    val customRelationship: String = "", // For when "Other" is selected
    val phoneNumber: String = "",
    val email: String = "",
    // Detailed address fields like in complete profile
    val country: String = "Philippines",
    val province: String = "",
    val municipality: String = "",
    val barangay: String = "",
    val additionalAddress: String = "",
    val isPrimary: Boolean = false,
    
    // Validation states for name fields
    val isFirstNameError: Boolean = false,
    val isMiddleNameError: Boolean = false,
    val isLastNameError: Boolean = false,
    val isSuffixError: Boolean = false,
    val isRelationshipError: Boolean = false,
    val isCustomRelationshipError: Boolean = false,
    val isPhoneNumberError: Boolean = false,
    val isEmailError: Boolean = false,
    // Validation states for address fields
    val isProvinceError: Boolean = false,
    val isMunicipalityError: Boolean = false,
    val isBarangayError: Boolean = false,
    val isAdditionalAddressError: Boolean = false,
    
    // Error messages for name fields
    val firstNameErrorMessage: String = "",
    val middleNameErrorMessage: String = "",
    val lastNameErrorMessage: String = "",
    val suffixErrorMessage: String = "",
    val relationshipErrorMessage: String = "",
    val customRelationshipErrorMessage: String = "",
    val phoneNumberErrorMessage: String = "",
    val emailErrorMessage: String = "",
    // Error messages for address fields
    val provinceErrorMessage: String = "",
    val municipalityErrorMessage: String = "",
    val barangayErrorMessage: String = "",
    val additionalAddressErrorMessage: String = ""
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

package com.brightcare.patient.data.repository

import android.util.Log
import com.brightcare.patient.data.model.ProfileValidationResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for validating user profile completion before booking
 * Service para sa pag-validate ng profile completion bago mag-book
 */
@Singleton
class ProfileValidationService @Inject constructor(
    private val completeProfileRepository: CompleteProfileRepository,
    private val emergencyContactRepository: EmergencyContactRepository
) {
    
    companion object {
        private const val TAG = "ProfileValidationService"
    }
    
    /**
     * Validate if user profile is complete for booking
     * I-validate kung kumpleto ang user profile para sa booking
     */
    suspend fun validateProfileForBooking(): ProfileValidationResult {
        return try {
            Log.d(TAG, "Starting profile validation for booking")
            
            // Check personal details completion
            val profileResult = completeProfileRepository.isProfileCompleted()
            val hasPersonalDetails = profileResult.getOrNull() ?: false
            
            Log.d(TAG, "Personal details completed: $hasPersonalDetails")
            if (profileResult.isFailure) {
                Log.e(TAG, "Error checking profile completion: ${profileResult.exceptionOrNull()?.message}")
            }
            
            // Check emergency contacts
            val emergencyContactsResult = emergencyContactRepository.getEmergencyContacts()
            val emergencyContacts = emergencyContactsResult.getOrNull() ?: emptyList()
            val hasEmergencyContact = emergencyContacts.isNotEmpty()
            
            Log.d(TAG, "Emergency contacts count: ${emergencyContacts.size}")
            Log.d(TAG, "Has emergency contact: $hasEmergencyContact")
            if (emergencyContactsResult.isFailure) {
                Log.e(TAG, "Error checking emergency contacts: ${emergencyContactsResult.exceptionOrNull()?.message}")
            }
            
            // Determine missing fields
            val missingFields = mutableListOf<String>()
            if (!hasPersonalDetails) {
                missingFields.add("personal_details")
            }
            if (!hasEmergencyContact) {
                missingFields.add("emergency_contact")
            }
            
            val isValid = hasPersonalDetails && hasEmergencyContact
            
            Log.d(TAG, "Profile validation result - Valid: $isValid, Missing: $missingFields")
            
            ProfileValidationResult(
                isValid = isValid,
                hasPersonalDetails = hasPersonalDetails,
                hasEmergencyContact = hasEmergencyContact,
                missingFields = missingFields,
                errorMessage = if (!isValid) createErrorMessage(missingFields) else null
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error validating profile for booking", e)
            ProfileValidationResult(
                isValid = false,
                hasPersonalDetails = false,
                hasEmergencyContact = false,
                missingFields = listOf("personal_details", "emergency_contact"),
                errorMessage = "Unable to validate profile. Please try again."
            )
        }
    }
    
    /**
     * Create user-friendly error message based on missing fields
     * Gumawa ng user-friendly error message base sa missing fields
     */
    private fun createErrorMessage(missingFields: List<String>): String {
        return when {
            missingFields.contains("personal_details") && missingFields.contains("emergency_contact") -> {
                "To book an appointment, please complete your personal details and add at least one emergency contact.\n\n" +
                "Para mag-book ng appointment, kumpletuhin ang inyong personal details at magdagdag ng hindi bababa sa isang emergency contact."
            }
            missingFields.contains("personal_details") -> {
                "Please complete your personal details to book an appointment.\n\n" +
                "Pakikumpletuhin ang inyong personal details para mag-book ng appointment."
            }
            missingFields.contains("emergency_contact") -> {
                "Please add at least one emergency contact to book an appointment.\n\n" +
                "Pakimagdagdag ng hindi bababa sa isang emergency contact para mag-book ng appointment."
            }
            else -> {
                "Profile validation failed. Please ensure your profile is complete.\n\n" +
                "Nabigo ang profile validation. Pakisiguro na kumpleto ang inyong profile."
            }
        }
    }
    
    /**
     * Get detailed validation status for UI display
     * Kumuha ng detalyadong validation status para sa UI display
     */
    suspend fun getDetailedValidationStatus(): DetailedValidationStatus {
        return try {
            val profileResult = completeProfileRepository.isProfileCompleted()
            val hasPersonalDetails = profileResult.getOrNull() ?: false
            
            val emergencyContactsResult = emergencyContactRepository.getEmergencyContacts()
            val emergencyContacts = emergencyContactsResult.getOrNull() ?: emptyList()
            val hasEmergencyContact = emergencyContacts.isNotEmpty()
            
            DetailedValidationStatus(
                hasPersonalDetails = hasPersonalDetails,
                hasEmergencyContact = hasEmergencyContact,
                emergencyContactsCount = emergencyContacts.size,
                personalDetailsMessage = if (hasPersonalDetails) {
                    "Personal details completed ✓\nKumpleto na ang personal details ✓"
                } else {
                    "Personal details required\nKailangan ang personal details"
                },
                emergencyContactMessage = if (hasEmergencyContact) {
                    "Emergency contact added ✓ (${emergencyContacts.size} contact${if (emergencyContacts.size > 1) "s" else ""})\n" +
                    "May emergency contact na ✓ (${emergencyContacts.size} contact${if (emergencyContacts.size > 1) "s" else ""})"
                } else {
                    "At least 1 emergency contact required\nKailangan ng hindi bababa sa 1 emergency contact"
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting detailed validation status", e)
            DetailedValidationStatus(
                hasPersonalDetails = false,
                hasEmergencyContact = false,
                emergencyContactsCount = 0,
                personalDetailsMessage = "Unable to check personal details",
                emergencyContactMessage = "Unable to check emergency contacts"
            )
        }
    }
}

/**
 * Detailed validation status for UI display
 * Detalyadong validation status para sa UI display
 */
data class DetailedValidationStatus(
    val hasPersonalDetails: Boolean = false,
    val hasEmergencyContact: Boolean = false,
    val emergencyContactsCount: Int = 0,
    val personalDetailsMessage: String = "",
    val emergencyContactMessage: String = ""
)

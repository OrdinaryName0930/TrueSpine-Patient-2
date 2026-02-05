package com.brightcare.patient.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.model.EmergencyContact
import com.brightcare.patient.data.model.EmergencyContactFormState
import com.brightcare.patient.data.model.EmergencyContactsUiState
import com.brightcare.patient.data.repository.EmergencyContactRepository
import com.brightcare.patient.ui.component.emergency_contact.EmergencyContactValidation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Extension function to format display names
 * Extension function para sa pag-format ng display names
 */
private fun String.toDisplayName(): String =
    lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

/**
 * ViewModel for managing emergency contacts screen state and operations
 * ViewModel para sa pag-manage ng emergency contacts screen state at operations
 */
@HiltViewModel
class EmergencyContactViewModel @Inject constructor(
    private val emergencyContactRepository: EmergencyContactRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "EmergencyContactViewModel"
    }
    
    private val _uiState = MutableStateFlow(EmergencyContactsUiState())
    val uiState: StateFlow<EmergencyContactsUiState> = _uiState.asStateFlow()
    
    init {
        loadEmergencyContacts()
    }
    
    /**
     * Load all emergency contacts
     * I-load lahat ng emergency contacts
     */
    fun loadEmergencyContacts() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                val result = emergencyContactRepository.getEmergencyContacts()
                result.fold(
                    onSuccess = { contacts ->
                        Log.d(TAG, "Emergency contacts loaded successfully: ${contacts.size} contacts")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            emergencyContacts = contacts
                        )
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error loading emergency contacts", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to load emergency contacts"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading emergency contacts", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unexpected error occurred"
                )
            }
        }
    }
    
    /**
     * Show add contact dialog
     * Ipakita ang add contact dialog
     */
    fun showAddContactDialog() {
        val currentContacts = _uiState.value.emergencyContacts
        val maxContacts = emergencyContactRepository.getMaxContactsAllowed()
        
        if (currentContacts.size >= maxContacts) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Maximum of $maxContacts emergency contacts allowed"
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(
            showAddContactDialog = true,
            editingContact = null,
            formState = EmergencyContactFormState()
        )
    }
    
    /**
     * Show edit contact dialog
     * Ipakita ang edit contact dialog
     */
    fun showEditContactDialog(contact: EmergencyContact) {
        // Check if the relationship is a custom one (not in predefined list)
        val isCustomRelationship = !EmergencyContact.relationshipOptions.contains(contact.relationship)
        val displayRelationship = if (isCustomRelationship) "Other" else contact.relationship
        val customRelationship = if (isCustomRelationship) contact.relationship else ""
        
        _uiState.value = _uiState.value.copy(
            showAddContactDialog = true,
            editingContact = contact,
            formState = EmergencyContactFormState(
                firstName = contact.firstName,
                middleName = contact.middleName,
                lastName = contact.lastName,
                suffix = contact.suffix,
                relationship = displayRelationship,
                customRelationship = customRelationship,
                phoneNumber = contact.phoneNumber,
                email = contact.email,
                country = contact.country,
                province = contact.province,
                municipality = contact.municipality,
                barangay = contact.barangay,
                additionalAddress = contact.additionalAddress,
                isPrimary = contact.isPrimary
            )
        )
    }
    
    /**
     * Hide contact dialog
     * Itago ang contact dialog
     */
    fun hideContactDialog() {
        _uiState.value = _uiState.value.copy(
            showAddContactDialog = false,
            editingContact = null,
            formState = EmergencyContactFormState()
        )
    }
    
    /**
     * Update form state
     * I-update ang form state
     */
    fun updateFormState(updater: (EmergencyContactFormState) -> EmergencyContactFormState) {
        val currentState = _uiState.value
        val newFormState = updater(currentState.formState)
        _uiState.value = currentState.copy(formState = newFormState)
    }
    
    /**
     * Update first name with real-time validation and formatting
     * I-update ang first name na may real-time validation at formatting
     */
    fun updateFirstName(firstName: String) {
        // Allow letters and spaces
        val filtered = firstName.filter { it.isLetter() || it == ' ' }
        // Collapse multiple spaces in the middle
        val collapsed = filtered.replace(Regex("\\s{2,}"), " ")
        // Keep trailing space while typing; capitalize each word
        val cleanValue = collapsed.toDisplayName()
        // Trim only for validation
        val isValid = com.brightcare.patient.ui.component.signup_component.ValidationUtils.isValidName(cleanValue.trim())
        
        updateFormState { currentState ->
            currentState.copy(
                firstName = cleanValue,
                isFirstNameError = cleanValue.trim().isNotBlank() && !isValid,
                firstNameErrorMessage = if (cleanValue.trim().isNotBlank() && !isValid)
                    "First name must be at least 2 characters and contain only letters" else ""
            )
        }
    }
    
    /**
     * Update middle name with real-time validation and formatting
     * I-update ang middle name na may real-time validation at formatting
     */
    fun updateMiddleName(middleName: String) {
        // Allow letters and spaces
        val filtered = middleName.filter { it.isLetter() || it == ' ' }
        // Collapse multiple spaces in the middle
        val collapsed = filtered.replace(Regex("\\s{2,}"), " ")
        // Keep trailing space while typing; capitalize each word
        val cleanValue = collapsed.toDisplayName()
        // Middle name is optional, only validate if not blank
        val isValid = if (cleanValue.trim().isBlank()) {
            true
        } else {
            com.brightcare.patient.ui.component.signup_component.ValidationUtils.isValidName(cleanValue.trim())
        }
        
        updateFormState { currentState ->
            currentState.copy(
                middleName = cleanValue,
                isMiddleNameError = cleanValue.trim().isNotBlank() && !isValid,
                middleNameErrorMessage = if (cleanValue.trim().isNotBlank() && !isValid)
                    "Middle name must be at least 2 characters and contain only letters" else ""
            )
        }
    }
    
    /**
     * Update last name with real-time validation and formatting
     * I-update ang last name na may real-time validation at formatting
     */
    fun updateLastName(lastName: String) {
        // Allow letters and spaces
        val filtered = lastName.filter { it.isLetter() || it == ' ' }
        // Collapse multiple spaces in the middle
        val collapsed = filtered.replace(Regex("\\s{2,}"), " ")
        // Keep trailing space while typing; capitalize each word
        val cleanValue = collapsed.toDisplayName()
        // Trim only for validation
        val isValid = com.brightcare.patient.ui.component.signup_component.ValidationUtils.isValidName(cleanValue.trim())
        
        updateFormState { currentState ->
            currentState.copy(
                lastName = cleanValue,
                isLastNameError = cleanValue.trim().isNotBlank() && !isValid,
                lastNameErrorMessage = if (cleanValue.trim().isNotBlank() && !isValid)
                    "Last name must be at least 2 characters and contain only letters" else ""
            )
        }
    }
    
    /**
     * Update suffix
     * I-update ang suffix
     */
    fun updateSuffix(suffix: String) {
        updateFormState { currentState ->
            currentState.copy(suffix = suffix)
        }
    }
    
    /**
     * Update relationship with validation
     * I-update ang relationship na may validation
     */
    fun updateRelationship(relationship: String) {
        val currentFormState = _uiState.value.formState
        val validation = EmergencyContactValidation.validateRelationship(relationship, currentFormState.customRelationship)
        
        updateFormState { currentState ->
            currentState.copy(
                relationship = relationship,
                isRelationshipError = !validation.isValid,
                relationshipErrorMessage = validation.errorMessage,
                // Clear custom relationship if not "Other"
                customRelationship = if (relationship != "Other") "" else currentState.customRelationship,
                isCustomRelationshipError = if (relationship != "Other") false else currentState.isCustomRelationshipError,
                customRelationshipErrorMessage = if (relationship != "Other") "" else currentState.customRelationshipErrorMessage
            )
        }
    }
    
    /**
     * Update custom relationship with real-time validation and formatting
     * I-update ang custom relationship na may real-time validation at formatting
     */
    fun updateCustomRelationship(customRelationship: String) {
        val formattedRelationship = EmergencyContactValidation.formatCustomRelationship(customRelationship)
        val currentFormState = _uiState.value.formState
        val validation = EmergencyContactValidation.validateRelationship(currentFormState.relationship, formattedRelationship)
        
        updateFormState { currentState ->
            currentState.copy(
                customRelationship = formattedRelationship,
                isCustomRelationshipError = !validation.isValid,
                customRelationshipErrorMessage = validation.errorMessage,
                // Also update main relationship validation
                isRelationshipError = !validation.isValid,
                relationshipErrorMessage = validation.errorMessage
            )
        }
    }
    
    /**
     * Update phone number with real-time validation and formatting
     * I-update ang phone number na may real-time validation at formatting
     */
    fun updatePhoneNumber(phone: String) {
        val formattedPhone = EmergencyContactValidation.formatPhoneNumber(phone)
        val validation = EmergencyContactValidation.validatePhoneNumber(formattedPhone)
        
        updateFormState { currentState ->
            currentState.copy(
                phoneNumber = formattedPhone,
                isPhoneNumberError = !validation.isValid,
                phoneNumberErrorMessage = validation.errorMessage
            )
        }
    }
    
    /**
     * Update email with real-time validation
     * I-update ang email na may real-time validation
     */
    fun updateEmail(email: String) {
        val validation = EmergencyContactValidation.validateEmail(email)
        
        updateFormState { currentState ->
            currentState.copy(
                email = email.trim(),
                isEmailError = !validation.isValid,
                emailErrorMessage = validation.errorMessage
            )
        }
    }
    
    /**
     * Update province
     * I-update ang province
     */
    fun updateProvince(province: String) {
        updateFormState { currentState ->
            currentState.copy(
                province = province,
                municipality = "", // Reset dependent fields
                barangay = "",
                isProvinceError = false,
                provinceErrorMessage = ""
            )
        }
    }
    
    /**
     * Update municipality
     * I-update ang municipality
     */
    fun updateMunicipality(municipality: String) {
        updateFormState { currentState ->
            currentState.copy(
                municipality = municipality,
                barangay = "", // Reset dependent field
                isMunicipalityError = false,
                municipalityErrorMessage = ""
            )
        }
    }
    
    /**
     * Update barangay
     * I-update ang barangay
     */
    fun updateBarangay(barangay: String) {
        updateFormState { currentState ->
            currentState.copy(
                barangay = barangay,
                isBarangayError = false,
                barangayErrorMessage = ""
            )
        }
    }
    
    /**
     * Update additional address with validation (matching complete profile)
     * I-update ang additional address na may validation (tumugma sa complete profile)
     */
    fun updateAdditionalAddress(additionalAddress: String) {
        // Apply same formatting as complete profile
        // Allow letters, numbers, ñ/Ñ, spaces, and punctuation
        val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZñÑ0123456789 ,.#'-/"
        val filtered = additionalAddress.filter { it in allowedChars }
        
        // Collapse multiple spaces in the middle
        val collapsed = filtered.replace(Regex("\\s{2,}"), " ")
        
        // Do NOT trim trailing spaces while typing
        val cleanValue = collapsed
        
        // Use same validation as complete profile
        val isValid = com.brightcare.patient.ui.component.signup_component.ValidationUtils.isValidAdditionalAddress(cleanValue.trim())
        
        updateFormState { currentState ->
            currentState.copy(
                additionalAddress = cleanValue,
                isAdditionalAddressError = cleanValue.trim().isNotBlank() && !isValid,
                additionalAddressErrorMessage = if (cleanValue.trim().isNotBlank() && !isValid)
                    "Additional address must be at least 3 characters long and may only contain letters, numbers, spaces, and basic punctuation (,.#'-/)." else ""
            )
        }
    }
    
    /**
     * Update primary contact status with validation
     * I-update ang primary contact status na may validation
     */
    fun updatePrimaryStatus(isPrimary: Boolean) {
        updateFormState { currentState ->
            currentState.copy(isPrimary = isPrimary)
        }
    }
    
    /**
     * Check if the form is valid and can be saved
     * I-check kung valid ang form at pwedeng i-save
     */
    fun isFormValid(): Boolean {
        val formState = _uiState.value.formState
        
        return !formState.isFirstNameError &&
               !formState.isMiddleNameError &&
               !formState.isLastNameError &&
               !formState.isRelationshipError &&
               !formState.isCustomRelationshipError &&
               !formState.isPhoneNumberError &&
               !formState.isEmailError &&
               !formState.isProvinceError &&
               !formState.isMunicipalityError &&
               !formState.isBarangayError &&
               !formState.isAdditionalAddressError &&
               formState.firstName.trim().isNotBlank() &&
               formState.lastName.trim().isNotBlank() &&
               formState.relationship.isNotBlank() &&
               formState.phoneNumber.isNotBlank() &&
               (formState.relationship != "Other" || formState.customRelationship.isNotBlank())
    }
    
    /**
     * Save emergency contact (add or update)
     * I-save ang emergency contact (add o update)
     */
    fun saveEmergencyContact() {
        val currentState = _uiState.value
        val formState = currentState.formState
        
        // Validate form
        if (!validateForm(formState)) {
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
                
                // Handle primary contact logic - only one can be primary
                val finalIsPrimary = if (formState.isPrimary) {
                    // If setting as primary, we'll handle this in the repository
                    true
                } else {
                    formState.isPrimary
                }
                
                // Determine final relationship value
                val finalRelationship = if (formState.relationship == "Other" && formState.customRelationship.isNotBlank()) {
                    formState.customRelationship.trim()
                } else {
                    formState.relationship
                }
                
                val contact = EmergencyContact(
                    id = currentState.editingContact?.id ?: "",
                    firstName = formState.firstName.trim(),
                    middleName = formState.middleName.trim(),
                    lastName = formState.lastName.trim(),
                    suffix = formState.suffix.trim(),
                    relationship = finalRelationship,
                    phoneNumber = formState.phoneNumber.trim(),
                    email = formState.email.trim(),
                    country = formState.country,
                    province = formState.province,
                    municipality = formState.municipality,
                    barangay = formState.barangay,
                    additionalAddress = formState.additionalAddress.trim(),
                    isPrimary = finalIsPrimary,
                    createdAt = currentState.editingContact?.createdAt ?: System.currentTimeMillis()
                )
                
                val result = if (currentState.editingContact != null) {
                    // Update existing contact
                    emergencyContactRepository.updateEmergencyContact(contact)
                } else {
                    // Add new contact
                    emergencyContactRepository.addEmergencyContact(contact)
                }
                
                result.fold(
                    onSuccess = { _ ->
                        Log.d(TAG, "Emergency contact saved successfully")
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            showAddContactDialog = false,
                            editingContact = null,
                            formState = EmergencyContactFormState(),
                            successMessage = if (currentState.editingContact != null) 
                                "Emergency contact updated successfully" 
                            else 
                                "Emergency contact added successfully"
                        )
                        // Reload contacts
                        loadEmergencyContacts()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error saving emergency contact", exception)
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            errorMessage = exception.message ?: "Failed to save emergency contact"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error saving emergency contact", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Unexpected error occurred"
                )
            }
        }
    }
    
    /**
     * Show delete confirmation dialog
     * Ipakita ang delete confirmation dialog
     */
    fun showDeleteConfirmation(contact: EmergencyContact) {
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmDialog = true,
            contactToDelete = contact
        )
    }
    
    /**
     * Hide delete confirmation dialog
     * Itago ang delete confirmation dialog
     */
    fun hideDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(
            showDeleteConfirmDialog = false,
            contactToDelete = null
        )
    }
    
    /**
     * Delete emergency contact (after confirmation)
     * Tanggalin ang emergency contact (pagkatapos ng confirmation)
     */
    fun confirmDeleteEmergencyContact() {
        val contactToDelete = _uiState.value.contactToDelete
        if (contactToDelete == null) {
            hideDeleteConfirmation()
            return
        }
        
        deleteEmergencyContact(contactToDelete.id)
        hideDeleteConfirmation()
    }
    
    /**
     * Delete emergency contact
     * Tanggalin ang emergency contact
     */
    private fun deleteEmergencyContact(contactId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)
                
                val result = emergencyContactRepository.deleteEmergencyContact(contactId)
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "Emergency contact deleted successfully")
                        _uiState.value = _uiState.value.copy(
                            isDeleting = false,
                            successMessage = "Emergency contact deleted successfully"
                        )
                        // Reload contacts
                        loadEmergencyContacts()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error deleting emergency contact", exception)
                        _uiState.value = _uiState.value.copy(
                            isDeleting = false,
                            errorMessage = exception.message ?: "Failed to delete emergency contact"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error deleting emergency contact", e)
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    errorMessage = "Unexpected error occurred"
                )
            }
        }
    }
    
    /**
     * Set primary emergency contact
     * Itakda ang primary emergency contact
     */
    fun setPrimaryContact(contactId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
                
                val result = emergencyContactRepository.setPrimaryContact(contactId)
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "Primary emergency contact set successfully")
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            successMessage = "Primary emergency contact updated"
                        )
                        // Reload contacts
                        loadEmergencyContacts()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error setting primary emergency contact", exception)
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            errorMessage = exception.message ?: "Failed to set primary contact"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error setting primary contact", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Unexpected error occurred"
                )
            }
        }
    }
    
    /**
     * Clear error message
     * I-clear ang error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Clear success message
     * I-clear ang success message
     */
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
    
    /**
     * Validate form fields using the validation system
     * I-validate ang form fields gamit ang validation system
     */
    private fun validateForm(formState: EmergencyContactFormState): Boolean {
        var isValid = true
        var updatedFormState = formState
        
        // Validate first name
        if (formState.firstName.trim().isBlank()) {
            updatedFormState = updatedFormState.copy(
                isFirstNameError = true,
                firstNameErrorMessage = "First name is required"
            )
            isValid = false
        } else if (!com.brightcare.patient.ui.component.signup_component.ValidationUtils.isValidName(formState.firstName.trim())) {
            updatedFormState = updatedFormState.copy(
                isFirstNameError = true,
                firstNameErrorMessage = "First name must be at least 2 characters and contain only letters"
            )
            isValid = false
        }
        
        // Validate middle name (optional)
        if (formState.middleName.trim().isNotBlank() && 
            !com.brightcare.patient.ui.component.signup_component.ValidationUtils.isValidName(formState.middleName.trim())) {
            updatedFormState = updatedFormState.copy(
                isMiddleNameError = true,
                middleNameErrorMessage = "Middle name must be at least 2 characters and contain only letters"
            )
            isValid = false
        }
        
        // Validate last name
        if (formState.lastName.trim().isBlank()) {
            updatedFormState = updatedFormState.copy(
                isLastNameError = true,
                lastNameErrorMessage = "Last name is required"
            )
            isValid = false
        } else if (!com.brightcare.patient.ui.component.signup_component.ValidationUtils.isValidName(formState.lastName.trim())) {
            updatedFormState = updatedFormState.copy(
                isLastNameError = true,
                lastNameErrorMessage = "Last name must be at least 2 characters and contain only letters"
            )
            isValid = false
        }
        
        // Validate relationship
        if (formState.relationship.isBlank()) {
            updatedFormState = updatedFormState.copy(
                isRelationshipError = true,
                relationshipErrorMessage = "Relationship is required"
            )
            isValid = false
        } else if (formState.relationship == "Other" && formState.customRelationship.trim().isBlank()) {
            updatedFormState = updatedFormState.copy(
                isCustomRelationshipError = true,
                customRelationshipErrorMessage = "Please specify the relationship"
            )
            isValid = false
        }
        
        // Validate phone number
        if (formState.phoneNumber.isBlank()) {
            updatedFormState = updatedFormState.copy(
                isPhoneNumberError = true,
                phoneNumberErrorMessage = "Phone number is required"
            )
            isValid = false
        } else if (!com.brightcare.patient.ui.component.signup_component.ValidationUtils.isValidPhoneNumber(formState.phoneNumber)) {
            updatedFormState = updatedFormState.copy(
                isPhoneNumberError = true,
                phoneNumberErrorMessage = "Phone number must start with 09 and have 11 digits"
            )
            isValid = false
        }
        
        // Validate email (optional)
        if (formState.email.trim().isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(formState.email.trim()).matches()) {
            updatedFormState = updatedFormState.copy(
                isEmailError = true,
                emailErrorMessage = "Please enter a valid email address"
            )
            isValid = false
        }
        
        // Validate additional address (optional but if provided, must be valid - matching complete profile)
        if (formState.additionalAddress.trim().isNotBlank()) {
            if (!com.brightcare.patient.ui.component.signup_component.ValidationUtils.isValidAdditionalAddress(formState.additionalAddress.trim())) {
                updatedFormState = updatedFormState.copy(
                    isAdditionalAddressError = true,
                    additionalAddressErrorMessage = "Additional address must be at least 3 characters long and may only contain letters, numbers, spaces, and basic punctuation (,.#'-/)."
                )
                isValid = false
            }
        }
        
        // Update form state with validation results
        _uiState.value = _uiState.value.copy(formState = updatedFormState)
        
        return isValid
    }
}

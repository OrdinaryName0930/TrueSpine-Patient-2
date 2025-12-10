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
                fullName = contact.fullName,
                relationship = displayRelationship,
                customRelationship = customRelationship,
                phoneNumber = contact.phoneNumber,
                email = contact.email,
                address = contact.address,
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
     * Update full name with real-time validation and formatting
     * I-update ang full name na may real-time validation at formatting
     */
    fun updateFullName(name: String) {
        val formattedName = EmergencyContactValidation.formatFullName(name)
        val validation = EmergencyContactValidation.validateFullName(formattedName)
        
        updateFormState { currentState ->
            currentState.copy(
                fullName = formattedName,
                isFullNameError = !validation.isValid,
                fullNameErrorMessage = validation.errorMessage
            )
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
     * Update address with real-time validation and formatting
     * I-update ang address na may real-time validation at formatting
     */
    fun updateAddress(address: String) {
        val formattedAddress = EmergencyContactValidation.formatAddress(address)
        val validation = EmergencyContactValidation.validateAddress(formattedAddress)
        
        updateFormState { currentState ->
            currentState.copy(
                address = formattedAddress,
                isAddressError = !validation.isValid,
                addressErrorMessage = validation.errorMessage
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
        
        return !formState.isFullNameError &&
               !formState.isRelationshipError &&
               !formState.isCustomRelationshipError &&
               !formState.isPhoneNumberError &&
               !formState.isEmailError &&
               !formState.isAddressError &&
               formState.fullName.isNotBlank() &&
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
                    fullName = formState.fullName.trim(),
                    relationship = finalRelationship,
                    phoneNumber = formState.phoneNumber.trim(),
                    email = formState.email.trim(),
                    address = formState.address.trim(),
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
     * Validate form fields using the new validation system
     * I-validate ang form fields gamit ang bagong validation system
     */
    private fun validateForm(formState: EmergencyContactFormState): Boolean {
        var isValid = true
        var updatedFormState = formState
        
        // Validate full name
        val nameValidation = EmergencyContactValidation.validateFullName(formState.fullName)
        if (!nameValidation.isValid) {
            updatedFormState = updatedFormState.copy(
                isFullNameError = true,
                fullNameErrorMessage = nameValidation.errorMessage
            )
            isValid = false
        }
        
        // Validate relationship
        val relationshipValidation = EmergencyContactValidation.validateRelationship(
            formState.relationship, 
            formState.customRelationship
        )
        if (!relationshipValidation.isValid) {
            updatedFormState = updatedFormState.copy(
                isRelationshipError = true,
                relationshipErrorMessage = relationshipValidation.errorMessage
            )
            isValid = false
        }
        
        // Validate phone number
        val phoneValidation = EmergencyContactValidation.validatePhoneNumber(formState.phoneNumber)
        if (!phoneValidation.isValid) {
            updatedFormState = updatedFormState.copy(
                isPhoneNumberError = true,
                phoneNumberErrorMessage = phoneValidation.errorMessage
            )
            isValid = false
        }
        
        // Validate email
        val emailValidation = EmergencyContactValidation.validateEmail(formState.email)
        if (!emailValidation.isValid) {
            updatedFormState = updatedFormState.copy(
                isEmailError = true,
                emailErrorMessage = emailValidation.errorMessage
            )
            isValid = false
        }
        
        // Validate address
        val addressValidation = EmergencyContactValidation.validateAddress(formState.address)
        if (!addressValidation.isValid) {
            updatedFormState = updatedFormState.copy(
                isAddressError = true,
                addressErrorMessage = addressValidation.errorMessage
            )
            isValid = false
        }
        
        // Update form state with validation results
        _uiState.value = _uiState.value.copy(formState = updatedFormState)
        
        return isValid
    }
}

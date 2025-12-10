package com.brightcare.patient.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.repository.CompleteProfileRepository
import com.brightcare.patient.ui.screens.CompleteProfileFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for complete profile screen
 */
data class CompleteProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingImages: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val uploadProgress: Float = 0f,
    val formState: CompleteProfileFormState = CompleteProfileFormState()
)

/**
 * ViewModel for managing complete profile screen state and operations
 */
@HiltViewModel
class CompleteProfileViewModel @Inject constructor(
    private val completeProfileRepository: CompleteProfileRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "CompleteProfileViewModel"
    }
    
    private val _uiState = MutableStateFlow(CompleteProfileUiState())
    val uiState: StateFlow<CompleteProfileUiState> = _uiState.asStateFlow()
    
    init {
        // Load existing profile data if available
        loadExistingProfile()
    }
    
    /**
     * Load existing profile data for editing
     */
    fun loadExistingProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                val result = completeProfileRepository.getProfileData()
                result.fold(
                    onSuccess = { profileData ->
                        if (profileData != null) {
                            Log.d(TAG, "Existing profile data loaded")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                formState = profileData
                            )
                        } else {
                            Log.d(TAG, "No existing profile data found")
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error loading profile data", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to load profile data: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading profile", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unexpected error occurred"
                )
            }
        }
    }
    
    /**
     * Update form state
     */
    fun updateFormState(updater: (CompleteProfileFormState) -> CompleteProfileFormState) {
        val currentState = _uiState.value
        val newFormState = updater(currentState.formState)
        _uiState.value = currentState.copy(formState = newFormState)
    }
    
    /**
     * Save complete profile data to Firestore
     */
    fun saveProfile() {
        val currentFormState = _uiState.value.formState
        
        // Validate required fields before saving
        if (!isFormValid(currentFormState)) {
            Log.w(TAG, "Form validation failed")
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please fill in all required fields correctly"
            )
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isSaving = true, 
                    isUploadingImages = currentFormState.idFrontImageUri.isNotBlank() || currentFormState.idBackImageUri.isNotBlank(),
                    errorMessage = null,
                    isSuccess = false,
                    uploadProgress = 0f
                )
                
                Log.d(TAG, "Saving profile data...")
                
                // Update progress for image uploads
                if (_uiState.value.isUploadingImages) {
                    updateUploadProgress(0.3f)
                }
                
                val result = completeProfileRepository.saveCompleteProfile(currentFormState)
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "Profile saved successfully")
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isUploadingImages = false,
                            isSuccess = true,
                            uploadProgress = 1f
                        )
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error saving profile", exception)
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isUploadingImages = false,
                            isSuccess = false,
                            uploadProgress = 0f,
                            errorMessage = getErrorMessage(exception)
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error saving profile", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isUploadingImages = false,
                    isSuccess = false,
                    uploadProgress = 0f,
                    errorMessage = "An unexpected error occurred. Please try again."
                )
            }
        }
    }
    
    /**
     * Update existing profile data
     */
    fun updateProfile() {
        val currentFormState = _uiState.value.formState
        
        // Validate required fields before updating
        if (!isFormValid(currentFormState)) {
            Log.w(TAG, "Form validation failed")
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please fill in all required fields correctly"
            )
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isSaving = true, 
                    isUploadingImages = currentFormState.idFrontImageUri.isNotBlank() || currentFormState.idBackImageUri.isNotBlank(),
                    errorMessage = null,
                    isSuccess = false,
                    uploadProgress = 0f
                )
                
                Log.d(TAG, "Updating profile data...")
                
                // Update progress for image uploads
                if (_uiState.value.isUploadingImages) {
                    updateUploadProgress(0.3f)
                }
                
                val result = completeProfileRepository.updateProfile(currentFormState)
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "Profile updated successfully")
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isUploadingImages = false,
                            isSuccess = true,
                            uploadProgress = 1f
                        )
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error updating profile", exception)
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isUploadingImages = false,
                            isSuccess = false,
                            uploadProgress = 0f,
                            errorMessage = getErrorMessage(exception)
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error updating profile", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isUploadingImages = false,
                    isSuccess = false,
                    uploadProgress = 0f,
                    errorMessage = "An unexpected error occurred. Please try again."
                )
            }
        }
    }
    
    /**
     * Check if profile is already completed
     */
    fun checkProfileCompletion(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val result = completeProfileRepository.isProfileCompleted()
                result.fold(
                    onSuccess = { isCompleted ->
                        Log.d(TAG, "Profile completion status: $isCompleted")
                        onResult(isCompleted)
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error checking profile completion", exception)
                        onResult(false) // Default to false if error
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error checking profile completion", e)
                onResult(false)
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Reset success state
     */
    fun resetSuccessState() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }
    
    /**
     * Validate first name field
     */
    fun validateFirstName(firstName: String) {
        val trimmedName = firstName.trim()
        val isValid = when {
            trimmedName.isBlank() -> false
            trimmedName.length < 2 -> false
            !trimmedName.all { it.isLetter() || it.isWhitespace() } -> false
            else -> true
        }
        
        val errorMessage = when {
            trimmedName.isBlank() -> "First name is required"
            trimmedName.length < 2 -> "First name must be at least 2 characters"
            !trimmedName.all { it.isLetter() || it.isWhitespace() } -> "First name must contain only letters"
            else -> ""
        }
        
        updateFormState { currentState ->
            currentState.copy(
                firstName = firstName,
                isFirstNameError = !isValid,
                firstNameErrorMessage = errorMessage
            )
        }
    }
    
    /**
     * Validate middle name field (optional)
     */
    fun validateMiddleName(middleName: String) {
        val trimmedName = middleName.trim()
        val isValid = if (trimmedName.isBlank()) {
            true // Middle name is optional
        } else {
            trimmedName.length >= 2 && trimmedName.all { it.isLetter() || it.isWhitespace() }
        }
        
        val errorMessage = if (trimmedName.isNotBlank() && !isValid) {
            when {
                trimmedName.length < 2 -> "Middle name must be at least 2 characters"
                !trimmedName.all { it.isLetter() || it.isWhitespace() } -> "Middle name must contain only letters"
                else -> ""
            }
        } else ""
        
        updateFormState { currentState ->
            currentState.copy(
                middleName = middleName,
                isMiddleNameError = !isValid,
                middleNameErrorMessage = errorMessage
            )
        }
    }
    
    /**
     * Validate last name field
     */
    fun validateLastName(lastName: String) {
        val trimmedName = lastName.trim()
        val isValid = when {
            trimmedName.isBlank() -> false
            trimmedName.length < 2 -> false
            !trimmedName.all { it.isLetter() || it.isWhitespace() } -> false
            else -> true
        }
        
        val errorMessage = when {
            trimmedName.isBlank() -> "Last name is required"
            trimmedName.length < 2 -> "Last name must be at least 2 characters"
            !trimmedName.all { it.isLetter() || it.isWhitespace() } -> "Last name must contain only letters"
            else -> ""
        }
        
        updateFormState { currentState ->
            currentState.copy(
                lastName = lastName,
                isLastNameError = !isValid,
                lastNameErrorMessage = errorMessage
            )
        }
    }
    
    /**
     * Validate phone number field
     */
    fun validatePhoneNumber(phoneNumber: String) {
        val cleanedNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        val isValid = when {
            cleanedNumber.isBlank() -> false
            cleanedNumber.startsWith("+63") && cleanedNumber.length == 13 -> true
            cleanedNumber.startsWith("09") && cleanedNumber.length == 11 -> true
            cleanedNumber.startsWith("9") && cleanedNumber.length == 10 -> true
            else -> false
        }
        
        val errorMessage = if (!isValid) {
            "Please enter a valid Philippine phone number (e.g., 09123456789 or +639123456789)"
        } else ""
        
        updateFormState { currentState ->
            currentState.copy(
                phoneNumber = phoneNumber,
                isPhoneNumberError = !isValid,
                phoneNumberErrorMessage = errorMessage
            )
        }
    }
    
    /**
     * Validate birth date field
     */
    fun validateBirthDate(birthDate: String) {
        val isValid = birthDate.trim().isNotBlank()
        val errorMessage = if (!isValid) "Birth date is required" else ""
        
        updateFormState { currentState ->
            currentState.copy(
                birthDate = birthDate,
                isBirthDateError = !isValid,
                birthDateErrorMessage = errorMessage
            )
        }
    }
    
    /**
     * Validate sex field
     */
    fun validateSex(sex: String) {
        val isValid = sex.isNotBlank()
        val errorMessage = if (!isValid) "Sex is required" else ""
        
        updateFormState { currentState ->
            currentState.copy(
                sex = sex,
                isSexError = !isValid,
                sexErrorMessage = errorMessage
            )
        }
    }
    
    /**
     * Validate province field
     */
    fun validateProvince(province: String) {
        val isValid = province.isNotBlank()
        val errorMessage = if (!isValid) "Province is required" else ""
        
        updateFormState { currentState ->
            currentState.copy(
                province = province,
                isProvinceError = !isValid,
                provinceErrorMessage = errorMessage
            )
        }
    }
    
    /**
     * Validate municipality field
     */
    fun validateMunicipality(municipality: String) {
        val isValid = municipality.isNotBlank()
        val errorMessage = if (!isValid) "Municipality is required" else ""
        
        updateFormState { currentState ->
            currentState.copy(
                municipality = municipality,
                isMunicipalityError = !isValid,
                municipalityErrorMessage = errorMessage
            )
        }
    }
    
    /**
     * Validate additional address field (optional)
     */
    fun validateAdditionalAddress(additionalAddress: String) {
        // Additional address is optional, no validation needed
        updateFormState { currentState ->
            currentState.copy(
                additionalAddress = additionalAddress,
                isAdditionalAddressError = false,
                additionalAddressErrorMessage = ""
            )
        }
    }
    
    /**
     * Validate ID front image
     */
    fun validateIdFrontImage(imageUri: String) {
        val currentState = _uiState.value.formState
        val isValid = imageUri.isNotBlank() || currentState.idFrontImageUrl.isNotBlank()
        val errorMessage = if (!isValid) "Front ID image is required" else ""
        
        updateFormState { currentFormState ->
            currentFormState.copy(
                idFrontImageUri = imageUri,
                isIdFrontError = !isValid,
                idFrontErrorMessage = errorMessage
            )
        }
    }
    
    /**
     * Validate ID back image
     */
    fun validateIdBackImage(imageUri: String) {
        val currentState = _uiState.value.formState
        val isValid = imageUri.isNotBlank() || currentState.idBackImageUrl.isNotBlank()
        val errorMessage = if (!isValid) "Back ID image is required" else ""
        
        updateFormState { currentFormState ->
            currentFormState.copy(
                idBackImageUri = imageUri,
                isIdBackError = !isValid,
                idBackErrorMessage = errorMessage
            )
        }
    }
    
    /**
     * Validate terms and conditions agreement
     */
    fun validateTermsAgreement(agreed: Boolean) {
        val errorMessage = if (!agreed) "You must agree to the terms and conditions" else ""
        
        updateFormState { currentState ->
            currentState.copy(
                agreedToTerms = agreed,
                isTermsError = !agreed
            )
        }
    }
    
    /**
     * Validate privacy policy agreement
     */
    fun validatePrivacyAgreement(agreed: Boolean) {
        updateFormState { currentState ->
            currentState.copy(agreedToPrivacy = agreed)
        }
    }
    
    /**
     * Clear upload progress
     */
    fun clearUploadProgress() {
        _uiState.value = _uiState.value.copy(
            isUploadingImages = false,
            uploadProgress = 0f
        )
    }
    
    /**
     * Update upload progress
     */
    fun updateUploadProgress(progress: Float) {
        _uiState.value = _uiState.value.copy(
            uploadProgress = progress
        )
    }
    
    /**
     * Upload profile picture
     * Mag-upload ng profile picture
     */
    fun uploadProfilePicture(imageUri: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isSaving = true,
                    isUploadingImages = true,
                    errorMessage = null,
                    isSuccess = false,
                    uploadProgress = 0f
                )
                
                Log.d(TAG, "Uploading profile picture...")
                
                // Update progress
                updateUploadProgress(0.3f)
                
                val result = completeProfileRepository.uploadProfilePicture(imageUri)
                result.fold(
                    onSuccess = { imageUrl ->
                        Log.d(TAG, "Profile picture uploaded successfully: $imageUrl")
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isUploadingImages = false,
                            isSuccess = true,
                            uploadProgress = 1f
                        )
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error uploading profile picture", exception)
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            isUploadingImages = false,
                            isSuccess = false,
                            uploadProgress = 0f,
                            errorMessage = getErrorMessage(exception)
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error uploading profile picture", e)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isUploadingImages = false,
                    isSuccess = false,
                    uploadProgress = 0f,
                    errorMessage = "An unexpected error occurred while uploading profile picture. Please try again. / May naganap na hindi inaasahang error sa pag-upload ng profile picture. Pakisubukan ulit."
                )
            }
        }
    }
    
    /**
     * Validate form fields including terms, privacy policy, and ID images
     */
    private fun isFormValid(formState: CompleteProfileFormState): Boolean {
        return formState.firstName.trim().isNotBlank() &&
               formState.lastName.trim().isNotBlank() &&
               formState.birthDate.trim().isNotBlank() &&
               formState.sex.isNotBlank() &&
               formState.phoneNumber.isNotBlank() &&
               formState.province.isNotBlank() &&
               formState.municipality.isNotBlank() &&
               formState.barangay.isNotBlank() &&
               formState.agreedToTerms &&
               formState.agreedToPrivacy &&
               (formState.idFrontImageUri.isNotBlank() || formState.idFrontImageUrl.isNotBlank()) &&
               (formState.idBackImageUri.isNotBlank() || formState.idBackImageUrl.isNotBlank()) &&
               !formState.isFirstNameError &&
               !formState.isMiddleNameError &&
               !formState.isLastNameError &&
               !formState.isBirthDateError &&
               !formState.isSexError &&
               !formState.isPhoneNumberError &&
               !formState.isProvinceError &&
               !formState.isMunicipalityError &&
               !formState.isAdditionalAddressError &&
               !formState.isTermsError &&
               !formState.isIdFrontError &&
               !formState.isIdBackError
    }
    
    /**
     * Get user-friendly error message
     * Kumuha ng user-friendly na error message
     */
    private fun getErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("network", ignoreCase = true) == true -> 
                "Network error. Please check your internet connection and try again. / May problema sa network. Pakisuri ang inyong internet connection at subukan ulit."
            exception.message?.contains("permission", ignoreCase = true) == true -> 
                "Permission denied. Please try logging in again. / Walang pahintulot. Pakisubukan ang pag-login ulit."
            exception.message?.contains("not authenticated", ignoreCase = true) == true -> 
                "Session expired. Please log in again. / Nag-expire na ang session. Pakisubukan ang pag-login ulit."
            exception.message?.contains("upload", ignoreCase = true) == true -> 
                "Failed to upload images. Please check your internet connection and try again. / Hindi nai-upload ang mga larawan. Pakisuri ang internet connection at subukan ulit."
            exception.message?.contains("storage", ignoreCase = true) == true -> 
                "Storage error occurred. Please try again later. / May problema sa storage. Pakisubukan ulit mamaya."
            exception.message?.contains("validation", ignoreCase = true) == true -> 
                "Please check all required fields and try again. / Pakisuri ang lahat ng required fields at subukan ulit."
            else -> "Failed to save profile. Please try again. / Hindi na-save ang profile. Pakisubukan ulit."
        }
    }
}
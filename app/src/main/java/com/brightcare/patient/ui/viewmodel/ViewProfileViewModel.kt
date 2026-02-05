package com.brightcare.patient.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.model.ChiropractorProfileModel
import com.brightcare.patient.data.model.ProfileValidationResult
import com.brightcare.patient.data.model.Review
import com.brightcare.patient.data.repository.ChiropractorProfileRepository
import com.brightcare.patient.data.repository.ProfileValidationService
import com.brightcare.patient.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for ViewProfileScreen
 * ViewModel para sa ViewProfileScreen
 */
@HiltViewModel
class ViewProfileViewModel @Inject constructor(
    private val chiropractorProfileRepository: ChiropractorProfileRepository,
    private val profileValidationService: ProfileValidationService,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ViewProfileViewModel"
    }

    // UI State
    private val _uiState = MutableStateFlow(ViewProfileUiState())
    val uiState: StateFlow<ViewProfileUiState> = _uiState.asStateFlow()
    
    // Reviews state
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()
    
    // Selected tab state
    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()
    
    // Tab loading states
    private val _tabLoadingStates = MutableStateFlow(
        mapOf(
            0 to false, // Overview
            1 to false, // Education
            2 to false, // Experience
            3 to false, // Credentials
            4 to false, // Others
            5 to false  // Reviews
        )
    )
    val tabLoadingStates: StateFlow<Map<Int, Boolean>> = _tabLoadingStates.asStateFlow()
    
    // Profile validation state
    private val _profileValidation = MutableStateFlow(ProfileValidationResult())
    val profileValidation: StateFlow<ProfileValidationResult> = _profileValidation.asStateFlow()
    
    private val _showProfileIncompleteDialog = MutableStateFlow(false)
    val showProfileIncompleteDialog: StateFlow<Boolean> = _showProfileIncompleteDialog.asStateFlow()
    
    private val _isValidatingProfile = MutableStateFlow(false)
    val isValidatingProfile: StateFlow<Boolean> = _isValidatingProfile.asStateFlow()
    
    private val _shouldNavigateToBooking = MutableStateFlow<String?>(null)
    val shouldNavigateToBooking: StateFlow<String?> = _shouldNavigateToBooking.asStateFlow()

    /**
     * Load chiropractor profile by ID
     * I-load ang chiropractor profile gamit ang ID
     */
    fun loadChiropractorProfile(chiropractorId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            chiropractorProfileRepository.getChiropractorProfile(chiropractorId)
                .catch { exception ->
                    Log.e(TAG, "Error loading chiropractor profile", exception)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load chiropractor profile: ${exception.message}"
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { chiropractor ->
                            if (chiropractor != null) {
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false,
                                        chiropractor = chiropractor,
                                        errorMessage = null
                                    )
                                }
                                Log.d(TAG, "Successfully loaded chiropractor profile: ${chiropractor.name}")
                            } else {
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = "Chiropractor profile not found"
                                    )
                                }
                                Log.w(TAG, "Chiropractor profile not found for ID: $chiropractorId")
                            }
                        },
                        onFailure = { exception ->
                            Log.e(TAG, "Failed to load chiropractor profile", exception)
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Failed to load chiropractor profile: ${exception.message}"
                                )
                            }
                        }
                    )
                }
        }
    }

    /**
     * Clear error message
     * I-clear ang error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Refresh chiropractor profile
     * I-refresh ang chiropractor profile
     */
    fun refreshProfile(chiropractorId: String) {
        loadChiropractorProfile(chiropractorId)
    }
    
    /**
     * Set selected tab index
     * I-set ang selected tab index
     */
    fun setSelectedTab(tabIndex: Int) {
        _selectedTabIndex.value = tabIndex
    }
    
    /**
     * Load specific tab data
     * I-load ang specific tab data
     */
    fun loadTabData(chiropractorId: String, tabIndex: Int) {
        if (_uiState.value.chiropractor == null) {
            // If no basic profile data, load it first
            loadChiropractorProfile(chiropractorId)
            return
        }
        
        viewModelScope.launch {
            // Set tab loading state
            _tabLoadingStates.update { states ->
                states.toMutableMap().apply { this[tabIndex] = true }
            }
            
            try {
                // Simulate tab-specific data loading
                // In a real implementation, you might have separate endpoints for each tab
                chiropractorProfileRepository.getChiropractorProfile(chiropractorId)
                    .catch { exception ->
                        Log.e(TAG, "Error loading tab $tabIndex data", exception)
                        // Handle tab-specific error if needed
                    }
                    .collect { result ->
                        result.fold(
                            onSuccess = { chiropractor ->
                                if (chiropractor != null) {
                                    _uiState.update { 
                                        it.copy(chiropractor = chiropractor)
                                    }
                                    Log.d(TAG, "Successfully loaded tab $tabIndex data")
                                }
                            },
                            onFailure = { exception ->
                                Log.e(TAG, "Failed to load tab $tabIndex data", exception)
                            }
                        )
                        
                        // Clear tab loading state
                        _tabLoadingStates.update { states ->
                            states.toMutableMap().apply { this[tabIndex] = false }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadTabData", e)
                _tabLoadingStates.update { states ->
                    states.toMutableMap().apply { this[tabIndex] = false }
                }
            }
        }
    }
    
    /**
     * Check if tab has data loaded
     * I-check kung may loaded data ang tab
     */
    fun hasTabData(tabIndex: Int): Boolean {
        val chiropractor = _uiState.value.chiropractor ?: return false
        
        return when (tabIndex) {
            0 -> true // Overview always has basic data
            1 -> chiropractor.education.isNotEmpty()
            2 -> chiropractor.experienceHistory.isNotEmpty()
            3 -> chiropractor.professionalCredentials.isNotEmpty()
            4 -> chiropractor.others.isNotEmpty()
            5 -> _reviews.value.isNotEmpty() // Reviews tab
            else -> false
        }
    }
    
    /**
     * Get tab data count
     * Kunin ang bilang ng data sa tab
     */
    fun getTabDataCount(tabIndex: Int): Int {
        val chiropractor = _uiState.value.chiropractor ?: return 0
        
        return when (tabIndex) {
            0 -> 3 // Overview has 3 sections (About, Contact, Professional)
            1 -> chiropractor.education.size
            2 -> chiropractor.experienceHistory.size
            3 -> chiropractor.professionalCredentials.size
            4 -> chiropractor.others.size
            5 -> _reviews.value.size // Reviews tab
            else -> 0
        }
    }
    
    /**
     * Load reviews for chiropractor
     * I-load ang mga reviews para sa chiropractor
     */
    fun loadReviews(chiropractorId: String) {
        viewModelScope.launch {
            _tabLoadingStates.update { states ->
                states.toMutableMap().apply { this[5] = true }
            }
            
            try {
                val result = reviewRepository.getChiropractorReviews(chiropractorId)
                result.fold(
                    onSuccess = { reviewsList ->
                        _reviews.value = reviewsList
                        Log.d(TAG, "Successfully loaded ${reviewsList.size} reviews")
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to load reviews", exception)
                        _reviews.value = emptyList()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading reviews", e)
                _reviews.value = emptyList()
            } finally {
                _tabLoadingStates.update { states ->
                    states.toMutableMap().apply { this[5] = false }
                }
            }
        }
    }
    
    /**
     * Validate profile for booking
     */
    fun validateProfileForBooking(chiropractorId: String) {
        viewModelScope.launch {
            try {
                _isValidatingProfile.value = true
                
                val validation = profileValidationService.validateProfileForBooking()
                
                _profileValidation.value = validation
                _showProfileIncompleteDialog.value = !validation.isValid
                
                // Trigger navigation if profile is valid
                if (validation.isValid) {
                    _shouldNavigateToBooking.value = chiropractorId
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error validating profile", e)
                _profileValidation.value = ProfileValidationResult(
                    isValid = false,
                    errorMessage = "Unable to validate profile. Please try again."
                )
                _showProfileIncompleteDialog.value = true
            } finally {
                _isValidatingProfile.value = false
            }
        }
    }
    
    /**
     * Hide profile incomplete dialog
     */
    fun hideProfileIncompleteDialog() {
        _showProfileIncompleteDialog.value = false
    }
    
    /**
     * Clear navigation trigger
     */
    fun clearNavigationTrigger() {
        _shouldNavigateToBooking.value = null
    }
}

/**
 * UI State for ViewProfileScreen
 * UI State para sa ViewProfileScreen
 */
data class ViewProfileUiState(
    val isLoading: Boolean = false,
    val chiropractor: ChiropractorProfileModel? = null,
    val errorMessage: String? = null,
    val profileValidation: ProfileValidationResult = ProfileValidationResult(),
    val showProfileIncompleteDialog: Boolean = false,
    val isValidatingProfile: Boolean = false
) {
    val hasError: Boolean get() = errorMessage != null
    val hasData: Boolean get() = chiropractor != null && !isLoading
}

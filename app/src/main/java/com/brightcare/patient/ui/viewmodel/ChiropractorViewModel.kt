package com.brightcare.patient.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.repository.ChiropractorRepository
import com.brightcare.patient.data.repository.ProfileValidationService
import com.brightcare.patient.data.model.ProfileValidationResult
import com.brightcare.patient.ui.component.chirocomponents.ChiropractorInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing chiropractor data and UI state
 * Handles data fetching from repository and provides state to UI
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class ChiropractorViewModel @Inject constructor(
    private val chiropractorRepository: ChiropractorRepository,
    private val profileValidationService: ProfileValidationService
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(ChiropractorUiState())
    val uiState: StateFlow<ChiropractorUiState> = _uiState.asStateFlow()
    
    // All chiropractors (unfiltered)
    private val _allChiropractors = MutableStateFlow<List<ChiropractorInfo>>(emptyList())
    
    // Filtered chiropractors list (displayed to UI)
    private val _chiropractors = MutableStateFlow<List<ChiropractorInfo>>(emptyList())
    val chiropractors: StateFlow<List<ChiropractorInfo>> = _chiropractors.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Search and filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _showAvailableOnly = MutableStateFlow(false)
    val showAvailableOnly: StateFlow<Boolean> = _showAvailableOnly.asStateFlow()
    
    private val _showNearMeOnly = MutableStateFlow(false)
    val showNearMeOnly: StateFlow<Boolean> = _showNearMeOnly.asStateFlow()
    
    // Profile validation state
    private val _profileValidation = MutableStateFlow(ProfileValidationResult())
    val profileValidation: StateFlow<ProfileValidationResult> = _profileValidation.asStateFlow()
    
    private val _showProfileIncompleteDialog = MutableStateFlow(false)
    val showProfileIncompleteDialog: StateFlow<Boolean> = _showProfileIncompleteDialog.asStateFlow()
    
    private val _isValidatingProfile = MutableStateFlow(false)
    val isValidatingProfile: StateFlow<Boolean> = _isValidatingProfile.asStateFlow()
    
    private val _shouldNavigateToBooking = MutableStateFlow(false)
    val shouldNavigateToBooking: StateFlow<Boolean> = _shouldNavigateToBooking.asStateFlow()
    
    private val _selectedChiropractorId = MutableStateFlow<String?>(null)
    val selectedChiropractorId: StateFlow<String?> = _selectedChiropractorId.asStateFlow()
    
    init {
        loadChiropractors()
        setupSearch()
    }
    
    /**
     * Load all chiropractors from Firestore
     */
    fun loadChiropractors() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            chiropractorRepository.getAllChiropractors()
                .onSuccess { chiropractorsList ->
                    _allChiropractors.value = chiropractorsList
                    _chiropractors.value = chiropractorsList
                    _uiState.value = _uiState.value.copy(
                        chiropractors = chiropractorsList,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _errorMessage.value = "Failed to load chiropractors: ${exception.message}"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Setup real-time search functionality
     * I-setup ang real-time search functionality
     */
    private fun setupSearch() {
        _searchQuery
            .debounce(300) // Wait 300ms after user stops typing
            .distinctUntilChanged()
            .combine(_allChiropractors) { query, allChiropractors ->
                if (query.isBlank()) {
                    allChiropractors
                } else {
                    allChiropractors.filter { chiropractor ->
                        chiropractor.name.contains(query, ignoreCase = true) ||
                        chiropractor.specialization.contains(query, ignoreCase = true) ||
                        chiropractor.location.contains(query, ignoreCase = true)
                    }
                }
            }
            .onEach { filteredList ->
                _chiropractors.value = filteredList
                _uiState.value = _uiState.value.copy(
                    chiropractors = filteredList
                )
            }
            .launchIn(viewModelScope)
    }
    
    /**
     * Update search query
     * I-update ang search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Clear search
     * I-clear ang search
     */
    fun clearSearch() {
        _searchQuery.value = ""
    }

    /**
     * Search chiropractors by specialization
     */
    fun searchBySpecialization(specialization: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            chiropractorRepository.searchBySpecialization(specialization)
                .onSuccess { chiropractorsList ->
                    _chiropractors.value = chiropractorsList
                    _uiState.value = _uiState.value.copy(
                        chiropractors = chiropractorsList,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _errorMessage.value = "Search failed: ${exception.message}"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Filter to show only available chiropractors
     */
    fun toggleAvailableFilter() {
        _showAvailableOnly.value = !_showAvailableOnly.value
        applyFilters()
    }
    
    /**
     * Filter to show chiropractors near user location
     */
    fun toggleNearMeFilter() {
        _showNearMeOnly.value = !_showNearMeOnly.value
        applyFilters()
    }
    
    /**
     * Apply current filters to the chiropractors list
     */
    private fun applyFilters() {
        viewModelScope.launch {
            var filteredList = _allChiropractors.value
            
            // Apply search query filter
            val query = _searchQuery.value
            if (query.isNotBlank()) {
                filteredList = filteredList.filter { chiropractor ->
                    chiropractor.name.contains(query, ignoreCase = true) ||
                    chiropractor.specialization.contains(query, ignoreCase = true) ||
                    chiropractor.location.contains(query, ignoreCase = true)
                }
            }
            
            // Apply available filter
            if (_showAvailableOnly.value) {
                filteredList = filteredList.filter { it.isAvailable }
            }
            
            // Apply near me filter (dummy implementation - filter by common locations)
            if (_showNearMeOnly.value) {
                val nearbyLocations = listOf("Makati City", "Quezon City", "Manila", "Pasig City")
                filteredList = filteredList.filter { it.location in nearbyLocations }
            }
            
            _chiropractors.value = filteredList
            _uiState.value = _uiState.value.copy(
                chiropractors = filteredList
            )
        }
    }
    
    /**
     * Get chiropractor by ID
     */
    fun getChiropractorById(id: String, callback: (ChiropractorInfo?) -> Unit) {
        viewModelScope.launch {
            chiropractorRepository.getChiropractorById(id)
                .onSuccess { chiropractor ->
                    callback(chiropractor)
                }
                .onFailure {
                    callback(null)
                }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Refresh data
     */
    fun refresh() {
        loadChiropractors()
    }
    
    /**
     * Validate profile for booking
     */
    fun validateProfileForBooking(chiropractorId: String) {
        viewModelScope.launch {
            try {
                _isValidatingProfile.value = true
                
                // Store the chiropractor ID for later use
                _selectedChiropractorId.value = chiropractorId
                
                val validation = profileValidationService.validateProfileForBooking()
                
                _profileValidation.value = validation
                _showProfileIncompleteDialog.value = !validation.isValid
                
                // Trigger navigation if profile is valid
                if (validation.isValid) {
                    _shouldNavigateToBooking.value = true
                }
                
            } catch (e: Exception) {
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
     * Re-validate profile for booking using stored chiropractor ID
     */
    fun revalidateProfileForBooking() {
        val chiropractorId = _selectedChiropractorId.value
        if (chiropractorId != null) {
            validateProfileForBooking(chiropractorId)
        }
    }
    
    /**
     * Clear navigation trigger
     */
    fun clearNavigationTrigger() {
        _shouldNavigateToBooking.value = false
        _selectedChiropractorId.value = null
    }
}

/**
 * UI State data class for chiropractor screen
 */
data class ChiropractorUiState(
    val chiropractors: List<ChiropractorInfo> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val showAvailableOnly: Boolean = false,
    val showNearMeOnly: Boolean = false,
    val profileValidation: ProfileValidationResult = ProfileValidationResult(),
    val showProfileIncompleteDialog: Boolean = false,
    val isValidatingProfile: Boolean = false
)




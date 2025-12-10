package com.brightcare.patient.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.model.ChiropractorProfileModel
import com.brightcare.patient.data.repository.ChiropractorProfileRepository
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
    private val chiropractorProfileRepository: ChiropractorProfileRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ViewProfileViewModel"
    }

    // UI State
    private val _uiState = MutableStateFlow(ViewProfileUiState())
    val uiState: StateFlow<ViewProfileUiState> = _uiState.asStateFlow()
    
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
            4 to false  // Others
        )
    )
    val tabLoadingStates: StateFlow<Map<Int, Boolean>> = _tabLoadingStates.asStateFlow()

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
            else -> 0
        }
    }
}

/**
 * UI State for ViewProfileScreen
 * UI State para sa ViewProfileScreen
 */
data class ViewProfileUiState(
    val isLoading: Boolean = false,
    val chiropractor: ChiropractorProfileModel? = null,
    val errorMessage: String? = null
) {
    val hasError: Boolean get() = errorMessage != null
    val hasData: Boolean get() = chiropractor != null && !isLoading
}

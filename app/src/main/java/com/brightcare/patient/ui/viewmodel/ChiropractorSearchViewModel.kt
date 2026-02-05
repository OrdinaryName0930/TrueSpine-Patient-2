package com.brightcare.patient.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.model.Chiropractor
import com.brightcare.patient.domain.usecase.ChiropractorSearchUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for chiropractor search functionality
 * ViewModel para sa chiropractor search functionality
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class ChiropractorSearchViewModel @Inject constructor(
    private val searchUseCases: ChiropractorSearchUseCases
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(ChiropractorSearchUiState())
    val uiState: StateFlow<ChiropractorSearchUiState> = _uiState.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Search results
    private val _searchResults = MutableStateFlow<List<Chiropractor>>(emptyList())
    val searchResults: StateFlow<List<Chiropractor>> = _searchResults.asStateFlow()

    // Top rated chiropractors
    private val _topRatedChiropractors = MutableStateFlow<List<Chiropractor>>(emptyList())
    val topRatedChiropractors: StateFlow<List<Chiropractor>> = _topRatedChiropractors.asStateFlow()

    // Available specializations
    private val _specializations = MutableStateFlow<List<String>>(emptyList())
    val specializations: StateFlow<List<String>> = _specializations.asStateFlow()

    // Selected specialization filter
    private val _selectedSpecialization = MutableStateFlow<String?>(null)
    val selectedSpecialization: StateFlow<String?> = _selectedSpecialization.asStateFlow()

    init {
        // Load initial data
        loadInitialData()
        
        // Setup real-time search
        setupRealTimeSearch()
    }

    /**
     * Load initial data
     * I-load ang initial data
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Load all active chiropractors initially
                searchUseCases.getAllActiveChiropractors()
                    .onSuccess { chiropractors ->
                        _searchResults.value = chiropractors
                    }
                    .onFailure { exception ->
                        _uiState.update { 
                            it.copy(error = exception.message)
                        }
                    }

                // Load top rated chiropractors
                searchUseCases.getTopRatedChiropractors()
                    .onSuccess { chiropractors ->
                        _topRatedChiropractors.value = chiropractors
                    }

                // Load available specializations
                searchUseCases.getAvailableSpecializations()
                    .onSuccess { specs ->
                        _specializations.value = specs
                    }

            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message)
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Setup real-time search
     * I-setup ang real-time search
     */
    private fun setupRealTimeSearch() {
        _searchQuery
            .debounce(300) // Wait 300ms after user stops typing
            .distinctUntilChanged()
            .onEach { query ->
                _uiState.update { it.copy(isSearching = true) }
                performSearch(query)
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
     * Perform search
     * Magsagawa ng search
     */
    private fun performSearch(query: String) {
        viewModelScope.launch {
            try {
                val selectedSpec = _selectedSpecialization.value
                
                val result = if (selectedSpec != null) {
                    // Search within selected specialization
                    if (query.isBlank()) {
                        searchUseCases.getChiropractorsBySpecialization(selectedSpec)
                    } else {
                        // Search by name within specialization
                        searchUseCases.searchChiropractors(query)
                            .map { chiropractors ->
                                chiropractors.filter { it.specialization == selectedSpec }
                            }
                    }
                } else {
                    // General search
                    if (query.isBlank()) {
                        searchUseCases.getAllActiveChiropractors()
                    } else {
                        searchUseCases.searchChiropractors(query)
                    }
                }

                result.onSuccess { chiropractors ->
                    _searchResults.value = chiropractors
                }.onFailure { exception ->
                    _uiState.update { 
                        it.copy(error = exception.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message)
                }
            } finally {
                _uiState.update { it.copy(isSearching = false) }
            }
        }
    }

    /**
     * Select specialization filter
     * Pumili ng specialization filter
     */
    fun selectSpecialization(specialization: String?) {
        _selectedSpecialization.value = specialization
        // Trigger search with current query
        performSearch(_searchQuery.value)
    }

    /**
     * Clear search
     * I-clear ang search
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _selectedSpecialization.value = null
        loadInitialData()
    }

    /**
     * Refresh data
     * I-refresh ang data
     */
    fun refreshData() {
        loadInitialData()
    }

    /**
     * Get chiropractor by ID
     * Kunin ang chiropractor gamit ang ID
     */
    fun getChiropractorById(chiropractorId: String, callback: (Chiropractor?) -> Unit) {
        viewModelScope.launch {
            searchUseCases.getChiropractorById(chiropractorId)
                .onSuccess { chiropractor ->
                    callback(chiropractor)
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(error = exception.message)
                    }
                    callback(null)
                }
        }
    }

    /**
     * Clear error message
     * I-clear ang error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Check if chiropractor is in search results
     * I-check kung ang chiropractor ay nasa search results
     */
    fun isChiropractorInResults(chiropractorId: String): Boolean {
        return _searchResults.value.any { it.id == chiropractorId }
    }

    /**
     * Get filtered results count
     * Kunin ang bilang ng filtered results
     */
    fun getFilteredResultsCount(): Int {
        return _searchResults.value.size
    }

    /**
     * Get search suggestions based on current query
     * Kunin ang search suggestions base sa current query
     */
    fun getSearchSuggestions(): List<String> {
        val query = _searchQuery.value.lowercase()
        if (query.isBlank()) return emptyList()

        val suggestions = mutableSetOf<String>()
        
        // Add matching chiropractor names
        _searchResults.value.forEach { chiropractor ->
            if (chiropractor.name.lowercase().contains(query)) {
                suggestions.add(chiropractor.name)
            }
        }
        
        // Add matching specializations
        _specializations.value.forEach { specialization ->
            if (specialization.lowercase().contains(query)) {
                suggestions.add(specialization)
            }
        }
        
        return suggestions.take(5).toList()
    }
}

/**
 * UI State for chiropractor search screen
 * UI State para sa chiropractor search screen
 */
data class ChiropractorSearchUiState(
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null,
    val hasResults: Boolean = true
)
















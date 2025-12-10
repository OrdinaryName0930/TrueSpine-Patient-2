package com.brightcare.patient.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.model.ConversationDisplay
import com.brightcare.patient.data.model.User
import com.brightcare.patient.data.repository.ConversationRepository
import com.brightcare.patient.domain.usecase.MarkConversationAsReadUseCase
import com.brightcare.patient.domain.usecase.MarkConversationAsReadSetToZeroUseCase
import com.brightcare.patient.domain.usecase.ImmediateSetUnreadCountToZeroUseCase
import com.brightcare.patient.domain.usecase.ComprehensiveMarkAsReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for conversation list with chiropractor search
 * ViewModel para sa conversation list na may chiropractor search
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val repository: ConversationRepository,
    private val markConversationAsReadUseCase: MarkConversationAsReadUseCase,
    private val markConversationAsReadSetToZeroUseCase: MarkConversationAsReadSetToZeroUseCase,
    private val immediateSetUnreadCountToZeroUseCase: ImmediateSetUnreadCountToZeroUseCase,
    private val comprehensiveMarkAsReadUseCase: ComprehensiveMarkAsReadUseCase
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(ConversationListUiState())
    val uiState: StateFlow<ConversationListUiState> = _uiState.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // All chiropractors
    private val _allChiropractors = MutableStateFlow<List<User>>(emptyList())
    val allChiropractors: StateFlow<List<User>> = _allChiropractors.asStateFlow()

    // Filtered chiropractors based on search
    private val _filteredChiropractors = MutableStateFlow<List<User>>(emptyList())
    val filteredChiropractors: StateFlow<List<User>> = _filteredChiropractors.asStateFlow()

    // Existing conversations
    private val _conversations = MutableStateFlow<List<ConversationDisplay>>(emptyList())
    val conversations: StateFlow<List<ConversationDisplay>> = _conversations.asStateFlow()

    init {
        loadCombinedData()
        setupSearch()
    }

    /**
     * Load combined chiropractors and conversations data efficiently
     * I-load ang combined chiropractors at conversations data nang efficient
     */
    private fun loadCombinedData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                repository.getCombinedChiropractorsAndConversations()
                    .catch { exception ->
                        val errorMessage = when {
                            exception.message?.contains("FAILED_PRECONDITION") == true -> 
                                "Setting up database... Please try again in a moment."
                            exception.message?.contains("index") == true -> 
                                "Database is being configured. Please wait a moment and try again."
                            exception.message?.contains("permission-denied") == true ->
                                "Access denied. Please check your authentication."
                            else -> exception.message ?: "Failed to load data"
                        }
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = errorMessage
                            )
                        }
                    }
                    .collect { (chiropractors, conversations) ->
                        _allChiropractors.value = chiropractors
                        _conversations.value = conversations
                        _uiState.update { it.copy(isLoading = false, error = null) }
                    }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("FAILED_PRECONDITION") == true -> 
                        "Setting up database... Please try again in a moment."
                    e.message?.contains("index") == true -> 
                        "Database is being configured. Please wait a moment and try again."
                    e.message?.contains("permission-denied") == true ->
                        "Access denied. Please check your authentication."
                    else -> e.message ?: "Failed to load data"
                }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            }
        }
    }

    /**
     * Setup real-time search
     * I-setup ang real-time search
     */
    private fun setupSearch() {
        _searchQuery
            .debounce(300) // Wait 300ms after user stops typing
            .distinctUntilChanged()
            .combine(_allChiropractors) { query, chiropractors ->
                if (query.isBlank()) {
                    chiropractors
                } else {
                    chiropractors.filter { chiropractor ->
                        chiropractor.fullName.contains(query, ignoreCase = true) ||
                        chiropractor.specialization?.contains(query, ignoreCase = true) == true
                    }
                }
            }
            .onEach { filtered ->
                _filteredChiropractors.value = filtered
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
     * Get or create conversation with chiropractor
     * Kunin o gumawa ng conversation sa chiropractor
     */
    fun getOrCreateConversation(
        chiropractorId: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingConversation = true) }
            
            repository.getOrCreateConversation(chiropractorId)
                .onSuccess { conversation ->
                    _uiState.update { it.copy(isCreatingConversation = false) }
                    onSuccess(conversation.id)
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isCreatingConversation = false,
                            error = exception.message
                        )
                    }
                    onError(exception.message ?: "Failed to create conversation")
                }
        }
    }

    /**
     * Refresh data
     * I-refresh ang data
     */
    fun refreshData() {
        loadCombinedData()
    }

    /**
     * Clear error
     * I-clear ang error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Check if chiropractor has existing conversation
     * I-check kung ang chiropractor ay may existing conversation
     */
    fun hasExistingConversation(chiropractorId: String): Boolean {
        return _conversations.value.any { it.chiropractor.uid == chiropractorId }
    }

    /**
     * Get conversation for chiropractor
     * Kunin ang conversation para sa chiropractor
     */
    fun getConversationForChiropractor(chiropractorId: String): ConversationDisplay? {
        return _conversations.value.find { it.chiropractor.uid == chiropractorId }
    }

    /**
     * Mark conversation as read when card is clicked - IMMEDIATE AND RELIABLE
     * I-mark ang conversation bilang nabasa kapag na-click ang card - AGAD AT RELIABLE
     */
    fun markConversationAsReadOnClick(conversationId: String) {
        // Only mark existing conversations as read, not new ones
        // I-mark lang ang existing conversations bilang nabasa, hindi ang mga bago
        if (!conversationId.startsWith("new_")) {
            viewModelScope.launch {
                try {
                    println("🚀 IMMEDIATE: Marking conversation as read from card click: $conversationId")
                    
                    // Use the most reliable method first - immediate set to 0
                    // Gamitin ang pinaka reliable na paraan muna - agad i-set sa 0
                    val immediateResult = immediateSetUnreadCountToZeroUseCase(conversationId)
                    immediateResult.onSuccess {
                        println("✅ IMMEDIATE SUCCESS: Unread count set to 0 from card click: $conversationId")
                        
                        // Then do comprehensive mark as read for completeness
                        // Tapos gawin ang comprehensive mark as read para kumpleto
                        comprehensiveMarkAsReadInBackground(conversationId)
                    }.onFailure { exception ->
                        println("❌ IMMEDIATE FAILED: ${exception.message}")
                        // Fallback to old method
                        fallbackMarkAsRead(conversationId)
                    }
                } catch (e: Exception) {
                    println("❌ IMMEDIATE EXCEPTION: ${e.message}")
                    // Fallback to old method
                    fallbackMarkAsRead(conversationId)
                }
            }
        }
    }

    /**
     * Comprehensive mark as read in background (doesn't block UI)
     * Comprehensive mark as read sa background (hindi naka-block ang UI)
     */
    private fun comprehensiveMarkAsReadInBackground(conversationId: String) {
        viewModelScope.launch {
            try {
                println("🔄 BACKGROUND: Running comprehensive mark as read: $conversationId")
                comprehensiveMarkAsReadUseCase(conversationId)
                println("✅ BACKGROUND: Comprehensive mark as read completed: $conversationId")
            } catch (e: Exception) {
                println("⚠️ BACKGROUND: Comprehensive mark as read failed (but UI already updated): ${e.message}")
            }
        }
    }

    /**
     * Fallback method using old approach
     * Fallback method gamit ang lumang paraan
     */
    private fun fallbackMarkAsRead(conversationId: String) {
        viewModelScope.launch {
            try {
                println("🔄 FALLBACK: Using old mark as read method: $conversationId")
                
                val markAsReadResult = markConversationAsReadUseCase(conversationId)
                markAsReadResult.onSuccess {
                    println("✅ FALLBACK SUCCESS: Old method worked: $conversationId")
                }.onFailure { exception ->
                    println("❌ FALLBACK FAILED: ${exception.message}")
                    // Last resort - set to zero
                    markConversationAsReadSetToZero(conversationId)
                }
            } catch (e: Exception) {
                println("❌ FALLBACK EXCEPTION: ${e.message}")
                markConversationAsReadSetToZero(conversationId)
            }
        }
    }

    /**
     * Alternative: Mark conversation as read by setting unread count to 0
     * Alternative: Markahan ang conversation bilang nabasa sa pamamagitan ng pag-set ng unread count sa 0
     */
    private fun markConversationAsReadSetToZero(conversationId: String) {
        viewModelScope.launch {
            try {
                println("🔄 Trying alternative approach: setting unread count to 0 for: $conversationId")
                
                val result = markConversationAsReadSetToZeroUseCase(conversationId)
                result.onSuccess {
                    println("✅ Conversation marked as read (set to 0) successfully from card click: $conversationId")
                }.onFailure { exception ->
                    println("❌ Failed to mark conversation as read (set to 0) from card click: ${exception.message}")
                }
            } catch (e: Exception) {
                println("❌ Exception while marking conversation as read (set to 0) from card click: ${e.message}")
            }
        }
    }

    /**
     * Get chiropractors to display (all chiropractors with conversation status)
     * Kunin ang mga chiropractor na ipapakita (lahat ng chiropractor na may conversation status)
     */
    fun getDisplayChiropractors(): StateFlow<List<ChiropractorDisplayItem>> {
        return combine(
            _filteredChiropractors,
            _conversations,
            _searchQuery
        ) { filtered, conversations, query ->
            // Show ALL chiropractors, mark which ones have conversations
            // Ipakita ang LAHAT ng chiropractor, markahan kung sino ang may conversation
            filtered.map { chiropractor ->
                val existingConversation = conversations.find { it.chiropractor.uid == chiropractor.uid }
                
                ChiropractorDisplayItem(
                    chiropractor = chiropractor,
                    hasConversation = existingConversation != null,
                    conversationId = existingConversation?.conversationId,
                    lastMessage = existingConversation?.lastMessage,
                    lastMessageTime = existingConversation?.lastMessageTime,
                    unreadCount = existingConversation?.unreadCount ?: 0
                )
            }.sortedWith(
                // Sort by: 1) Has conversation (existing chats first), 2) Last message time, 3) Name
                // I-sort ayon sa: 1) May conversation (existing chats muna), 2) Oras ng huling mensahe, 3) Pangalan
                compareByDescending<ChiropractorDisplayItem> { it.hasConversation }
                    .thenByDescending { it.lastMessageTime ?: java.util.Date(0) }
                    .thenBy { it.chiropractor.fullName }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
}

/**
 * UI State for conversation list
 * UI State para sa conversation list
 */
data class ConversationListUiState(
    val isLoading: Boolean = false,
    val isCreatingConversation: Boolean = false,
    val error: String? = null
)

/**
 * Display item for chiropractor in the list
 * Display item para sa chiropractor sa list
 */
data class ChiropractorDisplayItem(
    val chiropractor: User,
    val hasConversation: Boolean,
    val conversationId: String? = null,
    val lastMessage: String? = null,
    val lastMessageTime: java.util.Date? = null,
    val unreadCount: Int = 0
)

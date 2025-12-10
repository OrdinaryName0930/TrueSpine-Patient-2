package com.brightcare.patient.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.model.Conversation
import com.brightcare.patient.domain.usecase.MessagingUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for conversations list
 * ViewModel para sa listahan ng mga conversation
 */
@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val messagingUseCases: MessagingUseCases
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    // Conversations list
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    init {
        loadConversations()
    }

    /**
     * Load conversations
     * I-load ang mga conversation
     */
    private fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            messagingUseCases.getConversations()
                .catch { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                }
                .collect { conversationList ->
                    _conversations.value = conversationList
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    /**
     * Refresh conversations
     * I-refresh ang mga conversation
     */
    fun refreshConversations() {
        loadConversations()
    }

    /**
     * Clear error message
     * I-clear ang error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Format conversation timestamp
     * I-format ang conversation timestamp
     */
    fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
        return messagingUseCases.formatMessageTimestamp(timestamp)
    }

    /**
     * Get total unread count
     * Kunin ang kabuuang unread count
     */
    fun getTotalUnreadCount(): Int {
        return _conversations.value.sumOf { conversation ->
            conversation.unreadCounts.values.sum()
        }
    }
}

/**
 * UI State for conversations screen
 * UI State para sa conversations screen
 */
data class ConversationsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)








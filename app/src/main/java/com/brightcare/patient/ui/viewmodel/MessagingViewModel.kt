package com.brightcare.patient.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.model.*
import com.brightcare.patient.domain.usecase.MessagingUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for messaging functionality
 * ViewModel para sa messaging functionality
 */
@HiltViewModel
class MessagingViewModel @Inject constructor(
    private val messagingUseCases: MessagingUseCases
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(MessagingUiState())
    val uiState: StateFlow<MessagingUiState> = _uiState.asStateFlow()

    // Current conversation
    private val _currentConversation = MutableStateFlow<Conversation?>(null)
    val currentConversation: StateFlow<Conversation?> = _currentConversation.asStateFlow()

    // Messages for current conversation
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // Message input text
    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    // File upload progress
    private val _uploadProgress = MutableStateFlow<Map<String, UploadProgress>>(emptyMap())
    val uploadProgress: StateFlow<Map<String, UploadProgress>> = _uploadProgress.asStateFlow()

    // Assigned chiropractor
    private val _assignedChiropractor = MutableStateFlow<Chiropractor?>(null)
    val assignedChiropractor: StateFlow<Chiropractor?> = _assignedChiropractor.asStateFlow()

    init {
        loadAssignedChiropractor()
    }

    /**
     * Load assigned chiropractor
     * I-load ang assigned chiropractor
     */
    private fun loadAssignedChiropractor() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            messagingUseCases.getAssignedChiropractor()
                .onSuccess { chiropractor ->
                    _assignedChiropractor.value = chiropractor
                    chiropractor?.let { 
                        createOrFindConversation(it.id)
                    }
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                }
        }
    }

    /**
     * Create or find conversation with chiropractor
     * Gumawa o hanapin ang conversation sa chiropractor
     */
    private fun createOrFindConversation(chiropractorId: String) {
        viewModelScope.launch {
            messagingUseCases.createOrFindConversation(chiropractorId)
                .onSuccess { conversation ->
                    _currentConversation.value = conversation
                    loadMessages(conversation.id)
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message
                        )
                    }
                }
        }
    }

    /**
     * Load messages for conversation
     * I-load ang mga mensahe para sa conversation
     */
    private fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            messagingUseCases.getMessages(conversationId)
                .catch { exception ->
                    _uiState.update { 
                        it.copy(error = exception.message)
                    }
                }
                .collect { messageList ->
                    _messages.value = messageList
                    // Mark messages as read
                    markAllMessagesAsRead()
                }
        }
    }

    /**
     * Update message text
     * I-update ang message text
     */
    fun updateMessageText(text: String) {
        _messageText.value = text
    }

    /**
     * Send text message
     * Magpadala ng text message
     */
    fun sendTextMessage() {
        val conversation = _currentConversation.value ?: return
        val chiropractor = _assignedChiropractor.value ?: return
        val content = _messageText.value.trim()

        if (content.isEmpty()) return

        viewModelScope.launch {
            // Validate content
            messagingUseCases.validateMessageContent(content)
                .onSuccess { validContent ->
                    // Clear input immediately for better UX
                    _messageText.value = ""
                    
                    messagingUseCases.sendTextMessage(
                        conversationId = conversation.id,
                        receiverId = chiropractor.id,
                        content = validContent
                    ).onFailure { exception ->
                        _uiState.update { 
                            it.copy(error = exception.message)
                        }
                        // Restore message text on failure
                        _messageText.value = content
                    }
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(error = exception.message)
                    }
                }
        }
    }

    /**
     * Send image message
     * Magpadala ng image message
     */
    fun sendImageMessage(imageUri: Uri, fileName: String? = null) {
        val conversation = _currentConversation.value ?: return
        val chiropractor = _assignedChiropractor.value ?: return

        viewModelScope.launch {
            messagingUseCases.sendImageMessage(
                conversationId = conversation.id,
                receiverId = chiropractor.id,
                imageUri = imageUri,
                fileName = fileName
            ).catch { exception ->
                _uiState.update { 
                    it.copy(error = exception.message)
                }
            }.collect { messageWithStatus ->
                // Update upload progress if needed
                messageWithStatus.uploadProgress?.let { progress ->
                    _uploadProgress.update { currentProgress ->
                        currentProgress + (messageWithStatus.message.id to progress)
                    }
                }
                
                // Remove progress when complete or failed
                if (messageWithStatus.status == MessageStatus.SENT || 
                    messageWithStatus.status == MessageStatus.FAILED) {
                    _uploadProgress.update { currentProgress ->
                        currentProgress - messageWithStatus.message.id
                    }
                }
            }
        }
    }

    /**
     * Send file message
     * Magpadala ng file message
     */
    fun sendFileMessage(fileUri: Uri, fileName: String, mimeType: String) {
        val conversation = _currentConversation.value ?: return
        val chiropractor = _assignedChiropractor.value ?: return

        viewModelScope.launch {
            messagingUseCases.sendFileMessage(
                conversationId = conversation.id,
                receiverId = chiropractor.id,
                fileUri = fileUri,
                fileName = fileName,
                mimeType = mimeType
            ).catch { exception ->
                _uiState.update { 
                    it.copy(error = exception.message)
                }
            }.collect { messageWithStatus ->
                // Update upload progress if needed
                messageWithStatus.uploadProgress?.let { progress ->
                    _uploadProgress.update { currentProgress ->
                        currentProgress + (messageWithStatus.message.id to progress)
                    }
                }
                
                // Remove progress when complete or failed
                if (messageWithStatus.status == MessageStatus.SENT || 
                    messageWithStatus.status == MessageStatus.FAILED) {
                    _uploadProgress.update { currentProgress ->
                        currentProgress - messageWithStatus.message.id
                    }
                }
            }
        }
    }

    /**
     * Delete message
     * Tanggalin ang mensahe
     */
    fun deleteMessage(messageId: String) {
        val conversation = _currentConversation.value ?: return

        viewModelScope.launch {
            messagingUseCases.deleteMessage(conversation.id, messageId)
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(error = exception.message)
                    }
                }
        }
    }

    /**
     * Mark all messages as read
     * Markahan ang lahat ng mensahe bilang nabasa
     */
    private fun markAllMessagesAsRead() {
        val conversation = _currentConversation.value ?: return

        viewModelScope.launch {
            messagingUseCases.markAllMessagesAsRead(conversation.id)
        }
    }

    /**
     * Make phone call to chiropractor
     * Tumawag sa chiropractor
     */
    fun makePhoneCall(context: Context) {
        val chiropractor = _assignedChiropractor.value ?: return

        viewModelScope.launch {
            messagingUseCases.makePhoneCall(context, chiropractor.id)
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(error = exception.message)
                    }
                }
        }
    }

    /**
     * Get chiropractor phone number for display
     * Kunin ang phone number ng chiropractor para sa display
     */
    suspend fun getChiropractorPhoneNumber(): String? {
        val chiropractor = _assignedChiropractor.value ?: return null
        
        return messagingUseCases.makePhoneCall.getPhoneNumber(chiropractor.id)
            .getOrNull()
    }

    /**
     * Clear error message
     * I-clear ang error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Refresh conversation
     * I-refresh ang conversation
     */
    fun refreshConversation() {
        loadAssignedChiropractor()
    }

    /**
     * Format message timestamp
     * I-format ang message timestamp
     */
    fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
        return messagingUseCases.formatMessageTimestamp(timestamp)
    }

    /**
     * Get file size string
     * Kunin ang file size string
     */
    fun getFileSizeString(sizeBytes: Long): String {
        return messagingUseCases.getFileSizeString(sizeBytes)
    }
}

/**
 * UI State for messaging screen
 * UI State para sa messaging screen
 */
data class MessagingUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isConnected: Boolean = true
)

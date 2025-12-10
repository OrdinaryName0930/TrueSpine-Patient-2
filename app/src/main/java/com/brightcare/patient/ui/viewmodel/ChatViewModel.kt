package com.brightcare.patient.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightcare.patient.data.model.*
import com.brightcare.patient.data.repository.ConversationRepository
import com.brightcare.patient.domain.usecase.MarkConversationAsReadUseCase
import com.brightcare.patient.domain.usecase.MarkConversationAsReadSetToZeroUseCase
import com.brightcare.patient.domain.usecase.ImmediateSetUnreadCountToZeroUseCase
import com.brightcare.patient.domain.usecase.ComprehensiveMarkAsReadUseCase
import com.brightcare.patient.ui.component.conversationcomponent.AttachmentType
import com.brightcare.patient.ui.component.conversationcomponent.ChatMessage
import com.brightcare.patient.ui.component.conversationcomponent.MessageAttachment
import com.brightcare.patient.ui.component.conversationcomponent.SenderType
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for individual chat conversation
 * ViewModel para sa individual chat conversation
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ConversationRepository,
    private val auth: FirebaseAuth,
    private val markConversationAsReadUseCase: MarkConversationAsReadUseCase,
    private val markConversationAsReadSetToZeroUseCase: MarkConversationAsReadSetToZeroUseCase,
    private val immediateSetUnreadCountToZeroUseCase: ImmediateSetUnreadCountToZeroUseCase,
    private val comprehensiveMarkAsReadUseCase: ComprehensiveMarkAsReadUseCase
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Current conversation ID
    private val _conversationId = MutableStateFlow("")
    val conversationId: StateFlow<String> = _conversationId.asStateFlow()

    // Current chiropractor ID (for new conversations)
    private val _chiropractorId = MutableStateFlow<String?>(null)
    val chiropractorId: StateFlow<String?> = _chiropractorId.asStateFlow()

    // Messages
    private val _messages = MutableStateFlow<List<ChatMessageNew>>(emptyList())
    val messages: StateFlow<List<ChatMessageNew>> = _messages.asStateFlow()

    // Message input text
    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    // Chiropractor info
    private val _chiropractor = MutableStateFlow<User?>(null)
    val chiropractor: StateFlow<User?> = _chiropractor.asStateFlow()

    // Upload progress
    private val _uploadProgress = MutableStateFlow<Map<String, UploadProgress>>(emptyMap())
    val uploadProgress: StateFlow<Map<String, UploadProgress>> = _uploadProgress.asStateFlow()

    /**
     * Load conversation and start listening for messages
     * I-load ang conversation at simulan ang pakikinig sa mga mensahe
     */
    fun loadConversation(conversationId: String) {
        _conversationId.value = conversationId
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // Check if this is a new conversation (starts with "new_")
                val isNewConversation = conversationId.startsWith("new_")
                
                if (isNewConversation) {
                    // For new conversations, extract chiropractor ID and load their info
                    val chiropractorId = conversationId.removePrefix("new_")
                    _chiropractorId.value = chiropractorId
                    loadChiropractorForNewConversation(chiropractorId)
                    
                    // Set empty messages for new conversation
                    _messages.value = emptyList()
                    _uiState.update { it.copy(isLoading = false) }
                } else {
                    // For existing conversations, load chiropractor info from conversation participants
                    loadChiropractorFromConversation(conversationId)
                    
                    // Mark conversation as read when user opens it (only for existing conversations)
                    // I-mark ang conversation bilang nabasa kapag binuksan ng user (existing conversations lang)
                    println("🔄 ChatViewModel: Marking conversation as read when opening: $conversationId")
                    markConversationAsRead(conversationId)
                    
                    // Then load messages
                    repository.getMessages(conversationId)
                        .catch { exception ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = exception.message
                                )
                            }
                        }
                        .collect { messageList ->
                            _messages.value = messageList
                            _uiState.update { it.copy(isLoading = false) }
                            
                            // Load chiropractor info from messages as fallback if not already loaded
                            if (_chiropractor.value == null && messageList.isNotEmpty()) {
                                loadChiropractorInfo(messageList)
                            }
                        }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    /**
     * Load chiropractor info for new conversation (before any messages)
     * I-load ang chiropractor info para sa bagong conversation (bago pa ang mga mensahe)
     */
    fun loadChiropractorForNewConversation(chiropractorId: String) {
        _chiropractorId.value = chiropractorId
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            repository.getUserById(chiropractorId)
                .onSuccess { user ->
                    _chiropractor.value = user
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
     * Load chiropractor information from conversation participants
     * I-load ang impormasyon ng chiropractor mula sa conversation participants
     */
    private suspend fun loadChiropractorFromConversation(conversationId: String) {
        try {
            val currentUserId = auth.currentUser?.uid ?: return
            
            // Get conversation metadata to find participants
            repository.getConversationById(conversationId)
                .onSuccess { conversation ->
                    if (conversation != null) {
                        val chiropractorId = conversation.participants.find { it != currentUserId }
                        if (chiropractorId != null) {
                            repository.getUserById(chiropractorId)
                                .onSuccess { user ->
                                    _chiropractor.value = user
                                }
                        }
                    }
                }
        } catch (e: Exception) {
            // Log error but don't fail the UI
            e.printStackTrace()
        }
    }

    /**
     * Load chiropractor information from messages (fallback method)
     * I-load ang impormasyon ng chiropractor mula sa mga mensahe (fallback method)
     */
    private suspend fun loadChiropractorInfo(messages: List<ChatMessageNew>) {
        try {
            val currentUserId = auth.currentUser?.uid ?: return
            val chiropractorId = messages.find { it.senderId != currentUserId }?.senderId
            
            if (chiropractorId != null) {
                repository.getUserById(chiropractorId)
                    .onSuccess { user ->
                        _chiropractor.value = user
                    }
            }
        } catch (e: Exception) {
            // Log error but don't fail the UI
            e.printStackTrace()
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
        val content = _messageText.value.trim()
        if (content.isEmpty()) return

        val conversationId = _conversationId.value
        if (conversationId.isEmpty()) return

        viewModelScope.launch {
            // Clear input immediately for better UX
            _messageText.value = ""
            
            repository.sendTextMessage(conversationId, content)
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(error = exception.message)
                    }
                    // Restore message text on failure
                    _messageText.value = content
                }
        }
    }

    /**
     * Send first message (creates conversation)
     * Magpadala ng unang mensahe (gumagawa ng conversation)
     */
    fun sendFirstMessage(chiropractorId: String, content: String) {
        if (content.trim().isEmpty()) return

        viewModelScope.launch {
            // Clear input immediately for better UX
            _messageText.value = ""
            
            // Create conversation first
            repository.getOrCreateConversation(chiropractorId)
                .onSuccess { conversation ->
                    // Update conversation ID and start listening to messages
                    _conversationId.value = conversation.id
                    
                    // Send the message
                    repository.sendTextMessage(conversation.id, content.trim())
                        .onSuccess { message ->
                            // Start listening to messages for this conversation
                            loadConversation(conversation.id)
                        }
                        .onFailure { exception ->
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
                    // Restore message text on failure
                    _messageText.value = content
                }
        }
    }

    /**
     * Send image message
     * Magpadala ng image message
     */
    fun sendImageMessage(imageUri: Uri, fileName: String? = null) {
        val conversationId = _conversationId.value
        if (conversationId.isEmpty()) return

        viewModelScope.launch {
            repository.sendImageMessage(conversationId, imageUri, fileName)
                .catch { exception ->
                    _uiState.update { 
                        it.copy(error = exception.message)
                    }
                }
                .collect { (message, progress) ->
                    progress?.let { prog ->
                        _uploadProgress.update { currentProgress ->
                            if (prog.isComplete || prog.error != null) {
                                currentProgress - message.id
                            } else {
                                currentProgress + (message.id to prog)
                            }
                        }
                    }
                }
        }
    }

    /**
     * Send first image message (creates conversation)
     * Magpadala ng unang image message (gumagawa ng conversation)
     */
    fun sendFirstImageMessage(chiropractorId: String, imageUri: Uri, fileName: String? = null) {
        viewModelScope.launch {
            // Create conversation first
            repository.getOrCreateConversation(chiropractorId)
                .onSuccess { conversation ->
                    // Update conversation ID and start listening to messages
                    _conversationId.value = conversation.id
                    
                    // Send the image message
                    repository.sendImageMessage(conversation.id, imageUri, fileName)
                        .catch { exception ->
                            _uiState.update { 
                                it.copy(error = exception.message)
                            }
                        }
                        .collect { (message, progress) ->
                            progress?.let { prog ->
                                _uploadProgress.update { currentProgress ->
                                    if (prog.isComplete || prog.error != null) {
                                        currentProgress - message.id
                                    } else {
                                        currentProgress + (message.id to prog)
                                    }
                                }
                            }
                            
                            // Start listening to messages for this conversation if upload is complete
                            if (progress?.isComplete == true) {
                                loadConversation(conversation.id)
                            }
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
     * Send file message
     * Magpadala ng file message
     */
    fun sendFileMessage(fileUri: Uri, fileName: String, mimeType: String) {
        val conversationId = _conversationId.value
        if (conversationId.isEmpty()) return

        viewModelScope.launch {
            repository.sendFileMessage(conversationId, fileUri, fileName, mimeType)
                .catch { exception ->
                    _uiState.update { 
                        it.copy(error = exception.message)
                    }
                }
                .collect { (message, progress) ->
                    progress?.let { prog ->
                        _uploadProgress.update { currentProgress ->
                            if (prog.isComplete || prog.error != null) {
                                currentProgress - message.id
                            } else {
                                currentProgress + (message.id to prog)
                            }
                        }
                    }
                }
        }
    }

    /**
     * Send first file message (creates conversation)
     * Magpadala ng unang file message (gumagawa ng conversation)
     */
    fun sendFirstFileMessage(chiropractorId: String, fileUri: Uri, fileName: String, mimeType: String) {
        viewModelScope.launch {
            // Create conversation first
            repository.getOrCreateConversation(chiropractorId)
                .onSuccess { conversation ->
                    // Update conversation ID and start listening to messages
                    _conversationId.value = conversation.id
                    
                    // Send the file message
                    repository.sendFileMessage(conversation.id, fileUri, fileName, mimeType)
                        .catch { exception ->
                            _uiState.update { 
                                it.copy(error = exception.message)
                            }
                        }
                        .collect { (message, progress) ->
                            progress?.let { prog ->
                                _uploadProgress.update { currentProgress ->
                                    if (prog.isComplete || prog.error != null) {
                                        currentProgress - message.id
                                    } else {
                                        currentProgress + (message.id to prog)
                                    }
                                }
                            }
                            
                            // Start listening to messages for this conversation if upload is complete
                            if (progress?.isComplete == true) {
                                loadConversation(conversation.id)
                            }
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
     * Make phone call to chiropractor
     * Tumawag sa chiropractor
     */
    fun makePhoneCall(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(error = "Failed to make call: ${e.message}")
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
     * Check if user is current user
     * I-check kung ang user ay current user
     */
    fun isCurrentUser(senderId: String): Boolean {
        return senderId == auth.currentUser?.uid
    }

    /**
     * Convert ChatMessageNew to UI ChatMessage
     * I-convert ang ChatMessageNew sa UI ChatMessage
     */
    fun convertToUiMessage(message: ChatMessageNew): ChatMessage {
        val currentUserId = auth.currentUser?.uid ?: ""
        val isFromCurrentUser = message.senderId == currentUserId
        
        return ChatMessage(
            id = message.id,
            senderId = message.senderId,
            senderName = if (isFromCurrentUser) "You" else (_chiropractor.value?.fullName ?: "Chiropractor"),
            senderType = if (isFromCurrentUser) SenderType.PATIENT else SenderType.DOCTOR,
            message = message.content,
            timestamp = message.timestamp.toDate(),
            isRead = message.isRead,
            attachments = if (message.fileUrl != null) {
                listOf(
                    MessageAttachment(
                        id = "${message.id}_attachment",
                        name = message.fileName ?: "File",
                        url = message.fileUrl,
                        type = when (message.type) {
                            "image" -> AttachmentType.IMAGE
                            "file" -> AttachmentType.FILE
                            else -> AttachmentType.FILE
                        },
                        size = message.fileSize,
                        mimeType = message.mimeType ?: ""
                    )
                )
            } else emptyList()
        )
    }

    /**
     * Mark message as read
     * Markahan ang mensahe bilang nabasa
     */
    fun markMessageAsRead(messageId: String) {
        val conversationId = _conversationId.value
        if (conversationId.isEmpty()) return

        viewModelScope.launch {
            repository.markMessageAsRead(conversationId, messageId)
        }
    }

    /**
     * Mark conversation as read - IMMEDIATE AND COMPREHENSIVE
     * Markahan ang conversation bilang nabasa - AGAD AT KOMPREHENSIBO
     */
    private fun markConversationAsRead(conversationId: String) {
        viewModelScope.launch {
            try {
                println("🚀 ChatViewModel: IMMEDIATE mark as read for: $conversationId")
                
                // Use immediate method for instant UI update
                // Gamitin ang immediate method para sa instant UI update
                val immediateResult = immediateSetUnreadCountToZeroUseCase(conversationId)
                immediateResult.onSuccess {
                    println("✅ ChatViewModel: IMMEDIATE SUCCESS - unread count set to 0: $conversationId")
                    
                    // Then do comprehensive mark as read in background
                    // Tapos gawin ang comprehensive mark as read sa background
                    viewModelScope.launch {
                        try {
                            comprehensiveMarkAsReadUseCase(conversationId)
                            println("✅ ChatViewModel: COMPREHENSIVE SUCCESS: $conversationId")
                        } catch (e: Exception) {
                            println("⚠️ ChatViewModel: COMPREHENSIVE failed (but UI already updated): ${e.message}")
                        }
                    }
                }.onFailure { exception ->
                    println("❌ ChatViewModel: IMMEDIATE failed: ${exception.message}")
                    // Fallback to old method
                    fallbackMarkAsRead(conversationId)
                }
            } catch (e: Exception) {
                println("❌ ChatViewModel: IMMEDIATE exception: ${e.message}")
                // Fallback to old method
                fallbackMarkAsRead(conversationId)
            }
        }
    }

    /**
     * Fallback mark as read method
     * Fallback mark as read method
     */
    private fun fallbackMarkAsRead(conversationId: String) {
        viewModelScope.launch {
            try {
                val markAsReadResult = markConversationAsReadUseCase(conversationId)
                markAsReadResult.onSuccess {
                    println("✅ ChatViewModel: FALLBACK SUCCESS: $conversationId")
                }.onFailure { exception ->
                    println("❌ ChatViewModel: FALLBACK failed: ${exception.message}")
                    // If deletion approach fails, try the set-to-zero approach
                    markConversationAsReadSetToZero(conversationId)
                }
            } catch (e: Exception) {
                println("❌ ChatViewModel: FALLBACK exception: ${e.message}")
                // If deletion approach fails, try the set-to-zero approach
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
                val result = markConversationAsReadSetToZeroUseCase(conversationId)
                result.onSuccess {
                    println("✅ Conversation marked as read (set to 0) successfully: $conversationId")
                }.onFailure { exception ->
                    println("❌ Failed to mark conversation as read (set to 0): ${exception.message}")
                }
            } catch (e: Exception) {
                println("❌ Exception while marking conversation as read (set to 0): ${e.message}")
            }
        }
    }

    /**
     * Public function to immediately force unread count to 0
     * Public function para agad na i-force ang unread count sa 0
     */
    fun forceMarkAsReadSetToZero() {
        val conversationId = _conversationId.value
        if (conversationId.isNotEmpty() && !conversationId.startsWith("new_")) {
            viewModelScope.launch {
                try {
                    println("🚀 FORCE: Immediately setting unread count to 0: $conversationId")
                    immediateSetUnreadCountToZeroUseCase(conversationId)
                    println("✅ FORCE: Successfully set unread count to 0: $conversationId")
                } catch (e: Exception) {
                    println("❌ FORCE: Failed to set unread count to 0: ${e.message}")
                    // Fallback to old method
                    markConversationAsReadSetToZero(conversationId)
                }
            }
        }
    }

    /**
     * Refresh conversation
     * I-refresh ang conversation
     */
    fun refreshConversation() {
        val conversationId = _conversationId.value
        if (conversationId.isNotEmpty()) {
            loadConversation(conversationId)
        }
    }
}

/**
 * UI State for chat screen
 * UI State para sa chat screen
 */
data class ChatUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isConnected: Boolean = true
)

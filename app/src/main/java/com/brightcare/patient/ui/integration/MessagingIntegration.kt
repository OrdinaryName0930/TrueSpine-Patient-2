package com.brightcare.patient.ui.integration

import android.net.Uri
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.brightcare.patient.data.model.Message
import com.brightcare.patient.data.model.MessageType
import com.brightcare.patient.data.model.UploadProgress
import com.brightcare.patient.ui.component.conversationcomponent.ChatConversation
import com.brightcare.patient.ui.component.conversationcomponent.SenderType
import com.brightcare.patient.ui.viewmodel.MessagingViewModel
import java.util.Date

/**
 * Integration helper to connect existing UI with new backend
 * Integration helper para sa pag-connect ng existing UI sa bagong backend
 */

/**
 * Convert backend Message to UI ChatConversation
 * I-convert ang backend Message sa UI ChatConversation
 */
fun Message.toChatConversation(chiropractorName: String, chiropractorPhone: String?): ChatConversation {
    return ChatConversation(
        id = this.conversationId,
        participantName = chiropractorName,
        participantType = SenderType.DOCTOR, // Assuming chiropractor maps to doctor
        lastMessage = when (this.type) {
            MessageType.TEXT -> this.content
            MessageType.IMAGE -> "📷 Image"
            MessageType.FILE -> "📎 ${this.fileName ?: "File"}"
            MessageType.AUDIO -> "🎵 Audio"
            MessageType.VIDEO -> "🎥 Video"
            MessageType.LOCATION -> "📍 Location"
            MessageType.CONTACT -> "👤 Contact"
        },
        lastMessageTime = this.timestamp.toDate(),
        unreadCount = 0, // This should come from conversation data
        isOnline = true, // You might want to implement online status
        phoneNumber = chiropractorPhone
    )
}

/**
 * Composable to integrate messaging functionality with existing UI
 * Composable para sa pag-integrate ng messaging functionality sa existing UI
 */
@Composable
fun MessagingIntegrationProvider(
    content: @Composable (MessagingIntegrationState) -> Unit
) {
    val viewModel: MessagingViewModel = hiltViewModel()
    
    val uiState by viewModel.uiState.collectAsState()
    val currentConversation by viewModel.currentConversation.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val messageText by viewModel.messageText.collectAsState()
    val assignedChiropractor by viewModel.assignedChiropractor.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()

    val integrationState = MessagingIntegrationState(
        // Data
        conversation = currentConversation,
        messages = messages,
        messageText = messageText,
        chiropractor = assignedChiropractor,
        uploadProgress = uploadProgress,
        
        // UI State
        isLoading = uiState.isLoading,
        error = uiState.error,
        
        // Actions
        onMessageTextChange = viewModel::updateMessageText,
        onSendMessage = viewModel::sendTextMessage,
        onSendImage = viewModel::sendImageMessage,
        onSendFile = viewModel::sendFileMessage,
        onDeleteMessage = viewModel::deleteMessage,
        onMakePhoneCall = viewModel::makePhoneCall,
        onClearError = viewModel::clearError,
        onRefresh = viewModel::refreshConversation,
        
        // Utilities
        formatTimestamp = viewModel::formatTimestamp,
        getFileSizeString = viewModel::getFileSizeString,
        getChiropractorPhoneNumber = viewModel::getChiropractorPhoneNumber
    )
    
    content(integrationState)
}

/**
 * State holder for messaging integration
 * State holder para sa messaging integration
 */
data class MessagingIntegrationState(
    // Data
    val conversation: com.brightcare.patient.data.model.Conversation?,
    val messages: List<Message>,
    val messageText: String,
    val chiropractor: com.brightcare.patient.data.model.Chiropractor?,
    val uploadProgress: Map<String, UploadProgress>,
    
    // UI State
    val isLoading: Boolean,
    val error: String?,
    
    // Actions
    val onMessageTextChange: (String) -> Unit,
    val onSendMessage: () -> Unit,
    val onSendImage: (Uri, String?) -> Unit,
    val onSendFile: (Uri, String, String) -> Unit,
    val onDeleteMessage: (String) -> Unit,
    val onMakePhoneCall: (android.content.Context) -> Unit,
    val onClearError: () -> Unit,
    val onRefresh: () -> Unit,
    
    // Utilities
    val formatTimestamp: (com.google.firebase.Timestamp) -> String,
    val getFileSizeString: (Long) -> String,
    val getChiropractorPhoneNumber: suspend () -> String?
)

/**
 * Extension functions to help with UI integration
 * Extension functions para sa pag-tulong sa UI integration
 */

/**
 * Convert backend Message to existing UI format
 * I-convert ang backend Message sa existing UI format
 */
fun Message.toUiMessage(): com.brightcare.patient.ui.component.conversationcomponent.ChatMessage {
    return com.brightcare.patient.ui.component.conversationcomponent.ChatMessage(
        id = this.id,
        senderId = this.senderId,
        senderName = "", // You might need to get this from user data
        senderType = com.brightcare.patient.ui.component.conversationcomponent.SenderType.PATIENT, // Determine based on senderId
        message = this.content,
        timestamp = this.timestamp.toDate(),
        isRead = this.isRead,
        attachments = if (this.fileUrl != null) {
            listOf(
                com.brightcare.patient.ui.component.conversationcomponent.MessageAttachment(
                    id = "${this.id}_attachment",
                    name = this.fileName ?: "File",
                    url = this.fileUrl,
                    type = when (this.type) {
                        MessageType.IMAGE -> com.brightcare.patient.ui.component.conversationcomponent.AttachmentType.IMAGE
                        MessageType.FILE -> com.brightcare.patient.ui.component.conversationcomponent.AttachmentType.FILE
                        MessageType.VIDEO -> com.brightcare.patient.ui.component.conversationcomponent.AttachmentType.VIDEO
                        MessageType.AUDIO -> com.brightcare.patient.ui.component.conversationcomponent.AttachmentType.AUDIO
                        else -> com.brightcare.patient.ui.component.conversationcomponent.AttachmentType.FILE
                    },
                    size = this.fileSize,
                    mimeType = this.mimeType ?: ""
                )
            )
        } else emptyList()
    )
}

/**
 * Helper to determine sender type
 * Helper para sa pag-determine ng sender type
 */
fun determineSenderType(senderId: String, currentUserId: String): com.brightcare.patient.ui.component.conversationcomponent.SenderType {
    return if (senderId == currentUserId) {
        com.brightcare.patient.ui.component.conversationcomponent.SenderType.PATIENT
    } else {
        com.brightcare.patient.ui.component.conversationcomponent.SenderType.DOCTOR
    }
}

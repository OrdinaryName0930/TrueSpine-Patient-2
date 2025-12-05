package com.brightcare.patient.ui.component.conversationcomponent

import java.util.Date

/**
 * Data classes for conversation components
 * Mga data class para sa conversation components
 */

/**
 * Enhanced ChatMessage with attachment support
 * Enhanced ChatMessage na may attachment support
 */
data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderType: SenderType,
    val message: String,
    val timestamp: Date,
    val isRead: Boolean = false,
    val attachments: List<MessageAttachment> = emptyList(),
    val replyToMessageId: String? = null,
    val isEdited: Boolean = false,
    val editedTimestamp: Date? = null
)

/**
 * Message attachment data class
 * Message attachment data class
 */
data class MessageAttachment(
    val id: String,
    val name: String,
    val url: String,
    val type: AttachmentType,
    val size: Long = 0L, // Size in bytes
    val mimeType: String = "",
    val thumbnailUrl: String? = null,
    val uploadProgress: Float = 1.0f, // 0.0 to 1.0, 1.0 means fully uploaded
    val isUploading: Boolean = false,
    val uploadError: String? = null
)

/**
 * Attachment type enumeration
 * Attachment type enumeration
 */
enum class AttachmentType {
    IMAGE,
    FILE,
    VIDEO,
    AUDIO,
    DOCUMENT
}

/**
 * Enhanced ChatConversation
 * Enhanced ChatConversation
 */
data class ChatConversation(
    val id: String,
    val participantName: String,
    val participantType: SenderType,
    val participantId: String = "",
    val lastMessage: String,
    val lastMessageTime: Date,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val lastSeenTime: Date? = null,
    val conversationType: ConversationType = ConversationType.DIRECT,
    val groupMembers: List<ConversationParticipant> = emptyList(),
    val isTyping: Boolean = false,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val avatarUrl: String? = null
)

/**
 * Conversation participant for group chats
 * Conversation participant para sa group chats
 */
data class ConversationParticipant(
    val id: String,
    val name: String,
    val type: SenderType,
    val avatarUrl: String? = null,
    val isOnline: Boolean = false,
    val lastSeenTime: Date? = null
)

/**
 * Conversation type enumeration
 * Conversation type enumeration
 */
enum class ConversationType {
    DIRECT,     // One-on-one conversation
    GROUP,      // Group conversation
    BROADCAST   // Broadcast message
}

/**
 * Sender type enumeration (reused from message component)
 * Sender type enumeration (reused from message component)
 */
enum class SenderType {
    PATIENT,
    DOCTOR,
    ADMIN,
    NURSE,
    SUPPORT
}

/**
 * Message status for tracking delivery and read status
 * Message status para sa pag-track ng delivery at read status
 */
enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}

/**
 * Enhanced message with status tracking
 * Enhanced message na may status tracking
 */
data class EnhancedChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderType: SenderType,
    val message: String,
    val timestamp: Date,
    val status: MessageStatus = MessageStatus.SENDING,
    val attachments: List<MessageAttachment> = emptyList(),
    val replyToMessageId: String? = null,
    val isEdited: Boolean = false,
    val editedTimestamp: Date? = null,
    val reactions: List<MessageReaction> = emptyList(),
    val mentions: List<String> = emptyList() // User IDs mentioned in the message
)

/**
 * Message reaction data class
 * Message reaction data class
 */
data class MessageReaction(
    val userId: String,
    val userName: String,
    val emoji: String,
    val timestamp: Date
)

/**
 * Conversation settings
 * Conversation settings
 */
data class ConversationSettings(
    val conversationId: String,
    val isMuted: Boolean = false,
    val muteUntil: Date? = null,
    val customNotificationSound: String? = null,
    val isArchived: Boolean = false,
    val isPinned: Boolean = false,
    val theme: ConversationTheme = ConversationTheme.DEFAULT
)

/**
 * Conversation theme enumeration
 * Conversation theme enumeration
 */
enum class ConversationTheme {
    DEFAULT,
    MEDICAL,
    SUPPORT,
    URGENT
}

/**
 * Typing indicator data class
 * Typing indicator data class
 */
data class TypingIndicator(
    val userId: String,
    val userName: String,
    val timestamp: Date
)

/**
 * File upload progress data class
 * File upload progress data class
 */
data class FileUploadProgress(
    val attachmentId: String,
    val fileName: String,
    val progress: Float, // 0.0 to 1.0
    val isComplete: Boolean = false,
    val error: String? = null
)
package com.brightcare.patient.data.model

import com.google.firebase.Timestamp
import java.util.Date

/**
 * User model for both patients and chiropractors
 * User model para sa patients at chiropractors
 */
data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val profileImage: String? = null,
    val role: UserRole = UserRole.PATIENT,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    // Additional fields for chiropractors
    val specialization: String? = null,
    val licenseNumber: String? = null,
    val phoneNumber: String? = null,
    val experience: Int = 0,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val isAvailable: Boolean = true,
    val bio: String? = null
) {
    // No-argument constructor for Firestore
    constructor() : this(
        uid = "",
        fullName = "",
        email = "",
        profileImage = null,
        role = UserRole.PATIENT
    )

    /**
     * Convert to map for Firestore
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "fullName" to fullName,
            "email" to email,
            "profileImage" to profileImage,
            "role" to role.name.lowercase(),
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "specialization" to specialization,
            "licenseNumber" to licenseNumber,
            "phoneNumber" to phoneNumber,
            "experience" to experience,
            "rating" to rating,
            "reviewCount" to reviewCount,
            "isAvailable" to isAvailable,
            "bio" to bio
        )
    }
}

/**
 * User role enumeration
 * Enumeration ng user role
 */
enum class UserRole {
    PATIENT,
    CHIROPRACTOR
}

/**
 * Conversation model for metadata
 * Conversation model para sa metadata
 */
data class ConversationMetadata(
    val id: String = "",
    val participants: List<String> = emptyList(), // [patientId, chiropractorId]
    val lastMessage: String = "",
    val lastMessageType: String = "text",
    val lastMessageSenderId: String = "",
    val updatedAt: Timestamp = Timestamp.now(),
    val createdAt: Timestamp = Timestamp.now(),
    val unreadCounts: Map<String, Int> = emptyMap() // userId -> unread count
) {
    // No-argument constructor for Firestore
    constructor() : this(
        id = "",
        participants = emptyList(),
        lastMessage = "",
        updatedAt = Timestamp.now()
    )

    /**
     * Convert to map for Firestore
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "participants" to participants,
            "lastMessage" to lastMessage,
            "lastMessageType" to lastMessageType,
            "lastMessageSenderId" to lastMessageSenderId,
            "updatedAt" to updatedAt,
            "createdAt" to createdAt,
            "unreadCounts" to unreadCounts
        )
    }
}

/**
 * Message model for individual messages
 * Message model para sa individual messages
 */
data class ChatMessageNew(
    val id: String = "",
    val senderId: String = "",
    val type: String = "text", // "text", "image", "file"
    val content: String = "", // Text content or file name
    val fileUrl: String? = null, // Firebase Storage URL for files/images
    val fileName: String? = null, // Original file name
    val fileSize: Long = 0L, // File size in bytes
    val mimeType: String? = null, // MIME type for files
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false,
    val replyToMessageId: String? = null // For message replies
) {
    // No-argument constructor for Firestore
    constructor() : this(
        id = "",
        senderId = "",
        type = "text",
        content = "",
        timestamp = Timestamp.now()
    )

    /**
     * Convert to map for Firestore
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "senderId" to senderId,
            "type" to type,
            "content" to content,
            "fileUrl" to fileUrl,
            "fileName" to fileName,
            "fileSize" to fileSize,
            "mimeType" to mimeType,
            "timestamp" to timestamp,
            "isRead" to isRead,
            "replyToMessageId" to replyToMessageId
        )
    }
}

/**
 * File upload progress model
 * File upload progress model
 */
data class UploadProgress(
    val fileName: String,
    val progress: Float, // 0.0 to 1.0
    val isComplete: Boolean = false,
    val error: String? = null,
    val downloadUrl: String? = null
)

/**
 * Conversation display model for UI
 * Conversation display model para sa UI
 */
data class ConversationDisplay(
    val conversationId: String,
    val chiropractor: User,
    val lastMessage: String,
    val lastMessageTime: Date,
    val unreadCount: Int,
    val isOnline: Boolean = false
)
















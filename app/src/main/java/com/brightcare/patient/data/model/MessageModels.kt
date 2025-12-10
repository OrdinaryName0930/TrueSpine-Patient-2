package com.brightcare.patient.data.model

import com.google.firebase.Timestamp
import java.util.Date

/**
 * Message data model for Firestore
 * Modelo ng mensahe para sa Firestore
 */
data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val conversationId: String = "",
    val type: MessageType = MessageType.TEXT,
    val content: String = "", // Text content or file name
    val fileUrl: String? = null, // Firebase Storage URL for files/images
    val fileName: String? = null, // Original file name
    val fileSize: Long = 0L, // File size in bytes
    val mimeType: String? = null, // MIME type for files
    val thumbnailUrl: String? = null, // Thumbnail for images/videos
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false,
    val isDelivered: Boolean = false,
    val isEdited: Boolean = false,
    val editedAt: Timestamp? = null,
    val replyToMessageId: String? = null, // For message replies
    val metadata: Map<String, Any> = emptyMap() // Additional metadata
) {
    // No-argument constructor for Firestore
    constructor() : this(
        id = "",
        senderId = "",
        receiverId = "",
        conversationId = "",
        type = MessageType.TEXT,
        content = "",
        fileUrl = null,
        fileName = null,
        fileSize = 0L,
        mimeType = null,
        thumbnailUrl = null,
        timestamp = Timestamp.now(),
        isRead = false,
        isDelivered = false,
        isEdited = false,
        editedAt = null,
        replyToMessageId = null,
        metadata = emptyMap()
    )

    /**
     * Convert to map for Firestore
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "senderId" to senderId,
            "receiverId" to receiverId,
            "conversationId" to conversationId,
            "type" to type.name,
            "content" to content,
            "fileUrl" to fileUrl,
            "fileName" to fileName,
            "fileSize" to fileSize,
            "mimeType" to mimeType,
            "thumbnailUrl" to thumbnailUrl,
            "timestamp" to timestamp,
            "isRead" to isRead,
            "isDelivered" to isDelivered,
            "isEdited" to isEdited,
            "editedAt" to editedAt,
            "replyToMessageId" to replyToMessageId,
            "metadata" to metadata
        )
    }
}

/**
 * Message type enumeration
 * Enumeration ng uri ng mensahe
 */
enum class MessageType {
    TEXT,
    IMAGE,
    FILE,
    AUDIO,
    VIDEO,
    LOCATION,
    CONTACT
}

/**
 * Conversation data model
 * Modelo ng conversation
 */
data class Conversation(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(), // userId -> name
    val participantTypes: Map<String, UserType> = emptyMap(), // userId -> type
    val lastMessage: String = "",
    val lastMessageType: MessageType = MessageType.TEXT,
    val lastMessageTimestamp: Timestamp = Timestamp.now(),
    val lastMessageSenderId: String = "",
    val unreadCounts: Map<String, Int> = emptyMap(), // userId -> unread count
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val metadata: Map<String, Any> = emptyMap()
) {
    // No-argument constructor for Firestore
    constructor() : this(
        id = "",
        participantIds = emptyList(),
        participantNames = emptyMap(),
        participantTypes = emptyMap(),
        lastMessage = "",
        lastMessageType = MessageType.TEXT,
        lastMessageTimestamp = Timestamp.now(),
        lastMessageSenderId = "",
        unreadCounts = emptyMap(),
        isActive = true,
        createdAt = Timestamp.now(),
        updatedAt = Timestamp.now(),
        metadata = emptyMap()
    )

    /**
     * Convert to map for Firestore
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "participantIds" to participantIds,
            "participantNames" to participantNames,
            "participantTypes" to participantTypes.mapValues { it.value.name },
            "lastMessage" to lastMessage,
            "lastMessageType" to lastMessageType.name,
            "lastMessageTimestamp" to lastMessageTimestamp,
            "lastMessageSenderId" to lastMessageSenderId,
            "unreadCounts" to unreadCounts,
            "isActive" to isActive,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "metadata" to metadata
        )
    }
}

/**
 * User type enumeration
 * Enumeration ng uri ng user
 */
enum class UserType {
    PATIENT,
    CHIROPRACTOR,
    ADMIN,
    NURSE,
    SUPPORT
}

/**
 * Chiropractor data model
 * Modelo ng chiropractor
 */
data class Chiropractor(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val photoUrl: String? = null,
    val specialization: String = "",
    val licenseNumber: String = "",
    val experience: Int = 0, // Years of experience
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val isAvailable: Boolean = true,
    val workingHours: Map<String, String> = emptyMap(), // day -> "09:00-17:00"
    val serviceHours: String? = null, // Raw service hours string from Firestore
    val location: String = "",
    val bio: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    // No-argument constructor for Firestore
    constructor() : this(
        id = "",
        name = "",
        email = "",
        phoneNumber = "",
        photoUrl = null,
        specialization = "",
        licenseNumber = "",
        experience = 0,
        rating = 0.0,
        reviewCount = 0,
        isAvailable = true,
        workingHours = emptyMap(),
        serviceHours = null,
        location = "",
        bio = "",
        createdAt = Timestamp.now(),
        updatedAt = Timestamp.now()
    )
}

/**
 * Patient data model
 * Modelo ng patient
 */
data class Patient(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val photoUrl: String? = null,
    val assignedChiropractorId: String? = null,
    val dateOfBirth: Timestamp? = null,
    val gender: String = "",
    val address: String = "",
    val emergencyContact: String = "",
    val medicalHistory: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    // No-argument constructor for Firestore
    constructor() : this(
        id = "",
        name = "",
        email = "",
        phoneNumber = "",
        photoUrl = null,
        assignedChiropractorId = null,
        dateOfBirth = null,
        gender = "",
        address = "",
        emergencyContact = "",
        medicalHistory = emptyList(),
        allergies = emptyList(),
        createdAt = Timestamp.now(),
        updatedAt = Timestamp.now()
    )
}


/**
 * Message status for UI updates
 * Status ng mensahe para sa UI updates
 */
enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}

/**
 * Enhanced message for UI with status
 * Enhanced na mensahe para sa UI na may status
 */
data class MessageWithStatus(
    val message: Message,
    val status: MessageStatus = MessageStatus.SENT,
    val uploadProgress: UploadProgress? = null
)


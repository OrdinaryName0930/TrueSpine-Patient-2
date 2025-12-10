package com.brightcare.patient.ui.component.messagecomponent

import java.util.*

/**
 * Data classes for message functionality
 * Mga data class para sa message functionality
 */

/**
 * Represents a single chat message
 * Kumakatawan sa isang chat message
 */
data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderType: SenderType,
    val message: String,
    val timestamp: Date,
    val isRead: Boolean
)

/**
 * Represents a conversation between participants
 * Kumakatawan sa conversation sa pagitan ng mga participant
 */
data class ChatConversation(
    val id: String,
    val participantName: String,
    val participantType: SenderType,
    val lastMessage: String,
    val lastMessageTime: Date,
    val unreadCount: Int,
    val isOnline: Boolean,
    val profileImageUrl: String? = null, // Added for displaying actual profile images
    val hasNewMessage: Boolean = false, // Added to indicate if there are new/unread messages
    val phoneNumber: String? = null, // Added for phone call functionality
    val specialization: String? = null // Added for displaying chiropractor specialization
)

/**
 * Enum for different types of message senders
 * Enum para sa iba't ibang uri ng message sender
 */
enum class SenderType {
    DOCTOR,     // Mga doktor
    PATIENT,    // Mga pasyente
    ADMIN       // Mga admin/support
}








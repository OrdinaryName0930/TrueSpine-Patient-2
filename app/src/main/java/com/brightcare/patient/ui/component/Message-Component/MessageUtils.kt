package com.brightcare.patient.ui.component.messagecomponent

import java.util.*

/**
 * Utility functions for message component
 * Mga utility function para sa message component
 */

/**
 * Format conversation timestamp for display
 * I-format ang conversation timestamp para sa display
 */
fun formatConversationTime(timestamp: Date): String {
    val now = System.currentTimeMillis()
    val messageTime = timestamp.time
    val diff = now - messageTime
    
    return when {
        diff < 60_000 -> "Just now" // Less than 1 minute
        diff < 3600_000 -> "${diff / 60_000}m" // Less than 1 hour
        diff < 86400_000 -> "${diff / 3600_000}h" // Less than 1 day
        diff < 604800_000 -> "${diff / 86400_000}d" // Less than 1 week
        else -> {
            val date = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
            date.format(timestamp)
        }
    }
}

/**
 * Get conversation preview text
 * Kunin ang conversation preview text
 */
fun getConversationPreview(lastMessage: String, messageType: String = "TEXT"): String {
    return when (messageType.uppercase()) {
        "IMAGE" -> "📷 Photo"
        "FILE" -> "📎 File"
        "AUDIO" -> "🎵 Audio"
        "VIDEO" -> "🎥 Video"
        "DOCUMENT" -> "📄 Document"
        else -> lastMessage.take(50) + if (lastMessage.length > 50) "..." else ""
    }
}

/**
 * Get unread count display text
 * Kunin ang unread count display text
 */
fun getUnreadCountText(count: Int): String {
    return when {
        count == 0 -> ""
        count < 100 -> count.toString()
        else -> "99+"
    }
}

/**
 * Check if conversation is recent (within 24 hours)
 * I-check kung ang conversation ay recent (within 24 hours)
 */
fun isRecentConversation(timestamp: Date): Boolean {
    val now = System.currentTimeMillis()
    val messageTime = timestamp.time
    val diff = now - messageTime
    return diff < 86400_000 // 24 hours
}

/**
 * Get sender type display name
 * Kunin ang sender type display name
 */
fun getSenderTypeDisplayName(senderType: SenderType): String {
    return when (senderType) {
        SenderType.DOCTOR -> "Doctor"
        SenderType.PATIENT -> "Patient"
        SenderType.ADMIN -> "Admin"
    }
}

/**
 * Sort conversations by priority (unread first, then by time)
 * I-sort ang conversations ayon sa priority (unread muna, then by time)
 */
fun sortConversationsByPriority(conversations: List<ChatConversation>): List<ChatConversation> {
    return conversations.sortedWith(
        compareByDescending<ChatConversation> { it.unreadCount > 0 }
            .thenByDescending { it.lastMessageTime }
    )
}

/**
 * Filter conversations by search query
 * I-filter ang conversations gamit ang search query
 */
fun filterConversations(
    conversations: List<ChatConversation>,
    searchQuery: String
): List<ChatConversation> {
    if (searchQuery.isBlank()) return conversations
    
    val query = searchQuery.lowercase().trim()
    return conversations.filter { conversation ->
        conversation.participantName.lowercase().contains(query) ||
        conversation.lastMessage.lowercase().contains(query)
    }
}

/**
 * Get conversation status text
 * Kunin ang conversation status text
 */
fun getConversationStatusText(conversation: ChatConversation): String {
    return when {
        conversation.isOnline -> "Online"
        else -> "Offline"
    }
}
package com.brightcare.patient.ui.component.messagecomponent

import java.util.*

/**
 * Utility functions for message component
 * Mga utility function para sa message component
 */

/**
 * Get sample conversations data for testing and preview
 * Kumuha ng sample conversation data para sa testing at preview
 * TEMPORARY DUMP DATA - More conversations for better visibility
 */
fun getSampleConversations(): List<ChatConversation> {
    val calendar = Calendar.getInstance()
    return listOf(
        ChatConversation(
            id = "1",
            participantName = "Dr. Maria Santos",
            participantType = SenderType.DOCTOR,
            lastMessage = "Your next appointment is confirmed for tomorrow at 10 AM. Please arrive 15 minutes early.",
            lastMessageTime = calendar.apply { add(Calendar.HOUR, -2) }.time,
            unreadCount = 2,
            isOnline = true
        ),
        ChatConversation(
            id = "2",
            participantName = "Dr. John Reyes",
            participantType = SenderType.DOCTOR,
            lastMessage = "The X-ray results look good. Continue with the prescribed exercises.",
            lastMessageTime = calendar.apply { add(Calendar.DAY_OF_MONTH, -1) }.time,
            unreadCount = 0,
            isOnline = false
        ),
        ChatConversation(
            id = "3",
            participantName = "BrightCare Support",
            participantType = SenderType.ADMIN,
            lastMessage = "Thank you for your feedback. We'll improve our services based on your suggestions.",
            lastMessageTime = calendar.apply { add(Calendar.DAY_OF_MONTH, -3) }.time,
            unreadCount = 1,
            isOnline = true
        ),
        ChatConversation(
            id = "4",
            participantName = "Dr. Ana Cruz",
            participantType = SenderType.DOCTOR,
            lastMessage = "Please follow the home care instructions I sent you.",
            lastMessageTime = calendar.apply { add(Calendar.WEEK_OF_YEAR, -1) }.time,
            unreadCount = 0,
            isOnline = true
        ),
        // ADDITIONAL DUMP DATA FOR BETTER VISIBILITY
        ChatConversation(
            id = "5",
            participantName = "Dr. Michael Chen",
            participantType = SenderType.DOCTOR,
            lastMessage = "Your blood test results are ready. Everything looks normal. Great job on maintaining your health!",
            lastMessageTime = calendar.apply { add(Calendar.HOUR, -5) }.time,
            unreadCount = 3,
            isOnline = true
        ),
        ChatConversation(
            id = "6",
            participantName = "Nurse Patricia",
            participantType = SenderType.ADMIN,
            lastMessage = "Reminder: Please take your medication before meals as prescribed by your doctor.",
            lastMessageTime = calendar.apply { add(Calendar.HOUR, -8) }.time,
            unreadCount = 1,
            isOnline = false
        ),
        ChatConversation(
            id = "7",
            participantName = "Dr. Sarah Johnson",
            participantType = SenderType.DOCTOR,
            lastMessage = "How are you feeling after the physical therapy session? Any pain or discomfort?",
            lastMessageTime = calendar.apply { add(Calendar.DAY_OF_MONTH, -2) }.time,
            unreadCount = 0,
            isOnline = true
        ),
        ChatConversation(
            id = "8",
            participantName = "BrightCare Pharmacy",
            participantType = SenderType.ADMIN,
            lastMessage = "Your prescription is ready for pickup. Store hours: 9 AM - 7 PM.",
            lastMessageTime = calendar.apply { add(Calendar.HOUR, -12) }.time,
            unreadCount = 2,
            isOnline = false
        ),
        ChatConversation(
            id = "9",
            participantName = "Dr. Robert Kim",
            participantType = SenderType.DOCTOR,
            lastMessage = "The MRI scan shows significant improvement. Keep up with the treatment plan.",
            lastMessageTime = calendar.apply { add(Calendar.DAY_OF_MONTH, -4) }.time,
            unreadCount = 0,
            isOnline = false
        ),
        ChatConversation(
            id = "10",
            participantName = "Emergency Support",
            participantType = SenderType.ADMIN,
            lastMessage = "This is a 24/7 emergency support line. How can we assist you today?",
            lastMessageTime = calendar.apply { add(Calendar.WEEK_OF_YEAR, -2) }.time,
            unreadCount = 0,
            isOnline = true
        ),
        ChatConversation(
            id = "11",
            participantName = "Dr. Lisa Wong",
            participantType = SenderType.DOCTOR,
            lastMessage = "Your vaccination is scheduled for next week. Please bring your ID and insurance card.",
            lastMessageTime = calendar.apply { add(Calendar.DAY_OF_MONTH, -6) }.time,
            unreadCount = 1,
            isOnline = true
        ),
        ChatConversation(
            id = "12",
            participantName = "Appointment Scheduler",
            participantType = SenderType.ADMIN,
            lastMessage = "Your appointment has been rescheduled to Friday 2 PM. Please confirm your availability.",
            lastMessageTime = calendar.apply { add(Calendar.HOUR, -18) }.time,
            unreadCount = 4,
            isOnline = false
        )
    )
}

/**
 * Filter conversations by selected tab
 * I-filter ang mga conversation base sa selected tab
 */
fun filterConversationsByTab(selectedTab: Int, conversations: List<ChatConversation>): List<ChatConversation> {
    return when (selectedTab) {
        0 -> conversations // All chats - Lahat ng chat
        1 -> conversations.filter { it.participantType == SenderType.DOCTOR } // Doctors only
        2 -> conversations.filter { it.participantType == SenderType.ADMIN } // Support only
        else -> conversations
    }
}

/**
 * Format time for conversation display
 * I-format ang oras para sa conversation display
 */
fun formatConversationTime(timestamp: Date): String {
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply { time = timestamp }
    
    val timeFormat = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = java.text.SimpleDateFormat("MMM dd", Locale.getDefault())
    
    return if (now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR) &&
        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR)) {
        timeFormat.format(timestamp)
    } else {
        dateFormat.format(timestamp)
    }
}

/**
 * Search conversations by query string
 * Maghanap ng mga conversation gamit ang query string
 */
fun searchConversations(
    conversations: List<ChatConversation>,
    query: String,
    senderTypeFilter: SenderType? = null
): List<ChatConversation> {
    if (query.isBlank() && senderTypeFilter == null) {
        return conversations
    }
    
    return conversations.filter { conversation ->
        val matchesQuery = if (query.isBlank()) {
            true
        } else {
            conversation.participantName.contains(query, ignoreCase = true) ||
            conversation.lastMessage.contains(query, ignoreCase = true)
        }
        
        val matchesSenderType = senderTypeFilter?.let { filter ->
            conversation.participantType == filter
        } ?: true
        
        matchesQuery && matchesSenderType
    }
}

/**
 * Get search suggestions based on conversations
 * Kumuha ng mga search suggestion base sa mga conversation
 */
fun getSearchSuggestions(
    conversations: List<ChatConversation>,
    query: String,
    maxSuggestions: Int = 5
): List<String> {
    if (query.isBlank()) return emptyList()
    
    val suggestions = mutableSetOf<String>()
    
    // Add participant names that match
    conversations.forEach { conversation ->
        if (conversation.participantName.contains(query, ignoreCase = true)) {
            suggestions.add(conversation.participantName)
        }
    }
    
    // Add common search terms
    val commonTerms = listOf("appointment", "results", "prescription", "follow-up", "emergency")
    commonTerms.forEach { term ->
        if (term.contains(query, ignoreCase = true)) {
            suggestions.add(term)
        }
    }
    
    return suggestions.take(maxSuggestions)
}

/**
 * Highlight search query in text
 * I-highlight ang search query sa text
 */
fun highlightSearchQuery(text: String, query: String): String {
    if (query.isBlank()) return text
    
    return text.replace(
        query.toRegex(RegexOption.IGNORE_CASE),
        "<b>$query</b>"
    )
}

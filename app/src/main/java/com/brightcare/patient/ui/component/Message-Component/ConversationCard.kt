package com.brightcare.patient.ui.component.messagecomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.brightcare.patient.ui.theme.*
import java.util.*

/**
 * Individual conversation card component
 * Card component para sa bawat conversation
 */
@Composable
fun ConversationCard(
    conversation: ChatConversation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeText = if (conversation.lastMessage == "Tap to start conversation") {
        "" // Don't show time for new conversations
    } else {
        formatConversationTime(conversation.lastMessageTime)
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                conversation.hasNewMessage || conversation.unreadCount > 0 -> Blue50
                else -> White
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (conversation.hasNewMessage || conversation.unreadCount > 0) 4.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (conversation.hasNewMessage || conversation.unreadCount > 0) {
            androidx.compose.foundation.BorderStroke(1.dp, Blue200)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with online indicator and new message indicator
            ConversationAvatar(
                participantType = conversation.participantType,
                isOnline = conversation.isOnline,
                profileImageUrl = conversation.profileImageUrl,
                hasNewMessage = conversation.hasNewMessage || conversation.unreadCount > 0
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            ConversationContent(
                conversation = conversation,
                timeText = timeText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Avatar component for conversation participants
 * Avatar component para sa mga participant ng conversation
 */
@Composable
fun ConversationAvatar(
    participantType: SenderType,
    isOnline: Boolean,
    profileImageUrl: String? = null,
    hasNewMessage: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (!profileImageUrl.isNullOrBlank()) {
            // Display actual profile image
            AsyncImage(
                model = profileImageUrl,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Gray100),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback to icon-based avatar
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = when (participantType) {
                    SenderType.DOCTOR -> Blue100
                    SenderType.ADMIN -> Orange100
                    SenderType.PATIENT -> Gray200
                }
            ) {
                Icon(
                    imageVector = when (participantType) {
                        SenderType.DOCTOR -> Icons.Default.LocalHospital
                        SenderType.ADMIN -> Icons.Default.Support
                        SenderType.PATIENT -> Icons.Default.Person
                    },
                    contentDescription = null,
                    tint = when (participantType) {
                        SenderType.DOCTOR -> Blue500
                        SenderType.ADMIN -> Orange500
                        SenderType.PATIENT -> Gray600
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .wrapContentSize(Alignment.Center)
                )
            }
        }
        
        // Online indicator removed per user request
        // Green circle removed - no longer showing online status
        
        // New message indicator (red dot)
        if (hasNewMessage) {
            Surface(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.TopEnd),
                shape = CircleShape,
                color = androidx.compose.ui.graphics.Color.Red,
                border = androidx.compose.foundation.BorderStroke(2.dp, White)
            ) {}
        }
    }
}

/**
 * Content component for conversation details
 * Content component para sa mga detalye ng conversation
 */
@Composable
fun ConversationContent(
    conversation: ChatConversation,
    timeText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = conversation.participantName,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (conversation.hasNewMessage || conversation.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (conversation.hasNewMessage || conversation.unreadCount > 0) Gray900 else Gray800
                ),
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (conversation.hasNewMessage || conversation.unreadCount > 0) Blue500 else Gray500,
                    fontWeight = if (conversation.hasNewMessage || conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal
                )
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = conversation.lastMessage,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (conversation.hasNewMessage || conversation.unreadCount > 0) Gray800 else Gray600,
                    fontWeight = if (conversation.hasNewMessage || conversation.unreadCount > 0) FontWeight.Medium else FontWeight.Normal
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
            // Unread count badge or new message indicator
            if (conversation.unreadCount > 0) {
                UnreadCountBadge(count = conversation.unreadCount)
            } else if (conversation.hasNewMessage) {
                // Show "NEW" indicator when there's a new message but no specific count
                NewMessageIndicator()
            }
        }
    }
}

/**
 * Unread count badge component
 * Badge component para sa bilang ng hindi pa nabasa
 */
@Composable
fun UnreadCountBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(start = 8.dp)
            .size(20.dp),
        shape = CircleShape,
        color = Blue500
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 9) "9+" else count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )
        }
    }
}

/**
 * New message indicator component
 * Indicator component para sa bagong mensahe
 */
@Composable
fun NewMessageIndicator(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(start = 8.dp),
        shape = RoundedCornerShape(8.dp),
        color = androidx.compose.ui.graphics.Color.Red
    ) {
        Text(
            text = "NEW",
            style = MaterialTheme.typography.labelSmall.copy(
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Preview for ConversationCard
 */
@Preview(showBackground = true)
@Composable
fun ConversationCardPreview() {
    val sampleConversation = ChatConversation(
        id = "1",
        participantName = "Dr. Maria Santos",
        participantType = SenderType.DOCTOR,
        lastMessage = "Your next appointment is confirmed for tomorrow at 10 AM. Please arrive 15 minutes early.",
        lastMessageTime = Calendar.getInstance().apply { add(Calendar.HOUR, -2) }.time,
        unreadCount = 2,
        isOnline = true,
        profileImageUrl = "https://example.com/profile.jpg", // Sample profile image URL
        hasNewMessage = true, // Show new message indicator
        phoneNumber = "+63 912 345 6789",
        specialization = "Chiropractor"
    )
    
    BrightCarePatientTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card with unread count
            ConversationCard(
                conversation = sampleConversation,
                onClick = { /* Preview action */ }
            )
            
            // Card with new message indicator only
            ConversationCard(
                conversation = sampleConversation.copy(
                    unreadCount = 0,
                    hasNewMessage = true,
                    participantName = "Dr. John Doe",
                    lastMessage = "Hello! I have reviewed your test results."
                ),
                onClick = { /* Preview action */ }
            )
            
            // Normal card without indicators
            ConversationCard(
                conversation = sampleConversation.copy(
                    unreadCount = 0,
                    hasNewMessage = false,
                    participantName = "Dr. Sarah Wilson",
                    lastMessage = "Please remember to take your medication."
                ),
                onClick = { /* Preview action */ }
            )
        }
    }
}








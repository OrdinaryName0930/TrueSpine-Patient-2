package com.brightcare.patient.ui.component.messagecomponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val timeText = formatConversationTime(conversation.lastMessageTime)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (conversation.unreadCount > 0) Blue50 else White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with online indicator
            ConversationAvatar(
                participantType = conversation.participantType,
                isOnline = conversation.isOnline
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
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
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
        
        // Online indicator
        if (isOnline) {
            Surface(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.BottomEnd),
                shape = CircleShape,
                color = Success,
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
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                    color = Gray900
                ),
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (conversation.unreadCount > 0) Blue500 else Gray500,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal
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
                    color = if (conversation.unreadCount > 0) Gray800 else Gray600,
                    fontWeight = if (conversation.unreadCount > 0) FontWeight.Medium else FontWeight.Normal
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
            // Unread count badge
            if (conversation.unreadCount > 0) {
                UnreadCountBadge(count = conversation.unreadCount)
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
        isOnline = true
    )
    
    BrightCarePatientTheme {
        ConversationCard(
            conversation = sampleConversation,
            onClick = { /* Preview action */ }
        )
    }
}

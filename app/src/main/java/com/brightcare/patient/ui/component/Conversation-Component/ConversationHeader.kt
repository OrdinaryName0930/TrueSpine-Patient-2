package com.brightcare.patient.ui.component.conversationcomponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brightcare.patient.ui.component.messagecomponent.*
import com.brightcare.patient.ui.theme.*
import java.util.*

/**
 * Fixed header component for conversation screen
 */
@Composable
fun ConversationHeader(
    conversation: ChatConversation,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Back Button
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back / Bumalik",
                    tint = Blue500
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = when (conversation.participantType) {
                    SenderType.DOCTOR -> Blue100
                    SenderType.ADMIN -> Orange100
                    SenderType.PATIENT -> Gray200
                    SenderType.NURSE -> Blue100
                    SenderType.SUPPORT -> Orange100
                }
            ) {
                Icon(
                    imageVector = when (conversation.participantType) {
                        SenderType.DOCTOR -> Icons.Default.LocalHospital
                        SenderType.ADMIN -> Icons.Default.Support
                        SenderType.PATIENT -> Icons.Default.Person
                        SenderType.NURSE -> Icons.Default.MedicalServices
                        SenderType.SUPPORT -> Icons.Default.Support
                    },
                    contentDescription = null,
                    tint = when (conversation.participantType) {
                        SenderType.DOCTOR -> Blue500
                        SenderType.ADMIN -> Orange500
                        SenderType.PATIENT -> Gray600
                        SenderType.NURSE -> Blue500
                        SenderType.SUPPORT -> Orange500
                    },
                    modifier = Modifier
                        .size(20.dp)
                        .wrapContentSize(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name + Status
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.participantName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Gray900
                    )
                )
            }

            // Video Call Button
            IconButton(
                onClick = { /* TODO */ },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Video call / Video tawag",
                    tint = Gray600
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Conversation Header Preview")
@Composable
fun ConversationHeaderPreview() {
    BrightCarePatientTheme {
        ConversationHeader(
            conversation = ChatConversation(
                id = "1",
                participantName = "Dr. Maria Santos",
                participantType = SenderType.DOCTOR,
                lastMessage = "Good morning! How are you?",
                lastMessageTime = Date(),
                unreadCount = 0,
                isOnline = true
            ),
            onBackClick = {}
        )
    }
}

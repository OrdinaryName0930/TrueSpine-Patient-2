package com.brightcare.patient.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderType: SenderType,
    val message: String,
    val timestamp: Date,
    val isRead: Boolean
)

data class ChatConversation(
    val id: String,
    val participantName: String,
    val participantType: SenderType,
    val lastMessage: String,
    val lastMessageTime: Date,
    val unreadCount: Int,
    val isOnline: Boolean
)

enum class SenderType {
    DOCTOR, PATIENT, ADMIN
}

/**
 * Message screen - Chat with healthcare providers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Chats", "Doctors", "Support")
    
    // Sample conversation data
    val conversations = remember {
        val calendar = Calendar.getInstance()
        listOf(
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
            )
        )
    }

    val filteredConversations = remember(selectedTab, conversations) {
        when (selectedTab) {
            0 -> conversations // All chats
            1 -> conversations.filter { it.participantType == SenderType.DOCTOR }
            2 -> conversations.filter { it.participantType == SenderType.ADMIN }
            else -> conversations
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
    ) {
        // Header
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Messages",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Blue500,
                            fontSize = 28.sp
                        )
                    )
                    Text(
                        text = "Chat with your healthcare team",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Gray600
                        )
                    )
                }
                
                IconButton(
                    onClick = { /* Search messages */ },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Blue50
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Messages",
                        tint = Blue500
                    )
                }
            }
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = WhiteBg,
            contentColor = Blue500,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Blue500
                )
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Medium
                        )
                    },
                    selectedContentColor = Blue500,
                    unselectedContentColor = Gray500
                )
            }
        }

        // Conversations List
        if (filteredConversations.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "No messages",
                    tint = Gray400,
                    modifier = Modifier.size(80.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "No conversations yet",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Gray600
                    ),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Start chatting with your healthcare providers",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Gray500
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredConversations) { conversation ->
                    ConversationCard(
                        conversation = conversation,
                        onClick = {
                            // Navigate to chat screen
                            navController.navigate("chat/${conversation.id}")
                        }
                    )
                }
                
                // Add bottom padding
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ConversationCard(
    conversation: ChatConversation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply { time = conversation.lastMessageTime }
    
    val timeText = if (now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR) &&
        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR)) {
        timeFormat.format(conversation.lastMessageTime)
    } else {
        dateFormat.format(conversation.lastMessageTime)
    }

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
            // Avatar
            Box {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = when (conversation.participantType) {
                        SenderType.DOCTOR -> Blue100
                        SenderType.ADMIN -> Orange100
                        SenderType.PATIENT -> Gray200
                    }
                ) {
                    Icon(
                        imageVector = when (conversation.participantType) {
                            SenderType.DOCTOR -> Icons.Default.LocalHospital
                            SenderType.ADMIN -> Icons.Default.Support
                            SenderType.PATIENT -> Icons.Default.Person
                        },
                        contentDescription = null,
                        tint = when (conversation.participantType) {
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
                if (conversation.isOnline) {
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
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
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
                        Surface(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(20.dp),
                            shape = CircleShape,
                            color = Blue500
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (conversation.unreadCount > 9) "9+" else conversation.unreadCount.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "Message Screen Preview",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun MessageScreenPreview() {
    BrightCarePatientTheme {
        MessageScreen(
            navController = rememberNavController()
        )
    }
}

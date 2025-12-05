package com.brightcare.patient.ui.component.messagecomponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brightcare.patient.ui.theme.*

/**
 * Conversations list component
 * Lista component ng mga conversation
 */
@Composable
fun ConversationsList(
    conversations: List<ChatConversation>,
    onConversationClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (conversations.isEmpty()) {
        // Empty state - Walang conversation
        EmptyConversationsState(modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(conversations) { conversation ->
                ConversationCard(
                    conversation = conversation,
                    onClick = { onConversationClick(conversation.id) }
                )
            }
            
            // Add bottom padding
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Empty state when no conversations exist
 * Empty state kapag walang conversation
 */
@Composable
fun EmptyConversationsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
}

/**
 * Preview for ConversationsList
 */
@Preview(showBackground = true)
@Composable
fun ConversationsListPreview() {
    val sampleConversations = getSampleConversations()
    
    BrightCarePatientTheme {
        ConversationsList(
            conversations = sampleConversations,
            onConversationClick = { /* Preview action */ }
        )
    }
}

/**
 * Preview for EmptyConversationsState
 */
@Preview(showBackground = true)
@Composable
fun EmptyConversationsStatePreview() {
    BrightCarePatientTheme {
        EmptyConversationsState()
    }
}

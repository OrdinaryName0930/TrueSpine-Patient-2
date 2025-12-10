package com.brightcare.patient.ui.component.messagecomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.theme.*

/**
 * Main Message Component - Reusable component for chat functionality
 * Pangunahing Message Component - Reusable component para sa chat functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageComponent(
    navController: NavController,
    modifier: Modifier = Modifier,
    onConversationClick: (String) -> Unit = { conversationId ->
        navController.navigate("chat/$conversationId")
    }
) {
    // Search state management
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedSenderType by remember { mutableStateOf<SenderType?>(null) }

    // Real conversation data comes from Firestore through ConversationsViewModel
    // For preview purposes, use empty list
    val conversations = remember { emptyList<ChatConversation>() }

    // Apply search and filter logic
    val filteredConversations = remember(searchQuery, selectedSenderType, conversations) {
        filterConversations(conversations, searchQuery)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
    ) {
        // Header Section
        MessageHeader(
            onSearchClick = { isSearchActive = true }
        )

        // SimpleMessageSearch Section - Always visible below header (no container)
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SimpleMessageSearch(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                placeholder = "Search conversations..."
            )
        }

        // Search Results Summary
        if (searchQuery.isNotEmpty() || selectedSenderType != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Blue50
                )
            ) {
                Text(
                    text = if (filteredConversations.isNotEmpty()) {
                        "Found ${filteredConversations.size} conversations"
                    } else {
                        "No conversations found"
                    },
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Blue700
                )
            }
        }

        // Conversations List Section
        ConversationsList(
            conversations = filteredConversations,
            onConversationClick = onConversationClick
        )
    }
}

/**
 * Preview for Message Component
 */
@Preview(
    showBackground = true,
    name = "Message Component Preview",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun MessageComponentPreview() {
    BrightCarePatientTheme {
        MessageComponent(
            navController = rememberNavController()
        )
    }
}








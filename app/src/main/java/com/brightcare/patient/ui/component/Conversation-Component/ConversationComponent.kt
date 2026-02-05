package com.brightcare.patient.ui.component.conversationcomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.brightcare.patient.ui.component.messagecomponent.*
import java.util.Date
import com.brightcare.patient.ui.theme.*
import com.brightcare.patient.ui.viewmodel.ConversationListViewModel
import com.brightcare.patient.ui.viewmodel.ChiropractorDisplayItem
import com.brightcare.patient.navigation.NavigationRoutes
import kotlinx.coroutines.launch

/**
 * Main Conversation Component - Shows list of chiropractors with search
 * Pangunahing Conversation Component - Nagpapakita ng listahan ng mga chiropractor na may search
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationComponent(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ConversationListViewModel = hiltViewModel(),
    onChiropractorClick: (String) -> Unit = { conversationId ->
        // Mark conversation as read immediately when card is clicked
        // I-mark ang conversation bilang nabasa agad kapag na-click ang card
        viewModel.markConversationAsReadOnClick(conversationId)
        navController.navigate(NavigationRoutes.conversation(conversationId))
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val displayChiropractors by viewModel.getDisplayChiropractors().collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
    )
    {
        // Search bar
        
        // Show error if any
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = viewModel::refreshData
                        ) {
                            Text("Retry")
                        }
                        TextButton(
                            onClick = viewModel::clearError
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
        
        // Loading state
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Blue500)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading chiropractors...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600
                    )
                }
            }
        }
        
        // Use ConversationsList with ConversationCard components
        // Gamitin ang ConversationsList na may ConversationCard components
        val conversations: List<com.brightcare.patient.ui.component.messagecomponent.ChatConversation> = displayChiropractors.map { item ->
            // Convert ChiropractorDisplayItem to ChatConversation
            // I-convert ang ChiropractorDisplayItem sa ChatConversation
            com.brightcare.patient.ui.component.messagecomponent.ChatConversation(
                id = item.conversationId ?: "new_${item.chiropractor.uid}",
                participantName = item.chiropractor.fullName,
                participantType = com.brightcare.patient.ui.component.messagecomponent.SenderType.DOCTOR, // All chiropractors are doctors
                lastMessage = item.lastMessage ?: "Tap to start conversation",
                lastMessageTime = item.lastMessageTime ?: Date(0), // Use epoch time for new conversations to avoid "just now"
                unreadCount = item.unreadCount,
                isOnline = item.chiropractor.isAvailable,
                profileImageUrl = item.chiropractor.profileImage, // Pass the actual profile image URL
                hasNewMessage = item.unreadCount > 0, // Set hasNewMessage based on unread count
                phoneNumber = item.chiropractor.phoneNumber, // Pass the phone number for call functionality
                specialization = item.chiropractor.specialization // Pass the specialization
            )
        }
        
        ConversationsList(
            conversations = conversations,
            onConversationClick = { conversationId ->
                onChiropractorClick(conversationId)
            },
            modifier = Modifier.fillMaxSize(),
            isRefreshing = uiState.isLoading,
            onRefresh = {
                // Refresh the chiropractors data
                // I-refresh ang data ng mga chiropractor
                viewModel.refreshData()
            }
        )
    }
}

// Removed ChiropractorCard - now using ConversationCard from ConversationsList

// Dummy data functions removed - now using real Firestore data through MessagingIntegrationProvider

/**
 * Preview for ConversationComponent
 */
@Preview(
    showBackground = true,
    name = "Conversation Component Preview",
    widthDp = 360,
    heightDp = 740
)
@Composable
fun ConversationComponentPreview() {
    BrightCarePatientTheme {
        ConversationComponent(
            navController = rememberNavController()
        )
    }
}
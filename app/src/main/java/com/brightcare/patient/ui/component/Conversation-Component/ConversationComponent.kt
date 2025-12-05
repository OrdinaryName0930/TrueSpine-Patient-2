package com.brightcare.patient.ui.component.conversationcomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.component.messagecomponent.*
import com.brightcare.patient.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Main Conversation Component - Reusable component for individual chat conversations
 * Pangunahing Conversation Component - Reusable component para sa individual na chat conversations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationComponent(
    conversationId: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = { navController.popBackStack() }
) {
    // Message input state
    var messageText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Conversation and messages state
    var conversation by remember { mutableStateOf<ChatConversation?>(null) }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load conversation data
    LaunchedEffect(conversationId) {
        // Load from Firestore or use sample data
        conversation = getSampleConversationById(conversationId)
        messages = getSampleMessages(conversationId)
        isLoading = false
    }
    
    // Auto-scroll to bottom when new message is added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WhiteBg)
    ) {
        // Fixed Header
        conversation?.let { conv ->
            ConversationHeader(
                conversation = conv,
                onBackClick = onBackClick
            )
        }
        
        if (isLoading) {
            // Loading state
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator(color = Blue500)
            }
        } else {
            // Messages list - Start from bottom
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = false // Keep normal layout, messages will be added to bottom
            ) {
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        isFromCurrentUser = message.senderType == SenderType.PATIENT
                    )
                }
            }
        }
        
        // Message input area with attachment support
        MessageInputArea(
            messageText = messageText,
            onMessageTextChange = { messageText = it },
            onSendMessage = {
                if (messageText.trim().isNotEmpty()) {
                    // Send message to Firestore
                    sendMessageToFirestore(
                        conversationId = conversationId,
                        messageText = messageText.trim(),
                        onSuccess = { newMessage ->
                            // Add message to local list
                            messages = messages + newMessage
                            messageText = ""
                            keyboardController?.hide()
                        },
                        onError = { error ->
                            // Handle error (show snackbar, etc.)
                            // For now, just clear the message
                            messageText = ""
                        }
                    )
                }
            },
            onAttachmentClick = {
                // Handle attachment selection
                // TODO: Implement file picker
            },
            onImageClick = {
                // Handle image selection
                // TODO: Implement image picker
            }
        )
    }
}

/**
 * Send message to Firestore
 * Magpadala ng message sa Firestore
 */
private fun sendMessageToFirestore(
    conversationId: String,
    messageText: String,
    onSuccess: (ChatMessage) -> Unit,
    onError: (Exception) -> Unit
) {
    // Create new message
    val newMessage = ChatMessage(
        id = "msg_${System.currentTimeMillis()}",
        senderId = "current_user", // TODO: Get from AuthenticationManager
        senderName = "You",
        senderType = SenderType.PATIENT,
        message = messageText,
        timestamp = java.util.Date(),
        isRead = false,
        attachments = emptyList()
    )
    
    // TODO: Implement actual Firestore save
    // For now, simulate success
    onSuccess(newMessage)
    
    /* 
    Firestore implementation would look like:
    
    val db = FirebaseFirestore.getInstance()
    val messageData = hashMapOf(
        "senderId" to newMessage.senderId,
        "senderName" to newMessage.senderName,
        "senderType" to newMessage.senderType.name,
        "message" to newMessage.message,
        "timestamp" to newMessage.timestamp,
        "isRead" to newMessage.isRead,
        "attachments" to newMessage.attachments
    )
    
    db.collection("conversations")
        .document(conversationId)
        .collection("messages")
        .document(newMessage.id)
        .set(messageData)
        .addOnSuccessListener { onSuccess(newMessage) }
        .addOnFailureListener { exception -> onError(exception) }
    */
}

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
            conversationId = "1",
            navController = rememberNavController()
        )
    }
}
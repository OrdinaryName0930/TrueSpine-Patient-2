package com.brightcare.patient.ui.example

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.brightcare.patient.ui.component.conversationcomponent.*
import com.brightcare.patient.ui.integration.MessagingIntegrationProvider
import com.brightcare.patient.ui.integration.toUiMessage

/**
 * Example usage of the messaging system with existing UI components
 * Halimbawa ng paggamit ng messaging system sa existing UI components
 */

/**
 * Complete messaging screen using the new backend with existing UI
 * Kumpletong messaging screen gamit ang bagong backend sa existing UI
 */
@Composable
fun MessagingScreenExample() {
    MessagingIntegrationProvider { state ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Show loading state
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Show error if any
            state.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Conversation header
            state.chiropractor?.let { chiropractor ->
                val conversation = ChatConversation(
                    id = state.conversation?.id ?: "",
                    participantName = chiropractor.name,
                    participantType = SenderType.DOCTOR,
                    lastMessage = "",
                    lastMessageTime = java.util.Date(),
                    phoneNumber = chiropractor.phoneNumber
                )
                
                ConversationHeader(
                    conversation = conversation,
                    onBackClick = { /* Handle back navigation */ }
                )
            }
            
            // Messages list
            Box(modifier = Modifier.weight(1f)) {
                // Convert backend messages to UI format
                val uiMessages = state.messages.map { message ->
                    message.toUiMessage()
                }
                
                // Use your existing message list component here
                // MessagesList(messages = uiMessages)
            }
            
            // Message input area
            MessageInputArea(
                messageText = state.messageText,
                onMessageTextChange = state.onMessageTextChange,
                onSendMessage = state.onSendMessage,
                onImageClick = {
                    // Handle gallery image selection
                    // You would typically open image picker here
                    // state.onSendImage(selectedImageUri, fileName)
                },
                onCameraClick = {
                    // Handle camera image capture
                    // You would typically open camera here
                    // state.onSendImage(capturedImageUri, fileName)
                },
                onDocumentClick = {
                    // Handle document selection
                    // You would typically open document picker here
                    // state.onSendDocument(selectedDocumentUri, fileName, mimeType)
                }
            )
        }
    }
}

/**
 * Example of how to integrate with existing conversation component
 * Halimbawa ng pag-integrate sa existing conversation component
 */
@Composable
fun ExistingConversationComponentIntegration() {
    MessagingIntegrationProvider { state ->
        state.conversation?.let { conversation ->
            state.chiropractor?.let { chiropractor ->
                // Convert to existing UI format
                val chatConversation = ChatConversation(
                    id = conversation.id,
                    participantName = chiropractor.name,
                    participantType = SenderType.DOCTOR,
                    lastMessage = conversation.lastMessage,
                    lastMessageTime = conversation.lastMessageTimestamp.toDate(),
                    unreadCount = conversation.unreadCounts.values.sum(),
                    isOnline = chiropractor.isAvailable,
                    phoneNumber = chiropractor.phoneNumber
                )
                
                // Use existing ConversationComponent
                // Note: You'll need to adapt this to work with your existing component
                // ConversationComponent(
                //     conversationId = conversation.id,
                //     navController = navController,
                //     onBackClick = { /* Handle back */ }
                // )
                
                // For now, show a placeholder
                Text("Conversation with ${chiropractor.name}")
            }
        }
    }
}

/**
 * Example of phone call integration
 * Halimbawa ng phone call integration
 */
@Composable
fun PhoneCallIntegrationExample() {
    val context = LocalContext.current
    
    MessagingIntegrationProvider { state ->
        state.chiropractor?.let { chiropractor ->
            var showCallDialog by remember { mutableStateOf(false) }
            
            // Call button
            Button(
                onClick = { showCallDialog = true }
            ) {
                Text("Call Doctor")
            }
            
            // Call confirmation dialog
            if (showCallDialog) {
                AlertDialog(
                    onDismissRequest = { showCallDialog = false },
                    title = { Text("Call Doctor") },
                    text = { 
                        Text("Do you want to call ${chiropractor.name}?\nPhone: ${chiropractor.phoneNumber}")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showCallDialog = false
                                state.onMakePhoneCall(context)
                            }
                        ) {
                            Text("Call")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showCallDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

/**
 * Example of file upload with progress
 * Halimbawa ng file upload na may progress
 */
@Composable
fun FileUploadExample() {
    // This example shows how to use the new ConversationComponent
    // Ang example na ito ay nagpapakita kung paano gamitin ang bagong ConversationComponent
    val navController = rememberNavController()
    
    Column {
        Text(
            text = "File Upload Example - Use ConversationComponent",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        
        // Use the new ConversationComponent which handles file uploads
        ConversationComponent(
            navController = navController
        )
    }
}

/**
 * Helper functions for file handling
 * Helper functions para sa file handling
 */
object FileHandlingHelpers {
    
    /**
     * Handle image selection from gallery
     * Handle ng image selection mula sa gallery
     */
    fun handleImageFromGallery(
        imageUri: Uri,
        onSendImage: (Uri, String?) -> Unit
    ) {
        val fileName = "image_${System.currentTimeMillis()}.jpg"
        onSendImage(imageUri, fileName)
    }
    
    /**
     * Handle camera capture
     * Handle ng camera capture
     */
    fun handleCameraCapture(
        imageUri: Uri,
        onSendImage: (Uri, String?) -> Unit
    ) {
        val fileName = "camera_${System.currentTimeMillis()}.jpg"
        onSendImage(imageUri, fileName)
    }
    
    /**
     * Handle document selection
     * Handle ng document selection
     */
    fun handleDocumentSelection(
        fileUri: Uri,
        fileName: String,
        mimeType: String,
        onSendFile: (Uri, String, String) -> Unit
    ) {
        onSendFile(fileUri, fileName, mimeType)
    }
    
    /**
     * Get MIME type from file extension
     * Kunin ang MIME type mula sa file extension
     */
    fun getMimeTypeFromExtension(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "txt" -> "text/plain"
            "zip" -> "application/zip"
            else -> "application/octet-stream"
        }
    }
}

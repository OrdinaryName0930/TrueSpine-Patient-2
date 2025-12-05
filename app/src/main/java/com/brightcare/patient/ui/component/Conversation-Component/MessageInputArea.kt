package com.brightcare.patient.ui.component.conversationcomponent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brightcare.patient.ui.theme.*

/**
 * Message input area with attachment and image features
 * Message input area na may attachment at image features
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputArea(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachmentClick: () -> Unit = {},
    onImageClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAttachmentOptions by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column {
            // Attachment options (expandable)
            AnimatedVisibility(
                visible = showAttachmentOptions,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                AttachmentOptionsRow(
                    onImageClick = {
                        onImageClick()
                        showAttachmentOptions = false
                    },
                    onFileClick = {
                        onAttachmentClick()
                        showAttachmentOptions = false
                    },
                    onCameraClick = {
                        // TODO: Implement camera capture
                        showAttachmentOptions = false
                    },
                    onDocumentClick = {
                        // TODO: Implement document picker
                        showAttachmentOptions = false
                    }
                )
            }
            
            // Main input row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Attachment button
                IconButton(
                    onClick = { showAttachmentOptions = !showAttachmentOptions },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (showAttachmentOptions) Blue500 else Gray100
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (showAttachmentOptions) Icons.Default.Close else Icons.Default.AttachFile,
                        contentDescription = if (showAttachmentOptions) "Close / Isara" else "Attach file / Mag-attach ng file",
                        tint = if (showAttachmentOptions) White else Gray600,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Message input field
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "Type a message...",
                            color = Gray500
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue500,
                        unfocusedBorderColor = Gray300,
                        cursorColor = Blue500
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { onSendMessage() }
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Send button
                FloatingActionButton(
                    onClick = onSendMessage,
                    modifier = Modifier.size(48.dp),
                    containerColor = if (messageText.trim().isNotEmpty()) Blue500 else Gray300,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send message / Magpadala ng mensahe",
                        tint = White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Attachment options row
 * Row ng mga attachment options
 */
@Composable
private fun AttachmentOptionsRow(
    onImageClick: () -> Unit,
    onFileClick: () -> Unit,
    onCameraClick: () -> Unit,
    onDocumentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Gray50)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image gallery
        AttachmentOption(
            icon = Icons.Default.PhotoLibrary,
            label = "Gallery",
            backgroundColor = Blue100,
            iconColor = Blue500,
            onClick = onImageClick
        )
        
        // Camera
        AttachmentOption(
            icon = Icons.Default.CameraAlt,
            label = "Camera",
            backgroundColor = Blue100,
            iconColor = Blue500,
            onClick = onCameraClick
        )
        
        // Document
        AttachmentOption(
            icon = Icons.Default.Description,
            label = "Document",
            backgroundColor = Orange100,
            iconColor = Orange500,
            onClick = onDocumentClick
        )
        
        // File
        AttachmentOption(
            icon = Icons.Default.AttachFile,
            label = "File",
            backgroundColor = Gray100,
            iconColor = Gray600,
            onClick = onFileClick
        )
    }
}

/**
 * Individual attachment option
 * Individual na attachment option
 */
@Composable
private fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon button
        IconButton(
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = backgroundColor
            ),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Gray700
        )
    }
}

/**
 * Preview for MessageInputArea
 */
@Preview(
    showBackground = true,
    name = "Message Input Area Preview"
)
@Composable
fun MessageInputAreaPreview() {
    BrightCarePatientTheme {
        Column {
            // Empty input
            MessageInputArea(
                messageText = "",
                onMessageTextChange = {},
                onSendMessage = {}
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Input with text
            MessageInputArea(
                messageText = "Hello doctor, how are you?",
                onMessageTextChange = {},
                onSendMessage = {}
            )
        }
    }
}